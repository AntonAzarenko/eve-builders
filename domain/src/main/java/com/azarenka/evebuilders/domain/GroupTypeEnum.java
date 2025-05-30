package com.azarenka.evebuilders.domain;

public enum GroupTypeEnum {

    SHIPS(6) {
        @Override
        public String toString() {
            return "management.add_ships";
        }
    },

    MODULES(7) {
        @Override
        public String toString() {
            return "management.add_modules";
        }
    };

    private final Integer groupId;

    GroupTypeEnum(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public String[] getValues() {
        String[] values = new String[GroupTypeEnum.values().length];
        values[0] = GroupTypeEnum.SHIPS.name();
        values[1] = GroupTypeEnum.MODULES.name();
        return values;
    }

}


