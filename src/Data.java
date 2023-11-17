import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;

public class Data {
    private static Connection connection;

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
                    " ENDHOUR TEXT NOT NULL," +
                    " CODE TEXT," +
                    " CODEEXPIRATIONTIME TIME)";
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
                int nif = resultSet.getInt("NIF");
                boolean isAdmin = resultSet.getBoolean("ISADMIN");

                return new User(name, nif, email, password, isAdmin);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean registerUser(User newUser) {
        String query = "SELECT * FROM USER WHERE EMAIL = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, newUser.getEmail());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String insertUserSql = "INSERT INTO USER (EMAIL, NAME, PASSWORD, NIF, ISADMIN) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertUserSql)) {
            preparedStatement.setString(1, newUser.getEmail());
            preparedStatement.setString(2, newUser.getName());
            preparedStatement.setString(3, newUser.getPassword());
            preparedStatement.setInt(4, newUser.getNif());
            preparedStatement.setBoolean(5, newUser.isAdmin());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean changeEmail(User loggedUser, String newEmail) {
        String query = "SELECT * FROM USER WHERE EMAIL = ?";
        String updateEmailSql = "UPDATE USER SET EMAIL = ? WHERE EMAIL = ?";
        return updateField(loggedUser.getEmail(), newEmail, query, updateEmailSql);
    }

    public boolean changeName(User loggedUser, String newName) {
        String updateNameSql = "UPDATE USER SET NAME = ? WHERE EMAIL = ?";
        return updateField(loggedUser.getEmail(), newName, null, updateNameSql);
    }

    public boolean changePassword(User loggedUser, String newPassword) {
        String updatePasswordSql = "UPDATE USER SET PASSWORD = ? WHERE EMAIL = ?";
        return updateField(loggedUser.getEmail(), newPassword, null, updatePasswordSql);
    }

    public boolean changeNIF(User loggedUser, int newNIF) {
        String updateNIFSql = "UPDATE USER SET NIF = ? WHERE EMAIL = ?";
        return updateField(loggedUser.getEmail(), String.valueOf(newNIF), null, updateNIFSql);
    }

    private boolean updateField(String userEmail, String newValue, String selectSql, String updateSql) {
        try {
            if (selectSql != null) {
                try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
                    selectStatement.setString(1, newValue);
                    ResultSet resultSet = selectStatement.executeQuery();
                    if (resultSet.next()) return false;
                }
            }

            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                updateStatement.setString(1, newValue);
                updateStatement.setString(2, userEmail);
                updateStatement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateCode() {
        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        final int CODE_LENGTH = 5;

        SecureRandom random = new SecureRandom();
        StringBuilder codeBuilder = new StringBuilder();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            codeBuilder.append(randomChar);
        }

        return codeBuilder.toString();
    }

    public boolean updateCode(int eventID, int minutes) {
        String selectQuery = "SELECT * FROM EVENT WHERE ID = ?";
        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
            selectStatement.setInt(1, eventID);
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    String updateQuery = "UPDATE EVENT SET CODE = ?, CODEEXPIRATIONTIME = ? WHERE ID = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                        updateStatement.setString(1, generateCode());
                        updateStatement.setString(2, String.valueOf(LocalDateTime.now().plusMinutes(minutes)));

                        int rowsUpdated = updateStatement.executeUpdate();

                        if (rowsUpdated > 0) return true;
                        else return false;
                    }
                } else
                    return false;
                }
        } catch (SQLException e) {
            return false;
        }
    }

    public String checkEvent(String eventCode) {
        //TODO
        return "";
    }
}
