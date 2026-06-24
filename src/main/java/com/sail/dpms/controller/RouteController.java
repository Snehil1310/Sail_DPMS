package com.sail.dpms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RouteController {

    @GetMapping("/")
    public String root() {
        return "redirect:/1a2b3c";
    }

    // Landing Page
    @GetMapping("/1a2b3c")
    public String index() {
        return "forward:/index.html";
    }

    // Login Page
    @GetMapping("/2b3c4d")
    public String signin() {
        return "forward:/signin.html";
    }

    // Admin Dashboard
    @GetMapping("/d4e5f6")
    public String admin() {
        return "forward:/admin.html";
    }

    // Distributor Dashboard
    @GetMapping("/7a8b9c")
    public String distributor() {
        return "forward:/distributor.html";
    }
}
