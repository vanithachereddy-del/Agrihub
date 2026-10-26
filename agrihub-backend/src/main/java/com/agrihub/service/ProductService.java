package com.agrihub.service;

import com.agrihub.model.Product;
import com.agrihub.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    public @NonNull Product createProduct(@NonNull Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(@NonNull String id) {
        return productRepository.findById(id);
    }

    public Product updateProduct(@NonNull String id, Product updatedProduct) {
        return productRepository.findById(id).map(existing -> {
            existing.setName(updatedProduct.getName());
            existing.setBrand(updatedProduct.getBrand());
            existing.setCategory(updatedProduct.getCategory());
            existing.setPrice(updatedProduct.getPrice());
            existing.setStock(updatedProduct.getStock());
            existing.setUnit(updatedProduct.getUnit());
            existing.setQuantity(updatedProduct.getQuantity());
            existing.setDescription(updatedProduct.getDescription());
            existing.setImageUrl(updatedProduct.getImageUrl());
            return productRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public void deleteProduct(@NonNull String id) {
        productRepository.deleteById(id);
    }

    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return productRepository.findAll();
        }
        String term = keyword.trim();
        return productRepository.findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrCategoryContainingIgnoreCase(term, term, term);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryContainingIgnoreCase(category.trim());
    }
}