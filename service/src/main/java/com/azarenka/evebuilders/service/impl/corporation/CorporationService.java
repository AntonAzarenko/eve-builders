package com.azarenka.evebuilders.service.impl.corporation;

import com.azarenka.evebuilders.domain.db.Corporation;
import com.azarenka.evebuilders.domain.db.ManagedCorporation;
import com.azarenka.evebuilders.domain.exeptions.ValidationException;
import com.azarenka.evebuilders.repository.database.IManagedCorporationRepository;
import com.azarenka.evebuilders.service.api.ICorporationService;
import com.azarenka.evebuilders.service.api.IEveCorporationService;
import com.azarenka.evebuilders.service.impl.auth.eve.SecurityUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
@Service
public class CorporationService implements ICorporationService {

    @Autowired
    private IManagedCorporationRepository managedCorporationRepository;
    @Autowired
    private IEveCorporationService eveCorporationService;

    @Override
    @Transactional
    public ManagedCorporation addCorporation(String corporationName) {
        validateInput(corporationName);
        var username = SecurityUtils.getUserName();
        if (username == null || username.isBlank()) {
            throw new ValidationException("Current user is not authenticated");
        }
        Long eveCorporationId = resolveCorporationId(corporationName);
        if (managedCorporationRepository.existsByOwnerUsernameAndEveCorporationId(username, eveCorporationId)) {
            throw new ValidationException("Corporation is already added for current user");
        }
        var corporation = validateCorporationExistsInEsi(eveCorporationId);

        ManagedCorporation entity = new ManagedCorporation();
        entity.setId(UUID.randomUUID().toString());
        entity.setEveCorporationId(eveCorporationId);
        entity.setCorporationName(corporation.getName());
        entity.setCorporationTicker(corporation.getTicker());
        entity.setOwnerUsername(username);
        entity.setCreatedBy(username);
        entity.setCreatedDate(LocalDate.now());
        entity.setUpdatedBy(username);
        entity.setUpdatedDate(LocalDate.now());
        return managedCorporationRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManagedCorporation> getMyCorporations() {
        var username = SecurityUtils.getUserName();
        if (username == null || username.isBlank()) {
            return List.of();
        }
        return managedCorporationRepository.findAllByOwnerUsernameOrderByCreatedDateDesc(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManagedCorporation> getAllCorporations() {
        return managedCorporationRepository.findAllByOrderByCreatedDateDesc();
    }

    private void validateInput(String corporationName) {
        if (corporationName == null || corporationName.trim().isEmpty()) {
            throw new ValidationException("Corporation name is required");
        }
    }

    private Long resolveCorporationId(String corporationName) {
        try {
            Long eveCorporationId = eveCorporationService.findCorporationIdByName(corporationName.trim());
            if (eveCorporationId == null || eveCorporationId <= 0) {
                throw new ValidationException("Corporation not found");
            }
            return eveCorporationId;
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Failed to search corporation in ESI");
        }
    }

    private Corporation validateCorporationExistsInEsi(Long eveCorporationId) {
        try {
            var corporation = eveCorporationService.getCorporation(String.valueOf(eveCorporationId));
            if (Objects.isNull(corporation) || corporation.getName() == null || corporation.getName().isBlank()) {
                throw new ValidationException("Corporation not found");
            }
            return corporation;
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Failed to validate corporation in ESI");
        }
    }
}
