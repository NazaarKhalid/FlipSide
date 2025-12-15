package com.example.flipside.utils.filters;

import com.example.flipside.models.Product;
import java.util.ArrayList;
import java.util.List;

public class NameFilter implements ProductFilter {
    private String nameCriteria;

    public NameFilter(String nameCriteria) {
        this.nameCriteria = nameCriteria.toLowerCase();
    }

    @Override
    public List<Product> meetCriteria(List<Product> products) {
        List<Product> filtered = new ArrayList<>();
        for (Product product : products) {
            if (product.getName().toLowerCase().contains(nameCriteria)) {
                filtered.add(product);
            }
        }
        return filtered;
    }
}