package com.azarenka.evebuilders.service.impl.corporation;

import com.azarenka.evebuilders.domain.db.Corporation;
import com.azarenka.evebuilders.domain.db.ManagedCorporation;
import com.azarenka.evebuilders.domain.exeptions.ValidationException;
import com.azarenka.evebuilders.repository.database.IManagedCorporationRepository;
import com.azarenka.evebuilders.service.api.ICorporationService;
import com.azarenka.evebuilders.service.api.IEveCharacterService;
import com.azarenka.evebuilders.service.api.IEveCorporationService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.auth.eve.SecurityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
@Service
public class CorporationService implements ICorporationService {
    private static final String ERR_NOT_AUTHENTICATED = "errors.corporation.not_authenticated";
    private static final String ERR_ALREADY_ADDED = "errors.corporation.already_added";
    private static final String ERR_NAME_REQUIRED = "errors.corporation.name_required";
    private static final String ERR_NOT_FOUND = "errors.corporation.not_found";
    private static final String ERR_SEARCH_FAILED = "errors.corporation.search_failed";
    private static final String ERR_VALIDATE_FAILED = "errors.corporation.validate_failed";
    private static final String ERR_CEO_VERIFY_FAILED = "errors.corporation.ceo_verify_failed";
    private static final String ERR_ONLY_CEO = "errors.corporation.only_ceo";

    @Autowired
    private IManagedCorporationRepository managedCorporationRepository;
    @Autowired
    private IEveCorporationService eveCorporationService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IEveCharacterService eveCharacterService;

    @Override
    @Transactional
    public ManagedCorporation addCorporation(String corporationName) {
        validateInput(corporationName);
        var username = SecurityUtils.getUserName();
        if (username == null || username.isBlank()) {
            throw new ValidationException(ERR_NOT_AUTHENTICATED);
        }
        Long eveCorporationId = resolveCorporationId(corporationName);
        if (managedCorporationRepository.existsByOwnerUsernameAndEveCorporationId(username, eveCorporationId)) {
            throw new ValidationException(ERR_ALREADY_ADDED);
        }
        var corporation = validateCorporationExistsInEsi(eveCorporationId);
        validateCurrentUserIsCeo(corporation, eveCorporationId);

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
            throw new ValidationException(ERR_NAME_REQUIRED);
        }
    }

    private Long resolveCorporationId(String corporationName) {
        try {
            Long eveCorporationId = eveCorporationService.findCorporationIdByName(corporationName.trim());
            if (eveCorporationId == null || eveCorporationId <= 0) {
                throw new ValidationException(ERR_NOT_FOUND);
            }
            return eveCorporationId;
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException(ERR_SEARCH_FAILED);
        }
    }

    private Corporation validateCorporationExistsInEsi(Long eveCorporationId) {
        try {
            var corporation = eveCorporationService.getCorporation(String.valueOf(eveCorporationId));
            if (Objects.isNull(corporation) || corporation.getName() == null || corporation.getName().isBlank()) {
                throw new ValidationException(ERR_NOT_FOUND);
            }
            return corporation;
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException(ERR_VALIDATE_FAILED);
        }
    }

    private void validateCurrentUserIsCeo(Corporation corporation, Long eveCorporationId) {
        try {
            String currentCharacterId = userService.getCharacterId();
            String accessToken = userService.getUserToken();
            if (currentCharacterId == null || currentCharacterId.isBlank() || accessToken == null || accessToken.isBlank()) {
                throw new ValidationException(ERR_CEO_VERIFY_FAILED);
            }

            String characterInfoJson = eveCharacterService.getCharacterInfo(accessToken, currentCharacterId);
            Long currentCorporationId = extractLongField(characterInfoJson, "corporation_id");
            if (currentCorporationId == null || !currentCorporationId.equals(eveCorporationId)) {
                throw new ValidationException(ERR_ONLY_CEO);
            }

            Long ceoId = corporation.getCeoId() == null ? null : corporation.getCeoId().longValue();
            Long characterId = Long.valueOf(currentCharacterId);
            if (ceoId == null || !ceoId.equals(characterId)) {
                throw new ValidationException(ERR_ONLY_CEO);
            }
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException(ERR_CEO_VERIFY_FAILED);
        }
    }

    private Long extractLongField(String json, String field) {
        try {
            JsonNode root = new ObjectMapper().readTree(json);
            JsonNode node = root.get(field);
            if (node == null || node.isNull()) {
                return null;
            }
            return node.asLong();
        } catch (Exception e) {
            return null;
        }
    }
}
