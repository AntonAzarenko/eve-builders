package com.azarenka.evebuilders.domain.db;

import jakarta.persistence.*;

@Entity
@Table(name = "permissions", schema = "builders")
public class Permission {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "permission_id", nullable = false, unique = true)
    private String permissionId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
