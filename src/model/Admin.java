package model;

import java.util.Date;

public class Admin extends Person {

    public Admin() {
        super();
    }

    public Admin(String adminId, String username, String email, String passwordHash,
            String fullName, Date registeredDate, String phoneNumber, String address) {
        super(adminId, username, email, passwordHash, fullName, registeredDate, phoneNumber, address);
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    public String getAdminId() {
        return getId();
    }
}