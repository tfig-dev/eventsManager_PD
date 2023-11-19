import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                    "(EMAIL TEXT PRIMARY KEY," +
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
                    " USER_EMAIL STRING," +
                    " PRIMARY KEY (EVENT_ID, USER_EMAIL)," +
                    " FOREIGN KEY (EVENT_ID) REFERENCES EVENT(ID)," +
                    " FOREIGN KEY (USER_EMAIL) REFERENCES USER(EMAIL))";

            stmt.executeUpdate(eventParticipants);

            String insertDefaultEvents = "INSERT OR IGNORE INTO EVENT (ID, NAME, LOCAL, DATE, BEGINHOUR, ENDHOUR) VALUES " +
                    "(1, 'Ze dos leitoes', 'ISEC', '2023-11-19', '15:30', '19:30'), " +
                    "(2, 'Maria das vacas', 'ESEC', '2023-11-20', '16:30', '20:30')";
            stmt.executeUpdate(insertDefaultEvents);

            String insertDefaultUsers = "INSERT OR IGNORE INTO USER (EMAIL, NAME, PASSWORD, NIF, ISADMIN) VALUES " +
                    "('admin', 'admin', 'admin', 123456789, 1), " +
                    "('user', 'user', 'user', 987654321, 0)";
            stmt.executeUpdate(insertDefaultUsers);

            String insertDefaultParticipations = "INSERT OR IGNORE INTO EVENT_PARTICIPANT (EVENT_ID, USER_EMAIL) VALUES " +
                    "(1, 'user')";
            stmt.executeUpdate(insertDefaultParticipations);

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

    public List<Event> getAttendanceRecords(String eventName, String day, String startDate, String endDate, boolean admin) {
        List<Event> attendanceRecords = new ArrayList<>();
        if(!admin) {
            try {
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("SELECT * FROM EVENT_PARTICIPANT EP ");
                queryBuilder.append("INNER JOIN EVENT E ON EP.EVENT_ID = E.ID ");
                queryBuilder.append("INNER JOIN USER U ON EP.USER_EMAIL = U.EMAIL ");
                queryBuilder.append("WHERE 1=1 ");

                if (eventName != null && !eventName.isEmpty()) queryBuilder.append("AND E.NAME = ? ");
                if (day != null && !day.isEmpty()) queryBuilder.append("AND E.DATE = ? ");
                if (startDate != null && !startDate.isEmpty()) queryBuilder.append("AND E.DATE >= ? ");
                if (endDate != null && !endDate.isEmpty()) queryBuilder.append("AND E.DATE <= ? ");

                try (PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())) {
                    int parameterIndex = 1;

                    if (eventName != null && !eventName.isEmpty()) preparedStatement.setString(parameterIndex++, eventName);
                    if (day != null && !day.isEmpty()) preparedStatement.setString(parameterIndex++, day);
                    if (startDate != null && !startDate.isEmpty()) preparedStatement.setString(parameterIndex++, startDate);
                    if (endDate != null && !endDate.isEmpty()) preparedStatement.setString(parameterIndex, endDate);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            Event attendanceRecord = new Event(
                                    resultSet.getInt("ID"),
                                    resultSet.getString("NAME"),
                                    resultSet.getString("LOCAL"),
                                    resultSet.getString("DATE"),
                                    resultSet.getString("BEGINHOUR"),
                                    resultSet.getString("ENDHOUR")
                            );
                            attendanceRecords.add(attendanceRecord);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("SELECT * FROM EVENT");

                if (eventName != null && !eventName.isEmpty()) queryBuilder.append(" WHERE NAME = ?");
                if (day != null && !day.isEmpty()) queryBuilder.append(" WHERE DATE = ?");
                if (startDate != null && !startDate.isEmpty()) queryBuilder.append(" WHERE DATE >= ?");
                if (endDate != null && !endDate.isEmpty()) queryBuilder.append(" AND DATE <= ?");

                try (PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())) {
                    int parameterIndex = 1;

                    if (eventName != null && !eventName.isEmpty()) preparedStatement.setString(parameterIndex++, eventName);
                    if (day != null && !day.isEmpty()) preparedStatement.setString(parameterIndex++, day);
                    if (startDate != null && !startDate.isEmpty())preparedStatement.setString(parameterIndex++, startDate);
                    if (endDate != null && !endDate.isEmpty()) preparedStatement.setString(parameterIndex, endDate);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            Event attendanceRecord = new Event(
                                    resultSet.getInt("ID"),
                                    resultSet.getString("NAME"),
                                    resultSet.getString("LOCAL"),
                                    resultSet.getString("DATE"),
                                    resultSet.getString("BEGINHOUR"),
                                    resultSet.getString("ENDHOUR")
                            );
                            attendanceRecords.add(attendanceRecord);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return attendanceRecords;
    }

    public boolean saveAttendanceRecords(List<Event> events, User loggedUser) {
        if(events.isEmpty()) return false;

        String file = "src/datafiles/output_" + loggedUser.getName() + ".csv";
        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.println("ID,NAME,LOCAL,DATE,BEGINHOUR,ENDHOUR");
            for (Event event : events) {
                printWriter.println(eventToCSV(event));
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String eventToCSV(Event event) {
        return String.format("%d,%s,%s,%s,%s,%s",
                event.getId(),
                event.getName(),
                event.getLocation(),
                event.getDate(),
                event.getStartTime(),
                event.getEndTime());
    }

    public boolean checkIfEventCanBeEdited(int eventID) {
        String query = "SELECT COUNT(*) FROM EVENT_PARTICIPANT WHERE EVENT_ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, eventID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int participationCount = resultSet.getInt(1);
                    return participationCount != 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean editEvent(int eventID, String name, String local, String date, String startHour, String endHour) {
        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("UPDATE EVENT SET");

            if (name != null && !name.isEmpty()) queryBuilder.append(" NAME = ?");
            if (local != null && !local.isEmpty()) queryBuilder.append(" LOCAL = ?");
            if (date != null && !date.isEmpty()) queryBuilder.append(" DATE = ?");
            if (startHour != null && !startHour.isEmpty()) queryBuilder.append(" BEGINHOUR = ?");
            if (endHour != null && !endHour.isEmpty()) queryBuilder.append(" ENDHOUR = ?");

            queryBuilder.append(" WHERE ID = ?");

            try (PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())) {
                int parameterIndex = 1;

                if (name != null && !name.isEmpty()) preparedStatement.setString(parameterIndex++, name);
                if (local != null && !local.isEmpty()) preparedStatement.setString(parameterIndex++, local);
                if (date != null && !date.isEmpty()) preparedStatement.setString(parameterIndex++, date);
                if (startHour != null && !startHour.isEmpty()) preparedStatement.setString(parameterIndex++, startHour);
                if (endHour != null && !endHour.isEmpty()) preparedStatement.setString(parameterIndex++, endHour);
                preparedStatement.setInt(parameterIndex, eventID);

                int rowsAffected = preparedStatement.executeUpdate();
                return rowsAffected > 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteEvent(int eventID) {
        String query = "DELETE FROM EVENT WHERE ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, eventID);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getRecords(int eventID) {
        StringBuilder records = new StringBuilder();
        String query = "SELECT * FROM EVENT_PARTICIPANT WHERE EVENT_ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, eventID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String email = resultSet.getString("USER_EMAIL");
                    records.append(email).append("\n");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records.toString();
    }
}
