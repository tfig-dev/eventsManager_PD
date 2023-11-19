public class User {
    private final String name;
    private final int nif;
    private final String email;
    private final String password;
    private final boolean isAdmin;

    public User(String name, int nif, String email, String password, boolean isAdmin) {
        this.name = name;
        this.nif = nif;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public User(String name, int nif, String email, String password) {
        this.name = name;
        this.nif = nif;
        this.email = email;
        this.password = password;
        this.isAdmin = false;
    }

    public String getName() {
        return name;
    }

    public int getNif() {
        return nif;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Name: " + name + "\nNIF: " + nif + "\nEmail: " + email;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
