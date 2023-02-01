package com.agaloth.townywild.settings;

public enum ConfigNodes {

    VERSION_HEADER("version", "", ""),
    VERSION(
            "version.version",
            "",
            "# This is the current version of TownyWild. DO NOT EDIT THIS."),
    LANGUAGE("language",
            "english.yml",
            "# The language file you wish to use"),
    TOWNY_WILD_ENABLED(
            "towny_wild_enabled",
            "true",
            "",
            "############################################################",
            "# +------------------------------------------------------+ #",
            "# |                 PROTECTION NEAR TOWN                 | #",
            "# +------------------------------------------------------+ #",
            "############################################################",
            ""),
    PROTECTION_AFTER_EXITING_TOWN_BORDER(
            "protection_after_exiting_town_border",
            "true",
            "",
            "# If this value is true, players will get protection near town blocks."),
    PROTECTION_TIME_AFTER_EXITING_TOWN_BORDER(
            "protection_time_after_exiting_town_border",
            "20",
            "# This is the amount of time in seconds, the player exiting the town border will have PvP protection for, during that time, the player cannot hit or get hit by other players."),
    BOSSBAR_ENABLED(
            "bossbar_enabled",
            "true",
            "#",
            "############################################################",
            "# +------------------------------------------------------+ #",
            "# |                   BOSSBAR SETTINGS                   | #",
            "# +------------------------------------------------------+ #",
            "############################################################",
            ""),
    BOSSBAR_COLOR(
            "bossbar_color",
            "YELLOW",
            "# This is the color for the bossbar, you can use BLUE, GREEN, PINK, PURPLE, RED, WHITE and YELLOW"),
    BOSSBAR_STYLE(
            "bossbar_style",
            "STATIC",
            "# This is the style for the bossbar, you can use STATIC, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12 and SEGMENTED_20"),
    BOSS_BAR_MESSAGE(
            "bossbar_message",
            "You are protected for %townywild_countdown%!",
            "# This is the message that will be displayed on the boss bar. (Use the %townywild_countdown% placeholder if you want to include the amount of time left before their protection ends)");

    private final String Root;
    private final String Default;
    private String[] comments;

    ConfigNodes(String root, String def, String... comments) {

        this.Root = root;
        this.Default = def;
        this.comments = comments;
    }

    /**
     * Retrieves the root for a config option
     *
     * @return The root for a config option
     */
    public String getRoot() {

        return Root;
    }

    /**
     * Retrieves the default value for a config path
     *
     * @return The default value for a config path
     */
    public String getDefault() {

        return Default;
    }

    /**
     * Retrieves the comment for a config path
     *
     * @return The comments for a config path
     */

    public String[] getComments() {

        if (comments != null) {
            return comments;
        }

        String[] comments = new String[1];
        comments[0] = "";
        return comments;
    }

}