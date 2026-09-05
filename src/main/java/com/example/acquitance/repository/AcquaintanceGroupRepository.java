package com.example.acquitance.repository;
import com.example.acquitance.model.AcquaintanceGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface AcquaintanceGroupRepository extends JpaRepository<AcquaintanceGroup, Long> { }
