package tu_store.demo.dtos;


public class User {
    private String username;
    private String password;
    
    public String GetUser(){
        return username;
    }
    public void SetUser(String user){
        this.username = user;
    }
    public String GetPass(){
        return password;
    }
    public void SetPass(String pass){
        this.password = pass;
    }
        
}
