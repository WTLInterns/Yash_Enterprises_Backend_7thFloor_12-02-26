package com.company.attendance.service;

import com.company.attendance.entity.Role;
import com.company.attendance.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final RoleRepository roleRepository;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Transactional
    public Role save(Role role) {
        if (role.getId() == null) {
            role.setCreatedAt(LocalDateTime.now());
        }
        role.setUpdatedAt(LocalDateTime.now());
        return roleRepository.save(role);
    }

    @Transactional
    public Role update(Long id, Role updated) {
        Role existing = findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        updated.setId(id);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(LocalDateTime.now());
        return roleRepository.save(updated);
    }

    public void delete(Long id) {
        roleRepository.deleteById(id);
    }

    public List<Role> findByIsActive(Boolean isActive) {
        return roleRepository.findByIsActive(isActive);
    }

    public List<Role> findAllActiveRoles() {
        return roleRepository.findByIsActive(true);
    }

    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
}
