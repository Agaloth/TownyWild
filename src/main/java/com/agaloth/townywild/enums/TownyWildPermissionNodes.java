package com.agaloth.townywild.enums;

public enum TownyWildPermissionNodes {
    TOWNYWILD_ADMIN_COMMAND("townywild.reload");

    private final String value;

    TownyWildPermissionNodes(String permission) {
        this.value = permission;
    }
    public String getNode() {
        return value;
    }
    public String getNode(String replace) {
        return value.replace("*", replace);
    }
}
