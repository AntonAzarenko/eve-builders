package com.azarenka.evebuilders.domain;

public enum ModuleSlot {

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

    ModuleSlot(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public Integer getAttributeId() {
        return attributeId;
    }

    public static ModuleSlot getSlot(Integer attributeId) {
        for (ModuleSlot slot : ModuleSlot.values()) {
            if (slot.getAttributeId().equals(attributeId)) {
                return slot;
            }
        }
        throw new IllegalArgumentException("No ModuleSlot found for attributeId: " + attributeId);
    }
}
