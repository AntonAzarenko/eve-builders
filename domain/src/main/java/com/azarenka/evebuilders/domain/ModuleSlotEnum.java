package com.azarenka.evebuilders.domain;

public enum ModuleSlotEnum {

    CARGO(0),
    LOW_SLOT(16),
    MIDDLE_SLOT(13),
    HIGH_SLOT(12),
    RIG(2663),
    SUBSYSTEM(3772),
    DRONE_BAY(18),
    CHARGE(8),
    UNKNOWN(-1);

    private final Integer attributeId;

    ModuleSlotEnum(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public Integer getAttributeId() {
        return attributeId;
    }

    public static ModuleSlotEnum getSlot(Integer attributeId) {
        for (ModuleSlotEnum slot : ModuleSlotEnum.values()) {
            if (slot.getAttributeId().equals(attributeId)) {
                return slot;
            }
        }
        throw new IllegalArgumentException("No ModuleSlot found for attributeId: " + attributeId);
    }
}
