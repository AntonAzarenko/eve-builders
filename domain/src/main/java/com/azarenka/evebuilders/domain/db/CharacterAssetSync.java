package com.azarenka.evebuilders.domain.db;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "character_asset_sync")
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
}
