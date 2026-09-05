package com.example.acquitance.service;

import com.example.acquitance.model.AcquaintanceGroup;
import com.example.acquitance.model.Theme;
import com.example.acquitance.repository.AcquaintanceGroupRepository;
import com.example.acquitance.repository.ThemeRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final AcquaintanceGroupRepository groupRepository;

    public ThemeService(ThemeRepository themeRepository, AcquaintanceGroupRepository groupRepository) {
        this.themeRepository = themeRepository;
        this.groupRepository = groupRepository;
    }

    public Theme createTheme(Long groupId, Theme theme) {
        AcquaintanceGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (theme.getTitle() == null || theme.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Title is required.");
        }

        if (theme.getTitle().length() > 35) {
            throw new RuntimeException("Title is too long (Max 35 characters).");
        }

        if (theme.getDescription() == null || theme.getDescription().length() > 100) {
            throw new RuntimeException("Description is too long (Max 100 characters).");
        }

        if (themeRepository.countByGroupId(groupId) >= 8) {
            throw new RuntimeException("Maximum of 8 themes reached for this group.");
        }

        if (theme.getImages() != null && theme.getImages().size() > 3) {
            throw new RuntimeException("Maximum of 3 images allowed per theme.");
        }

        theme.setGroup(group);
        return themeRepository.save(theme);
    }

    public List<Theme> getThemesByGroup(Long groupId, boolean includeDisabled) {
        AcquaintanceGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        if (includeDisabled) return group.getThemes();
        return group.getThemes().stream().filter(Theme::isEnabled).toList();
    }

    public Theme updateTheme(Theme theme) {
        if (theme.getTitle() != null && theme.getTitle().length() > 35) {
            throw new RuntimeException("Title is too long (Max 35 characters).");
        }
        if (theme.getDescription() != null && theme.getDescription().length() > 100) {
            throw new RuntimeException("Description is too long (Max 100 characters).");
        }
        if (theme.getImages() != null && theme.getImages().size() > 3) {
            throw new RuntimeException("Maximum of 3 images allowed per theme.");
        }
        return themeRepository.save(theme);
    }

    public void deleteTheme(Long id) {
        themeRepository.deleteById(id);
    }

    public Theme getTheme(Long id) {
        return themeRepository.findById(id).orElseThrow(() -> new RuntimeException("Theme not found"));
    }
}
