package com.ecom.shopping_cart.service;

import com.ecom.shopping_cart.module.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    public Category saveCategory(Category category);

    public Boolean existsByName(String name);

    public List<Category> getCategories();

    public Boolean deleteCategory(int id);

    public Category getCategoryById(int id);

    public List<Category> getAllActiveCategories();

    public Page<Category> getAllCategoriesPagination(Integer pageNo, Integer pageSize);
}
