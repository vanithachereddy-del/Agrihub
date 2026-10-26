package com.agrihub.controller;

import com.agrihub.model.CropGuide;
import com.agrihub.model.Product;
import com.agrihub.repository.CropGuideRepository;
import com.agrihub.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CropGuideRepository cropGuideRepository;

    @GetMapping
    public Map<String, Object> smartSearch(@RequestParam String q) {
        List<Product> products = productService.searchProducts(q);
        List<CropGuide> crops = cropGuideRepository.findAll().stream()
                .filter(c -> matches(c.getName(), q) || matches(c.getCategory(), q))
                .toList();

        List<Map<String, String>> suggestions = new ArrayList<>();
        for (Product p : products) {
            Map<String, String> item = new HashMap<>();
            item.put("type", "product");
            item.put("id", p.getId());
            item.put("label", p.getName());
            item.put("sub", p.getCategory() + " · " + p.getBrand());
            suggestions.add(item);
        }
        for (CropGuide c : crops) {
            Map<String, String> item = new HashMap<>();
            item.put("type", "crop");
            item.put("id", c.getId());
            item.put("label", c.getName());
            item.put("sub", c.getBestSeason());
            suggestions.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("crops", crops);
        result.put("suggestions", suggestions);
        return result;
    }

    private boolean matches(String value, String query) {
        return value != null && value.toLowerCase().contains(query.toLowerCase());
    }
}
