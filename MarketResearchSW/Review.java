package MarketResearchSW;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Random;


class Review 
{
    String pID;
    String review;
    int rating;
    Date dateOfReview;

    Review(String id, String rev, int rating)
    {
        this.pID = id;
        this.review = rev;
        this.rating = rating;
        this.dateOfReview = new Date();
    }

    static String viewAvailableReviews()
    {
        Connection conn = null;
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        boolean hasRows = false;

        try
        {
            conn = DBConnect.getConnection();
            Statement statement = conn.createStatement();
            String query = "select p.ID as productID, p.name as productName, c.name as companyName, " +
                           "count(r.ID) as totalReviews " +
                           "from products p " +
                           "join company c on c.ID = p.companyID " +
                           "left join review r on r.PID = p.ID " +
                           "group by p.ID, p.name, c.name " +
                           "order by c.name, p.name;";

            ResultSet resultSet = statement.executeQuery(query);

            System.out.println("ProductID\tProduct\tCompany\tExistingReviews");
            while(resultSet.next())
            {
                hasRows = true;
                String productID = resultSet.getString("productID");
                String productName = resultSet.getString("productName");
                String companyName = resultSet.getString("companyName");
                int totalReviews = resultSet.getInt("totalReviews");

                System.out.println(productID + "\t" + productName + "\t" + companyName + "\t" + totalReviews);

                if(!first)
                {
                    json.append(",");
                }

                json.append("{\"productID\":\"").append(escapeJson(productID)).append("\",")
                    .append("\"productName\":\"").append(escapeJson(productName)).append("\",")
                    .append("\"companyName\":\"").append(escapeJson(companyName)).append("\",")
                    .append("\"existingReviews\":").append(totalReviews).append("}");
                first = false;
            }

            if(!hasRows)
            {
                System.out.println("No devices available to review");
            }

            resultSet.close();
            statement.close();
            conn.close();
        }
        catch(SQLException ex)
        {
            System.out.println("Error while fetching reviewable devices: " + ex.getMessage());
        }

        json.append("]");
        return json.toString();
    }

    private static String escapeJson(String value)
    {
        if(value == null)
        {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    boolean addReview(User u) 
    {
        if (u == null || u.username == null || u.username.isBlank() || this.pID == null || this.pID.isBlank()) {
            System.out.println("Invalid review request");
            return false;
        }

        try 
        {
            Connection conn = DBConnect.getConnection();

            String duplicateCheckSql = "SELECT 1 FROM review WHERE PID = ? AND UserID = ? LIMIT 1";
            PreparedStatement duplicateCheck = conn.prepareStatement(duplicateCheckSql);
            duplicateCheck.setString(1, this.pID);
            duplicateCheck.setString(2, u.username);
            ResultSet existing = duplicateCheck.executeQuery();

            if (existing.next()) {
                System.out.println("You have already reviewed this device.");
                existing.close();
                duplicateCheck.close();
                conn.close();
                return false;
            }

            existing.close();
            duplicateCheck.close();

            Random rand = new Random();
            String newID = this.pID + u.username + rand.nextInt(1000);

            String sql = "INSERT INTO review VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, newID);
            statement.setString(2, this.pID);
            statement.setString(3, u.username); 
            statement.setDate(4, new java.sql.Date(this.dateOfReview.getTime()));
            statement.setInt(5, this.rating);
            statement.setString(6, this.review);
            
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Review added successfully!");
                statement.close();
                conn.close();
                return true;
            }

            statement.close();
            conn.close();
            return false;
        } 
        catch (SQLException ex) 
        {
            System.out.println("Error: " + ex.getMessage());
            return false;
        }
    }
}