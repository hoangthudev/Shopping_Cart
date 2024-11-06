package com.ecom.shopping_cart.controller;

import com.ecom.shopping_cart.module.Category;
import com.ecom.shopping_cart.module.Product;
import com.ecom.shopping_cart.module.UserDtls;
import com.ecom.shopping_cart.service.CategoryService;
import com.ecom.shopping_cart.service.ProductService;
import com.ecom.shopping_cart.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/save-user")
    public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file,
                           HttpSession session) throws IOException {

        String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
        user.setProfileImage(imageName);
        UserDtls saveUser = this.userService.saveUser(user);

        if (!ObjectUtils.isEmpty(saveUser)) {
            if (!file.isEmpty()) {
                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath()
                        + File.separator + "profile_img"
                        + File.separator + file.getOriginalFilename());

//            System.out.println(path);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                session.setAttribute("successMsg", "Register successfully");
            } else {
                session.setAttribute("errorMsg", "Register failed");
            }
        }
        return "redirect:/register";
    }

    @GetMapping("/products")
    public String products(Model model, @RequestParam(value = "category", defaultValue = "") String category) {
//        System.out.println("category = " + category);
        List<Category> categories = this.categoryService.getAllActiveCategories();
        model.addAttribute("categories", categories);
        List<Product> products = this.productService.getAllActiveProducts(category);
        model.addAttribute("products", products);
        model.addAttribute("paramValue", category);
        return "product";
    }

    @GetMapping("/product/{id}")
    public String product(@PathVariable int id, Model model) {
        Product productById = this.productService.getProductById(id);
        model.addAttribute("product", productById);
        return "view_product";
    }
}
