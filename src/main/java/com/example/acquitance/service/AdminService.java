package com.example.acquitance.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.acquitance.model.AcquaintanceGroup;
import com.example.acquitance.model.Theme;
import com.example.acquitance.repository.AcquaintanceGroupRepository;
import com.example.acquitance.repository.ThemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final AcquaintanceGroupRepository groupRepository;
    private final ThemeRepository themeRepository;
    private final Cloudinary cloudinary;

    public AdminService(AcquaintanceGroupRepository groupRepository, ThemeRepository themeRepository, Cloudinary cloudinary) {
        this.groupRepository = groupRepository;
        this.themeRepository = themeRepository;
        this.cloudinary = cloudinary;
    }

    @Transactional
    public AcquaintanceGroup toggleGroup(Long id) {
        AcquaintanceGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        group.setEnabled(!group.isEnabled());
        return groupRepository.save(group);
    }

    @Transactional
    public Theme toggleTheme(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theme not found"));
        theme.setEnabled(!theme.isEnabled());
        return themeRepository.save(theme);
    }

    @Transactional
    public void deleteGroup(Long id) {
        AcquaintanceGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Collect all images from all themes in this group
        List<String> allPublicIds = group.getThemes().stream()
                .flatMap(theme -> theme.getImages().stream())
                .collect(Collectors.toList());

        // Delete from Cloudinary
        deleteImagesFromCloudinary(allPublicIds);

        // Delete from DB (Themes will be deleted via CascadeType.ALL)
        groupRepository.delete(group);
    }

    @Transactional
    public void deleteTheme(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theme not found"));

        // Delete from Cloudinary
        deleteImagesFromCloudinary(theme.getImages());

        // Delete from DB
        themeRepository.delete(theme);
    }

    private void deleteImagesFromCloudinary(List<String> publicIds) {
        if (publicIds == null || publicIds.isEmpty()) return;

        for (String publicId : publicIds) {
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                System.out.println("Cloudinary: Deleted image " + publicId);
            } catch (Exception e) {
                System.err.println("Cloudinary: Failed to delete image " + publicId + " | Error: " + e.getMessage());
            }
        }
    }
}
