package com.chronode.security;

import com.chronode.user.User;
import com.chronode.user.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        final User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        return UserPrincipal.fromUser(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(final UUID id) throws UsernameNotFoundException {
        final User user = userRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con id: " + id));

        return UserPrincipal.fromUser(user);
    }
}
