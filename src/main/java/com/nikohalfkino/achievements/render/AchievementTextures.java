package com.nikohalfkino.achievements.render;


import net.minecraft.resources.ResourceLocation;

public final class AchievementTextures {

    public static final ResourceLocation ACHIEVEMENT_BG = texture("achievement_background");

    private static final ResourceLocation ICON_REGULAR = texture("icon_bg_regular");
    private static final ResourceLocation ICON_CHALLENGE = texture("icon_bg_challenge");
    private static final ResourceLocation ICON_REGULAR_GOLD = texture("icon_bg_regular_gold");
    private static final ResourceLocation ICON_CHALLENGE_GOLD = texture("icon_bg_challenge_gold");

    private AchievementTextures() {}

    static ResourceLocation iconBackground(boolean challenge, boolean gold) {
        if (challenge) return gold ? ICON_CHALLENGE_GOLD : ICON_CHALLENGE;
        return gold ? ICON_REGULAR_GOLD : ICON_REGULAR;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/achievement/" + name + ".png");
    }
}
