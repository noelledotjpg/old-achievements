package com.nikohalfkino.achievements.render;

import com.nikohalfkino.achievements.state.AdvancementStateHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

import static com.nikohalfkino.achievements.layout.NodeMetrics.SIZE;

final class NodeRenderer {

    private static final int DRAGGED_OUTLINE = 0xFFFFFF00;
    private static final int SELECTED_OUTLINE = 0xFF00FFFF;

    private final AdvancementStateHelper stateHelper;

    NodeRenderer(AdvancementStateHelper stateHelper) {
        this.stateHelper = stateHelper;
    }

    Advancement render(GuiGraphics g, TreeView view) {
        EditorOverlay overlay = view.overlay();
        boolean treeComplete = overlay == null && isTreeComplete(view.positions());
        Advancement hovered = null;

        for (Map.Entry<Advancement, int[]> entry : view.positions().entrySet()) {
            Advancement advancement = entry.getKey();
            if (!stateHelper.isNodeDrawn(advancement)) continue;

            int x = view.screenX(entry.getValue());
            int y = view.screenY(entry.getValue());
            if (x < -SIZE || y < -SIZE || x > view.viewportW() * view.zoom() || y > view.viewportH() * view.zoom()) continue;

            boolean done = stateHelper.isUnlocked(advancement);
            boolean canUnlock = stateHelper.canUnlock(advancement);
            int depth = stateHelper.getRequirementCount(advancement);
            float brightness = done ? 1.0F : (canUnlock ? 0.4F : (depth == 2 ? 0.2F : 0.1F));

            boolean challenge = advancement.display().get().getType() == AdvancementType.CHALLENGE;
            ResourceLocation iconBackground = AchievementTextures.iconBackground(challenge, treeComplete);
            g.setColor(brightness, brightness, brightness, 1);
            g.blit(iconBackground, x, y, 0, 0, SIZE, SIZE, SIZE, SIZE);
            g.setColor(1, 1, 1, 1);
            g.renderFakeItem(advancement.display().get().getIcon(), x + 5, y + 5);

            if (overlay != null) {
                if (advancement == overlay.dragged()) {
                    drawOutline(g, x, y, DRAGGED_OUTLINE);
                } else if (overlay.selected().contains(advancement)) {
                    drawOutline(g, x, y, SELECTED_OUTLINE);
                }
            }

            if (view.mouseX() >= x && view.mouseX() <= x + SIZE && view.mouseY() >= y && view.mouseY() <= y + SIZE) {
                hovered = advancement;
            }
        }
        return hovered;
    }

    private boolean isTreeComplete(Map<Advancement, int[]> positions) {
        if (positions.isEmpty()) return false;
        for (Advancement advancement : positions.keySet()) {
            if (stateHelper.isHiddenFromScreen(advancement)) continue;
            if (!stateHelper.isUnlocked(advancement)) return false;
        }
        return true;
    }

    private static void drawOutline(GuiGraphics g, int x, int y, int color) {
        g.hLine(x - 1, x + SIZE, y - 1, color);
        g.hLine(x - 1, x + SIZE, y + SIZE, color);
        g.vLine(x - 1, y - 1, y + SIZE, color);
        g.vLine(x + SIZE, y - 1, y + SIZE, color);
    }
}
