import java.io.*;
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
        String filePath = "src/datafiles/events.txt";
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
        String filePath = "src/datafiles/users.txt";
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

    public User getUser(String email) {
        for (User user : this.users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }

        return null;
    }

    public boolean getUserState(String email) {
        for (User user : this.users) {
            if (user.getEmail().equals(email) && user.isLoggedIn()) {
                return true;
            }
        }
        return false;
    }

    public boolean authenticate(String email, String password) {
        for (User user : this.users) {
            if (user.authenticate(email, password)) return true;
        }
        return false;
    }

    public void registerUser(User newUser) {
        this.users.add(newUser);
        saveUsers();
    }

    private void saveUsers() {
        try (FileWriter writer = new FileWriter("src/datafiles/users.txt")) {
            for (User user : this.users) {
                writer.write(user.toStringFile() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
}
