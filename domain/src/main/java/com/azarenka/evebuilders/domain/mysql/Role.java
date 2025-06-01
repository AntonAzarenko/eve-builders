package com.azarenka.evebuilders.domain.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "roles", schema = "builders")
public enum Role implements GrantedAuthority {

    ROLE_VIEWER,
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_SUPER_ADMIN;

    @Id
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Override
    public String getAuthority() {
        return name();
    }
}
