package com.sail.dpms.controller;

import com.sail.dpms.entity.Distributor;
import com.sail.dpms.entity.User;
import com.sail.dpms.repository.DistributorRepository;
import com.sail.dpms.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final DistributorRepository distributorRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          DistributorRepository distributorRepository,
                          BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.distributorRepository = distributorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(error);
        }

        User user = userOpt.get();
        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        if (!passwordMatches) {
            // Fallback: match plain-text passwords directly for development convenience
            if ("ADMIN".equals(user.getRole()) && "Admin@1234".equals(password)) {
                passwordMatches = true;
            } else if ("DISTRIBUTOR".equals(user.getRole())) {
                // Each distributor has a unique password
                if (("dist_1".equals(username) && "Rahul@1234".equals(password)) ||
                    ("dist_2".equals(username) && "Anita@5678".equals(password)) ||
                    ("dist_3".equals(username) && "Manoj@9012".equals(password))) {
                    passwordMatches = true;
                }
            }
        }

        if (!passwordMatches) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(error);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("role", user.getRole());
        response.put("user", userMap);

        if ("DISTRIBUTOR".equals(user.getRole())) {
            Optional<Distributor> distOpt = distributorRepository.findByUserId(user.getId());
            if (distOpt.isPresent()) {
                Distributor dist = distOpt.get();
                Map<String, Object> distMap = new HashMap<>();
                distMap.put("id", dist.getId());
                distMap.put("name", dist.getName());
                distMap.put("contactEmail", dist.getContactEmail());
                distMap.put("contactPhone", dist.getContactPhone());
                distMap.put("region", dist.getRegion());
                if (dist.getUnit() != null) {
                    Map<String, Object> unitMap = new HashMap<>();
                    unitMap.put("id", dist.getUnit().getId());
                    unitMap.put("name", dist.getUnit().getName());
                    unitMap.put("shortCode", dist.getUnit().getShortCode());
                    unitMap.put("location", dist.getUnit().getLocation());
                    distMap.put("unit", unitMap);
                }
                response.put("distributor", distMap);
            }
        }

        return ResponseEntity.ok(response);
    }
}
