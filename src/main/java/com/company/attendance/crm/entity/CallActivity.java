package com.company.attendance.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "call_activities")
public class CallActivity implements Persistable<UUID> {
    @Id
    @Column(length = 36)
    private UUID id; // same as Activity.id

    @Transient
    private boolean isNew = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "id")
    @MapsId
    @JsonIgnore
    private Activity activity;

    private String toFrom; // lookup placeholder
    private LocalDateTime callStartTime;
    private Integer callDurationSeconds;

    @Column(name = "call_type")
    private String callType; // INBOUND/OUTBOUND

    @Column(name = "call_status")
    private String callStatus;

    private String recordingUrl;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }
    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }
    public String getToFrom() { return toFrom; }
    public void setToFrom(String toFrom) { this.toFrom = toFrom; }
    public LocalDateTime getCallStartTime() { return callStartTime; }
    public void setCallStartTime(LocalDateTime callStartTime) { this.callStartTime = callStartTime; }
    public Integer getCallDurationSeconds() { return callDurationSeconds; }
    public void setCallDurationSeconds(Integer callDurationSeconds) { this.callDurationSeconds = callDurationSeconds; }
    public String getCallType() { return callType; }
    public void setCallType(String callType) { this.callType = callType; }
    public String getCallStatus() { return callStatus; }
    public void setCallStatus(String callStatus) { this.callStatus = callStatus; }
    public String getRecordingUrl() { return recordingUrl; }
    public void setRecordingUrl(String recordingUrl) { this.recordingUrl = recordingUrl; }
}
