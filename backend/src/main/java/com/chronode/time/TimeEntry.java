package com.chronode.time;

import com.chronode.entity.BaseEntity;
import com.chronode.level.LevelNode;
import com.chronode.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "time_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(length = 255)
    private String description;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @ManyToMany
    @JoinTable(
            name = "time_entry_levels",
            joinColumns = @JoinColumn(name = "time_entry_id"),
            inverseJoinColumns = @JoinColumn(name = "level_node_id")
    )
    @Builder.Default
    private Set<LevelNode> levels = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "time_entry_tags",
            joinColumns = @JoinColumn(name = "time_entry_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();
}