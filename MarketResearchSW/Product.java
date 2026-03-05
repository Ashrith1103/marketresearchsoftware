package MarketResearchSW;

import java.sql.*;

class Product
{
    String name;
    String desc;
    String pID;
    String cID;

    Product(String name, String desc, String pID, String cID)
    {
        this.name = name;
        this.desc = desc;
        this.pID = pID;
        this.cID = cID;
    }

    static Product getProductDetails(String pID)
    {
        Connection connection = null;
        Product p = null;
        try 
        {
            // below two lines are used for connectivity.
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/marketresearchsw",
                "root", "Ash11032004");
 
            Statement statement;
            statement = connection.createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(
                "select * from products where id = \"" + pID + "\";");

            //System.out.println(resultSet);

            if(resultSet.next()) 
            {
                // System.out.println("Product Found");

                String name = resultSet.getString("name");
                String desc = resultSet.getString("description");
                String cID = resultSet.getString("companyID");
                
                p = new Product(name, desc, pID, cID);
                
            }

            resultSet.close();
            statement.close();
            connection.close();
        }
        catch(Exception e)
        {
            //something
            System.out.println("Product not Found");
        }

        return p; 
    }

    static String getProductCompany(String pID)
    {
        Connection connection = null;
        String company = "";
        try 
        {
            // below two lines are used for connectivity.
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/marketresearchsw",
                "root", "Ash11032004");
 
            Statement statement;
            statement = connection.createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(
                "select * from products where id = \"" + pID + "\";");

            //System.out.println(resultSet);
            String cID = "";

            if(resultSet.next()) 
            {
                // System.out.println("Product Found");
                
                cID = resultSet.getString("companyID");
                //System.out.println("select * from company where id = \"" + cID + "\";");
                ResultSet resultSet1;
                resultSet1 = statement.executeQuery(
                "select * from company where id = \"" + cID + "\";");
                if(resultSet1.next())
                    company = resultSet1.getString("name");

            }

            resultSet.close();
            statement.close();
            connection.close();
        }
        catch(Exception e)
        {
            //something
            System.out.println("Not able to find Company for the given Product");
        }

        //System.out.println(company);

        return company; 
    }

    static String getProductID(String product, String company)
    {
        Connection connection = null;
        String pID = null;
        try 
        {
            // below two lines are used for connectivity.
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/marketresearchsw",
                "root", "Ash11032004");
 
            Statement statement;
            statement = connection.createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(
                "select * from products where name = \"" + product + "\";");

            //System.out.println(resultSet);

            if(resultSet.next()) 
            {
                // System.out.println("Product Found");

                pID = resultSet.getString("ID");
            }

            resultSet.close();
            statement.close();
            connection.close();
        }
        catch(Exception e)
        {
            //something
            System.out.println("Product Not Found");
        }

        //System.out.println(pID);

        return pID; 
    }

    void addProduct()
    {
        Connection connection = null;

        try 
        {
            // below two lines are used for connectivity.
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/marketresearchsw",
                "root", "Ash11032004");
 
            Statement statement;
            statement = connection.createStatement();

            String command = "insert into products values(\"" + this.pID + "\",\"" + this.name + "\",\"" + this.desc + "\",\"" + this.cID + "\");";
            // System.out.println(command);
            statement.executeUpdate(command);

            statement.close();
            connection.close();

        }
        catch(Exception e)
        {
            //something
            System.out.println("Unable to insert in product database");
        }
    }

    void setProductDetails(String name, String desc)
    {
        this.name = name;
        this.desc = desc;

        Connection connection = null;
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/marketresearchsw",
                "root", "Ash11032004");

            Statement statement = connection.createStatement();
            String command = "update products set name = \"" + this.name + "\", description = \"" + this.desc + "\" where ID = \"" + this.pID + "\";";
            statement.executeUpdate(command);

            statement.close();
            connection.close();
        }
        catch(Exception e)
        {
            System.out.println("Unable to update product details");
        }
    }
}