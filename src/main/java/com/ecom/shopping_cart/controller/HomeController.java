package com.ecom.shopping_cart.controller;

import com.ecom.shopping_cart.module.Category;
import com.ecom.shopping_cart.module.Product;
import com.ecom.shopping_cart.module.UserDtls;
import com.ecom.shopping_cart.service.CartService;
import com.ecom.shopping_cart.service.CategoryService;
import com.ecom.shopping_cart.service.ProductService;
import com.ecom.shopping_cart.service.UserService;
import com.ecom.shopping_cart.util.CommonUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CartService cartService;

    @GetMapping("/")
    public String index(Model model) {
        List<Category> allActiveCategory = this.categoryService.getAllActiveCategories()
                .stream()
                .sorted((ca1, ca2) -> ca2.getId())
                .limit(6)
                .toList();

        List<Product> allActiveProduct = this.productService.getAllActiveProducts("")
                .stream()
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                .limit(6)
                .toList();
        model.addAttribute("category", allActiveCategory);
        model.addAttribute("products", allActiveProduct);
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
    public String products(Model model,
                           @RequestParam(value = "category", defaultValue = "") String category,
                           @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                           @RequestParam(name = "pageSize", defaultValue = "8") Integer pageSize,
                           @RequestParam(name = "ch", defaultValue = "") String ch) {

        List<Category> categories = this.categoryService.getAllActiveCategories();
        model.addAttribute("paramValue", category);
        model.addAttribute("categories", categories);

//        List<Product> products = this.productService.getAllActiveProducts(category);
//        model.addAttribute("products", products);

        Page<Product> page = null;
        if (StringUtils.isEmpty(ch)) {
            page = this.productService.getAllActionProductPagination(pageNo, pageSize, category);
        } else {
            page = this.productService.searchActiveProductPagination(pageNo, pageSize, category, ch);
        }

//        page = this.productService.getAllActionProductPagination(pageNo, pageSize, category);
        List<Product> products = page.getContent();
        model.addAttribute("products", products);
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("productSize", products.size());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "product";
    }

    @GetMapping("/product/{id}")
    public String product(@PathVariable int id, Model model) {
        Product productById = this.productService.getProductById(id);
        model.addAttribute("product", productById);
        return "view_product";
    }

    // Forgot Password
    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, HttpSession session,
                                        HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        UserDtls userByEmail = this.userService.getUserByEmail(email);

        if (ObjectUtils.isEmpty(userByEmail)) {
            session.setAttribute("errorMsg", "Invalid email");
        } else {

            String resetToken = UUID.randomUUID().toString();
            System.out.println("Reset token: " + resetToken);

            this.userService.updateUserResetToken(email, resetToken);

            // Generate URL: http://localhost:8080/reset-password?token=sjafdkansfbsaj

            String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;
            System.out.println("Url: " + url);

            boolean sendMail = this.commonUtil.sendMail(url, email);

            if (sendMail) {
                session.setAttribute("successMsg", "Please check your email ... Password Reset link sent!");
            } else {
                session.setAttribute("errorMsg", "Something wrong on server | Mail not send");
            }
        }
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam String token,
                                    Model model) {

        UserDtls userByToken = this.userService.getUserByToken(token);
        if (userByToken == null) {

            model.addAttribute("msg", "Your link is invalid or expired");
            return "message";
        }

        model.addAttribute("token", token);
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String password, HttpSession session,
                                Model model) {

        UserDtls userByToken = this.userService.getUserByToken(token);
        if (userByToken == null) {

            model.addAttribute("msg", "Your link is invalid or expired");
            return "message";
        } else {
            userByToken.setPassword(this.passwordEncoder.encode(password));
            userByToken.setResetToken(null);
            this.userService.updateUser(userByToken);
            model.addAttribute("msg", "Password change successfully");
            return "message";
        }
    }

    @GetMapping("/search")
    public String search(@RequestParam String ch,
                         Model model) {
        List<Product> searchProducts = this.productService.searchProduct(ch);
        model.addAttribute("products", searchProducts);

        List<Category> categories = this.categoryService.getAllActiveCategories();
        model.addAttribute("categories", categories);
        return "product";
    }

}
