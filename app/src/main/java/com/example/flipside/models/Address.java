package com.example.flipside.models;

public class Address {

    private String addressId;
    private String userId;
    private String street;
    private String city;
    private String zipCode;
    private boolean isDefault;


    public Address() {
    }


    public Address(String addressId, String userId, String street, String city, String zipCode, boolean isDefault) {
        this.addressId = addressId;
        this.userId = userId;
        this.street = street;
        this.city = city;
        this.zipCode = zipCode;
        this.isDefault = isDefault;
    }


    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }


    public void setAsDefault() {
        this.isDefault = true;
    }
}