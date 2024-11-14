package com.ecom.shopping_cart.repository;

import com.ecom.shopping_cart.module.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<ProductOrder, Integer> {
    public List<ProductOrder> getByUserId(Integer userId);

    ProductOrder findByOrderId(String orderId);
}
