package tu_store.demo.models;

public class User {
    private String username;
    private String email;
    private String password;
    private String role;

    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User() {}

    public void setUsername(String username) {
        this.username = username;
    }
    public String getUsername(){
        return username;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getEmail(){
        return email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword(){
        return password;
    }

    public void setRole(String role) {
        this.role = role;
    }
    public String getRole(){
        return role;
    }
}
