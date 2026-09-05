package com.example.acquitance.config;

import com.example.acquitance.model.AdminUser;
import com.example.acquitance.repository.AdminUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (adminUserRepository.count() == 0) {
            AdminUser defaultAdmin = new AdminUser();
            defaultAdmin.setUsername("admin");
            defaultAdmin.setPassword(passwordEncoder.encode("ccs_governor_2026"));
            adminUserRepository.save(defaultAdmin);
            System.out.println("--- Default Admin Account Created: admin / ccs_governor_2026 ---");
        }
    }
}
