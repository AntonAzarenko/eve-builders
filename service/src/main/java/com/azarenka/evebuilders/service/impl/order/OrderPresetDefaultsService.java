package com.azarenka.evebuilders.service.impl.order;

import com.azarenka.evebuilders.domain.db.BlueprintOption;
import com.azarenka.evebuilders.domain.db.ManagedCorporation;
import com.azarenka.evebuilders.domain.db.OrderPresetDefaults;
import com.azarenka.evebuilders.domain.db.OrderPresetDefaultsHistory;
import com.azarenka.evebuilders.domain.db.OrderType;
import com.azarenka.evebuilders.domain.db.PriorityOption;
import com.azarenka.evebuilders.domain.dto.OrderPresetDefaultsDto;
import com.azarenka.evebuilders.domain.dto.OrderPresetDefaultsHistoryDto;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.domain.enums.ReceiverTargetType;
import com.azarenka.evebuilders.domain.sqllite.OrderRights;
import com.azarenka.evebuilders.repository.database.properties.IOrderPresetDefaultsHistoryRepository;
import com.azarenka.evebuilders.repository.database.properties.IOrderPresetDefaultsRepository;
import com.azarenka.evebuilders.service.api.ICorporationService;
import com.azarenka.evebuilders.service.api.IOrderPresetDefaultsService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.auth.eve.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderPresetDefaultsService implements IOrderPresetDefaultsService {

    private static final String DEFAULT_CORPORATION_NAME = "Scan Stakan";
    private static final String DEFAULT_RIGHTSHOLDER = "GROUP";

    private final IOrderPresetDefaultsRepository presetRepository;
    private final IOrderPresetDefaultsHistoryRepository historyRepository;
    private final ICorporationService corporationService;
    private final IUserService userService;

    public OrderPresetDefaultsService(IOrderPresetDefaultsRepository presetRepository,
                                      IOrderPresetDefaultsHistoryRepository historyRepository,
                                      ICorporationService corporationService,
                                      IUserService userService) {
        this.presetRepository = presetRepository;
        this.historyRepository = historyRepository;
        this.corporationService = corporationService;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderPresetDefaultsDto getDefaultsForCurrentUser() {
        String username = resolveCurrentUsername();
        Optional<OrderPresetDefaults> optionalPreset = presetRepository.findByOwnerUsername(username);
        if (optionalPreset.isEmpty()) {
            return buildLegacyFallback();
        }
        OrderPresetDefaultsDto dto = toDto(optionalPreset.get());
        dto.setReceiverMissing(!isReceiverReferenceValid(dto));
        return dto;
    }

    @Override
    @Transactional
    public OrderPresetDefaultsDto saveDefaultsForCurrentUser(OrderPresetDefaultsDto dto) {
        validate(dto);
        String username = resolveCurrentUsername();
        OrderPresetDefaults preset = presetRepository.findByOwnerUsername(username)
            .orElseGet(OrderPresetDefaults::new);
        boolean isNew = StringUtils.isBlank(preset.getId());
        if (isNew) {
            preset.setId(UUID.randomUUID().toString());
            preset.setCreatedBy(username);
            preset.setCreatedDate(LocalDate.now());
        }
        applyDtoToEntity(dto, preset, username);
        OrderPresetDefaults saved = presetRepository.save(preset);
        writeHistory(saved, username);
        OrderPresetDefaultsDto response = toDto(saved);
        response.setReceiverMissing(false);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderPresetDefaultsHistoryDto> getHistoryForCurrentUser() {
        String username = resolveCurrentUsername();
        return historyRepository.findByOwnerUsernameOrderByChangedDateDesc(username).stream()
            .map(this::toHistoryDto)
            .toList();
    }

    private void validate(OrderPresetDefaultsDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Preset defaults payload is required");
        }
        if (dto.getOrderType() == null || dto.getReceiverType() == null || dto.getPriority() == null
            || dto.getBlueprint() == null || dto.getOrderRights() == null) {
            throw new IllegalArgumentException("All preset enum fields are required");
        }
        if (StringUtils.isBlank(dto.getRightsholder())) {
            throw new IllegalArgumentException("Rightsholder is required");
        }
        if (StringUtils.isBlank(dto.getReceiverRefId()) || StringUtils.isBlank(dto.getReceiverName())) {
            throw new IllegalArgumentException("Receiver value is required");
        }
        if (!isReceiverReferenceValid(dto)) {
            throw new IllegalArgumentException("Receiver reference is not valid for selected receiver type");
        }
    }

    private boolean isReceiverReferenceValid(OrderPresetDefaultsDto dto) {
        if (dto.getReceiverType() == ReceiverTargetType.CORPORATION) {
            return corporationService.getAllCorporations().stream()
                .anyMatch(value -> value.getEveCorporationId() != null
                    && String.valueOf(value.getEveCorporationId()).equals(dto.getReceiverRefId())
                    && StringUtils.equals(value.getCorporationName(), dto.getReceiverName()));
        }
        return userService.getUsersDto().stream()
            .anyMatch(value -> StringUtils.equals(value.getCharacterId(), dto.getReceiverRefId())
                && StringUtils.equals(value.getUsername(), dto.getReceiverName()));
    }

    private void applyDtoToEntity(OrderPresetDefaultsDto dto, OrderPresetDefaults entity, String username) {
        entity.setOwnerUsername(username);
        entity.setOrderType(dto.getOrderType().name());
        entity.setReceiverType(dto.getReceiverType());
        entity.setReceiverRefId(dto.getReceiverRefId());
        entity.setReceiverName(dto.getReceiverName());
        entity.setPriority(dto.getPriority().name());
        entity.setBluePrint(dto.getBlueprint() == BlueprintOption.YES);
        entity.setOrderRights(dto.getOrderRights());
        entity.setRightsholder(dto.getRightsholder());
        entity.setUpdatedBy(username);
        entity.setUpdatedDate(LocalDate.now());
    }

    private OrderPresetDefaultsDto toDto(OrderPresetDefaults entity) {
        OrderPresetDefaultsDto dto = new OrderPresetDefaultsDto();
        dto.setOrderType(OrderType.valueOf(entity.getOrderType()));
        dto.setReceiverType(entity.getReceiverType());
        dto.setReceiverRefId(entity.getReceiverRefId());
        dto.setReceiverName(entity.getReceiverName());
        dto.setPriority(PriorityOption.valueOf(entity.getPriority()));
        dto.setBlueprint(entity.isBluePrint() ? BlueprintOption.YES : BlueprintOption.NO);
        dto.setOrderRights(entity.getOrderRights());
        dto.setRightsholder(entity.getRightsholder());
        return dto;
    }

    private OrderPresetDefaultsDto buildLegacyFallback() {
        OrderPresetDefaultsDto dto = new OrderPresetDefaultsDto();
        dto.setOrderType(OrderType.REDEMPTION);
        dto.setReceiverType(ReceiverTargetType.CORPORATION);
        dto.setPriority(PriorityOption.MEDIUM);
        dto.setBlueprint(BlueprintOption.NO);
        dto.setOrderRights(OrderRights.GROUP);
        dto.setRightsholder(DEFAULT_RIGHTSHOLDER);
        Optional<ManagedCorporation> corp = corporationService.getAllCorporations().stream()
            .filter(value -> StringUtils.equalsIgnoreCase(value.getCorporationName(), DEFAULT_CORPORATION_NAME))
            .findFirst();
        if (corp.isPresent() && corp.get().getEveCorporationId() != null) {
            dto.setReceiverRefId(String.valueOf(corp.get().getEveCorporationId()));
            dto.setReceiverName(corp.get().getCorporationName());
            dto.setReceiverMissing(false);
        } else {
            dto.setReceiverRefId("");
            dto.setReceiverName("");
            dto.setReceiverMissing(true);
        }
        return dto;
    }

    private void writeHistory(OrderPresetDefaults saved, String username) {
        OrderPresetDefaultsHistory history = new OrderPresetDefaultsHistory();
        history.setId(UUID.randomUUID().toString());
        history.setPresetId(saved.getId());
        history.setOwnerUsername(saved.getOwnerUsername());
        history.setOrderType(saved.getOrderType());
        history.setReceiverType(saved.getReceiverType());
        history.setReceiverRefId(saved.getReceiverRefId());
        history.setReceiverName(saved.getReceiverName());
        history.setPriority(saved.getPriority());
        history.setBluePrint(saved.isBluePrint());
        history.setOrderRights(saved.getOrderRights());
        history.setRightsholder(saved.getRightsholder());
        history.setChangedBy(username);
        history.setChangedDate(LocalDate.now());
        historyRepository.save(history);
    }

    private OrderPresetDefaultsHistoryDto toHistoryDto(OrderPresetDefaultsHistory value) {
        OrderPresetDefaultsHistoryDto dto = new OrderPresetDefaultsHistoryDto();
        dto.setChangedBy(value.getChangedBy());
        dto.setChangedDate(value.getChangedDate());
        dto.setOrderType(value.getOrderType());
        dto.setReceiverType(value.getReceiverType().name());
        dto.setReceiverName(value.getReceiverName());
        dto.setReceiverRefId(value.getReceiverRefId());
        dto.setPriority(value.getPriority());
        dto.setBlueprint(value.isBluePrint() ? "YES" : "NO");
        dto.setOrderRights(value.getOrderRights().name());
        dto.setRightsholder(value.getRightsholder());
        return dto;
    }

    private String resolveCurrentUsername() {
        String username = SecurityUtils.getUserName();
        return StringUtils.isBlank(username) ? "SYSTEM" : username;
    }
}
