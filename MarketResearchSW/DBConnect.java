package MarketResearchSW;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

final class DBConnect {
    private static final String URL =
            System.getProperty("mr.db.url", "jdbc:mysql://localhost:3306/marketresearchsw");
    private static final String USER =
            System.getProperty("mr.db.user", "root");
    private static final String PASSWORD =
            System.getProperty("mr.db.password", "");
    private static final String FALLBACK_PASSWORD =
            System.getProperty("mr.db.password.fallback", "Ash11032004");

    private DBConnect() {
    }

    static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found.", e);
        }

        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException primaryEx) {
            if (FALLBACK_PASSWORD.equals(PASSWORD)) {
                throw primaryEx;
            }
            return DriverManager.getConnection(URL, USER, FALLBACK_PASSWORD);
        }
    }
}
