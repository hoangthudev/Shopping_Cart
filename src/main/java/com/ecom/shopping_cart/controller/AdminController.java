package com.ecom.shopping_cart.controller;

import com.ecom.shopping_cart.module.Category;
import com.ecom.shopping_cart.module.Product;
import com.ecom.shopping_cart.service.CategoryService;
import com.ecom.shopping_cart.service.ProductService;
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
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public String index() {
        return "admin/index";
    }

    // Products

    @GetMapping("/products")
    public String viewProduct(Model model) {
        List<Product> listProduct = this.productService.getAllProducts();
        model.addAttribute("products", listProduct);
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
    public String category(Model model) {
        model.addAttribute("categories", this.categoryService.getCategories());
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
}
