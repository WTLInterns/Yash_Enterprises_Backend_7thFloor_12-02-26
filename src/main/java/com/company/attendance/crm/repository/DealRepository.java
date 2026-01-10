package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealRepository extends JpaRepository<Deal, UUID> {
    List<Deal> findByBankId(UUID bankId);

    List<Deal> findByClientId(UUID clientId);

    Optional<Deal> findFirstByClientIdOrderByCreatedAtDesc(UUID clientId);

    // Compatibility lookup for legacy schemas storing UUID in BINARY(36) (first 16 bytes UUID + zero padding)
    @Query(value = "select * from deals where substring(id,1,16) = uuid_to_bin(:uuid)", nativeQuery = true)
    Optional<Deal> findByIdCompat(@Param("uuid") String uuid);

    @Modifying
    @Query(value = "update deals set stage = :stage, modified_at = :modifiedAt, modified_by = :modifiedBy where substring(id,1,16) = uuid_to_bin(:dealId)", nativeQuery = true)
    int updateStageCompat(@Param("dealId") String dealId,
                          @Param("stage") String stage,
                          @Param("modifiedAt") OffsetDateTime modifiedAt,
                          @Param("modifiedBy") UUID modifiedBy);

    default Optional<Deal> findByIdSafe(UUID id) {
        Optional<Deal> direct = findById(id);
        if (direct.isPresent()) return direct;
        return findByIdCompat(id.toString());
    }
}
