package com.example.acquitance.service;

import com.example.acquitance.model.AcquaintanceGroup;
import com.example.acquitance.model.Comment;
import com.example.acquitance.model.Theme;
import com.example.acquitance.repository.AcquaintanceGroupRepository;
import com.example.acquitance.repository.CommentRepository;
import com.example.acquitance.repository.ThemeRepository;
import com.example.acquitance.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AcquaintanceServiceTests {

    @Mock private ThemeRepository themeRepository;
    @Mock private AcquaintanceGroupRepository groupRepository;
    @Mock private VoteRepository voteRepository;
    @Mock private CommentRepository commentRepository;

    @InjectMocks private ThemeService themeService;
    @InjectMocks private SocialService socialService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTheme_LimitReached() {
        Long groupId = 1L;
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(new AcquaintanceGroup()));
        when(themeRepository.countByGroupId(groupId)).thenReturn(8L);

        Theme theme = new Theme();
        Exception exception = assertThrows(RuntimeException.class, () -> themeService.createTheme(groupId, theme));
        assertEquals("Maximum of 8 themes reached for this group.", exception.getMessage());
    }

    @Test
    void testCreateTheme_ImageLimitExceeded() {
        Long groupId = 1L;
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(new AcquaintanceGroup()));
        when(themeRepository.countByGroupId(groupId)).thenReturn(5L);

        Theme theme = new Theme();
        theme.setImages(List.of("img1", "img2", "img3", "img4"));

        Exception exception = assertThrows(RuntimeException.class, () -> themeService.createTheme(groupId, theme));
        assertEquals("Maximum of 3 images allowed per theme.", exception.getMessage());
    }

    @Test
    void testUpdateComment_After15Minutes() {
        String userId = "user1";
        Long commentId = 100L;
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setCreatedAt(LocalDateTime.now().minusMinutes(16));

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        Exception exception = assertThrows(RuntimeException.class, () -> socialService.updateComment(userId, commentId, "new content"));
        assertEquals("Edit window (15 minutes) has expired.", exception.getMessage());
    }

    @Test
    void testAddComment_DuplicateUser() {
        String userId = "user1";
        Long themeId = 1L;
        Theme theme = new Theme();
        
        Comment existingComment = new Comment();
        existingComment.setUserId(userId);

        when(themeRepository.findById(themeId)).thenReturn(Optional.of(theme));
        when(commentRepository.findByThemeIdOrderByCreatedAtAsc(themeId)).thenReturn(List.of(existingComment));

        Exception exception = assertThrows(RuntimeException.class, () -> socialService.addComment(userId, "Name", themeId, "Hello"));
        assertEquals("You can only comment once on this theme.", exception.getMessage());
    }
}
