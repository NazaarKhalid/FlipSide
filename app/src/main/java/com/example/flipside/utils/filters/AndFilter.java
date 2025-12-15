package com.example.flipside.utils.filters;

import com.example.flipside.models.Product;
import java.util.List;

public class AndFilter implements ProductFilter {
    private ProductFilter criteria;
    private ProductFilter otherCriteria;

    public AndFilter(ProductFilter criteria, ProductFilter otherCriteria) {
        this.criteria = criteria;
        this.otherCriteria = otherCriteria;
    }

    @Override
    public List<Product> meetCriteria(List<Product> products) {
        List<Product> firstCriteriaItems = criteria.meetCriteria(products);
        return otherCriteria.meetCriteria(firstCriteriaItems);
    }
}