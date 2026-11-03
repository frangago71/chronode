package com.chronode.config;

import com.chronode.level.LevelDefinition;
import com.chronode.level.LevelDefinitionRepository;
import com.chronode.level.LevelNode;
import com.chronode.level.LevelNodeRepository;
import com.chronode.time.Tag;
import com.chronode.time.TagRepository;
import com.chronode.time.TimeEntry;
import com.chronode.time.TimeEntryRepository;
import com.chronode.user.User;
import com.chronode.user.UserRepository;
import com.chronode.workspace.Workspace;
import com.chronode.workspace.WorkspaceMember;
import com.chronode.workspace.WorkspaceMemberRepository;
import com.chronode.workspace.WorkspaceRepository;
import com.chronode.workspace.WorkspaceRole;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "password123";
    private static final String ADMIN_EMAIL = "admin@chronode.com";
    private static final String USER_EMAIL = "user@chronode.com";

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final LevelDefinitionRepository levelDefinitionRepository;
    private final LevelNodeRepository levelNodeRepository;
    private final TagRepository tagRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(final String... args) {
        final User admin = findOrCreateUser(ADMIN_EMAIL, "Administrador Chronode");
        final User user = findOrCreateUser(USER_EMAIL, "Usuario Chronode");

        seedWorkspace(
            "Espacio personal",
            "PERSONAL",
            "Espacio personal para probar el seguimiento de tiempo.",
            admin,
            user
        );
        seedWorkspace(
            "Equipo Chronode",
            "TEAM",
            "Espacio compartido para probar equipos y proyectos.",
            admin,
            user
        );
    }

    private User findOrCreateUser(final String email, final String fullName) {
        return userRepository.findByEmail(email).orElseGet(() -> userRepository.save(
            User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .fullName(fullName)
                .enabled(true)
                .build()
        ));
    }

    private void seedWorkspace(
        final String name,
        final String code,
        final String description,
        final User admin,
        final User user
    ) {
        final Workspace workspace = workspaceRepository.findByCode(code).orElseGet(() -> workspaceRepository.save(
            Workspace.builder()
                .name(name)
                .code(code)
                .description(description)
                .build()
        ));

        createMembershipIfMissing(admin, workspace, WorkspaceRole.OWNER);
        createMembershipIfMissing(user, workspace, WorkspaceRole.MEMBER);

        final List<LevelDefinition> levels = seedLevelDefinitions(workspace);
        final LevelNode projectNode = findOrCreateNode(workspace, levels.get(0), "Chronode", null, "#4F46E5");
        final LevelNode backendNode = findOrCreateNode(workspace, levels.get(1), "Backend", projectNode, "#059669");
        final LevelNode frontendNode = findOrCreateNode(workspace, levels.get(1), "Frontend", projectNode, "#D97706");
        final LevelNode apiNode = findOrCreateNode(workspace, levels.get(2), "API", backendNode, "#0284C7");

        final Tag developmentTag = findOrCreateTag(workspace, "development", "#2563EB");
        final Tag planningTag = findOrCreateTag(workspace, "planning", "#7C3AED");

        createTimeEntryIfMissing(
            workspace,
            "Implementar endpoint de autenticación",
            Instant.now().minus(2, ChronoUnit.DAYS),
            7_200L,
            List.of(backendNode, apiNode),
            List.of(developmentTag)
        );
        createTimeEntryIfMissing(
            workspace,
            "Planificar trabajo del equipo",
            Instant.now().minus(1, ChronoUnit.DAYS),
            3_600L,
            List.of(projectNode, frontendNode),
            List.of(planningTag)
        );
    }

    private List<LevelDefinition> seedLevelDefinitions(final Workspace workspace) {
        return List.of(
            findOrCreateLevelDefinition(workspace, "Proyecto", 0),
            findOrCreateLevelDefinition(workspace, "Área", 1),
            findOrCreateLevelDefinition(workspace, "Tarea", 2)
        );
    }

    private LevelDefinition findOrCreateLevelDefinition(
        final Workspace workspace,
        final String name,
        final int depthOrder
    ) {
        return levelDefinitionRepository.findByWorkspaceIdAndDepthOrder(workspace.getId(), depthOrder)
            .orElseGet(() -> levelDefinitionRepository.save(
                LevelDefinition.builder()
                    .workspace(workspace)
                    .name(name)
                    .depthOrder(depthOrder)
                    .build()
            ));
    }

    private LevelNode findOrCreateNode(
        final Workspace workspace,
        final LevelDefinition levelDefinition,
        final String name,
        final LevelNode parent,
        final String color
    ) {
        return levelNodeRepository.findByWorkspaceIdAndName(workspace.getId(), name).orElseGet(
            () -> levelNodeRepository.save(
                LevelNode.builder()
                    .workspace(workspace)
                    .levelDefinition(levelDefinition)
                    .parent(parent)
                    .name(name)
                    .color(color)
                    .build()
            )
        );
    }

    private Tag findOrCreateTag(final Workspace workspace, final String name, final String color) {
        return tagRepository.findByWorkspaceIdAndName(workspace.getId(), name).orElseGet(
            () -> tagRepository.save(
                Tag.builder()
                    .workspace(workspace)
                    .name(name)
                    .color(color)
                    .build()
            )
        );
    }

    private void createMembershipIfMissing(
        final User user,
        final Workspace workspace,
        final WorkspaceRole role
    ) {
        if (!workspaceMemberRepository.existsByUserIdAndWorkspaceId(user.getId(), workspace.getId())) {
            workspaceMemberRepository.save(
                WorkspaceMember.builder()
                    .user(user)
                    .workspace(workspace)
                    .role(role)
                    .build()
            );
        }
    }

    private void createTimeEntryIfMissing(
        final Workspace workspace,
        final String description,
        final Instant startTime,
        final long durationSeconds,
        final List<LevelNode> levels,
        final List<Tag> tags
    ) {
        if (!timeEntryRepository.existsByWorkspaceIdAndDescription(workspace.getId(), description)) {
            timeEntryRepository.save(
                TimeEntry.builder()
                    .workspace(workspace)
                    .description(description)
                    .startTime(startTime)
                    .endTime(startTime.plusSeconds(durationSeconds))
                    .durationSeconds(durationSeconds)
                    .levels(new java.util.HashSet<>(levels))
                    .tags(new java.util.HashSet<>(tags))
                    .build()
            );
        }
    }
}
