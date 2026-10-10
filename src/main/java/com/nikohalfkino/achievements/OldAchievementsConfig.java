package com.nikohalfkino.achievements;

import net.neoforged.neoforge.common.ModConfigSpec;

public class OldAchievementsConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<Boolean> HIDE_PAGE = BUILDER
            .comment("Hide the page-switching button when multiple advancement trees are present.")
            .define("HidePage", false);
    public static final ModConfigSpec.ConfigValue<Boolean> DEBUG_MODE = BUILDER
            .comment("When true, allows repositioning advancement nodes.")
            .define("DebugMode", false);
    public static final ModConfigSpec.ConfigValue<Boolean> SHOW_ACHIEVEMENT_COUNT = BUILDER
            .comment("When true, appends 'X/Y' completion counts to the Achievements title.")
            .define("ShowAchievementCount", false);
    public static final ModConfigSpec.ConfigValue<Boolean> SHOW_HINTS = BUILDER
            .comment("When true, shows control hints (F: Fullscreen, Scroll: Zoom, Drag: Move) under the non-fullscreen achievement panel.")
            .define("ShowHints", false);
    public static final ModConfigSpec.ConfigValue<Boolean> RELIABLE_ARROWS = BUILDER
            .comment("When true, the arrow rendering's switched out for one derived from Reliable Advancements.")
            .define("reliableArrows", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}
