package com.example.acquitance.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ThemeResponseDto {
    private Long id;
    private String title;
    private String description;
    private boolean enabled;
    private LocalDateTime createdAt;
    private List<String> images;
    private long voteCount;
    private long commentCount;

    public ThemeResponseDto(Long id, String title, String description, boolean enabled, LocalDateTime createdAt, List<String> images, long voteCount, long commentCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.images = images;
        this.voteCount = voteCount;
        this.commentCount = commentCount;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<String> getImages() { return images; }
    public long getVoteCount() { return voteCount; }
    public long getCommentCount() { return commentCount; }
}
