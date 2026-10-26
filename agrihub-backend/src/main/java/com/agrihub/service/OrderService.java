package com.agrihub.service;

import com.agrihub.model.Order;
import com.agrihub.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public @NonNull Order createOrder(@NonNull Order order) {
        if (order.getTrackingNumber() == null || order.getTrackingNumber().isBlank()) {
            order.setTrackingNumber("AGR-" + System.currentTimeMillis());
        }
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(@NonNull String id) {
        return orderRepository.findById(id);
    }

    public Order updateOrder(@NonNull String id, Order updatedOrder) {
        return orderRepository.findById(id).map(order -> {
            if (updatedOrder.getStatus() != null) {
                order.setStatus(updatedOrder.getStatus());
            }
            if (updatedOrder.getTrackingNumber() != null) {
                order.setTrackingNumber(updatedOrder.getTrackingNumber());
            }
            return orderRepository.save(order);
        }).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order updateOrderStatus(@NonNull String id, String status) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(status);
            return orderRepository.save(order);
        }).orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
