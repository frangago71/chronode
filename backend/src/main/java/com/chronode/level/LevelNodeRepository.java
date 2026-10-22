package com.chronode.level;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LevelNodeRepository extends JpaRepository<LevelNode, UUID> {
    List<LevelNode> findByWorkspaceId(UUID workspaceId);

    List<LevelNode> findByLevelDefinitionId(UUID levelDefinitionId);

    List<LevelNode> findByParentId(UUID parentId);
    
    List<LevelNode> findByWorkspaceIdAndParentIsNull(UUID workspaceId);
}