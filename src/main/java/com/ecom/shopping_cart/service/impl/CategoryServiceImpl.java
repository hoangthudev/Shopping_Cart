package com.ecom.shopping_cart.service.impl;

import com.ecom.shopping_cart.module.Category;
import com.ecom.shopping_cart.repository.CategoryRepository;
import com.ecom.shopping_cart.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Category saveCategory(Category category) {
        return this.categoryRepository.save(category);
    }

    @Override
    public Boolean existsByName(String name) {
        return this.categoryRepository.existsByName(name);
    }

    @Override
    public List<Category> getCategories() {
        return this.categoryRepository.findAll();
    }

    @Override
    public Boolean deleteCategory(int id) {
        Category category = this.categoryRepository.findById(id).orElse(null);
        if (!ObjectUtils.isEmpty(category)) {
            this.categoryRepository.delete(category);
            return true;
        }

        return false;
    }

    @Override
    public Category getCategoryById(int id) {
        Category category = this.categoryRepository.findById(id).orElse(null);
        return category;
    }

    @Override
    public List<Category> getAllActiveCategories() {
        List<Category> categories = this.categoryRepository.findByIsActiveTrue();
        return categories;
    }

    @Override
    public Page<Category> getAllCategoriesPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return this.categoryRepository.findAll(pageable);
    }
}
