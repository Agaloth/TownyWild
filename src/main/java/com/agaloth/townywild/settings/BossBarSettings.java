package com.agaloth.townywild.settings;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import static com.agaloth.townywild.settings.Settings.getConfig;

public class BossBarSettings {
    private String message;
    private BarColor color;
    private BarStyle style;

    public BossBarSettings() {
    }

    public void loadBossBarSettings() {
        this.message = getConfig().getString("bossbar_message");
        this.color = BarColor.valueOf(getConfig().getString("bossbar_color").toUpperCase());
        this.style = BarStyle.valueOf(getConfig().getString("bossbar_style").toUpperCase());
    }
}
