package pt.upskill.groceryroutepro.models;

public class ChangePasswordRequestModel {
    private String email;
    private String password;

    private String token;

    public ChangePasswordRequestModel() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
