package model;

import java.util.Date;

public abstract class Person {
    protected String id;
    protected Date registeredDate;
    protected String username;
    protected String email;
    protected String passwordHash;
    protected String fullName;
    protected String phoneNumber;
    protected String address;

    public Person() {
        // Required for Gson
    }

    public Person(String id, String username, String email, String passwordHash,
            String fullName, Date registeredDate, String phoneNumber, String address) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.registeredDate = registeredDate;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public Date getRegisteredDate() {
        return registeredDate;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public abstract String getRole();
}