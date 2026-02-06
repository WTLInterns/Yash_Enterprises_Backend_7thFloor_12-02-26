package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.ClientFieldValue;
import com.company.attendance.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientFieldValueRepository extends JpaRepository<ClientFieldValue, Long> {

    List<ClientFieldValue> findByClientId(Long clientId);

    @Query("SELECT cfv FROM ClientFieldValue cfv WHERE cfv.client.id = :clientId AND cfv.fieldDefinition.active = true ORDER BY cfv.fieldDefinition.orderIndex ASC")
    List<ClientFieldValue> findByClientIdWithActiveFields(@Param("clientId") Long clientId);

    Optional<ClientFieldValue> findByClientIdAndFieldDefinitionId(Long clientId, Long fieldDefinitionId);

    void deleteByClientId(Long clientId);

    boolean existsByClientIdAndFieldDefinitionId(Long clientId, Long fieldDefinitionId);
}
