package com.ecom.shopping_cart.controller;

import com.ecom.shopping_cart.module.*;
import com.ecom.shopping_cart.service.CartService;
import com.ecom.shopping_cart.service.CategoryService;
import com.ecom.shopping_cart.service.OrderService;
import com.ecom.shopping_cart.service.UserService;
import com.ecom.shopping_cart.util.CommonUtil;
import com.ecom.shopping_cart.util.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
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

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonUtil commonUtil;

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


    @GetMapping("/orders")
    public String orderPage(Principal principal, Model model) {

        UserDtls userDtls = this.getLoggedInUserDetails(principal);

        List<Cart> carts = this.cartService.getCartByUser(userDtls.getId());
        model.addAttribute("carts", carts);
        if (carts.size() > 0) {
            Double orderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
            Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice() + 250 + 100;
            model.addAttribute("totalOrderPrice", totalOrderPrice);
            model.addAttribute("orderPrice", orderPrice);
        }
        return "user/order";
    }

    @PostMapping("/save-order")
    public String saveOrder(@ModelAttribute OrderRequest orderRequest,
                            Principal principal) throws MessagingException, UnsupportedEncodingException {
//        System.out.println(orderRequest.toString());
        UserDtls user = this.getLoggedInUserDetails(principal);
        this.orderService.saveOrder(user.getId(), orderRequest);

        return "user/success";
    }

    @GetMapping("/my-orders")
    public String myOrder(Model model, Principal principal) {
        UserDtls user = this.getLoggedInUserDetails(principal);
        List<ProductOrder> orders = this.orderService.getOrdersByUser(user.getId());
        model.addAttribute("orders", orders);
        return "user/my_orders";
    }

    @GetMapping("/update-status")
    public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

        OrderStatus[] values = OrderStatus.values();

        String status = null;

        for (OrderStatus orderStatus : values) {
            if (orderStatus.getId().equals(st)) {
                status = orderStatus.getName();
            }
        }

        ProductOrder updateOrder = this.orderService.updateOrderStatus(id, status);
        try {
            this.commonUtil.sendEmailForProductOrder(updateOrder, status);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!ObjectUtils.isEmpty(updateOrder)) {
            session.setAttribute("successMsg", "Order status updated");
        } else {
            session.setAttribute("successMsg", "Status not updated");
        }
        return "redirect:/user/my-orders";
    }

    //profile
    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        UserDtls user = this.userService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile image, HttpSession session) {
        UserDtls userProfileUpdate = this.userService.updateUserProfile(user, image);
        if (ObjectUtils.isEmpty(userProfileUpdate)) {
            session.setAttribute("error", "Profile update failed");
        } else {
            session.setAttribute("successMsg", "Profile updated");
        }
        return "redirect:/user/profile";
    }

}
