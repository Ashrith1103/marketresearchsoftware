package MarketResearchSW;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

    void addReview(User u) 
    {
        try 
        {
            Connection conn = DBConnect.getConnection();

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
            }
            conn.close();
        } 
        catch (SQLException ex) 
        {
            System.out.println("Error: " + ex.getMessage());
        }
    }
}