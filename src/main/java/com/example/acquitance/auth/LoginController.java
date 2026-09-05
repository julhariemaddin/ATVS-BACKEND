package com.example.acquitance.auth;

import com.example.acquitance.dto.AuthResultDto;
import com.example.acquitance.model.AdminUser;
import com.example.acquitance.repository.AdminUserRepository;
import com.example.acquitance.service.ExternalLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final ExternalLoginService externalLoginService;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    public LoginController(ExternalLoginService externalLoginService, AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.externalLoginService = externalLoginService;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest, HttpServletRequest request, HttpServletResponse response) {
        // 0. Nuclear Clear: kill any existing thread-local context or session
        SecurityContextHolder.clearContext();
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        String campus = loginRequest.get("campus");
        String type = loginRequest.get("type");

        // 1. Try Local Admin Auth (DB backed)
        Optional<AdminUser> localAdmin = adminUserRepository.findByUsername(username);
        if (localAdmin.isPresent() && passwordEncoder.matches(password, localAdmin.get().getPassword())) {
            return establishSession(username, "Administrator", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), null, request, response);
        }

        // 2. Try ARMS Student Auth
        AuthResultDto result = externalLoginService.authenticate(username, password, campus, type);
        if (result != null) {
            String displayName = result.getFullName() != null ? result.getFullName() : username;
            
            // Student logins are strictly ROLE_USER. NO AUTO-ADMIN LOGIC HERE.
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

            return establishSession(username, displayName, authorities, result.getCookies(), request, response);
        }

        return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
    }

    private ResponseEntity<?> establishSession(String id, String displayName, List<SimpleGrantedAuthority> authorities, String externalCookies, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = new UsernamePasswordAuthenticationToken(displayName, null, authorities);
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(auth);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        HttpSession session = request.getSession(true);
        session.setAttribute("STUDENT_ID", id);
        System.out.println("Session established for ID: " + id + " | Session ID: " + session.getId());

        if (externalCookies != null) {
            session.setAttribute("ARMS_COOKIES", externalCookies);
        }

        return ResponseEntity.ok(Map.of(
            "message", "Login successful", 
            "name", displayName,
            "isAdmin", authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
        ));
    }
}
