package com.company.attendance.repository;

import com.company.attendance.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    
    // Basic CRUD operations are provided by JpaRepository
    
    // Find by organization code (unique)
    Optional<Organization> findByCode(String code);
    
    // Find all active/inactive organizations
    List<Organization> findByIsActive(Boolean isActive);
    
    // Check if organization with given code exists
    boolean existsByCode(String code);
    
    // Find organizations by name (case-insensitive)
    List<Organization> findByNameContainingIgnoreCase(String name);
    
    // Find organization by exact name
    Optional<Organization> findByName(String name);
    
    // Find organizations by industry
    List<Organization> findByIndustry(String industry);
    
    // Find organizations by country and active status
    List<Organization> findByCountryAndIsActive(String country, Boolean isActive);
    
    // Find organizations with pagination
    Page<Organization> findAll(Pageable pageable);
    
    // Search organizations by multiple criteria
    @Query("SELECT o FROM Organization o WHERE " +
           "(:name IS NULL OR LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:code IS NULL OR LOWER(o.code) = LOWER(:code)) AND " +
           "(:industry IS NULL OR LOWER(o.industry) = LOWER(:industry)) AND " +
           "(:isActive IS NULL OR o.isActive = :isActive)")
    Page<Organization> searchOrganizations(
            @Param("name") String name,
            @Param("code") String code,
            @Param("industry") String industry,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
    
    // Check if organization with given email exists (excluding the current organization)
    boolean existsByContactEmailAndIdNot(String email, Long id);
    
    // Check if organization with given phone exists (excluding the current organization)
    boolean existsByContactPhoneAndIdNot(String phone, Long id);
}
