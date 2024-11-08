package com.ecom.shopping_cart.repository;

import com.ecom.shopping_cart.module.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    public Cart findCartByProductIdAndUserId(Integer productId, Integer userId);
}
