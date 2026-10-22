package com.chronode.workspace;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {
    Optional<WorkspaceMember> findByUserIdAndWorkspaceId(UUID userId, UUID workspaceId);

    List<WorkspaceMember> findByUserId(UUID userId);

    List<WorkspaceMember> findByWorkspaceId(UUID workspaceId);
    
    boolean existsByUserIdAndWorkspaceId(UUID userId, UUID workspaceId);
}