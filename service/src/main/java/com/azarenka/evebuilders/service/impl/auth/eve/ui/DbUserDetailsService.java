package com.azarenka.evebuilders.service.impl.auth.eve.ui;

import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.repository.database.IUserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DbUserDetailsService implements UserDetailsService {

    @Autowired
    private IUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
        if (user.getUserRoles() != null && !user.getUserRoles().isEmpty()) {
            authorities.addAll(user.getUserRoles().stream()
                .filter(userRole -> userRole.getRole() != null && userRole.getRole().getCode() != null)
                .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getCode()))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        } else if (user.getRoles() != null) {
            authorities.addAll(user.getRoles().stream()
                .map(Role::getAuthority)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        }

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            .authorities(authorities)
            .accountLocked(!user.isEnabled())
            .disabled(!user.isEnabled())
            .build();
    }
}
