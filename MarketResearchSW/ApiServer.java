package MarketResearchSW;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Very small HTTP API server to expose the existing
 * Login logic over HTTP so the frontend can talk to
 * the real database-backed Java code.
 *
 * Endpoint:
 *   POST http://localhost:8080/login
 *   Body: { "username": "...", "password": "..." }
 *   Response JSON:
 *     {
 *       "success": true/false,
 *       "message": "...",
 *       "role": "MarketResearcher" | "CompanyExecutive" | "Customer",
 *       "company": "Acme",
 *       "accessLevel": 3
 *     }
 */
public class ApiServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/login", new LoginHandler());
        server.createContext("/market-researcher/surveys", new ActionHandler("market-researcher-surveys"));
        server.createContext("/market-researcher/surveys/delete", new ActionHandler("market-researcher-delete-survey"));
        server.createContext("/market-researcher/reports", new ActionHandler("market-researcher-reports"));
        server.createContext("/market-researcher/catalogue", new ActionHandler("market-researcher-catalogue"));
        server.createContext("/market-researcher/survey-answers", new ActionHandler("market-researcher-survey-answers"));
        server.createContext("/company-exec/catalogue", new ActionHandler("company-exec-catalogue"));
        server.createContext("/company-exec/reports", new ActionHandler("company-exec-reports"));
        server.createContext("/company-exec/reviews", new ActionHandler("company-exec-reviews"));
        server.createContext("/company-exec/survey-answers", new ActionHandler("company-exec-survey-answers"));
        server.createContext("/customer/surveys/fill", new ActionHandler("customer-fill-survey"));
        server.createContext("/customer/reviews/fill", new ActionHandler("customer-fill-review"));
        server.createContext("/customer/reviews", new ActionHandler("customer-reviews"));
        server.createContext("/customer/surveys", new ActionHandler("customer-surveys"));
        server.createContext("/customer/catalogue", new ActionHandler("customer-catalogue"));
        server.setExecutor(null); // default executor
        System.out.println("HTTP API server listening on http://localhost:8080");
        server.start();
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle CORS preflight
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 204, "");
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"success\":false,\"message\":\"Only POST is allowed.\"}");
                return;
            }

            String body;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                body = sb.toString();
            }

            String username = extractJsonField(body, "username");
            String password = extractJsonField(body, "password");

            if (username == null || password == null) {
                sendJson(exchange, 400,
                        "{\"success\":false,\"message\":\"username and password are required.\"}");
                return;
            }

            Login login = new Login();
            User user = login.login(username, password);

            if (user == null) {
                sendJson(exchange, 200,
                        "{\"success\":false,\"message\":\"Invalid username or password.\"}");
                return;
            }

            String roleStr;
            if (user.role == Role.MarketResearcher) {
                roleStr = "MarketResearcher";
            } else if (user.role == Role.CompanyExecutive) {
                roleStr = "CompanyExecutive";
            } else {
                roleStr = "Customer";
            }

            String company = user.company == null ? "" : user.company;
            int accessLevel = user.accessLevel;

            String json = String.format(
                    "{\"success\":true," +
                            "\"message\":\"Logged in successfully.\"," +
                            "\"role\":\"%s\"," +
                            "\"company\":\"%s\"," +
                            "\"accessLevel\":%d}",
                    escapeJson(roleStr),
                    escapeJson(company),
                    accessLevel
            );

            sendJson(exchange, 200, json);
        }
    }

    static class ActionHandler implements HttpHandler {
        private final String actionKey;

        ActionHandler(String actionKey) {
            this.actionKey = actionKey;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 204, "");
                return;
            }

            if (!("GET".equalsIgnoreCase(exchange.getRequestMethod()) || "POST".equalsIgnoreCase(exchange.getRequestMethod()))) {
                sendJson(exchange, 405, "{\"success\":false,\"message\":\"Only GET and POST are allowed.\"}");
                return;
            }

            Map<String, String> queryParams = parseQuery(exchange.getRequestURI().getRawQuery());
            String username = queryParams.get("username");

            if (username == null || username.isBlank()) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"username query parameter is required.\"}");
                return;
            }

            User user = loadUserByUsername(username);
            if (user == null) {
                sendJson(exchange, 404, "{\"success\":false,\"message\":\"User not found.\"}");
                return;
            }

            try {
                switch (actionKey) {
                    case "market-researcher-surveys":
                        if (user.role != Role.MarketResearcher) {
                            sendJson(exchange, 403, "{\"success\":false,\"message\":\"This endpoint is only for Market Researchers.\"}");
                            return;
                        }
                        String pname = queryParams.get("pname");
                        String cname = queryParams.get("cname");
                        String q1 = queryParams.get("q1");
                        String q2 = queryParams.get("q2");
                        String q3 = queryParams.get("q3");
                        if (isBlank(pname) || isBlank(cname) || isBlank(q1) || isBlank(q2) || isBlank(q3)) {
                            String surveys = getCompanySurveysJson(user.company);
                            sendJson(exchange, 200,
                                    "{\"success\":true,\"message\":\"Company surveys retrieved.\",\"surveys\":" + surveys + "}");
                            return;
                        }
                        user.createSurvey(pname, cname, q1, q2, q3);
                        String surveys = getCompanySurveysJson(user.company);
                        sendJson(exchange, 200,
                                "{\"success\":true,\"message\":\"Survey created successfully.\",\"surveys\":" + surveys + "}");
                        return;

                    case "market-researcher-delete-survey":
                        if (user.role != Role.MarketResearcher) {
                            sendJson(exchange, 403, "{\"success\":false,\"message\":\"This endpoint is only for Market Researchers.\"}");
                            return;
                        }
                        String surveyToDelete = queryParams.get("surveyID");
                        if (isBlank(surveyToDelete)) {
                            sendJson(exchange, 200, "{\"success\":false,\"message\":\"Delete survey requires surveyID param.\"}");
                            return;
                        }
                        boolean deleted = deleteCompanySurvey(user.company, surveyToDelete);
                        String refreshedSurveys = getCompanySurveysJson(user.company);
                        if (!deleted) {
                            sendJson(exchange, 200,
                                    "{\"success\":false,\"message\":\"Survey not found for your company.\",\"surveys\":" + refreshedSurveys + "}");
                            return;
                        }
                        sendJson(exchange, 200,
                                "{\"success\":true,\"message\":\"Survey deleted successfully.\",\"surveys\":" + refreshedSurveys + "}");
                        return;

                    case "market-researcher-reports":
                        if (user.role != Role.MarketResearcher) {
                            sendJson(exchange, 403, "{\"success\":false,\"message\":\"This endpoint is only for Market Researchers.\"}");
                            return;
                        }
                        String mrProductID = queryParams.get("productId");
                        String mrReportType = queryParams.get("reportType");
                        if (isBlank(mrProductID) || isBlank(mrReportType)) {
                            String reports = getCompanyReportSummaryJson(user.company);
                            sendJson(exchange, 200,
                                    "{\"success\":true,\"message\":\"Report summary retrieved.\",\"reports\":" + reports + "}");
                            return;
                        }
                        Product mrProduct = Product.getProductDetails(mrProductID);
                        if (mrProduct == null) {
                            sendJson(exchange, 200, "{\"success\":false,\"message\":\"Invalid productId.\"}");
                            return;
                        }
                        ReportType mrType = "PDF".equalsIgnoreCase(mrReportType) ? ReportType.PDF : ReportType.HTML;
                        user.generateReport(mrProduct, mrType);
                        sendJson(exchange, 200, "{\"success\":true,\"message\":\"Report generated successfully.\"}");
                        return;

                    case "market-researcher-catalogue":
                        if (user.role != Role.MarketResearcher) {
                            sendJson(exchange, 403, "{\"success\":false,\"message\":\"This endpoint is only for Market Researchers.\"}");
                            return;
                        }
                        String catalogue = getCompanyCatalogueJson(user.company);
                        sendJson(exchange, 200,
                                "{\"success\":true,\"message\":\"Company catalogue retrieved.\",\"catalogue\":" + catalogue + "}");
                        return;

                    case "market-researcher-survey-answers":
                        if (user.role != Role.MarketResearcher) {
                            sendJson(exchange, 403, "{\"success\":false,\"message\":\"This endpoint is only for Market Researchers.\"}");
                            return;
                        }
                        String mrSurveyAnswers = getCompanySurveyAnswersJson(user.company);
                        sendJson(exchange, 200,
                                "{\"success\":true,\"message\":\"Survey answers retrieved.\",\"surveyAnswers\":" + mrSurveyAnswers + "}");
                        return;

                    case "company-exec-catalogue":
                        if (user.role != Role.CompanyExecutive) {
                            sendJson(exchange, 403, "{\"success\":false,\"message\":\"This endpoint is only for Company Executives.\"}");
                            return;
                        }
                        String ceCatalogue = getCompanyCatalogueJson(user.company);
                        sendJson(exchange, 200,
                                "{\"success\":true,\"message\":\"Company catalogue retrieved.\",\"catalogue\":" + ceCatalogue + "}");
                        return;

                    case "company-exec-reports":
                        if (user.role != Role.CompanyExecutive) {
                            sendJson(exchange, 403, "{\"success\":false,\"message\":\"This endpoint is only for Company Executives.\"}");
                            return;
                        }
                        String ceProductID = queryParams.get("productId");
                        String ceReportType = queryParams.get("reportType");
                        if (isBlank(ceProductID) || isBlank(ceReportType)) {
                            String ceReports = getCompanyReportSummaryJson(user.company);
                            sendJson(exchange, 200,
                                    "{\"success\":true,\"message\":\"Report summary retrieved.\",\"reports\":" + ceReports + "}");
                            return;
                        }
                        Product ceProduct = Product.getProductDetails(ceProductID);
                        if (ceProduct == null) {
                            sendJson(exchange, 200, "{\"success\":false,\"message\":\"Invalid productId.\"}");
                            return;
                        }
                        ReportType ceType = "PDF".equalsIgnoreCase(ceReportType) ? ReportType.PDF : ReportType.HTML;
                        user.generateReport(ceProduct, ceType);
                        sendJson(exchange, 200, "{\"success\":true,\"message\":\"Report generated successfully.\"}");
                        return;

                    case "company-exec-reviews":
                        if (user.role != Role.CompanyExecutive) {
                            sendJson(exchange, 403, "{\"success\":false,\"message\":\"This endpoint is only for Company Executives.\"}");
                            return;
                        }
                        String reviewProductID = queryParams.get("productId");
                        String reviews = getCompanyReviewsJson(user.company, reviewProductID);
                        sendJson(exchange, 200,
                                "{\"success\":true,\"message\":\"Reviews retrieved.\",\"reviews\":" + reviews + "}");
                        return;

                    case "company-exec-survey-answers":
                        if (user.role != Role.CompanyExecutive) {
                            sendJson(exchange, 403, "{\"success\":false,\"message\":\"This endpoint is only for Company Executives.\"}");
                            return;
                        }
                        String surveyAnswers = getCompanySurveyAnswersJson(user.company);
                        sendJson(exchange, 200,
                                "{\"success\":true,\"message\":\"Survey answers retrieved.\",\"surveyAnswers\":" + surveyAnswers + "}");
                        return;

                    case "customer-fill-survey":
                        if (user.role != Role.Customer) {
                            sendJson(exchange, 403, "{\"success\":false,\"message\":\"This endpoint is only for Customers.\"}");
                            return;
                        }
                        String surveyID = queryParams.get("surveyID");
                        String a1 = queryParams.get("a1");
                        String a2 = queryParams.get("a2");
                        String a3 = queryParams.get("a3");
                        if (isBlank(surveyID) || isBlank(a1) || isBlank(a2) || isBlank(a3)) {
                            sendJson(exchange, 200, "{\"success\":false,\"message\":\"Fill survey requires surveyID, a1, a2, a3 params.\"}");
                            return;
                        }
                        boolean surveySubmitted = user.fillSurvey(surveyID, a1, a2, a3);
                        if (!surveySubmitted) {
                            sendJson(exchange, 200, "{\"success\":false,\"message\":\"Survey response was not saved. Please check Survey ID and try again.\"}");
                            return;
                        }
                        sendJson(exchange, 200, "{\"success\":true,\"message\":\"Survey submitted successfully.\"}");
                        return;

                    case "customer-fill-review":
                        if (user.role != Role.Customer) {
                            sendJson(exchange, 403, "{\"success\":false,\"message\":\"This endpoint is only for Customers.\"}");
                            return;
                        }
                        String company = queryParams.get("company");
                        String product = queryParams.get("product");
                        String reviewText = queryParams.get("review");
                        String ratingText = queryParams.get("rating");
                        if (isBlank(company) || isBlank(product) || isBlank(reviewText) || isBlank(ratingText)) {
                            sendJson(exchange, 200, "{\"success\":false,\"message\":\"Fill review requires company, product, review, rating params.\"}");
                            return;
                        }
                        int rating;
                        try {
                            rating = Integer.parseInt(ratingText);
                        } catch (NumberFormatException e) {
                            sendJson(exchange, 200, "{\"success\":false,\"message\":\"Rating must be a number between 1 and 5.\"}");
                            return;
                        }
                        if (rating < 1 || rating > 5) {
                            sendJson(exchange, 200, "{\"success\":false,\"message\":\"Rating must be between 1 and 5.\"}");
                            return;
                        }
                        boolean submitted = user.reviewProduct(company, product, reviewText, rating);
                        if (!submitted) {
                            sendJson(exchange, 200, "{\"success\":false,\"message\":\"You cannot review the same device twice (or product is invalid).\"}");
                            return;
                        }
                        sendJson(exchange, 200, "{\"success\":true,\"message\":\"Review submitted successfully.\"}");
                        return;

                    case "customer-surveys":
                        if (user.role != Role.Customer) {
                            sendJson(exchange, 403, "{\"success\":false,\"message\":\"This endpoint is only for Customers.\"}");
                            return;
                        }
                        String customerSurveys = getAllSurveysJson();
                        sendJson(exchange, 200,
                                "{\"success\":true,\"message\":\"Available surveys retrieved.\",\"surveys\":" + customerSurveys + "}");
                        return;

                    case "customer-catalogue":
                        if (user.role != Role.Customer) {
                            sendJson(exchange, 403, "{\"success\":false,\"message\":\"This endpoint is only for Customers.\"}");
                            return;
                        }
                        String customerCatalogue = getAllCatalogueJson();
                        sendJson(exchange, 200,
                                "{\"success\":true,\"message\":\"Catalogue retrieved.\",\"catalogue\":" + customerCatalogue + "}");
                        return;

                    case "customer-reviews":
                        if (user.role != Role.Customer) {
                            sendJson(exchange, 403, "{\"success\":false,\"message\":\"This endpoint is only for Customers.\"}");
                            return;
                        }
                        String customerReviews = Review.viewAvailableReviews();
                        sendJson(exchange, 200,
                                "{\"success\":true,\"message\":\"Available devices to review retrieved.\",\"reviews\":" + customerReviews + "}");
                        return;

                    default:
                        sendJson(exchange, 404, "{\"success\":false,\"message\":\"Unknown endpoint action.\"}");
                }
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"success\":false,\"message\":\"Failed to perform action.\"}");
            }
        }
    }

    private static User loadUserByUsername(String username) {
        Connection connection = null;
        try {
            connection = getDatabaseConnection();

            if (connection == null) {
                return null;
            }

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "select * from userlogin where username = \"" + username + "\";");

            User user = null;
            if (resultSet.next()) {
                String password = resultSet.getString("password");
                String company = resultSet.getString("company");
                String role = resultSet.getString("role");
                int accessLevel = resultSet.getInt("accesslevel");

                if ("Market Researcher".equals(role)) {
                    user = new MarketResearcher(username, password, company, accessLevel, Role.MarketResearcher);
                } else if ("Company Executive".equals(role)) {
                    user = new CompanyExec(username, password, company, accessLevel, Role.CompanyExecutive);
                } else {
                    user = new Customer(username, password, company, accessLevel, Role.Customer);
                }
            }

            resultSet.close();
            statement.close();
            connection.close();
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    private static Connection getDatabaseConnection() {
        try {
            return DBConnect.getConnection();
        } catch (Exception e) {
            return null;
        }
    }

    private static String getCompanyCatalogueJson(String companyName) {
        if (isBlank(companyName)) {
            return "[]";
        }

        Connection connection = null;
        try {
            connection = getDatabaseConnection();
            if (connection == null) {
                return "[]";
            }

            Statement statement = connection.createStatement();
            ResultSet companySet = statement.executeQuery(
                    "select ID from company where name = \"" + escapeSql(companyName) + "\";");

            String companyId = null;
            if (companySet.next()) {
                companyId = companySet.getString("ID");
            }
            companySet.close();

            if (isBlank(companyId)) {
                statement.close();
                connection.close();
                return "[]";
            }

            ResultSet resultSet = statement.executeQuery(
                    "select ID, name, description, companyID from products where companyID = \"" + escapeSql(companyId) + "\";");

            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            while (resultSet.next()) {
                if (!first) {
                    builder.append(",");
                }
                builder.append("{\"id\":\"").append(escapeJson(resultSet.getString("ID"))).append("\",")
                        .append("\"name\":\"").append(escapeJson(resultSet.getString("name"))).append("\",")
                        .append("\"description\":\"").append(escapeJson(resultSet.getString("description"))).append("\",")
                        .append("\"companyID\":\"").append(escapeJson(resultSet.getString("companyID"))).append("\"}");
                first = false;
            }
            builder.append("]");

            resultSet.close();
            statement.close();
            connection.close();
            return builder.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    private static String getCompanyReportSummaryJson(String companyName) {
        if (isBlank(companyName)) {
            return "[]";
        }

        Connection connection = null;
        try {
            connection = getDatabaseConnection();
            if (connection == null) {
                return "[]";
            }

            Statement statement = connection.createStatement();
            ResultSet companySet = statement.executeQuery(
                    "select ID from company where name = \"" + escapeSql(companyName) + "\";");

            String companyId = null;
            if (companySet.next()) {
                companyId = companySet.getString("ID");
            }
            companySet.close();

            if (isBlank(companyId)) {
                statement.close();
                connection.close();
                return "[]";
            }

            String query =
                    "select p.ID, p.name, count(r.ID) as reviewCount, " +
                            "coalesce(avg(r.rating), 0) as avgRating " +
                            "from products p left join review r on r.PID = p.ID " +
                            "where p.companyID = \"" + escapeSql(companyId) + "\" " +
                            "group by p.ID, p.name order by p.name;";

            ResultSet resultSet = statement.executeQuery(query);

            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            while (resultSet.next()) {
                if (!first) {
                    builder.append(",");
                }
                builder.append("{\"productId\":\"").append(escapeJson(resultSet.getString("ID"))).append("\",")
                        .append("\"productName\":\"").append(escapeJson(resultSet.getString("name"))).append("\",")
                        .append("\"reviewCount\":").append(resultSet.getInt("reviewCount")).append(",")
                    .append("\"averageRating\":").append(String.format(Locale.US, "%.2f", resultSet.getDouble("avgRating")))
                        .append("}");
                first = false;
            }
            builder.append("]");

            resultSet.close();
            statement.close();
            connection.close();
            return builder.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    private static String getCompanySurveysJson(String companyName) {
        if (isBlank(companyName)) {
            return "[]";
        }

        Connection connection = null;
        try {
            connection = getDatabaseConnection();
            if (connection == null) {
                return "[]";
            }

            Statement statement = connection.createStatement();
            ResultSet companySet = statement.executeQuery(
                    "select ID from company where name = \"" + escapeSql(companyName) + "\";");

            String companyId = null;
            if (companySet.next()) {
                companyId = companySet.getString("ID");
            }
            companySet.close();

            if (isBlank(companyId)) {
                statement.close();
                connection.close();
                return "[]";
            }

            String query =
                    "select s.ID as surveyID, p.name as productName, s.q1, s.q2, s.q3 " +
                            "from survey s join products p on p.ID = s.productid " +
                            "where p.companyID = \"" + escapeSql(companyId) + "\" order by s.ID;";

            ResultSet resultSet = statement.executeQuery(query);

            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            while (resultSet.next()) {
                if (!first) {
                    builder.append(",");
                }
                builder.append("{\"surveyID\":\"").append(escapeJson(resultSet.getString("surveyID"))).append("\",")
                        .append("\"productName\":\"").append(escapeJson(resultSet.getString("productName"))).append("\",")
                        .append("\"q1\":\"").append(escapeJson(resultSet.getString("q1"))).append("\",")
                        .append("\"q2\":\"").append(escapeJson(resultSet.getString("q2"))).append("\",")
                        .append("\"q3\":\"").append(escapeJson(resultSet.getString("q3"))).append("\"}");
                first = false;
            }
            builder.append("]");

            resultSet.close();
            statement.close();
            connection.close();
            return builder.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    private static boolean deleteCompanySurvey(String companyName, String surveyID) {
        if (isBlank(companyName) || isBlank(surveyID)) {
            return false;
        }

        Connection connection = null;
        try {
            connection = getDatabaseConnection();
            if (connection == null) {
                return false;
            }

            Statement statement = connection.createStatement();
            ResultSet companySet = statement.executeQuery(
                    "select ID from company where name = \"" + escapeSql(companyName) + "\";");

            String companyId = null;
            if (companySet.next()) {
                companyId = companySet.getString("ID");
            }
            companySet.close();

            if (isBlank(companyId)) {
                statement.close();
                connection.close();
                return false;
            }

            ResultSet surveySet = statement.executeQuery(
                    "select s.ID from survey s join products p on p.ID = s.productid " +
                            "where s.ID = \"" + escapeSql(surveyID) + "\" and p.companyID = \"" + escapeSql(companyId) + "\";");

            boolean existsForCompany = surveySet.next();
            surveySet.close();

            if (!existsForCompany) {
                statement.close();
                connection.close();
                return false;
            }

            statement.executeUpdate("delete from surveyresponse where surveyID = \"" + escapeSql(surveyID) + "\";");
            int deletedRows = statement.executeUpdate("delete from survey where ID = \"" + escapeSql(surveyID) + "\";");

            statement.close();
            connection.close();
            return deletedRows > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getCompanyReviewsJson(String companyName, String productID) {
        if (isBlank(companyName)) {
            return "[]";
        }

        Connection connection = null;
        try {
            connection = getDatabaseConnection();
            if (connection == null) {
                return "[]";
            }

            Statement statement = connection.createStatement();
            ResultSet companySet = statement.executeQuery(
                    "select ID from company where name = \"" + escapeSql(companyName) + "\";");

            String companyId = null;
            if (companySet.next()) {
                companyId = companySet.getString("ID");
            }
            companySet.close();

            if (isBlank(companyId)) {
                statement.close();
                connection.close();
                return "[]";
            }

            String query =
                    "select r.ID as reviewID, p.ID as productID, p.name as productName, " +
                            "r.rating, r.review, r.UserID as username, r.date " +
                            "from review r join products p on p.ID = r.PID " +
                            "where p.companyID = \"" + escapeSql(companyId) + "\"";

            if (!isBlank(productID)) {
                query = query + " and p.ID = \"" + escapeSql(productID) + "\"";
            }
            query = query + " order by r.date desc, r.ID desc;";

            ResultSet resultSet = statement.executeQuery(query);

            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            while (resultSet.next()) {
                if (!first) {
                    builder.append(",");
                }
                builder.append("{\"reviewID\":\"").append(escapeJson(resultSet.getString("reviewID"))).append("\",")
                        .append("\"productID\":\"").append(escapeJson(resultSet.getString("productID"))).append("\",")
                        .append("\"productName\":\"").append(escapeJson(resultSet.getString("productName"))).append("\",")
                        .append("\"rating\":").append(resultSet.getInt("rating")).append(",")
                        .append("\"review\":\"").append(escapeJson(resultSet.getString("review"))).append("\",")
                        .append("\"username\":\"").append(escapeJson(resultSet.getString("username"))).append("\",")
                        .append("\"date\":\"").append(escapeJson(resultSet.getString("date"))).append("\"}");
                first = false;
            }
            builder.append("]");

            resultSet.close();
            statement.close();
            connection.close();
            return builder.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    private static String getCompanySurveyAnswersJson(String companyName) {
        if (isBlank(companyName)) {
            return "[]";
        }

        Connection connection = null;
        try {
            connection = getDatabaseConnection();
            if (connection == null) {
                return "[]";
            }

            Statement statement = connection.createStatement();
            ResultSet companySet = statement.executeQuery(
                    "select ID from company where name = \"" + escapeSql(companyName) + "\";");

            String companyId = null;
            if (companySet.next()) {
                companyId = companySet.getString("ID");
            }
            companySet.close();

            if (isBlank(companyId)) {
                statement.close();
                connection.close();
                return "[]";
            }

            String query =
                    "select sr.surveyID, sr.responseID, p.ID as productID, p.name as productName, " +
                            "s.q1, s.q2, s.q3, sr.A1, sr.A2, sr.A3 " +
                            "from surveyresponse sr " +
                            "join survey s on s.ID = sr.surveyID " +
                            "join products p on p.ID = s.productid " +
                            "where p.companyID = \"" + escapeSql(companyId) + "\" " +
                            "order by sr.surveyID, sr.responseID;";

            ResultSet resultSet = statement.executeQuery(query);

            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            while (resultSet.next()) {
                if (!first) {
                    builder.append(",");
                }
                builder.append("{\"surveyID\":\"").append(escapeJson(resultSet.getString("surveyID"))).append("\",")
                        .append("\"responseID\":\"").append(escapeJson(resultSet.getString("responseID"))).append("\",")
                        .append("\"productID\":\"").append(escapeJson(resultSet.getString("productID"))).append("\",")
                        .append("\"productName\":\"").append(escapeJson(resultSet.getString("productName"))).append("\",")
                        .append("\"q1\":\"").append(escapeJson(resultSet.getString("q1"))).append("\",")
                        .append("\"q2\":\"").append(escapeJson(resultSet.getString("q2"))).append("\",")
                        .append("\"q3\":\"").append(escapeJson(resultSet.getString("q3"))).append("\",")
                        .append("\"a1\":\"").append(escapeJson(resultSet.getString("A1"))).append("\",")
                        .append("\"a2\":\"").append(escapeJson(resultSet.getString("A2"))).append("\",")
                        .append("\"a3\":\"").append(escapeJson(resultSet.getString("A3"))).append("\"}");
                first = false;
            }
            builder.append("]");

            resultSet.close();
            statement.close();
            connection.close();
            return builder.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    private static String getAllSurveysJson() {
        Connection connection = null;
        try {
            connection = getDatabaseConnection();
            if (connection == null) {
                return "[]";
            }

            Statement statement = connection.createStatement();
            String query =
                    "select s.ID as surveyID, p.name as productName, c.name as companyName, s.q1, s.q2, s.q3 " +
                            "from survey s " +
                            "join products p on p.ID = s.productid " +
                            "join company c on c.ID = p.companyID " +
                            "order by s.ID;";

            ResultSet resultSet = statement.executeQuery(query);

            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            while (resultSet.next()) {
                if (!first) {
                    builder.append(",");
                }
                builder.append("{\"surveyID\":\"").append(escapeJson(resultSet.getString("surveyID"))).append("\",")
                        .append("\"productName\":\"").append(escapeJson(resultSet.getString("productName"))).append("\",")
                        .append("\"companyName\":\"").append(escapeJson(resultSet.getString("companyName"))).append("\",")
                        .append("\"q1\":\"").append(escapeJson(resultSet.getString("q1"))).append("\",")
                        .append("\"q2\":\"").append(escapeJson(resultSet.getString("q2"))).append("\",")
                        .append("\"q3\":\"").append(escapeJson(resultSet.getString("q3"))).append("\"}");
                first = false;
            }
            builder.append("]");

            resultSet.close();
            statement.close();
            connection.close();
            return builder.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    private static String getAllCatalogueJson() {
        Connection connection = null;
        try {
            connection = getDatabaseConnection();
            if (connection == null) {
                return "[]";
            }

            Statement statement = connection.createStatement();
            String query =
                    "select p.ID, p.name, p.description, p.companyID, c.name as companyName " +
                            "from products p join company c on c.ID = p.companyID order by p.name;";

            ResultSet resultSet = statement.executeQuery(query);

            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            while (resultSet.next()) {
                if (!first) {
                    builder.append(",");
                }
                builder.append("{\"id\":\"").append(escapeJson(resultSet.getString("ID"))).append("\",")
                        .append("\"name\":\"").append(escapeJson(resultSet.getString("name"))).append("\",")
                        .append("\"description\":\"").append(escapeJson(resultSet.getString("description"))).append("\",")
                        .append("\"companyID\":\"").append(escapeJson(resultSet.getString("companyID"))).append("\",")
                        .append("\"companyName\":\"").append(escapeJson(resultSet.getString("companyName"))).append("\"}");
                first = false;
            }
            builder.append("]");

            resultSet.close();
            statement.close();
            connection.close();
            return builder.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx <= 0) {
                continue;
            }
            String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
            params.put(key, value);
        }
        return params;
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Very small and naive JSON string extractor
     * for flat payloads like {"username":"x","password":"y"}.
     */
    private static String extractJsonField(String json, String field) {
        if (json == null) return null;
        String pattern = "\"" + field + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int colon = json.indexOf(":", idx + pattern.length());
        if (colon < 0) return null;

        int firstQuote = json.indexOf("\"", colon + 1);
        if (firstQuote < 0) return null;
        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (secondQuote < 0) return null;

        return json.substring(firstQuote + 1, secondQuote);
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String escapeSql(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

