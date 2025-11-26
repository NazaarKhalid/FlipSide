package com.example.flipside.models;

import java.util.ArrayList;
import java.util.List;

public class Store {
    private String storeId;
    private String ownerId;
    private String storeName;
    private String description;
    private String bannerImageBase64;
    private double rating;

    private List<String> followerIds;
    private List<Product> productList;

    //empty contructor
    public Store() {
    }

    //constructor
    public Store(String storeId, String ownerId, String storeName, String description, String bannerImageBase64) {
        this.storeId = storeId;
        this.ownerId = ownerId;
        this.storeName = storeName;
        this.description = description;
        this.bannerImageBase64 = bannerImageBase64;
        this.rating = 0.0; // Start with 0 rating
        this.followerIds = new ArrayList<>();
        this.productList = new ArrayList<>();
    }

    //getter and setter functions
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBannerImageBase64() { return bannerImageBase64; }
    public void setBannerImageBase64(String bannerImageBase64) { this.bannerImageBase64 = bannerImageBase64; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public List<String> getFollowerIds() { return followerIds; }
    public void setFollowerIds(List<String> followerIds) { this.followerIds = followerIds; }

    public List<Product> getProductList() { return productList; }
    public void setProductList(List<Product> productList) { this.productList = productList; }
}