package com.example.acquitance.repository;
import com.example.acquitance.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserIdAndGroupId(String userId, Long groupId);
    long countByThemeId(Long themeId);
}
