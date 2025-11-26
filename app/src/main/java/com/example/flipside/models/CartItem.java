package com.example.flipside.models;

public class CartItem {
    private Product product;
    private int quantity;

    // default constructor
    public CartItem() {
    }

    // paramterized constructor
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    //getter setters
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    //helper
    public double getItemTotalPrice() {
        if (product != null) {
            return product.getPrice() * quantity;
        }
        return 0.0;
    }
}