package tu_store.demo.models;

public class User {
    private String username;
    private String password;
    private String passwordConfirm;
    private String role;

    public User(){
    }
    public User(String username,String password){
        this.username = username;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.role = role;
    }

    public void setUsername(String username) { this.username = username; }
    public String getUsername(){return this.username;}

    public void setPassword(String password) { this.password = password; }
    public String getPassword(){return this.password;}

    public void setPasswordConfirm(String passwordConfirm) { this.passwordConfirm = passwordConfirm; }
    public String getPasswordConfirm(){return this.passwordConfirm;}

    public void setRole(String role) { this.role = role; }
    public String getRoleassword(){return this.role;}
}
