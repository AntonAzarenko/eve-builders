package com.azarenka.evebuilders.service.api;

import java.time.LocalDateTime;

public interface ICharacterAssetSyncService {

    String getEtagForUser(String userName);

    void updateExpiresOnly(String userName, LocalDateTime expiresAt);

    void updateSync(String userName, String etag, LocalDateTime expiresAt);
}
