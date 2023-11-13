import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Data {
    private static Connection connection;
    private static HashMap<String, User> loggedUsers = new HashMap<>();

    public Data() {
        connect();
        createTables();
    }

    public static void connect(){
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        String url = "jdbc:sqlite:src/datafiles/database.db";
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createTables() {
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:src/datafiles/database.db");

            stmt = connection.createStatement();

            String users = "CREATE TABLE IF NOT EXISTS USER " +
                    "(EMAIL INT PRIMARY KEY," +
                    " NAME TEXT NOT NULL, " +
                    " PASSWORD TEXT NOT NULL, " +
                    " NIF CHAR(9) NOT NULL, " +
                    " ISADMIN INT NOT NULL)";
            stmt.executeUpdate(users);

            String events = "CREATE TABLE IF NOT EXISTS EVENT " +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " NAME TEXT NOT NULL," +
                    " LOCAL TEXT NOT NULL, " +
                    " DATE TEXT NOT NULL, " +
                    " BEGINHOUR TEXT NOT NULL, " +
                    " ENDHOUR TEXT NOT NULL)";
            stmt.executeUpdate(events);
            stmt.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    public User authenticate(String email1, String password1) {
        String query = "SELECT * FROM USER WHERE EMAIL = ? AND PASSWORD = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email1);
            preparedStatement.setString(2, password1);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String email = resultSet.getString("EMAIL");
                String name = resultSet.getString("NAME");
                String password = resultSet.getString("PASSWORD");
                int identificationNumber = resultSet.getInt("NIF");
                boolean isAdmin = resultSet.getBoolean("ISADMIN");
                User user = new User(name, identificationNumber, email, password, isAdmin);

                loggedUsers.put(email, user);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void registerUser(User newUser) {
        String insertUserSql = "INSERT INTO USER (EMAIL, NAME, PASSWORD, NIF, ISADMIN) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertUserSql)) {
            preparedStatement.setString(1, newUser.getEmail());
            preparedStatement.setString(2, newUser.getName());
            preparedStatement.setString(3, newUser.getPassword());
            preparedStatement.setInt(4, newUser.getIdentificationNumber());
            preparedStatement.setBoolean(5, newUser.isAdmin());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
