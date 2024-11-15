package com.ecom.shopping_cart.service.impl;

import com.ecom.shopping_cart.module.Product;
import com.ecom.shopping_cart.repository.ProductRepository;
import com.ecom.shopping_cart.service.ProductService;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> getAllProducts() {
        return this.productRepository.findAll();
    }

    @Override
    public Product saveProduct(Product product) {
        return this.productRepository.save(product);
    }

    @Override
    public Boolean deleteProduct(Integer id) {
        Product product = this.productRepository.findById(id).orElse(null);
        if (!ObjectUtils.isEmpty(product)) {
            this.productRepository.delete(product);
            return true;
        }
        return false;
    }

    @Override
    public Product getProductById(Integer id) {
        Product product = this.productRepository.findById(id).orElse(null);
        return product;
    }

    @Override
    public Product updateProduct(Product product, MultipartFile image) {

        Product dbProduct = this.getProductById(product.getId());

        String imageName = image.isEmpty() ? dbProduct.getImage() : image.getOriginalFilename();

        dbProduct.setTitle(product.getTitle());
        dbProduct.setCategory(product.getCategory());
        dbProduct.setDescription(product.getDescription());
        dbProduct.setPrice(product.getPrice());
        dbProduct.setStock(product.getStock());
        dbProduct.setImage(imageName);
        dbProduct.setIsActive(product.getIsActive());

        dbProduct.setDiscount(product.getDiscount());

        // 5=100*(5/100); 100-5=95
        Double discount = dbProduct.getPrice() * (product.getDiscount() / 100.0);
        Double discountPrice = product.getPrice() - discount;
        dbProduct.setDiscountPrice(discountPrice);

        Product updateProduct = this.productRepository.save(dbProduct);

        if (!ObjectUtils.isEmpty(product)) {
            if (!image.isEmpty()) {

                try {
                    File saveFile = new ClassPathResource("static/img").getFile();

                    Path path = Paths.get(saveFile.getAbsolutePath()
                            + File.separator + "product_img"
                            + File.separator + image.getOriginalFilename());

                    System.out.println(path);
                    Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return product;
            }
        }

        return null;
    }

    @Override
    public List<Product> getAllActiveProducts(String category) {
        List<Product> products = null;
        if (ObjectUtils.isEmpty(category)) {
            products = this.productRepository.findByIsActiveTrue();
        } else {
            products = this.productRepository.findByCategory(category);
        }


        return products;
    }

    @Override
    public List<Product> searchProduct(String ch) {
        return this.productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
    }

    @Override
    public Page<Product> getAllActionProductPagination(Integer pageNo, Integer pageSize, String category) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<Product> pageProduct = null;

        if (ObjectUtils.isEmpty(category)) {
            pageProduct = this.productRepository.findByIsActiveTrue(pageable);
        } else {
            pageProduct = this.productRepository.findByCategory(pageable, category);
        }

        this.productRepository.findByIsActiveTrue(pageable);
        return pageProduct;
    }

    @Override
    public Page<Product> searchProductPagination(String ch, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return this.productRepository.findByIsActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
    }

    @Override
    public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return this.productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch) {
        Page<Product> pageProduct = null;
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        pageProduct = this.productRepository.findByIsActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
//        if (StringUtils.isEmpty(ch)) {
//            pageProduct = this.productRepository.findByIsActiveTrue(pageable);
//        } else {
//            pageProduct = this.productRepository.findByCategory(pageable, category);
//        }
        return pageProduct;
    }
}
