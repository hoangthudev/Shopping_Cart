package com.ecom.shopping_cart.repository;

import com.ecom.shopping_cart.module.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Boolean existsByName(String name);
}
