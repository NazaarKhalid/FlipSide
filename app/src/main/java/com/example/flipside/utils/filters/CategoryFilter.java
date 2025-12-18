package com.example.flipside.utils.filters;

import com.example.flipside.models.Product;
import java.util.ArrayList;
import java.util.List;

public class CategoryFilter implements ProductFilter {
    private String categoryCriteria;

    public CategoryFilter(String categoryCriteria) {
        this.categoryCriteria = categoryCriteria;
    }

    @Override
    public List<Product> meetCriteria(List<Product> products) {
        List<Product> filtered = new ArrayList<>();
        for (Product product : products) {
            if (product.getCategory() != null &&
                    product.getCategory().equalsIgnoreCase(categoryCriteria)) {
                filtered.add(product);
            }
        }
        return filtered;
    }
}