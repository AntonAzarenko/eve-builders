package com.azarenka.evebuilders.domain.db;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "character_asset_sync", schema = "builders")
public class CharacterAssetSync {

    @Id
    @Column(name = "user_name", nullable = false)
    private String userName; // или characterId, если используешь его
    @Column(name = "etag", length = 128)
    private String etag;
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    public CharacterAssetSync() {
    }

    public CharacterAssetSync(String userName, String etag, LocalDateTime expiresAt, LocalDateTime lastSyncAt) {
        this.userName = userName;
        this.etag = etag;
        this.expiresAt = expiresAt;
        this.lastSyncAt = lastSyncAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(LocalDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }
}
