package com.ecom.shopping_cart.service;

import com.ecom.shopping_cart.module.Category;

import java.util.List;

public interface CategoryService {
    public Category saveCategory(Category category);

    public Boolean existsByName(String name);

    public List<Category> getCategories();

    public Boolean deleteCategory(int id);

    public Category getCategoryById(int id);
}
