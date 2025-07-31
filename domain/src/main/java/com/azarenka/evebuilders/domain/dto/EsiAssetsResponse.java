package com.azarenka.evebuilders.domain.dto;

import com.azarenka.evebuilders.domain.db.Asset;

import java.time.LocalDateTime;
import java.util.List;

public class EsiAssetsResponse {

    private List<Asset> assets;
    private String etag;
    private LocalDateTime expiresAt;
    private boolean notModified;

    public EsiAssetsResponse() {
    }

    public EsiAssetsResponse(boolean notModified, String etag, LocalDateTime expiresAt, List<Asset> assets) {
        this.notModified = notModified;
        this.etag = etag;
        this.expiresAt = expiresAt;
        this.assets = assets;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
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

    public boolean isNotModified() {
        return notModified;
    }

    public void setNotModified(boolean notModified) {
        this.notModified = notModified;
    }
}
