package com.ecom.shopping_cart.service.impl;

import com.ecom.shopping_cart.module.Cart;
import com.ecom.shopping_cart.module.Product;
import com.ecom.shopping_cart.module.UserDtls;
import com.ecom.shopping_cart.repository.CartRepository;
import com.ecom.shopping_cart.repository.ProductRepository;
import com.ecom.shopping_cart.repository.UserRepository;
import com.ecom.shopping_cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Cart saveCart(Integer productId, Integer userId) {

        UserDtls userDtls = this.userRepository.findById(userId).get();
        Product product = this.productRepository.findById(productId).get();

        Cart cartStatus = this.cartRepository.findCartByProductIdAndUserId(productId, userId);

        Cart cart = null;

        if (ObjectUtils.isEmpty(cartStatus)) {
            cart = new Cart();
            cart.setProduct(product);
            cart.setUser(userDtls);
            cart.setQuantity(1);
            cart.setTotalPrice(1 * product.getDiscountPrice());
        } else {
            cart = cartStatus;
            cart.setQuantity(cartStatus.getQuantity() + 1);
            cart.setTotalPrice(cart.getQuantity() * product.getDiscountPrice());
        }

        Cart saveCart = this.cartRepository.save(cart);

        return saveCart;
    }

    @Override
    public List<Cart> getCartByUser(Integer userId) {
        return List.of();
    }
}
