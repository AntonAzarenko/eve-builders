package com.azarenka.evebuilders.domain.enums;

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
            return "management.add_components";
        }
    },

    ADVANCED_COMPONENTS(22) {
        @Override
        public String toString() {
            return "management.add_advanced_components";
        }
    },

    SUPER_COMPONENTS(17) {
        @Override
        public String toString() {
            return "management.add_super_components";
        }
    },

    DRONES(18) {
        @Override
        public String toString() {
            return "management.add_drones";
        }
    },

    FIGHTERS(87) {
        @Override
        public String toString() {
            return "management.add_fighters";
        }
    },

    STRUCTURES(65) {
        @Override
        public String toString() {
            return "management.add_structures";
        }
    },

    STRUCTURE_MODULES(66) {
        @Override
        public String toString() {
            return "management.add_structure_modules";
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
        values[5] = GroupTypeEnum.DRONES.name();
        values[6] = GroupTypeEnum.FIGHTERS.name();
        values[7] = GroupTypeEnum.STRUCTURES.name();
        values[8] = GroupTypeEnum.STRUCTURE_MODULES.name();
        return values;
    }

    public String[] getValuesForTranslation() {
        String[] values = new String[GroupTypeEnum.values().length];
        values[0] = GroupTypeEnum.SHIPS.toString();
        values[1] = GroupTypeEnum.MODULES.toString();
        values[2] = GroupTypeEnum.COMPONENTS.toString();
        values[3] = GroupTypeEnum.ADVANCED_COMPONENTS.toString();
        values[4] = GroupTypeEnum.SUPER_COMPONENTS.toString();
        values[5] = GroupTypeEnum.DRONES.toString();
        values[6] = GroupTypeEnum.FIGHTERS.toString();
        values[7] = GroupTypeEnum.STRUCTURES.toString();
        values[8] = GroupTypeEnum.STRUCTURE_MODULES.toString();
        return values;
    }

}


