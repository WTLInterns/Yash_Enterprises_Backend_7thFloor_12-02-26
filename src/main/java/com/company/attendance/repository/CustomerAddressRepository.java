package com.company.attendance.repository;

import com.company.attendance.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    
    List<CustomerAddress> findByClientIdOrderByAddressType(Long clientId);

    @Query("SELECT ca FROM CustomerAddress ca WHERE ca.clientId IN :clientIds")
    List<CustomerAddress> findAllByClientIdIn(@Param("clientIds") List<Long> clientIds);
    
    Optional<CustomerAddress> findByClientIdAndAddressType(Long clientId, CustomerAddress.AddressType addressType);
    
    void deleteByClientId(Long clientId);

    @Modifying
    @Query("DELETE FROM CustomerAddress a WHERE a.clientId IN :ids")
    void deleteAllByClientIdIn(@Param("ids") List<Long> ids);
    
    @Query("SELECT ca FROM CustomerAddress ca WHERE ca.clientId = :clientId AND ca.latitude IS NOT NULL AND ca.longitude IS NOT NULL")
    List<CustomerAddress> findWithCoordinatesByClientId(@Param("clientId") Long clientId);
    
    @Query("SELECT ca FROM CustomerAddress ca WHERE ca.clientId = :clientId AND ca.addressType = :addressType AND ca.latitude IS NOT NULL AND ca.longitude IS NOT NULL")
    Optional<CustomerAddress> findWithCoordinatesByClientIdAndAddressType(@Param("clientId") Long clientId, @Param("addressType") CustomerAddress.AddressType addressType);
    
    @Modifying
    @Query("""
        UPDATE CustomerAddress a
        SET a.isPrimary = false
        WHERE a.clientId = :clientId AND a.isPrimary = true
        """)
    void clearPrimaryAddress(@Param("clientId") Long clientId);
    
    @Query("SELECT ca FROM CustomerAddress ca WHERE ca.clientId = :clientId AND ca.isPrimary = true")
    Optional<CustomerAddress> findPrimaryByClientId(@Param("clientId") Long clientId);
}
