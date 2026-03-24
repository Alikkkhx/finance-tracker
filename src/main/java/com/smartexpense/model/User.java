package com.smartexpense.model;

import com.smartexpense.model.base.BaseEntity;

/**
 * User entity — extends BaseEntity.
 */
public class User extends BaseEntity {

    private String username;
    private String password;
    private String email;
    private String fullName;
    private java.math.BigDecimal monthlyIncome;

    public User() { super(); }

    public User(String username, String password, String email, String fullName) {
        super();
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.monthlyIncome = java.math.BigDecimal.ZERO;
    }

    @Override
    public String getEntityType() {
        return "USER";
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public java.math.BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(java.math.BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }
}
