package com.company.attendance.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_activities")
public class EventActivity implements Persistable<UUID> {
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

    private String title;
    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;
    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;
    @Column(name = "repeat_flag")
    private Boolean repeat;
    @Column(name = "reminder_minutes")
    private Integer reminderMinutes;
    private String location;
    @Lob
    private String participants; // CSV or JSON, flexible minimal implementation

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
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }
    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }
    public Boolean getRepeat() { return repeat; }
    public void setRepeat(Boolean repeat) { this.repeat = repeat; }
    public Integer getReminderMinutes() { return reminderMinutes; }
    public void setReminderMinutes(Integer reminderMinutes) { this.reminderMinutes = reminderMinutes; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getParticipants() { return participants; }
    public void setParticipants(String participants) { this.participants = participants; }
}
