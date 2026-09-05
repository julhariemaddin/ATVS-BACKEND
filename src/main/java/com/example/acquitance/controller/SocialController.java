package com.example.acquitance.controller;

import com.example.acquitance.model.Comment;
import com.example.acquitance.service.SocialService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    private final SocialService socialService;

    public SocialController(SocialService socialService) {
        this.socialService = socialService;
    }

    private String getStudentId(HttpSession session) {
        return (String) session.getAttribute("STUDENT_ID");
    }

    @PostMapping("/vote/{themeId}")
    public ResponseEntity<?> vote(@PathVariable Long themeId, HttpSession session, Authentication auth) {
        String userId = getStudentId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Login required."));
        
        try {
            socialService.vote(userId, auth.getName(), themeId);
            return ResponseEntity.ok(Map.of("message", "Voted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/comment/{themeId}")
    public ResponseEntity<?> comment(@PathVariable Long themeId, @RequestBody Map<String, String> body, HttpSession session, Authentication auth) {
        String userId = getStudentId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Login required."));
        
        Comment comment = socialService.addComment(userId, auth.getName(), themeId, body.get("content"));
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/comment/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId, @RequestBody Map<String, String> body, HttpSession session) {
        String userId = getStudentId(session);
        try {
            Comment comment = socialService.updateComment(userId, commentId, body.get("content"));
            return ResponseEntity.ok(comment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, HttpSession session) {
        String userId = getStudentId(session);
        try {
            socialService.deleteComment(userId, commentId);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/comments/{themeId}")
    public List<Comment> getComments(@PathVariable Long themeId) {
        return socialService.getComments(themeId);
    }

    @DeleteMapping("/vote/{themeId}")
    public ResponseEntity<?> unvote(@PathVariable Long themeId, HttpSession session) {
        String userId = getStudentId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Login required."));
        
        socialService.unvote(userId, themeId);
        return ResponseEntity.ok(Map.of("message", "Unvoted"));
    }

    @GetMapping("/votes/{themeId}")
    public Map<String, Object> getVoteInfo(@PathVariable Long themeId, HttpSession session) {
        String userId = getStudentId(session);
        boolean hasUserVoted = userId != null && socialService.hasUserVoted(userId, themeId);
        
        return Map.of(
            "count", socialService.getVoteCount(themeId),
            "voters", socialService.getVoterNames(themeId),
            "hasUserVoted", hasUserVoted
        );
    }
}
