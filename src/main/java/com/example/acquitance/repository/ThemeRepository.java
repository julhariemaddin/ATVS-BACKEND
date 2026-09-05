package com.example.acquitance.repository;
import com.example.acquitance.model.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {
    long countByGroupId(Long groupId);
}
