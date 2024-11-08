package com.ecom.shopping_cart.service;

import com.ecom.shopping_cart.module.Cart;

import java.util.List;

public interface CartService {

    public Cart saveCart(Integer productId, Integer userId);

    public List<Cart> getCartByUser(Integer userId);
}
