package MarketResearchSW;

import java.sql.ResultSet;

//import java.util.List;

enum Role
{
    CompanyExecutive,
    MarketResearcher,
    Customer
}

class User
{
    String username;
    String password;
    String company;
    int accessLevel;
    Role role;
    
    ResultSet viewReviews(String productID) // view reviews pertaining to a product
    {
        ResultSet x = null;
        errorMsg();
        return x;
    }

    ResultSet viewAllReviews() //view all reviews related to company
    {
        ResultSet x = null;
        errorMsg();
        return x;
    }

    void generateReport(Product p, ReportType type) //ideally return a downloadable report that is then pushed to a database
    {
        errorMsg();
    }

    void addProduct(String name, String type, String pID, String company, String cID)
    {
        errorMsg();
    }
    void createSurvey(String pname, String cname, String q1, String q2, String q3)
    {
        errorMsg();
    }
    
    /*void editEmailData()
    {
        errorMsg();
    }
    void sendEmails()
    {
        errorMsg();
    }*/

    ResultSet viewCompanyCatalogue()
    {
        ResultSet x = null;
        errorMsg();
        return x;
    }

    ResultSet viewCatalogue()
    {
        ResultSet x = null;
        errorMsg();
        return x;
    }

    void fillSurvey(String surveyID, String a1, String a2, String a3)
    {
        errorMsg();
    }
    ResultSet viewAvailableSurveys()
    {
        ResultSet x = null;
        errorMsg();
        return x;
    }
    void reviewProduct(String company, String product, String productReview, int rating)
    {
        errorMsg();
    }

    void errorMsg()
    {
        System.out.println("Method not applicable");
    }

}