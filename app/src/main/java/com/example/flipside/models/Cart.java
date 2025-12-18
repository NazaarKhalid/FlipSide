package com.example.flipside.models;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<CartItem> items;
    private double totalPrice;

    public Cart() {
        this.items = new ArrayList<>();
        this.totalPrice = 0.0;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
        calculateTotal();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void addItem(CartItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        calculateTotal();
    }

    public void removeItem(CartItem item) {
        if (this.items != null) {
            this.items.remove(item);
            calculateTotal();
        }
    }

    public void calculateTotal() {
        this.totalPrice = 0.0;
        if (items != null) {
            for (CartItem item : items) {
                if (item.getProduct() != null) {
                    this.totalPrice += item.getProduct().getPrice() * item.getQuantity();
                }
            }
        }
    }
}