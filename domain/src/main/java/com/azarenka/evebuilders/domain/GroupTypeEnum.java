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
    },

    COMPONENTS(4) {
        @Override
        public String toString() {
            return super.toString();
        }
    },
    ADVANCED_COMPONENTS(22) {
        @Override
        public String toString() {
            return super.toString();
        }
    },
    SUPER_COMPONENTS(17) {
        @Override
        public String toString() {
            return super.toString();
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
        values[2] = GroupTypeEnum.COMPONENTS.name();
        values[3] = GroupTypeEnum.ADVANCED_COMPONENTS.name();
        values[4] = GroupTypeEnum.SUPER_COMPONENTS.name();
        return values;
    }


}


