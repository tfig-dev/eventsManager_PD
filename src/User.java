public class User {
    private String name;
    private int identificationNumber;
    private String email;
    private String password;
    private boolean isAdmin;
    public User(String name, int identificationNumber, String email, String password, boolean isAdmin) {
        this.name = name;
        this.identificationNumber = identificationNumber;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public User(String name, int identificationNumber, String email, String password) {
        this.name = name;
        this.identificationNumber = identificationNumber;
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

    public int getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(int identificationNumber) {
        this.identificationNumber = identificationNumber;
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
        return "Name: " + name + "\nIdentification Number: " + identificationNumber + "\nEmail: " + email;
    }

    public String toStringFile() {
        return name + "," + identificationNumber + "," + email + "," + password + "," + isAdmin;
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
