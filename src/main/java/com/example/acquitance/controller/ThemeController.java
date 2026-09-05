package com.example.acquitance.controller;

import com.example.acquitance.dto.ThemeResponseDto;
import com.example.acquitance.model.Theme;
import com.example.acquitance.service.ThemeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/group/{groupId}")
    public List<ThemeResponseDto> getThemesByGroup(@PathVariable Long groupId, Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        List<Theme> themes = themeService.getThemesByGroup(groupId, isAdmin);
        
        return themes.stream().map(t -> new ThemeResponseDto(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.isEnabled(),
                t.getCreatedAt(),
                t.getImages(),
                t.getVotes().size(),
                t.getComments().size()
        )).collect(Collectors.toList());
    }

    @PostMapping("/group/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTheme(@PathVariable Long groupId, @RequestBody Theme theme) {
        try {
            return ResponseEntity.ok(themeService.createTheme(groupId, theme));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTheme(@PathVariable Long id, @RequestBody Theme theme) {
        theme.setId(id);
        try {
            return ResponseEntity.ok(themeService.updateTheme(theme));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTheme(@PathVariable Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
