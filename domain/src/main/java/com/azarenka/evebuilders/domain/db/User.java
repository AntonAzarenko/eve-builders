package com.azarenka.evebuilders.domain.db;

import com.azarenka.evebuilders.domain.acl.UserPermission;
import com.azarenka.evebuilders.domain.acl.UserRole;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "user_info", schema = "builders")
public class User {

    @Id
    @Column(name = "uid")
    private String uid;
    @Column(name = "user_name", unique = true, nullable = false)
    private String username;
    @Column(name = "character_id", nullable = false)
    private String characterId;
    @Column(name = "character_info", length = 10000)
    private String characterInfo;
    @Column(name = "password")
    private String password;
    @Transient
    private Set<Role> roles;
    @Column(name = "main_id")
    private String mainId;
    @Column(name = "is_main_character")
    private Boolean isMainCharacter;
    @Column(name = "corp_name")
    private String corporationName;
    @Column(name = "alliance_name")
    private String allianceName;
    @Column(name = "language")
    private String language;
    @Column(name = "theme", columnDefinition = "VARCHAR(255) DEFAULT 'light'")
    private String theme;
    @Column(nullable = false)
    private boolean enabled = true;

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<UserRole> userRoles = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<UserPermission> directPermissions = new LinkedHashSet<>();

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public Set<UserPermission> getDirectPermissions() {
        return directPermissions;
    }

    public void setDirectPermissions(Set<UserPermission> directPermissions) {
        this.directPermissions = directPermissions;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getCharacterInfo() {
        return characterInfo;
    }

    public void setCharacterInfo(String characterInfo) {
        this.characterInfo = characterInfo;
    }

    public String getMainId() {
        return mainId;
    }

    public void setMainId(String mainId) {
        this.mainId = mainId;
    }

    public Boolean getMainCharacter() {
        return isMainCharacter;
    }

    public void setMainCharacter(Boolean mainCharacter) {
        isMainCharacter = mainCharacter;
    }

    public String getCorporationName() {
        return corporationName;
    }

    public void setCorporationName(String corporationName) {
        this.corporationName = corporationName;
    }

    public String getAllianceName() {
        return allianceName;
    }

    public void setAllianceName(String allianceName) {
        this.allianceName = allianceName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
