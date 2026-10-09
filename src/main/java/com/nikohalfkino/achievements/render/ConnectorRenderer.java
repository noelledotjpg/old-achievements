package com.nikohalfkino.achievements.render;


import com.nikohalfkino.achievements.state.AdvancementStateHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nikohalfkino.achievements.layout.NodeMetrics.CENTER;
import static com.nikohalfkino.achievements.render.AchievementTextures.ACHIEVEMENT_BG;

final class ConnectorRenderer {

    private final AdvancementStateHelper stateHelper;

    ConnectorRenderer(AdvancementStateHelper stateHelper) {
        this.stateHelper = stateHelper;
    }

    void render(GuiGraphics g, TreeView view, Set<Advancement> highlighted) {
        Map<Advancement, int[]> positions = view.positions();

        for (Advancement advancement : highlightedLast(positions.keySet(), highlighted)) {
            Advancement parent = advancement.getParent();
            if (parent == null || !positions.containsKey(parent)) continue;
            if (!stateHelper.isNodeDrawn(advancement)) continue;

            int[] childPos = positions.get(advancement);
            int[] parentPos = positions.get(parent);

            int childX = view.screenX(childPos) + CENTER;
            int childY = view.screenY(childPos) + CENTER;
            int parentX = view.screenX(parentPos) + CENTER;
            int parentY = view.screenY(parentPos) + CENTER;

            boolean isHighlighted = highlighted.contains(advancement);
            int lineColor = isHighlighted ? EditorOverlayRenderer.SELECT_BLUE
                    : (stateHelper.isUnlocked(advancement) ? 0xFFA0A0A0
                    : (stateHelper.canUnlock(advancement) ? 0xFF00FF00 : 0xFF000000));
            setColor(g, lineColor);

            drawClassic(g, parentX, parentY, childX, childY);
        }
        g.setColor(1, 1, 1, 1);
    }

    private static List<Advancement> highlightedLast(Set<Advancement> all, Set<Advancement> highlighted) {
        List<Advancement> ordered = new ArrayList<>(all.size());
        for (Advancement advancement : all) if (!highlighted.contains(advancement)) ordered.add(advancement);
        for (Advancement advancement : all) if (highlighted.contains(advancement)) ordered.add(advancement);
        return ordered;
    }

    private static void drawClassic(GuiGraphics g, int parentX, int parentY, int childX, int childY) {
        g.hLine(parentX, childX, parentY, -1);
        g.vLine(childX, parentY, childY, -1);

        if (childY != parentY) {
            if (childY > parentY) arrowDown(g, childX, childY);
            else arrowUp(g, childX, childY);
        } else if (parentX != childX) {
            if (childX > parentX) arrowRight(g, childX, childY);
            else arrowLeft(g, childX, childY);
        }
    }

    private static void arrowDown(GuiGraphics g, int x, int y) {
        g.blit(ACHIEVEMENT_BG, x - 5, y - 18, 96, 234, 11, 7);
    }

    private static void arrowUp(GuiGraphics g, int x, int y) {
        g.blit(ACHIEVEMENT_BG, x - 5, y + 11, 96, 241, 11, 7);
    }

    private static void arrowRight(GuiGraphics g, int x, int y) {
        g.blit(ACHIEVEMENT_BG, x - 18, y - 5, 114, 234, 7, 11);
    }

    private static void arrowLeft(GuiGraphics g, int x, int y) {
        g.blit(ACHIEVEMENT_BG, x + 11, y - 5, 107, 234, 7, 11);
    }

    private static void setColor(GuiGraphics g, int argb) {
        g.setColor(((argb >> 16) & 255) / 255f, ((argb >> 8) & 255) / 255f, (argb & 255) / 255f, 1.0f);
    }
}
