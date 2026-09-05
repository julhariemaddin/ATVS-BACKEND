package com.example.acquitance.service;

import com.example.acquitance.model.Comment;
import com.example.acquitance.model.Theme;
import com.example.acquitance.model.Vote;
import com.example.acquitance.repository.CommentRepository;
import com.example.acquitance.repository.ThemeRepository;
import com.example.acquitance.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SocialService {

    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;
    private final ThemeRepository themeRepository;

    public SocialService(VoteRepository voteRepository, CommentRepository commentRepository, ThemeRepository themeRepository) {
        this.voteRepository = voteRepository;
        this.commentRepository = commentRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public void vote(String userId, String userName, Long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RuntimeException("Theme not found"));
        
        Long groupId = theme.getGroup().getId();

        Optional<Vote> existingVote = voteRepository.findByUserIdAndGroupId(userId, groupId);
        if (existingVote.isPresent()) {
            throw new RuntimeException("You have already voted in this group.");
        }

        Vote vote = new Vote(userId, userName, groupId, theme);
        voteRepository.save(vote);
    }

    @Transactional
    public Comment addComment(String userId, String userName, Long themeId, String content) {
        if (content == null || content.length() > 500) {
            throw new RuntimeException("Comment is too long (Max 500 characters).");
        }

        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RuntimeException("Theme not found"));

        // User can only comment once per theme
        List<Comment> existing = commentRepository.findByThemeIdOrderByCreatedAtAsc(themeId);
        boolean alreadyCommented = existing.stream().anyMatch(c -> c.getUserId().equals(userId));
        if (alreadyCommented) {
            throw new RuntimeException("You can only comment once on this theme.");
        }

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setUserName(userName);
        comment.setTheme(theme);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    @Transactional
    public void unvote(String userId, Long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RuntimeException("Theme not found"));
        
        Long groupId = theme.getGroup().getId();

        Optional<Vote> existingVote = voteRepository.findByUserIdAndGroupId(userId, groupId);
        existingVote.ifPresent(voteRepository::delete);
    }

    public boolean hasUserVoted(String userId, Long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RuntimeException("Theme not found"));
        
        Long groupId = theme.getGroup().getId();
        Optional<Vote> vote = voteRepository.findByUserIdAndGroupId(userId, groupId);
        
        return vote.isPresent() && vote.get().getTheme().getId().equals(themeId);
    }

    public List<String> getVoterNames(Long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RuntimeException("Theme not found"));
        return theme.getVotes().stream().map(Vote::getUserName).toList();
    }

    @Transactional
    public Comment updateComment(String userId, Long commentId, String content) {
        if (content == null || content.length() > 500) {
            throw new RuntimeException("Comment is too long (Max 500 characters).");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to edit this comment.");
        }

        if (Duration.between(comment.getCreatedAt(), LocalDateTime.now()).toMinutes() >= 15) {
            throw new RuntimeException("Edit window (15 minutes) has expired.");
        }

        comment.setContent(content);
        comment.setUpdatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(String userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this comment.");
        }

        if (Duration.between(comment.getCreatedAt(), LocalDateTime.now()).toMinutes() >= 15) {
            throw new RuntimeException("Delete window (15 minutes) has expired.");
        }

        commentRepository.delete(comment);
    }

    public List<Comment> getComments(Long themeId) {
        return commentRepository.findByThemeIdOrderByCreatedAtAsc(themeId);
    }

    public long getVoteCount(Long themeId) {
        return voteRepository.countByThemeId(themeId);
    }
}
