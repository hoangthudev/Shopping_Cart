package com.ecom.shopping_cart.service;

import com.ecom.shopping_cart.module.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    public List<Product> getAllProducts();

    public Product saveProduct(Product product);

    public Boolean deleteProduct(Integer id);

    public Product getProductById(Integer id);

    public Product updateProduct(Product product, MultipartFile image);

    public List<Product> getAllActiveProducts(String category);
}
