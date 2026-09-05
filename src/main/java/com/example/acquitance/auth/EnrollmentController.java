package com.example.acquitance.auth;

import com.example.acquitance.dto.EnrollmentDto;
import com.example.acquitance.dto.UserProfileDto;
import com.example.acquitance.service.ExternalLoginService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/arms")
public class EnrollmentController {

    private final ExternalLoginService externalLoginService;

    public EnrollmentController(ExternalLoginService externalLoginService) {
        this.externalLoginService = externalLoginService;
    }

    @GetMapping("/enrollments")
    public ResponseEntity<?> getEnrollments(HttpSession session) {
        String externalCookies = (String) session.getAttribute("ARMS_COOKIES");
        if (externalCookies == null) {
            return ResponseEntity.status(401).body(Map.of("message", "No active ARMS session."));
        }
        List<EnrollmentDto> enrollments = externalLoginService.fetchEnrollments(externalCookies);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpSession session, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        String externalCookies = (String) session.getAttribute("ARMS_COOKIES");
        String studentId = (String) session.getAttribute("STUDENT_ID");
        String name = authentication.getName();

        // If local admin (no external cookies)
        if (externalCookies == null) {
            if (isAdmin) {
                // Return a simplified profile for the local admin
                return ResponseEntity.ok(new UserProfileDto(name, studentId, "System Administrator", "N/A", true));
            }
            return ResponseEntity.status(401).body(Map.of("message", "Session expired or missing."));
        }

        UserProfileDto profile = externalLoginService.fetchUserProfile(externalCookies, name, studentId);
        // Map the admin status to the DTO
        UserProfileDto finalProfile = new UserProfileDto(profile.getName(), profile.getStudentId(), profile.getProgram(), profile.getYear(), isAdmin);
        
        System.out.println(">>> PROFILE REQUEST: User=" + name + " | RoleAdmin=" + isAdmin);

        return ResponseEntity.ok(finalProfile);
    }
}
