package com.nikohalfkino.achievements;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClassicAchievementsConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue HIDE_PAGE;
    public static final ForgeConfigSpec.BooleanValue DEBUG_MODE;
    public static final ForgeConfigSpec.BooleanValue SHOW_ACHIEVEMENT_COUNT;
    public static final ForgeConfigSpec.BooleanValue SHOW_HINTS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("General");

        HIDE_PAGE = builder
                .comment("Hide the page-switching button when multiple advancement trees are present.")
                .define("HidePage", false);

        DEBUG_MODE = builder
                .comment("When true, allows repositioning advancement nodes")
                .define("DebugMode", false);

        SHOW_ACHIEVEMENT_COUNT = builder
                .comment("When true, appends 'X/Y' completion counts to the Achievements title.")
                .define("ShowAchievementCount", false);

        SHOW_HINTS = builder
                .comment("When true, shows control hints (F: Fullscreen, Scroll: Zoom, Drag: Move) under the non-fullscreen achievement panel.")
                .define("ShowHints", false);

        builder.pop();
        SPEC = builder.build();
    }

    private ClassicAchievementsConfig() {}
}
