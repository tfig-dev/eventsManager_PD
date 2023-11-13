import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

public class Data {
    private final Set<User> users;
    private final Set<Event> events;

    public Data() {
        this.users = new HashSet<>();
        this.events = new HashSet<>();
        loadUsers();
        loadEvents();
    }

    private void loadEvents() {
        String filePath = "datafiles/events.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length == 5) {
                    String name = parts[0];
                    String location = parts[1];
                    LocalDate date = LocalDate.parse(parts[2]);
                    LocalTime startTime = LocalTime.parse(parts[3]);
                    LocalTime endTime = LocalTime.parse(parts[4]);

                    Event event = new Event(name, location, date, startTime, endTime);
                    events.add(event);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
        } catch (IOException e) {
            System.out.println("Error reading file: " + filePath);
        }
    }

    private void loadUsers() {
        String filePath = "datafiles/users.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String name = parts[0];
                    String identificationNumber = parts[1];
                    String email = parts[2];
                    String password = parts[3];
                    boolean isAdmin = Boolean.parseBoolean(parts[4]);
                    User user = new User(name, Integer.parseInt(identificationNumber), email, password, isAdmin);
                    users.add(user);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
        } catch (IOException e) {
            System.out.println("Error reading file: " + filePath);
        }
    }

    public Set<User> getUsers() {
        return this.users;
    }

    public Set<Event> getEvents() {
        return this.events;
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public void addEvent(Event event) {
        this.events.add(event);
    }

    public void removeUser(User user) {
        this.users.remove(user);
    }

    public void removeEvent(Event event) {
        this.events.remove(event);
    }

    public User getUser(String username) {
        for (User user : this.users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }

        return null;
    }

    public boolean authenticate(String email, String password) {
        for (User user : this.users) {
            if (user.authenticate(email, password)) return true;
        }
        return false;
    }
}
