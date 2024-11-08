package com.ecom.shopping_cart.repository;

import com.ecom.shopping_cart.module.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    public Cart findCartByProductIdAndUserId(Integer productId, Integer userId);

    public Integer countCartByUserId(Integer userId);

    public List<Cart> getCartByUserId(Integer userId);
}
