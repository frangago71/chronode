package com.chronode.time;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {
    List<TimeEntry> findByWorkspaceIdOrderByStartTimeDesc(UUID workspaceId);

    List<TimeEntry> findByWorkspaceIdAndStartTimeBetween(UUID workspaceId, Instant start, Instant end);

    Optional<TimeEntry> findByWorkspaceIdAndEndTimeIsNull(UUID workspaceId);
    
    boolean existsByWorkspaceIdAndEndTimeIsNull(UUID workspaceId);
}