package com.agrihub.controller;

import com.agrihub.model.Product;
import com.agrihub.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getProducts(@RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return productService.getProductsByCategory(category);
        }
        if (search != null && !search.isBlank()) {
            return productService.searchProducts(search);
        }
        return productService.getProducts();
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String query) {
        return productService.searchProducts(query);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody @NonNull Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable @NonNull String id) {
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok((Object) product))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable @NonNull String id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable @NonNull String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}