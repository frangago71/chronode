package com.chronode.time;

import com.chronode.entity.BaseEntity;
import com.chronode.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "tags",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"workspace_id", "name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String color;
}