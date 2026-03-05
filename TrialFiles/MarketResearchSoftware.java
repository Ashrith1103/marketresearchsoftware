package TrialFiles;


import java.util.Date;
import java.util.List;
import java.util.Scanner;

class MarketResearchSoftware
{
    public static void main(String args[]) 
    {
        Scanner sc = new Scanner(System.in);

        
        System.out.println("Welcome to Market Research Software");
        Login loginobj = new Login();
        //input username and password
        User user = loginobj.login("username", "password");
        while(loginobj.logged_in) //that user can do whatever he is allowed to as long as they are logged in
        {
            //switch case -> for each type of user diff interface
            Product p = new Product("name", "mobile", "alshdg", "Samsung", "alskbf");
            System.out.println("CP1");
            user.generateReport(p);
            user.checkCatalogue();
        }

        sc.close();
    }
}

class Login
{
    boolean logged_in;

    Login()
    {
        this.logged_in = false;
    }

    User login(String username, String password)
    {
        //connect to user database and verify details
        System.out.println("Logged in");
        this.logged_in = true;
        User user = new MarketResearcher("username", "password", "company", 0, Role.MarketResearcher);
        // output a user object
        return user;
    }

    void logout()
    {
        System.out.println("Logged out");
        this.logged_in = false;
    }
}

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

    void viewCatalogue() //view all products assoc. to a company
    {
        errorMsg();
    }
    
    void viewReport(String product) //view summary report of a particular product based on reviews like avg rating etc
    {
        errorMsg();
    }
    
    void viewReviews(String product) // view reviews pertaining to a product
    {
        errorMsg();
    }

    void viewAllReviews() //view all reviews related to company
    {
        errorMsg();
    }

    void generateReport(Product p) //ideally return a downloadable report that is then pushed to a database
    {
        errorMsg();
    }
    void editCatalogue()
    {
        errorMsg();
    }
    void createSurvey()
    {
        errorMsg();
    }
    void editEmailData()
    {
        errorMsg();
    }
    void sendEmails()
    {
        errorMsg();
    }

    void fillSurvey(String surveyID)
    {
        errorMsg();
    }
    void viewAvailableSurveys()
    {
        errorMsg();
    }
    void checkCatalogue() //check if product in catalogue
    {
        errorMsg();
    }
    void reviewProduct(String company, String product)
    {
        errorMsg();
    }

    void errorMsg()
    {
        System.out.println("Method not applicable");
    }

}

class CompanyExec extends User
{
    CompanyExec(String usr, String pass, String comp, int acl, Role r)
    {
        this.username = usr;
        this.password = pass;
        this.company = comp;
        this.accessLevel = acl;
        this.role = r;
    }

    @Override
    void viewCatalogue() //view all products assoc. to a company
    {
        System.out.println("Here is the current catalogue of products for company " + this.company);
        //retrieve all related products from catalogue table
        //print everything
    }

    @Override
    void viewReport(String product) //view summary report of a particular product based on reviews like avg rating etc
    {
        System.out.println("Report for product "  + product + "by company " + this.company);
        //retrieve report from report table
        //give option to view or download
    }

    @Override
    void viewReviews(String product) // view reviews pertaining to a product
    {
        System.out.println("Report for product "  + product + "by company " + this.company);
        //retrieve all reviews for product from review table
        //print everything
    }

    @Override
    void viewAllReviews() //view all reviews related to company
    {
        System.out.println("Here are all the reviews for company " + this.company);
        //retrieve all related reviews from review table
        //print everything
    }
}

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
    void fillSurvey(String surveyID)
    {
       //extract questions for this survey id from survey questions table
       //get customer answers
       //put answers back in db
       //confirmation msg
    }

    @Override
    void viewAvailableSurveys()
    {
        //look up db
        //display available surveys - ie. survey name, product/company
    }

    @Override
    void checkCatalogue() //check if product in catalogue
    {
        //look up db
        //display available products ordered by company
    }

    @Override
    void reviewProduct(String company, String product)
    {
        //get review and rating from user
        //put into db
        //get confirmation msg
    }
}

class MarketResearcher extends User
{
    MarketResearcher(String usr, String pass, String comp, int acl, Role r)
    {
        this.username = usr;
        this.password = pass;
        this.company = comp;
        this.accessLevel = acl;
        this.role = r;
    }

    @Override
    void viewCatalogue() //view all products assoc. to a company
    {
        System.out.println("Here is the current catalogue of products for company " + this.company);
        //retrieve all related products from catalogue table
        //print everything
    }

    @Override
    void viewReport(String product) //view summary report of a particular product based on reviews like avg rating etc
    {
        System.out.println("Report for product "  + product + "by company " + this.company);
        //retrieve report from report table
        //give option to view or download
    }

