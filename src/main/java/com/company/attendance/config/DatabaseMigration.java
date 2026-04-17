package com.company.attendance.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigration {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrate() {
        try {
            // Check if parent_team_id column exists in teams table
            boolean columnExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_name = 'teams' AND column_name = 'parent_team_id'",
                Integer.class
            ) > 0;

            if (!columnExists) {
                log.info("Adding parent_team_id column to teams table...");
                jdbcTemplate.execute("ALTER TABLE teams ADD COLUMN parent_team_id BIGINT");
                jdbcTemplate.execute("ALTER TABLE teams ADD CONSTRAINT fk_team_parent_team FOREIGN KEY (parent_team_id) REFERENCES teams(id)");
                jdbcTemplate.execute("CREATE INDEX idx_team_parent_team_id ON teams(parent_team_id)");
                log.info("Successfully added parent_team_id column to teams table");
            }

            // Check if team_members table exists for many-to-many relationship
            boolean tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_name = 'team_members'",
                Integer.class
            ) > 0;

            if (!tableExists) {
                log.info("Creating team_members table for many-to-many relationship...");
                jdbcTemplate.execute("""
                    CREATE TABLE team_members (
                        team_id BIGINT NOT NULL,
                        employee_id BIGINT NOT NULL,
                        PRIMARY KEY (team_id, employee_id),
                        FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
                        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
                    )
                """);
                log.info("Successfully created team_members table");
            }

        } catch (Exception e) {
            log.error("Error during database migration", e);
        }

        // Task time tracking columns
        try {
            boolean timeTakenMinutesExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_name = 'tasks' AND column_name = 'time_taken_minutes'",
                Integer.class
            ) > 0;
            if (!timeTakenMinutesExists) {
                log.info("Adding time_taken_minutes column to tasks table...");
                jdbcTemplate.execute("ALTER TABLE tasks ADD COLUMN time_taken_minutes BIGINT");
                log.info("Added time_taken_minutes to tasks");
            }

            boolean timeTakenExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_name = 'tasks' AND column_name = 'time_taken'",
                Integer.class
            ) > 0;
            if (!timeTakenExists) {
                log.info("Adding time_taken column to tasks table...");
                jdbcTemplate.execute("ALTER TABLE tasks ADD COLUMN time_taken VARCHAR(20)");
                log.info("Added time_taken to tasks");
            }
        } catch (Exception e) {
            log.error("Error adding time tracking columns to tasks", e);
        }
    }
}
