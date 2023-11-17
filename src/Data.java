import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Data {
    private static Connection connection;

    public Data() {
        connect();
        createTables();
    }

    private static void connect(){
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

    private static void createTables() {
        Statement stmt;

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
                    " CODEEXPIRATIONTIME TEXT)";
            stmt.executeUpdate(events);

            String eventParticipants = "CREATE TABLE IF NOT EXISTS EVENT_PARTICIPANT " +
                    "(EVENT_ID INT," +
                    " USER_EMAIL INT," +
                    " PRIMARY KEY (EVENT_ID, USER_EMAIL)," +
                    " FOREIGN KEY (EVENT_ID) REFERENCES EVENT(ID)," +
                    " FOREIGN KEY (USER_EMAIL) REFERENCES USER(EMAIL))";

            stmt.executeUpdate(eventParticipants);

            String insertDefaultEvents = "INSERT OR IGNORE INTO EVENT (ID, NAME, LOCAL, DATE, BEGINHOUR, ENDHOUR) VALUES " +
                    "(1, 'Ze dos leitoes', 'ISEC', '18/11/2023', '15:30', '19:30'), " +
                    "(2, 'Maria das vacas', 'ESEC', '19/11/2023', '16:30', '20:30')";
            stmt.executeUpdate(insertDefaultEvents);

            String insertDefaultUsers = "INSERT OR IGNORE INTO USER (EMAIL, NAME, PASSWORD, NIF, ISADMIN) VALUES " +
                    "('admin', 'admin', 'admin', 123456789, 1), " +
                    "('user', 'user', 'user', 987654321, 0)";
            stmt.executeUpdate(insertDefaultUsers);

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

                        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(minutes);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                        String formattedExpirationTime = expirationTime.format(formatter);
                        updateStatement.setString(2, formattedExpirationTime);

                        updateStatement.setInt(3, eventID);

                        int rowsUpdated = updateStatement.executeUpdate();
                        return rowsUpdated > 0;
                    }
                } else
                    return false;
                }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String checkEvent(String eventCode, User loggedUser) {
        try {
            // Check if the event with the given code exists
            String selectEventQuery = "SELECT * FROM EVENT WHERE CODE = ?";
            try (PreparedStatement selectEventStatement = connection.prepareStatement(selectEventQuery)) {
                selectEventStatement.setString(1, eventCode);
                try (ResultSet eventResultSet = selectEventStatement.executeQuery()) {
                    if (eventResultSet.next()) {
                        int eventId = eventResultSet.getInt("ID");

                        String selectParticipantQuery = "SELECT * FROM EVENT_PARTICIPANT WHERE EVENT_ID = ? AND USER_EMAIL = ?";
                        try (PreparedStatement selectParticipantStatement = connection.prepareStatement(selectParticipantQuery)) {
                            selectParticipantStatement.setInt(1, eventId);
                            selectParticipantStatement.setString(2, loggedUser.getEmail());
                            try (ResultSet participantResultSet = selectParticipantStatement.executeQuery()) {
                                if (participantResultSet.next()) {
                                    return "used";
                                } else {
                                    String insertParticipantQuery = "INSERT INTO EVENT_PARTICIPANT (EVENT_ID, USER_EMAIL) VALUES (?, ?)";
                                    try (PreparedStatement insertParticipantStatement = connection.prepareStatement(insertParticipantQuery)) {
                                        insertParticipantStatement.setInt(1, eventId);
                                        insertParticipantStatement.setString(2, loggedUser.getEmail());
                                        insertParticipantStatement.executeUpdate();
                                        return "success";
                                    }
                                }
                            }
                        }
                    } else {
                        return "error";
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }
    }

    public boolean createEvent(String eventName, String local, String date, String startTime, String endTime) {
        String insertEventSql = "INSERT INTO EVENT (NAME, LOCAL, DATE, BEGINHOUR, ENDHOUR) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertEventSql)) {
            preparedStatement.setString(1, eventName);
            preparedStatement.setString(2, local);
            preparedStatement.setString(3, date);
            preparedStatement.setString(4, startTime);
            preparedStatement.setString(5, endTime);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
