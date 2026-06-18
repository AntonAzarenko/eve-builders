package com.azarenka.evebuilders.domain.acl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class UserPermissionId implements Serializable {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "permission_id")
    private Long permissionId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserPermissionId that = (UserPermissionId) o;
        return new EqualsBuilder()
            .append(userId, that.userId)
            .append(permissionId, that.permissionId)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(userId)
            .append(permissionId)
            .toHashCode();
    }
}
