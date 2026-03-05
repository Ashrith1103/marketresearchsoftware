package MarketResearchSW;

import java.sql.*;

class Login
{
    boolean logged_in;

    Login()
    {
        this.logged_in = false;
    }

    User login(String username, String password)
    {
        //connect to user database and verify details, once verified get access level and role
        Connection connection = null;
        Role role = null;
        String company = null;
        int acl = 0;

        User user = null;

        try 
        {
            // below two lines are used for connectivity.
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = connectToDatabase();

            if(connection == null)
            {
                System.out.println("Unable to connect to database");
                return null;
            }
 
            Statement statement;
            statement = connection.createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(
                "select * from userlogin where username = \"" + username + "\";");

            //System.out.println(resultSet);

            while (resultSet.next()) 
            {
                if(password.equals(resultSet.getString("password").trim()))
                {
                    System.out.println("Logged in");
                    this.logged_in = true;
                    String r = resultSet.getString("role").trim();
                    if(r.equals("Market Researcher"))
                    {
                        role = Role.MarketResearcher;
                        company = resultSet.getString("company").trim();
                        acl = resultSet.getInt("accesslevel");
                    }
                    else if(r.equals("Company Executive"))
                    {
                        role = Role.CompanyExecutive;
                        company = resultSet.getString("company").trim();
                        acl = resultSet.getInt("accesslevel");
                    }
                    else
                    {
                        role = Role.Customer;
                    }
                }
            }

            if(this.logged_in == false)
            {
                System.out.println("Invalid username/password");
            }

            switch(role)
            {
                case MarketResearcher: user = new MarketResearcher(username, password, company, acl, Role.MarketResearcher); break;
                case CompanyExecutive: user = new CompanyExec(username, password, company, acl, Role.CompanyExecutive); break;
                case Customer:         user = new Customer(username, password, null, 0, Role.Customer); break;
            }
            resultSet.close();
            statement.close();
            connection.close();
        }
        catch (Exception exception) 
        {
            System.out.println(exception);
        }
        
        //output a user object
        return user;
    }

    private Connection connectToDatabase()
    {
        String url = "jdbc:mysql://localhost:3306/marketresearchsw";
        String username = "root";

        try
        {
            return DriverManager.getConnection(url, username, "");
        }
        catch(Exception e)
        {
            try
            {
                return DriverManager.getConnection(url, username, "Ash11032004");
            }
            catch(Exception ignored)
            {
                return null;
            }
        }
    }

    void logout()
    {
        System.out.println("Logged out");
        this.logged_in = false;
    }
}