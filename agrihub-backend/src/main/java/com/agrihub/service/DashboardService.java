package com.agrihub.service;

import com.agrihub.model.DashboardStats;
import com.agrihub.model.Order;
import com.agrihub.model.Product;
import com.agrihub.repository.CropGuideRepository;
import com.agrihub.repository.ExpertRequestRepository;
import com.agrihub.repository.FarmerRepository;
import com.agrihub.repository.OrderRepository;
import com.agrihub.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final int LOW_STOCK_THRESHOLD = 20;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private ExpertRequestRepository expertRequestRepository;

    @Autowired
    private CropGuideRepository cropGuideRepository;

    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalProducts(productRepository.count());
        stats.setTotalOrders(orderRepository.count());
        stats.setTotalFarmers(farmerRepository.count());
        stats.setExpertRequests(expertRequestRepository.count());
        stats.setCropsSupported(cropGuideRepository.count());
        long pendingOrders = orderRepository.findAll().stream()
                .filter(o -> "Pending".equalsIgnoreCase(o.getStatus()))
                .count();
        stats.setPendingOrders(pendingOrders);

        List<Product> allProducts = productRepository.findAll();
        stats.setLowStockProducts(allProducts.stream()
                .filter(p -> p.getStock() > 0 && p.getStock() < LOW_STOCK_THRESHOLD)
                .collect(Collectors.toList()));
        stats.setOutOfStockProducts(allProducts.stream()
                .filter(p -> p.getStock() <= 0)
                .collect(Collectors.toList()));

        List<Order> orders = orderRepository.findAll();
        stats.setTotalRevenue(orders.stream()
                .filter(o -> !"Cancelled".equalsIgnoreCase(o.getStatus())
                        && !"Rejected".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum());

        Map<String, Long> monthly = new HashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
        for (Order order : orders) {
            if (order.getOrderDate() != null) {
                String key = order.getOrderDate().format(fmt);
                monthly.merge(key, 1L, Long::sum);
            }
        }
        stats.setMonthlyOrders(monthly);

        Map<String, Long> bestSelling = new HashMap<>();
        for (Order order : orders) {
            if (order.getItems() != null) {
                for (Order.OrderItem item : order.getItems()) {
                    bestSelling.merge(item.getProductName(), (long) item.getQuantity(), Long::sum);
                }
            }
        }
        stats.setBestSellingProducts(bestSelling);

        Map<String, Long> categoryWise = new HashMap<>();
        for (Product product : allProducts) {
            categoryWise.merge(product.getCategory(), 1L, Long::sum);
        }
        stats.setCategoryWiseProducts(categoryWise);

        return stats;
    }
}
