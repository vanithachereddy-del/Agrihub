package com.agrihub.controller;

import com.agrihub.model.DashboardStats;
import com.agrihub.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // GET /dashboard returns the stats JSON
    @GetMapping
    public DashboardStats getDashboardStats() {
        return dashboardService.getStats();
    }

    // Optional explicit root mapping (GET /dashboard/ )
    @GetMapping("/")
    public DashboardStats getDashboardStatsRoot() {
        return dashboardService.getStats();
    }
}
