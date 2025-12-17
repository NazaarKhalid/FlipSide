package com.example.flipside.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String profileImageBase64;

    private String userType;

    private boolean isAdmin = false;

    private BuyerProfile buyerProfile;
    private SellerProfile sellerProfile;
    private List<Address> savedAddresses;

    // Empty constructor for Firebase
    public User() {
    }

    public User(String userId, String name, String email, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profileImageBase64 = "";
        this.savedAddresses = new ArrayList<>();
        this.userType = "Buyer";
    }

    // --- GETTERS AND SETTERS ---

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public BuyerProfile getBuyerProfile() { return buyerProfile; }
    public void setBuyerProfile(BuyerProfile buyerProfile) { this.buyerProfile = buyerProfile; }

    public SellerProfile getSellerProfile() { return sellerProfile; }
    public void setSellerProfile(SellerProfile sellerProfile) { this.sellerProfile = sellerProfile; }

    public List<Address> getSavedAddresses() { return savedAddresses; }
    public void setSavedAddresses(List<Address> savedAddresses) { this.savedAddresses = savedAddresses; }

    public boolean isSeller() {
        return this.sellerProfile != null;
    }
}