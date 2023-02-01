package com.agaloth.townywild.enums;

public enum TownyWildPermissionNodes {
    TOWNYWILD_ADMIN_COMMAND("townywild.admin.command.*"),
    TOWNYWILD_ADMIN_COMMAND_RELOAD("townywild.admin.command.reload");

    private String value;

    TownyWildPermissionNodes(String permission) {
        this.value = permission;
    }
    public String getNode() {
        return value;
    }
    public String getNode(String replace) {
        return value.replace("*", replace);
    }
    public String getNode(int replace) {
        return value.replace("*", replace + "");
    }
}
