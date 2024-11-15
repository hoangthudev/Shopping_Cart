package com.ecom.shopping_cart.controller;

import com.ecom.shopping_cart.module.Category;
import com.ecom.shopping_cart.module.Product;
import com.ecom.shopping_cart.module.ProductOrder;
import com.ecom.shopping_cart.module.UserDtls;
import com.ecom.shopping_cart.service.*;
import com.ecom.shopping_cart.util.CommonUtil;
import com.ecom.shopping_cart.util.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.hibernate.query.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
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

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonUtil commonUtil;

    @GetMapping({"/", ""})
    public String index() {
        return "admin/index";
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

    // Products

    @GetMapping("/products")
    public String viewProduct(Model model,
                              @RequestParam(defaultValue = "") String ch,
                              @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                              @RequestParam(name = "pageSize", defaultValue = "5") Integer pageSize) {
//        List<Product> listProduct = null;
//        if (ch != null && ch.trim().length() > 0) {
//            listProduct = this.productService.searchProduct(ch);
//        } else {
//            listProduct = this.productService.getAllProducts();
//        }
//
//        model.addAttribute("products", listProduct);

        Page<Product> page = null;
        if (ch != null && ch.trim().length() > 0) {
            page = this.productService.searchProductPagination(ch, pageNo, pageSize);
        } else {
            page = this.productService.getAllProductsPagination(pageNo, pageSize);
        }

        model.addAttribute("products", page.getContent());
        model.addAttribute("pageNo", page.getNumber());
//        model.addAttribute("productSize", page.size());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
        return "admin/products";
    }

    @GetMapping("/loadAddProduct")
    public String loadAddProduct(Model model) {
        List<Category> categories = this.categoryService.getCategories();
        model.addAttribute("categories", categories);
        return "admin/add_product";
    }

    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam("file") MultipartFile image,
                              HttpSession session) throws IOException {
        String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();

        product.setImage(imageName);

        product.setDiscount(0);
        product.setDiscountPrice(product.getPrice());

        Product saveProduct = this.productService.saveProduct(product);

        if (!ObjectUtils.isEmpty(saveProduct)) {

            File saveFile = new ClassPathResource("static/img").getFile();

            Path path = Paths.get(saveFile.getAbsolutePath()
                    + File.separator + "product_img"
                    + File.separator + image.getOriginalFilename());

//            System.out.println(path);
            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);


            session.setAttribute("successMsg", "Product Saved Successfully");
        } else {
            session.setAttribute("errorMsg", "Something wrong on server");
        }
        return "redirect:/admin/loadAddProduct";
    }

    @PostMapping("/updateProduct")
    public String updateProduct(@ModelAttribute("product") Product product, HttpSession session,
                                @RequestParam("file") MultipartFile image,
                                Model model) {

        if (product.getDiscount() < 0 || product.getDiscount() > 100) {
            session.setAttribute("errorMsg", "invalid discount");
        } else {

            Product updateProduct = this.productService.updateProduct(product, image);

            if (!ObjectUtils.isEmpty(updateProduct)) {
                session.setAttribute("successMsg", "Product Updated Successfully");
            } else {
                session.setAttribute("errorMsg", "Something wrong on server");
            }
        }

        return "redirect:/admin/editProduct/" + product.getId();
    }

    @GetMapping("/editProduct/{id}")
    public String editProduct(@PathVariable int id, Model model) {
        Product productById = this.productService.getProductById(id);
        model.addAttribute("product", productById);
        List<Category> categories = this.categoryService.getCategories();
        model.addAttribute(("categories"), categories);
        return "admin/edit_product";
    }

    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable int id, HttpSession session) {
        Boolean deleteProduct = this.productService.deleteProduct(id);
        if (deleteProduct) {
            session.setAttribute("successMsg", "Product Deleted Successfully");
        } else {
            session.setAttribute("errorMsg", "Something wrong on server");
        }
        return "redirect:/admin/products";
    }

    // End Products

    //    Category
    @GetMapping("/category")
    public String category(Model model,
                           @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                           @RequestParam(name = "pageSize", defaultValue = "5") Integer pageSize) {
        Page<Category> page = this.categoryService.getAllCategoriesPagination(pageNo, pageSize);
        List<Category> categorys = page.getContent();
        model.addAttribute("categorys", categorys);
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("categorySize", categorys.size());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
        return "admin/category";
    }

    @PostMapping("/saveCategory")
    public String saveCategory(@ModelAttribute Category category,
                               @RequestParam("file") MultipartFile file,
                               HttpSession session) throws IOException {

        String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
        category.setImageName(imageName);

        Boolean exitCategory = this.categoryService.existsByName(category.getName());

        if (exitCategory) {
            session.setAttribute("errorMsg", "Category Name already exist");
        } else {
            Category saveCategory = this.categoryService.saveCategory(category);
            if (ObjectUtils.isEmpty(saveCategory)) {
                session.setAttribute("errorMsg", "Insert save 1 internal server error");
            } else {

                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath()
                        + File.separator + "category_img"
                        + File.separator + file.getOriginalFilename());

                System.out.println(path);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                session.setAttribute("successMsg", "Save successfully");
            }
        }

        return "redirect:/admin/category";
    }

    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable Integer id,
                                 HttpSession session) {
        Boolean deleteCategory = this.categoryService.deleteCategory(id);
        if (deleteCategory) {
            session.setAttribute("successMsg", "Delete successfully");
        } else {
            session.setAttribute("errorMsg", "Something wrong on server");
        }
        return "redirect:/admin/category";
    }

    @GetMapping("/loadEditCategory/{id}")
    public String loadEdiCategory(@PathVariable int id, Model model) {
        model.addAttribute("category", this.categoryService.getCategoryById(id));
        return "admin/edit_category";
    }

    @PostMapping("updateCategory")
    public String updateCategory(@ModelAttribute Category category,
                                 @RequestParam("file") MultipartFile file,
                                 HttpSession session) throws IOException {

        Category oldCategory = this.categoryService.getCategoryById(category.getId());
        String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();

        if (!ObjectUtils.isEmpty(oldCategory)) {
            oldCategory.setName(category.getName());
            oldCategory.setIsActive(category.getIsActive());
            oldCategory.setImageName(imageName);
        }

        Category categoryUpdate = this.categoryService.saveCategory(oldCategory);

        if (!ObjectUtils.isEmpty(categoryUpdate)) {

            if (!file.isEmpty()) {
                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath()
                        + File.separator + "category_img"
                        + File.separator + file.getOriginalFilename());

                System.out.println(path);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            }

            session.setAttribute("successMsg", "Update successfully");
        } else {
            session.setAttribute("errorMsg", "Something wrong on server");
        }
        return "redirect:/admin/loadEditCategory/" + category.getId();
    }

    // User

    @GetMapping("/users")
    public String getAllUser(Model model) {
        List<UserDtls> users = this.userService.getAllUsers("ROLE_USER");
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/update-status")
    public String updateUserAccountStatus(@RequestParam Boolean status,
                                          @RequestParam Integer id,
                                          HttpSession session) {
        Boolean resultUpdateAccount = this.userService.updateAccountStatus(id, status);
        if (resultUpdateAccount) {
            session.setAttribute("successMsg", "Account status successfully");
        } else {
            session.setAttribute("errorMsg", "Something wrong on server");
        }
        return "redirect:/admin/users";
    }

    // Orders
    @GetMapping("orders")
    public String getAllOrders(Model model,
                               @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                               @RequestParam(name = "pageSize", defaultValue = "3") Integer pageSize) {
//        List<ProductOrder> allOrder = this.orderService.getAllOrder();
//        model.addAttribute("orders", allOrder);
//        model.addAttribute("srch", false);

        Page<ProductOrder> page = this.orderService.getAllOrdersPagination(pageNo, pageSize);
        List<ProductOrder> orders = page.getContent();
        model.addAttribute("orders", orders);
        model.addAttribute("srch", false);
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("orderSize", orders.size());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
        return "admin/orders";
    }

    @PostMapping("/update-order-status")
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
        return "redirect:/admin/orders";
    }

    @GetMapping("/search-order")
    public String searchProductOrder(@RequestParam String orderId,
                                     Model model,
                                     HttpSession session,
                                     @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                                     @RequestParam(name = "pageSize", defaultValue = "3") Integer pageSize) {

        if (orderId != null && orderId.length() > 0) {

            ProductOrder order = this.orderService.getOrderById(orderId.trim());
            if (ObjectUtils.isEmpty(order)) {
                session.setAttribute("errorMsg", "Order not found");
                model.addAttribute("orderDtls", null);
            } else {
                model.addAttribute("orderDtls", order);
            }

            model.addAttribute("srch", true);
        } else {
//            List<ProductOrder> allOrder = this.orderService.getAllOrder();
//            model.addAttribute("orders", allOrder);
//            model.addAttribute("srch", false);

            Page<ProductOrder> page = this.orderService.getAllOrdersPagination(pageNo, pageSize);
            List<ProductOrder> orders = page.getContent();
            model.addAttribute("orders", orders);
            model.addAttribute("srch", false);

            model.addAttribute("pageNo", page.getNumber());
            model.addAttribute("orderSize", orders.size());
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("totalElements", page.getTotalElements());
            model.addAttribute("totalPages", page.getTotalPages());
            model.addAttribute("isFirst", page.isFirst());
            model.addAttribute("isLast", page.isLast());
        }
        return "/admin/orders";
    }

}
