package com.azarenka.evebuilders.domain.dto;

import com.azarenka.evebuilders.domain.db.Role;
import jakarta.persistence.Column;

import java.util.Set;

public class UserDto {

    private String username;
    private String characterId;
    private Set<Role> roles;

    public UserDto() {
    }

    public UserDto(String username, String characterId, Set<Role> roles) {
        this.username = username;
        this.characterId = characterId;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCharacterId() {
        return characterId;
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
