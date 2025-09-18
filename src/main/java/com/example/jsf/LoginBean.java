package com.example.jsf;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("loginBean")  // make sure the name matches EL expression
@RequestScoped
public class LoginBean implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String username;
    private String password;
    private String message;

    public String login() {
        if ("admin".equals(username) && "admin".equals(password)) {
            message = "Login successful! Welcome " + username;
            return "index.xhtml?faces-redirect=true";
        } else {
            message = "Invalid username or password!";
            return null;
        }
    }

    // Getters & Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getMessage() { return message; }
}
