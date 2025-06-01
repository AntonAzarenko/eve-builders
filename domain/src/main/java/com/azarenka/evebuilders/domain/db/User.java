package com.azarenka.evebuilders.domain.db;

import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user_info", schema = "builders")
public class User {

    @Id
    @Column(name = "uid")
    private String uid;
    @Column(name="user_name",unique = true, nullable = false)
    private String username;
    @Column(name="character_id",nullable = false)
    private String characterId;
    @Column(name="character_info", length = 900)
    private String characterInfo;
    @Column(name = "password")
    private String password;
    private Set<Role> roles;
    @Column(name="main_id")
    private String mainId;
    @Column(name="is_main_character")
    private Boolean isMainCharacter;
    @Column(name="corp_name")
    private String corporationName;
    @Column(name="alliance_name")
    private String allianceName;
    @Column(name = "language")
    private String language;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Permission> permissions;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "roles",
            schema = "builders",
            joinColumns = @JoinColumn(name = "user_uid", referencedColumnName = "uid"),
            inverseJoinColumns = @JoinColumn(name = "role_name", referencedColumnName = "name")
    )
    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
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
}
