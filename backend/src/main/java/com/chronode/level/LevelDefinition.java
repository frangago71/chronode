package com.chronode.level;

import com.chronode.entity.BaseEntity;
import com.chronode.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "level_definitions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"workspace_id", "depth_order"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelDefinition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "depth_order", nullable = false)
    private int depthOrder;
}