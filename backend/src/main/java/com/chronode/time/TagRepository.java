package com.chronode.time;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    List<Tag> findByWorkspaceId(UUID workspaceId);

    Optional<Tag> findByWorkspaceIdAndName(UUID workspaceId, String name);
    
    boolean existsByWorkspaceIdAndName(UUID workspaceId, String name);
}