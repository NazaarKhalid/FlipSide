package com.example.flipside.models;

public class Category {
    private String categoryId;
    private String name;
    private String description;

    // default constructor
    public Category() {
    }

    // parameterized constructor
    public Category(String categoryId, String name, String description) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
    }

    // getter and setter methods
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}