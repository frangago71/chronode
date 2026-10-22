package com.chronode.level;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LevelDefinitionRepository extends JpaRepository<LevelDefinition, UUID> {
    List<LevelDefinition> findByWorkspaceIdOrderByDepthOrderAsc(UUID workspaceId);

    Optional<LevelDefinition> findByWorkspaceIdAndDepthOrder(UUID workspaceId, int depthOrder);
    
    boolean existsByWorkspaceIdAndDepthOrder(UUID workspaceId, int depthOrder);
}