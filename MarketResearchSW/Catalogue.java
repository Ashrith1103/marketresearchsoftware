package MarketResearchSW;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class Catalogue
{
    // Reference to list of products.
    List<Product> products;
 
    // Constructor of this class
    Catalogue(String company)
    {
        this.products = new ArrayList<Product>();

        Connection connection = null;
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/marketresearchsw",
                "root", "Ash11032004");

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                "select * from company where name = \"" + company + "\";");

            String companyID = null;
            if(resultSet.next())
            {
                companyID = resultSet.getString("ID");
            }

            resultSet.close();

            if(companyID != null)
            {
                resultSet = statement.executeQuery(
                    "select * from products where companyID = \"" + companyID + "\";");

                while(resultSet.next())
                {
                    String pID = resultSet.getString("ID");
                    String name = resultSet.getString("name");
                    String desc = resultSet.getString("description");
                    String cID = resultSet.getString("companyID");

                    this.products.add(new Product(name, desc, pID, cID));
                }

                resultSet.close();
            }

            statement.close();
            connection.close();
        }
        catch(Exception e)
        {
            System.out.println("Unable to load catalogue for company");
        }
    }

    Catalogue()
    {
        this.products = new ArrayList<Product>();

        Connection connection = null;
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/marketresearchsw",
                "root", "Ash11032004");

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from products;");

            while(resultSet.next())
            {
                String pID = resultSet.getString("ID");
                String name = resultSet.getString("name");
                String desc = resultSet.getString("description");
                String cID = resultSet.getString("companyID");

                this.products.add(new Product(name, desc, pID, cID));
            }

            resultSet.close();
            statement.close();
            connection.close();
        }
        catch(Exception e)
        {
            System.out.println("Unable to load catalogue");
        }
    }

    public List<Product> getCatalogue()
    {
        return products;
    }

    void addItem(Product p)
    {
        if(this.products == null)
        {
            this.products = new ArrayList<Product>();
        }
        this.products.add(p);
    }

    //Function to edit a certain product - use setProductDetails
    boolean editItem(String productID, String newName, String newDesc)
    {
        if(this.products != null)
        {
            for(Product p : this.products)
            {
                if(p.pID.equals(productID))
                {
                    p.setProductDetails(newName, newDesc);
                    return true;
                }
            }
        }

        Product p = Product.getProductDetails(productID);
        if(p != null)
        {
            p.setProductDetails(newName, newDesc);
            addItem(p);
            return true;
        }

        return false;
    }
    
}