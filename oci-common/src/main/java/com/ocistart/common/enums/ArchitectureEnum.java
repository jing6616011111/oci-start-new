package com.ocistart.common.enums;

import lombok.Getter;

@Getter
public enum ArchitectureEnum {
    AMD("VM.Standard.E2.1.Micro", "VM.Standard.E2.1"),
    ARM("VM.Standard.A1.Flex", "VM.Standard.A1.Flex");

    private final String shapeDetail;
    private final String displayName;

    ArchitectureEnum(String shapeDetail, String displayName) {
        this.shapeDetail = shapeDetail;
        this.displayName = displayName;
    }

    public static ArchitectureEnum getType(String architecture) {
        if (architecture == null) return ARM;
        for (ArchitectureEnum type : values()) {
            if (type.name().equalsIgnoreCase(architecture)) {
                return type;
            }
        }
        return ARM;
    }
}
