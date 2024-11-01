package com.ecom.shopping_cart.repository;

import com.ecom.shopping_cart.module.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
