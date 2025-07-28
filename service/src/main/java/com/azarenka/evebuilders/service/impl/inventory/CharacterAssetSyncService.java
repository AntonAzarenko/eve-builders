package com.azarenka.evebuilders.service.impl.inventory;

import com.azarenka.evebuilders.domain.db.CharacterAssetSync;
import com.azarenka.evebuilders.repository.database.CharacterAssetSyncRepository;
import com.azarenka.evebuilders.service.api.ICharacterAssetSyncService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import jakarta.transaction.Transactional;

@Service
public class CharacterAssetSyncService implements ICharacterAssetSyncService {

    @Autowired
    private CharacterAssetSyncRepository repository;

    @Override
    @Transactional
    public String getEtagForUser(String userName) {
        return repository.findByUserName(userName)
            .map(CharacterAssetSync::getEtag)
            .orElse(null);
    }

    @Override
    @Transactional
    public void updateExpiresOnly(String userName, LocalDateTime expiresAt) {
        repository.findByUserName(userName).ifPresent(sync -> {
            sync.setExpiresAt(expiresAt);
            repository.save(sync);
        });
    }

    @Override
    @Transactional
    public void updateSync(String userName, String etag, LocalDateTime expiresAt) {
        var sync = repository.findByUserName(userName)
            .orElseGet(() -> {
                var s = new CharacterAssetSync();
                s.setUserName(userName);
                return s;
            });
        sync.setEtag(etag);
        sync.setExpiresAt(expiresAt);
        sync.setLastSyncAt(LocalDateTime.now());
        repository.save(sync);
    }
}
