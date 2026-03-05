package MarketResearchSW;

import java.sql.*;

//import java.util.List;

class Customer extends User
{
    Customer(String usr, String pass, String comp, int acl, Role r)
    {
        this.username = usr;
        this.password = pass;
        this.company = comp;
        this.accessLevel = acl;
        this.role = r;
    }

    @Override
     boolean fillSurvey(String surveyID, String a1, String a2, String a3)
    {
       SurveyFilled answers = new SurveyFilled(surveyID);
       answers.fillAnswer1(a1);
       answers.fillAnswer2(a2);
       answers.fillAnswer3(a3);
       //put answers back in db
       //System.out.println(answers.answer1);
         boolean submitted = answers.fillSurveyForm(this);
       //confirmation msg
         if (submitted) {
              System.out.println("Survey has been filled");
         }
         return submitted;
    }

    @Override
    ResultSet viewAvailableSurveys()
    {
        Connection connection = null;
        ResultSet resultSet = null;
        try 
        {
            // below two lines are used for connectivity.
            connection = DBConnect.getConnection();
 
            Statement statement;
            statement = connection.createStatement();
            resultSet = statement.executeQuery(
                "select * from survey;");

            //System.out.println(resultSet);

            int flag = 0;

            while(resultSet.next()) 
            {
                flag = 1;
                String id = resultSet.getString("ID");
                String pid = resultSet.getString("productid");
                String q1 = resultSet.getString("q1");
                String q2 = resultSet.getString("q2");
                String q3 = resultSet.getString("q3");
                String pname = Product.getProductDetails(pid).name;
                System.out.println("--------------------- "+ id +" ----------------------");
                System.out.println("Product: " + pname);
                System.out.println("Question 1: " + q1);
                System.out.println("Question 2: " + q2);
                System.out.println("Question 3: " + q3);
                System.out.println("----------------------------------------------------");


            }

            if(flag==0)
            {
                System.out.println("No surveys available");
            }
            //System.out.println(resultSet.getString("ID").trim());

            statement.close();
            connection.close();
            
        }
        catch(Exception e)
        {
            System.out.println("Sorry, there was issue in retrieving survey records");
        }
        
        return resultSet;

    }

    @Override
    boolean reviewProduct(String company, String product, String productReview, int rating)
    {
        String pID = Product.getProductID(product, company);
        if (pID == null || pID.isBlank()) {
            System.out.println("Invalid product/company combination");
            return false;
        }
        //get review and rating from user
        Review review = new Review(pID, productReview, rating);
        //System.out.println(review.pID + "\t" + review.rating + "\t" + review.review + "\t" + review.dateOfReview);
        //put into db
        return review.addReview(this);
        //get confirmation msg
    }

    @Override
    ResultSet viewCatalogue() //view all products assoc. to a company
    {
        ResultSet resultSet = null;
        //retrieve all related products from db
        Connection connection = null;

        try 
        {
            // below two lines are used for connectivity.
            connection = DBConnect.getConnection();
 
            Statement statement;
            statement = connection.createStatement();

            String command = "select * from products;";
            //System.out.println(command);
            resultSet = statement.executeQuery(command);
            

            while(resultSet.next()) 
            {
                String product = resultSet.getString("name");
                String pID = resultSet.getString("ID");
                String desc = resultSet.getString("description");

                String c = Product.getProductCompany(pID);

                System.out.println(c + "\t" + product + "\t" + desc);

            }

            statement.close();
            connection.close();

        }
        catch(Exception e)
        {
            System.out.println("There was an issue retrieving catalogue");
        }

        return resultSet;
    }
}