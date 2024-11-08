package com.ecom.shopping_cart.controller;

import com.ecom.shopping_cart.module.Cart;
import com.ecom.shopping_cart.module.Category;
import com.ecom.shopping_cart.module.UserDtls;
import com.ecom.shopping_cart.service.CartService;
import com.ecom.shopping_cart.service.CategoryService;
import com.ecom.shopping_cart.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @GetMapping
    public String home() {
        return "user/home";
    }

    @ModelAttribute
    public void getUserDetail(Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            UserDtls userDtls = this.userService.getUserByEmail(email);
            model.addAttribute("user", userDtls);
            Integer countCart = this.cartService.getCountCart(userDtls.getId());
            model.addAttribute("countCart", countCart);
        }
        List<Category> allActiveCategory = this.categoryService.getAllActiveCategories();
        model.addAttribute("categorys", allActiveCategory);
    }

    @GetMapping("add-cart")
    public String addToCart(@RequestParam(name = "user-id") Integer userId,
                            @RequestParam(name = "product-id") Integer productId,
                            HttpSession session) {
        Cart saveCart = this.cartService.saveCart(productId, userId);
        if (ObjectUtils.isEmpty(saveCart)) {
            session.setAttribute("error", "Product add to cart failed");
        } else {
            session.setAttribute("successMsg", "Product added to cart");
        }
        return "redirect:/product/" + productId;
    }

    private UserDtls getLoggedInUserDetails(Principal principal) {
        String email = principal.getName();
        UserDtls userDtls = this.userService.getUserByEmail(email);
        return userDtls;
    }

    @GetMapping("/cart")
    public String loadCartPage(Principal principal, Model model) {

        UserDtls userDtls = this.getLoggedInUserDetails(principal);

        List<Cart> carts = this.cartService.getCartByUser(userDtls.getId());
        model.addAttribute("carts", carts);
        if (carts.size() > 0) {
            Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
            model.addAttribute("totalOrderPrice", totalOrderPrice);
        }
        return "user/cart";
    }

    @GetMapping("/cart-quantity-update")
    public String updateCartQuantity(@RequestParam String sy,
                                     @RequestParam Integer cartId) {
        this.cartService.updateQuantity(sy, cartId);
        return "redirect:/user/cart";
    }


}
