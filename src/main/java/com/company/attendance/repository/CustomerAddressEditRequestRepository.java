package com.company.attendance.repository;

import com.company.attendance.entity.CustomerAddressEditRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerAddressEditRequestRepository extends JpaRepository<CustomerAddressEditRequest, Long> {

    /**
     * Find requests by status
     */
    List<CustomerAddressEditRequest> findByStatus(CustomerAddressEditRequest.RequestStatus status);

    /**
     * Find pending request by address
     */
    @Query("SELECT req FROM CustomerAddressEditRequest req WHERE req.addressId = :addressId AND req.status = :status")
    Optional<CustomerAddressEditRequest> findByAddressIdAndStatus(
        @Param("addressId") Long addressId, 
        @Param("status") CustomerAddressEditRequest.RequestStatus status
    );

    /**
     * Find requests by employee
     */
    List<CustomerAddressEditRequest> findByRequestedByEmployeeId(Long employeeId);

    /**
     * Find requests by employee and status
     */
    List<CustomerAddressEditRequest> findByRequestedByEmployeeIdAndStatus(
        Long employeeId, 
        CustomerAddressEditRequest.RequestStatus status
    );

    /**
     * Count requests by status
     */
    @Query("SELECT COUNT(req) FROM CustomerAddressEditRequest req WHERE req.status = :status")
    long countByStatus(@Param("status") CustomerAddressEditRequest.RequestStatus status);

    /**
     * Find recent requests
     */
    @Query("SELECT req FROM CustomerAddressEditRequest req ORDER BY req.createdAt DESC")
    List<CustomerAddressEditRequest> findAllOrderByCreatedAtDesc();
}
