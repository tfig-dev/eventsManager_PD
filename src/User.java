public class User {
    private String name;
    private int nif;
    private String email;
    private String password;
    private boolean isAdmin;
    public User(String name, int identificationNumber, String email, String password, boolean isAdmin) {
        this.name = name;
        this.nif = identificationNumber;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public User(String name, int identificationNumber, String email, String password) {
        this.name = name;
        this.nif = identificationNumber;
        this.email = email;
        this.password = password;
        this.isAdmin = false;
    }

    // Getters and setters for the attributes

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNif() {
        return nif;
    }

    public void setNif(int nif) {
        this.nif = nif;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Name: " + name + "\nNIF: " + nif + "\nEmail: " + email;
    }

    public String toStringFile() {
        return name + "," + nif + "," + email + "," + password + "," + isAdmin;
    }

    public String getUsername() {
        return name;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
