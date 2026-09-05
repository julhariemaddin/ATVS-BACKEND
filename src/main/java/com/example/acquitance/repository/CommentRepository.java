package com.example.acquitance.repository;
import com.example.acquitance.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByThemeIdOrderByCreatedAtAsc(Long themeId);
}
