package com.ocistart.common.enums;

import lombok.Getter;

@Getter
public enum OperationSystemEnum {
    UBUNTU_22("Canonical Ubuntu", "22.04"),
    UBUNTU_24("Canonical Ubuntu", "24.04"),
    ORACLE_LINUX_8("Oracle Linux", "8"),
    ORACLE_LINUX_9("Oracle Linux", "9");

    private final String type;
    private final String version;

    OperationSystemEnum(String type, String version) {
        this.type = type;
        this.version = version;
    }

    public static OperationSystemEnum getSystemType(String system) {
        if (system == null) return UBUNTU_22;
        for (OperationSystemEnum os : values()) {
            if (os.name().equalsIgnoreCase(system)) {
                return os;
            }
        }
        return UBUNTU_22;
    }
}
