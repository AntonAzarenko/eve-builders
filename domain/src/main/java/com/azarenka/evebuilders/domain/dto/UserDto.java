package com.azarenka.evebuilders.domain.dto;

import com.azarenka.evebuilders.domain.db.Role;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserDto userDto = (UserDto) o;

        return new EqualsBuilder().append(username, userDto.username)
            .append(characterId, userDto.characterId)
            .append(roles, userDto.roles)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(username).append(characterId).append(roles).toHashCode();
    }

    @Override
    public String toString() {
        return "UserDto{" +
            "username='" + username + '\'' +
            ", characterId='" + characterId + '\'' +
            ", roles=" + roles +
            '}';
    }
}
