package com.mcbans.firestar.mcbans;

import com.mcbans.firestar.mcbans.permission.Perms;

public enum BanType {
    GLOBAL  ("globalBan", Perms.BAN_GLOBAL),
    LOCAL   ("localBan", Perms.BAN_LOCAL),
    TEMP    ("tempBan", Perms.BAN_TEMP),

    UNBAN   ("unBan", Perms.UNBAN),
    ;

    final private String actionName;
    final private Perms permission;

    private BanType(final String actionName, final Perms permission){
        this.actionName = actionName;
        this.permission = permission;
    }

    public String getActionName(){
        return this.actionName;
    }

    public Perms getPermission(){
        return this.permission;
    }
}
