package com.example.acquitance.service;

import com.example.acquitance.model.AcquaintanceGroup;
import com.example.acquitance.repository.AcquaintanceGroupRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GroupService {

    private final AcquaintanceGroupRepository groupRepository;

    public GroupService(AcquaintanceGroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public List<AcquaintanceGroup> getAllGroups(boolean includeDisabled) {
        if (includeDisabled) return groupRepository.findAll();
        return groupRepository.findAll().stream().filter(AcquaintanceGroup::isEnabled).toList();
    }

    public AcquaintanceGroup saveGroup(AcquaintanceGroup group) {
        return groupRepository.save(group);
    }

    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }

    public AcquaintanceGroup getGroup(Long id) {
        return groupRepository.findById(id).orElseThrow(() -> new RuntimeException("Group not found"));
    }
}
