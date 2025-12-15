package com.example.flipside.utils.filters;

import com.example.flipside.models.Product;
import java.util.List;

public interface ProductFilter {
    List<Product> meetCriteria(List<Product> products);
}