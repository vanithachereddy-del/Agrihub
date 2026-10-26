package com.agrihub.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Aggregated metrics for the admin dashboard. */
public class DashboardStats {

    private long totalProducts;
    private long totalOrders;
    private long totalFarmers;
    private long expertRequests;
    private long pendingOrders;
    private long cropsSupported;
    private double totalRevenue;
    private List<Product> lowStockProducts = new ArrayList<>();
    private List<Product> outOfStockProducts = new ArrayList<>();
    private Map<String, Long> monthlyOrders = new HashMap<>();
    private Map<String, Long> bestSellingProducts = new HashMap<>();
    private Map<String, Long> categoryWiseProducts = new HashMap<>();

    public DashboardStats() {
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getTotalFarmers() {
        return totalFarmers;
    }

    public void setTotalFarmers(long totalFarmers) {
        this.totalFarmers = totalFarmers;
    }

    public long getExpertRequests() {
        return expertRequests;
    }

    public void setExpertRequests(long expertRequests) {
        this.expertRequests = expertRequests;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public long getCropsSupported() {
        return cropsSupported;
    }

    public void setCropsSupported(long cropsSupported) {
        this.cropsSupported = cropsSupported;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public List<Product> getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(List<Product> lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public List<Product> getOutOfStockProducts() {
        return outOfStockProducts;
    }

    public void setOutOfStockProducts(List<Product> outOfStockProducts) {
        this.outOfStockProducts = outOfStockProducts;
    }

    public Map<String, Long> getMonthlyOrders() {
        return monthlyOrders;
    }

    public void setMonthlyOrders(Map<String, Long> monthlyOrders) {
        this.monthlyOrders = monthlyOrders;
    }

    public Map<String, Long> getBestSellingProducts() {
        return bestSellingProducts;
    }

    public void setBestSellingProducts(Map<String, Long> bestSellingProducts) {
        this.bestSellingProducts = bestSellingProducts;
    }

    public Map<String, Long> getCategoryWiseProducts() {
        return categoryWiseProducts;
    }

    public void setCategoryWiseProducts(Map<String, Long> categoryWiseProducts) {
        this.categoryWiseProducts = categoryWiseProducts;
    }
}
