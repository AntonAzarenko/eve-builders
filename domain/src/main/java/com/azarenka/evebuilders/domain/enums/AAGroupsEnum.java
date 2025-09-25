package com.azarenka.evebuilders.domain.enums;

public enum AAGroupsEnum {

    DEPARTMENT_OF_INDUSTRY(23), INDUSTRY(29), MINING(28);

    private final Integer groupId;

    AAGroupsEnum(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public boolean hasGroupId(Integer groupId) {
        return DEPARTMENT_OF_INDUSTRY.groupId.equals(groupId) ||
            INDUSTRY.groupId.equals(groupId)|| MINING.groupId.equals(groupId);
    }

    public AAGroupsEnum getGroupsEnum(Integer groupId) {
        switch (groupId) {
            case 23: return DEPARTMENT_OF_INDUSTRY;
            case 29: return INDUSTRY;
            case 28: return MINING;
            default: return null;
        }
    }
}
