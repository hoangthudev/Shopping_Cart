package com.ecom.shopping_cart.repository;

import com.ecom.shopping_cart.module.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<ProductOrder, Integer> {
}
