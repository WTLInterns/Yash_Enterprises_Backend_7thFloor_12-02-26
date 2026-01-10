package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Activity;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.enums.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    Page<Activity> findByDealAndType(Deal deal, ActivityType type, Pageable pageable);
    List<Activity> findByDealOrderByCreatedAtDesc(Deal deal);
    List<Activity> findByDealAndTypeOrderByCreatedAtDesc(Deal deal, ActivityType type);

    // Compatibility lookup for legacy schemas storing deal_id in BINARY(36) (first 16 bytes UUID + zero padding)
    @Query(value = "select * from activities where substring(deal_id,1,16) = uuid_to_bin(:dealId) and (:type is null or type = :type) order by created_at desc",
            nativeQuery = true)
    List<Activity> findByDealIdCompatAndTypeOrderByCreatedAtDesc(@Param("dealId") String dealId,
                                                                @Param("type") String type);

    @Query(value = "select * from activities where substring(id,1,16) = uuid_to_bin(:activityId)", nativeQuery = true)
    Activity findByIdCompat(@Param("activityId") String activityId);

    @Query(value = "select count(*) from activities where substring(id,1,16) = uuid_to_bin(:activityId) and substring(deal_id,1,16) = uuid_to_bin(:dealId)",
            nativeQuery = true)
    long countByIdCompatAndDealIdCompat(@Param("activityId") String activityId,
                                       @Param("dealId") String dealId);

    @Modifying
    @Transactional
    @Query(value = "delete from activities where substring(id,1,16) = uuid_to_bin(:activityId)", nativeQuery = true)
    int deleteByIdCompat(@Param("activityId") String activityId);

    @Modifying
    @Transactional
    @Query(value = "update activities set name=:name, description=:description, owner_id=:ownerId, due_date=:dueDate, start_date=:startDate, end_date=:endDate, priority=:priority, repeat_rule=:repeatRule, reminder=:reminder, type=:type, modified_at=now(), modified_by=:modifiedBy where substring(id,1,16) = uuid_to_bin(:activityId)",
            nativeQuery = true)
    int updateByIdCompat(@Param("activityId") String activityId,
                         @Param("name") String name,
                         @Param("description") String description,
                         @Param("ownerId") UUID ownerId,
                         @Param("dueDate") OffsetDateTime dueDate,
                         @Param("startDate") OffsetDateTime startDate,
                         @Param("endDate") OffsetDateTime endDate,
                         @Param("priority") String priority,
                         @Param("repeatRule") String repeatRule,
                         @Param("reminder") OffsetDateTime reminder,
                         @Param("type") String type,
                         @Param("modifiedBy") UUID modifiedBy);
}
