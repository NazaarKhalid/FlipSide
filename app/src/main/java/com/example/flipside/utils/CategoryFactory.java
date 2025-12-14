package com.example.flipside.utils;

import com.example.flipside.models.Category;

public class CategoryFactory {

    // factory method
    public static Category createCategory(String type) {
        String id = "cat_" + System.currentTimeMillis();

        switch (type.toLowerCase()) {
            case "clothing":
                return new Category(id, "Clothing", "Apparel and fashion items");
            case "shoes":
                return new Category(id, "Shoes", "Footwear for all sizes");
            case "electronics":
                return new Category(id, "Electronics", "Gadgets and devices");
            default:
                return new Category(id, "General", "General items");
        }
    }
}