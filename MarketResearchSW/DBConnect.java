package MarketResearchSW;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

final class DBConnect {
    private static final String URL =
            System.getProperty("mr.db.url", "jdbc:mysql://localhost:3306/marketresearchsw");
    private static final String USER =
            System.getProperty("mr.db.user", "root");
        private static final String PRIMARY_SECRET =
            System.getProperty("mr.db.secret", "");
        private static final String FALLBACK_SECRET =
            System.getProperty("mr.db.secret.fallback", "");

    private DBConnect() {
    }

    static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found.", e);
        }

        try {
            return DriverManager.getConnection(URL, USER, PRIMARY_SECRET);
        } catch (SQLException primaryEx) {
            if (FALLBACK_SECRET.equals(PRIMARY_SECRET)) {
                throw primaryEx;
            }
            return DriverManager.getConnection(URL, USER, FALLBACK_SECRET);
        }
    }
}
