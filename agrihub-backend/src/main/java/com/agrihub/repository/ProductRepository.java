package com.agrihub.repository;

import com.agrihub.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String name, String brand, String category);

    List<Product> findByCategoryContainingIgnoreCase(String category);

    List<Product> findByStockLessThan(int stock);
}