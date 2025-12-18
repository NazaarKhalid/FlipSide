package com.example.flipside.models;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private String productId;
    private String sellerId;
    private String storeId;
    private String name;
    private String description;
    private int stockQuantity;
    private double price;
    private double deliveryCharges;
    private boolean isAvailable;
    private String imageBase64;
    private String category;
    private List<String> tags;

    public Product() {
    }

    public Product(String productId, String sellerId, String storeId, String name, String description,
                   int stockQuantity, double price, double deliveryCharges, String category) {
        this.productId = productId;
        this.sellerId = sellerId;
        this.storeId = storeId;
        this.name = name;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.price = price;
        this.deliveryCharges = deliveryCharges;
        this.category = category;
        this.isAvailable = true;
        this.imageBase64 = "";
        this.tags = new ArrayList<>();
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getDeliveryCharges() { return deliveryCharges; }
    public void setDeliveryCharges(double deliveryCharges) { this.deliveryCharges = deliveryCharges; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public void toggleAvailability() {
        this.isAvailable = !this.isAvailable;
    }

    public void addTag(String tag) {
        if (this.tags == null) this.tags = new ArrayList<>();
        this.tags.add(tag);
    }
}