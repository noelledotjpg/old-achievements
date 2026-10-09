package com.nikohalfkino.achievements.render;

import net.minecraft.advancements.Advancement;

import java.util.Map;

public record TreeView(Map<Advancement, int[]> positions,
                       double scrollX, double scrollY, float zoom,
                       int mouseX, int mouseY,
                       int viewportW, int viewportH,
                       EditorOverlay overlay) {

    int screenX(int[] position) {
        return position[0] - (int) scrollX;
    }

    int screenY(int[] position) {
        return position[1] - (int) scrollY;
    }
}
