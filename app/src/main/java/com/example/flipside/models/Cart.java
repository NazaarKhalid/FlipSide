package com.example.flipside.models;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private String cartId;
    private List<CartItem> cartItems;

    // default constructor
    public Cart() {
        this.cartItems = new ArrayList<>();
    }

    // parameterized constructor
    public Cart(String cartId) {
        this.cartId = cartId;
        this.cartItems = new ArrayList<>();
    }

    // getter setters
    public String getCartId() { return cartId; }
    public void setCartId(String cartId) { this.cartId = cartId; }

    public List<CartItem> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItem> cartItems) { this.cartItems = cartItems; }

    //helper methods
    public void addItem(CartItem newItem) {
        this.cartItems.add(newItem);
    }

    public void removeItem(CartItem item) {
        this.cartItems.remove(item);
    }
    public double getTotalAmount() {
        double total = 0.0;
        for (CartItem item : cartItems) {
            total += item.getItemTotalPrice();
        }
        return total;
    }
}