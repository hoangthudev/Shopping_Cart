package com.ecom.shopping_cart.controller;

import com.ecom.shopping_cart.module.Category;
import com.ecom.shopping_cart.module.UserDtls;
import com.ecom.shopping_cart.service.CategoryService;
import com.ecom.shopping_cart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

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
        }
        List<Category> allActiveCategory = this.categoryService.getAllActiveCategories();
        model.addAttribute("categorys", allActiveCategory);
    }
}
