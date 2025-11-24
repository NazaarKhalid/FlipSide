package com.example.flipside.models;

public class SellerProfile {

    private String sellerProfileId;
    private String userId;
    private Store store;


    public SellerProfile() {
    }


    public SellerProfile(String sellerProfileId, String userId, Store store) {
        this.sellerProfileId = sellerProfileId;
        this.userId = userId;
        this.store = store;
    }


    public String getSellerProfileId() { return sellerProfileId; }
    public void setSellerProfileId(String sellerProfileId) { this.sellerProfileId = sellerProfileId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }
//add products()
    //getdashboardmetrics()

}