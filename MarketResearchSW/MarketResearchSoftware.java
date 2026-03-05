package MarketResearchSW;

import java.util.Scanner;

class MarketResearchSoftware
{
    public static void main(String args[]) 
    {
        Scanner sc = new Scanner(System.in);
        int num;
        String id,name,type,pID,company,cID,pname,cname,q1,q2,q3;
        Product p;

        System.out.println("Welcome to Market Research Software");

        //input username and password
        System.out.print("Username: ");
        String userName = sc.nextLine();
        System.out.print("Password: ");
        String passWord= sc.next();

        // Creating Login Model
        Login loginobj = new Login();
        
        // Creating User
        User user = loginobj.login(userName, passWord);
        // System.out.println(user.username + " " + user.company);
        // System.out.println(user.username + " " + user.company);
        
        while(loginobj.logged_in) //that user can do whatever he is allowed to as long as they are logged in
        {
            
            // Product P = new Product("Crutches", "Provides support to those who need it", "SLIFGH9876KJ", "LO2F03N4");
            // System.out.println(Product.getProductID("S23", "Samsung"));
            // P.addProduct();
            // user.viewAvailableSurveys();
            // user.createSurvey("S23", user.company, "sdf", "sfgsfgd", "asfg");
            // Survey s = new Survey("SLKFJG20498R");
            // System.out.println(s.getSurveyQ2());
            // user.fillSurvey("SLKFJG20498R", "Good", "Meh", "IDC");
            // user.viewCompanyCatalogue();
            // user.reviewProduct("Samsung", "S23", "Pretty cool", 4);
            // user.viewCatalogue();
            // Product p = Product.getProductDetails("POLKJFG98");
            // String cmp = Product.getProductCompany("POLKJFG98");
            // System.out.println(p.name + "\t" + p.cID + "\t" + p.desc + " \t" + cmp);
            // user.viewAllReviews();
            // user.viewReviews("POLKJFG98");
            // user.generateReport(p, ReportType.PDF, "D:\\Documents\\");

            switch(user.role)
            {
                case CompanyExecutive: //display company exec page
                    System.out.println("\n*** ROLE: Company Executive ***");
                    System.out.println("1. View Company Catalogue");
                    System.out.println("2. Generate Report");
                    System.out.println("3. View Product Review");
                    System.out.println("4. View All Reviews");
                    System.out.println("5. Logout");
                    System.out.print("Enter CHOICE: ");

                    num = sc.nextInt();

                    switch(num)
                    {
                        case 1:
                            user.viewCompanyCatalogue();
                            break;
                        case 2:
                            System.out.print("Enter Product ID: ");
                            pID = sc.next();
                            
                            p = Product.getProductDetails(pID);
                            
                            System.out.print("Enter Report Format (PDF/HTML): ");
                            type = sc.next();
                            if (type.equals("PDF"))
                                user.generateReport(p, ReportType.PDF);
                            else if(type.equals("HTML"))
                                user.generateReport(p, ReportType.HTML);
                            else
                            {
                                System.out.println("DEFAULT to PDF");
                                user.generateReport(p, ReportType.PDF);
                            }
                            break;
                        case 3:
                            System.out.print("Enter Product ID: ");
                            id = sc.next();
                            user.viewReviews(id);
                            break;
                        case 4:
                            user.viewAllReviews();
                            break;
                        case 5:
                            loginobj.logout();
                            break;
                        default:
                            System.out.println("Invalid Choice");
                            break;
                    }
                    break;

                case MarketResearcher: //display market researcher page
                    System.out.println("\n\n*** ROLE: Market Researcher ***");
                    System.out.println("1. Generate Report");
                    System.out.println("2. Add Product");
                    System.out.println("3. Create Survey");
                    System.out.println("4. View Available Surveys");
                    System.out.println("5. View Company Catalogue");
                    System.out.println("6. View Product Review");
                    System.out.println("7. View All Reviews");
                    System.out.println("8. Logout");
                    System.out.print("Enter CHOICE: ");

                    num = sc.nextInt();

                    switch(num)
                    {
                        case 1:
                            System.out.print("Enter Product ID: ");
                            pID = sc.next();
                            p = Product.getProductDetails(pID);
                            System.out.print("Enter Report Format (PDF/HTML): ");
                            type = sc.next();
                            if (type.equals("PDF"))
                                user.generateReport(p, ReportType.PDF);
                            else if(type.equals("HTML"))
                                user.generateReport(p, ReportType.HTML);
                            else
                            {
                                System.out.println("DEFAULT to PDF");
                                user.generateReport(p, ReportType.PDF);
                            }
                            break;
                        case 2:
                            System.out.print("Enter Product name: ");
                            name = sc.next();
                            System.out.print("Enter Product Description: ");
                            type = sc.nextLine();
                            type = sc.nextLine();
                            System.out.print("Enter Product ID: ");
                            pID = sc.next();
                            company = user.company;
                            System.out.print("Enter Company ID ");
                            cID = sc.next();
                            user.addProduct(name, type, pID, company, cID);
                            break;
                        case 3:
                            System.out.print("Enter Product name: ");
                            pname = sc.next();
                            cname = user.company;
                            System.out.println("Enter First Question");
                            q1 = sc.nextLine();
                            q1 = sc.nextLine();
                            System.out.println("Enter Second Question");
                            q2 = sc.nextLine();
                            System.out.println("Enter Third Question");
                            q3 = sc.nextLine();
                            user.createSurvey(pname,cname,q1,q2,q3);
                            break;
                        case 4:
                            user.viewAvailableSurveys();
                            break;
                        case 5:
                            user.viewCompanyCatalogue();
                            break;
                        case 6:
                            System.out.print("Enter Product ID: ");
                            pID = sc.next();
                            user.viewReviews(pID);
                            break;
                        case 7:
                            user.viewAllReviews();
                            break;
                        case 8:
                            loginobj.logout();
                            break;
                        default:
                            System.out.println("Invalid Choice");
                            break;
                    }
                    break;

                case Customer:         //display customer page
                    System.out.println("\n\nROLE: Customer");
                    System.out.println("1. Fill Survey");
                    System.out.println("2. View Available Survey");
                    System.out.println("3. Review product");
                    System.out.println("4. View Catalogue");
                    System.out.println("5. Logout");
                    System.out.print("Enter CHOICE: ");

                    num = sc.nextInt();

                    switch(num)
                    {
                        case 1://user.fillSurvey("SLKFJG20498R", "Good", "Meh", "IDC");
                            System.out.println("Enter the surveyID: ");
                            String sid = sc.nextLine();
                            sid = sc.nextLine();
                            Survey s = new Survey(sid);
                            System.out.println(s.getSurveyQ1());
                            String a1 = sc.nextLine();
                            System.out.println(s.getSurveyQ2());
                            String a2 = sc.nextLine();
                            System.out.println(s.getSurveyQ3());
                            String a3 = sc.nextLine();
                            user.fillSurvey(sid, a1, a2, a3);
                            break;
                        case 2: user.viewAvailableSurveys(); 
                        break;
                        case 3://user.reviewProduct("Samsung", "S23", "Pretty cool", 4);
                        System.out.println("Enter the name of the product: ");
                        String p1name = sc.nextLine();
                        p1name = sc.nextLine();
                        System.out.println("Enter the name of the company: ");
                        String c1name = sc.nextLine();
                        System.out.println("Enter your rating of the product: ");
                        int rating = sc.nextInt();
                        System.out.println("Enter your review for the product ");
                        String rev = sc.nextLine();
                        rev = sc.nextLine();
                        user.reviewProduct(c1name, p1name, rev, rating);
                        break;
                        case 4:
                            user.viewCatalogue();
                            break;
                        case 5:
                            loginobj.logout();
                            break;
                        default:
                            System.out.println("Invalid Choice");
                            break;
                    }
                    break;

                default:                    //display company exec page
                    break;
            }

        }

        sc.close();
    }
}