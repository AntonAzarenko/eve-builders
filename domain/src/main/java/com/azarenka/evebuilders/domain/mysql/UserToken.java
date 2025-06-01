package com.azarenka.evebuilders.domain.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_token", schema = "builders")
public class UserToken {

    @Id
    @Column(name="user_id",unique = true, nullable = false)
    private String userId;
    @Column(name="access_token",unique = true, nullable = false, length = 5000)
    private String accessToken;
    @Column(name="refresh_token",unique = true, nullable = false)
    private String refreshToken;
    @Column(name="expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
