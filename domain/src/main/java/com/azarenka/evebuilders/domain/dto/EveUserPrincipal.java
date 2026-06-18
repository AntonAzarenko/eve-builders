package com.azarenka.evebuilders.domain.dto;

import com.azarenka.evebuilders.domain.acl.UserRole;
import com.azarenka.evebuilders.domain.db.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EveUserPrincipal implements OAuth2User, UserDetails {

    private final User user;
    private final Map<String, Object> attributes;

    public EveUserPrincipal(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        if (user.getUserRoles() != null && !user.getUserRoles().isEmpty()) {
            authorities.addAll(user.getUserRoles().stream()
                .map(UserRole::getRole)
                .filter(role -> role != null && role.getCode() != null)
                .map(role -> new SimpleGrantedAuthority(role.getCode()))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        } else if (user.getRoles() != null) {
            authorities.addAll(user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
        return authorities;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public User getUser() {
        return user;
    }
}
