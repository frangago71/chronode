package com.chronode.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chronode.level.LevelDefinitionRepository;
import com.chronode.level.LevelNodeRepository;
import com.chronode.time.TagRepository;
import com.chronode.time.TimeEntryRepository;
import com.chronode.user.UserRepository;
import com.chronode.workspace.WorkspaceMemberRepository;
import com.chronode.workspace.WorkspaceRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private LevelDefinitionRepository levelDefinitionRepository;

    @Mock
    private LevelNodeRepository levelNodeRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void deberiaSerCommandLineRunnerYEstarLimitadoAlPerfilDev() {
        assertThat(CommandLineRunner.class).isAssignableFrom(DataInitializer.class);
        assertThat(DataInitializer.class.getAnnotation(org.springframework.context.annotation.Profile.class).value())
            .containsExactly("dev");
    }

    @Test
    void deberiaCrearUsuariosConPasswordCodificada() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        given(passwordEncoder.encode("password123")).willReturn("encoded-password");
        when(workspaceRepository.findByCode(any())).thenReturn(Optional.empty());
        when(workspaceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(levelDefinitionRepository.findByWorkspaceIdAndDepthOrder(any(), anyInt())).thenReturn(Optional.empty());
        when(levelDefinitionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(levelNodeRepository.findByWorkspaceIdAndName(any(), any())).thenReturn(Optional.empty());
        when(levelNodeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tagRepository.findByWorkspaceIdAndName(any(), any())).thenReturn(Optional.empty());
        when(tagRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(workspaceMemberRepository.existsByUserIdAndWorkspaceId(any(), any())).thenReturn(false);
        when(timeEntryRepository.existsByWorkspaceIdAndDescription(any(), any())).thenReturn(false);
        when(timeEntryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final DataInitializer initializer = new DataInitializer(
            userRepository,
            workspaceRepository,
            workspaceMemberRepository,
            levelDefinitionRepository,
            levelNodeRepository,
            tagRepository,
            timeEntryRepository,
            passwordEncoder
        );

        initializer.run();

        final ArgumentCaptor<com.chronode.user.User> userCaptor = ArgumentCaptor.forClass(com.chronode.user.User.class);
        verify(userRepository, org.mockito.Mockito.times(2)).save(userCaptor.capture());
        assertThat(userCaptor.getAllValues())
            .extracting(com.chronode.user.User::getPasswordHash)
            .containsOnly("encoded-password");
    }
}
