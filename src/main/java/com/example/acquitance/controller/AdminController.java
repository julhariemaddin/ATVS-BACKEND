package com.example.acquitance.controller;

import com.example.acquitance.model.AdminUser;
import com.example.acquitance.repository.AdminUserRepository;
import com.example.acquitance.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminService adminService, AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminService = adminService;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PatchMapping("/groups/{id}/toggle")
    public ResponseEntity<?> toggleGroup(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleGroup(id));
    }

    @PatchMapping("/themes/{id}/toggle")
    public ResponseEntity<?> toggleTheme(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleTheme(id));
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
        adminService.deleteGroup(id);
        return ResponseEntity.ok(Map.of("message", "Group deleted"));
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<?> deleteTheme(@PathVariable Long id) {
        adminService.deleteTheme(id);
        return ResponseEntity.ok(Map.of("message", "Theme deleted"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, HttpSession session) {
        String username = (String) session.getAttribute("STUDENT_ID");
        String newPassword = body.get("newPassword");

        System.out.println("Change password attempt for user: " + username);

        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Session missing user ID."));
        }

        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters."));
        }

        Optional<AdminUser> adminOpt = adminUserRepository.findByUsername(username);
        if (adminOpt.isEmpty()) {
            System.err.println("Admin user '" + username + "' not found in database!");
            return ResponseEntity.badRequest().body(Map.of("message", "User not found in database."));
        }

        AdminUser admin = adminOpt.get();
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminUserRepository.save(admin);

        return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
    }
}
