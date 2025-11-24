package com.example.flipside.models;

import java.util.ArrayList;
import java.util.List;

public class BuyerProfile {
    private String buyerProfileId;
    private String userId;
    private Cart cart;
    private List<Order> orderHistory;
    private List<Store> followedStores;

    public BuyerProfile() {
    }

    public BuyerProfile(String buyerProfileId, String userId) {
        this.buyerProfileId = buyerProfileId;
        this.userId = userId;
        this.cart = new Cart();
        this.orderHistory = new ArrayList<>();
        this.followedStores = new ArrayList<>();
    }

    public String getBuyerProfileId() { return buyerProfileId; }
    public void setBuyerProfileId(String buyerProfileId) { this.buyerProfileId = buyerProfileId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public List<Order> getOrderHistory() { return orderHistory; }
    public void setOrderHistory(List<Order> orderHistory) { this.orderHistory = orderHistory; }

    public List<Store> getFollowedStores() { return followedStores; }
    public void setFollowedStores(List<Store> followedStores) { this.followedStores = followedStores; }
}