    @Override
    void viewReviews(String product) // view reviews pertaining to a product
    {
        System.out.println("Report for product "  + product + "by company " + this.company);
        //retrieve all reviews for product from review table
        //print everything
    }

    @Override
    void viewAllReviews() //view all reviews related to company
    {
        System.out.println("Here are all the reviews for company " + this.company);
        //retrieve all related reviews from review table
        //print everything
    }

    @Override
    void generateReport(Product p) //ideally return a downloadable report that is then pushed to a database
    {
        //switch case -> what type of report would you like
        //also stats and visuals?
        System.out.println("CP2");
        ReportGeneratorFacade.generateReport(ReportType.HTML, null, p);
        //ReportGeneratorFacade.generateReport(ReportType.PDF, null);
        //Use something similar to facade example, facade pattern, option of HTML, pdf report
        // Builder pattern for adding stats and then visualizations?
    }

    @Override
    void editCatalogue()
    {
        // make changes to catalogue database
    }

    @Override
    void createSurvey()
    {
        // add survey to db
    }

    @Override
    void editEmailData()
    {
        // add email to db
    }

    @Override
    void sendEmails()
    {
        // send out emails to addresses from db
    }

    @Override
    void viewAvailableSurveys()
    {
        //look up db
        //display available surveys - ie. survey name, product/company
    }

    @Override
    void checkCatalogue() //check if product in catalogue
    {
        //look up db
        //display available products ordered by company
    }

}

class Product
{
    String name;
    String type;
    String pID;
    String company;
    String cID;

    Product(String name, String type, String pID, String company, String cID)
    {
        this.name = name;
        this.type = type;
        this.pID = pID;
        this.company = company;
        this.cID = cID;
    }

    void getProductDetails(String pID)
    {
        //connect to DB and retreive product info
    }

    void setProductDetails(String pID)
    {
        // edit db
    }

    Product getProduct(String pID)
    {
        return this;
    }
    
}

class Catalogue
{
    // Reference to refer to list of books.
    List<Product> products;
 
    // Constructor of this class
    Catalogue(List<Product> prod)
    {
        //connect to db and get products of this company
        // This keyword refers to current instance itself
        this.products = prod;
    }

    public List<Product> getCatalogue()
    {
        return products;
    }

    void addItem(Product p)
    {
        this.products.add(p);
    }

    //Function to edit a certain product
    //Function to view more details of a certain product given pID 
}

//Email Handler class -> to view/edit email database (Similar to catalogue?) ; Send emails (retreive email for product -> send)
//Review class -> add review
//SUrvey class


// Other classes that can possibly be added - a class each for Product, Catalogue, Email, Review, Survey to ensure single responsibility principle is maintained.
// Facade pattern, Builder pattern, prototype pattern for Survey
//MVC arch. ----idk how rn
// DAO/Proxy pattern for auth. using access level?

class ReportGenerator 
{
 
    private String header;
    private String data;
    private String footer;
     
    public String getHeader() 
    {
      return header;
    }
    public void setHeader(String header) 
    {
      System.out.println("Setting report header");
      this.header = header;
    }
    public String getData() 
    {
      return data;
    }
    public void setData(String data) 
    {
      System.out.println("Setting report data");
      this.data = data;
    }
    public String getFooter() 
    {
      return footer;
    }
    public void setFooter(String footer) 
    {
      System.out.println("Setting report footer");
      this.footer = footer;
    }
}

enum ReportType 
{
    PDF, 
    HTML
}
  
class ReportWriter 
{
     
    public void writeHtmlReport(ReportGenerator report, String location) 
    {
      System.out.println("HTML Report written");
      System.out.println(report.getHeader());
      System.out.println(report.getData());
      System.out.println(report.getFooter());
    }
     
    public void writePdfReport(ReportGenerator report, String location) 
    {
      System.out.println("Pdf Report written");
      System.out.println(report.getHeader());
      System.out.println(report.getData());
      System.out.println(report.getFooter());
    }
}
  
   
class ReportGeneratorFacade 
{
    public static void generateReport(ReportType type, String location, Product p) //throws Exception 
    {
      if(type == null) 
      {
        System.out.println("File type not specified");
        //throw new Exception();
      }

      //Create report
      ReportGenerator report = new ReportGenerator();
 
      report.setHeader(p.name + " Report");
      report.setFooter("Generated for " + p.company + " at time " + (new Date().toString()));
       
      //Get data from dataSource and set to String object
       
      String data = generateReportData();
      report.setData(data);
       
      //Write report
      ReportWriter writer = new ReportWriter();
      switch(type) 
      {
        case HTML:
          writer.writeHtmlReport(report, location);
          break;
           
        case PDF:
          writer.writePdfReport(report, location);
          break;
      }
    }

    static String generateReportData()
    {
        return "Data";
    }
  }