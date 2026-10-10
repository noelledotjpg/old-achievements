package com.nikohalfkino.achievements.render;

import com.nikohalfkino.achievements.OldAchievementsConfig;
import com.nikohalfkino.achievements.state.AdvancementStateHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.nikohalfkino.achievements.layout.NodeMetrics.CENTER;
import static com.nikohalfkino.achievements.render.AchievementTextures.ACHIEVEMENT_BG;

final class ConnectorRenderer {
    private static final int WIDGET_SIZE = 13;
    private static final int MAX_BEND_DISTANCE = 64;
    private final AdvancementStateHelper stateHelper;
    private final Function<ResourceLocation, AdvancementHolder> advHolderFunction;

    ConnectorRenderer(AdvancementStateHelper stateHelper, Function<ResourceLocation, AdvancementHolder> advHolderFunction) {
        this.stateHelper = stateHelper;
        this.advHolderFunction = advHolderFunction;
    }

    void render(GuiGraphics g, TreeView view, Set<Advancement> highlighted) {
        Map<Advancement, int[]> positions = view.positions();

        for (Advancement advancement : highlightedLast(positions.keySet(), highlighted)) {
            Advancement parent = advancement.parent().map(advHolderFunction).map(AdvancementHolder::value).orElse(null);
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

            if(!OldAchievementsConfig.RELIABLE_ARROWS.get()) drawClassic(g, parentX, parentY, childX, childY);
            else {
                Route route = route(parentX, parentY, childX, childY, Side.NONE);
                drawReliable(g, route);
            }
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

    // Below classes are derived from Reliable Advancements https://github.com/evanbones/Reliable-Advancements
    // And have been used under the mod's MIT License https://github.com/evanbones/Reliable-Advancements/blob/stonecutter/LICENSE
    private static void drawReliable(GuiGraphics g, Route route) {
        int startX = route.startX();
        int startY = route.startY();
        int startAnchorX = route.startAnchorX();
        int startAnchorY = route.startAnchorY();
        int endAnchorX = route.endAnchorX();
        int endAnchorY = route.endAnchorY();
        int endX = route.endX();
        int endY = route.endY();

        if (startX == startAnchorX) g.vLine(startX, startY, startAnchorY, -1);
        else g.hLine(startX, startAnchorX, startY, -1);

        if (startAnchorX == endAnchorX) g.vLine(startAnchorX, startAnchorY, endAnchorY, -1);
        else g.hLine(startAnchorX, endAnchorX, startAnchorY, -1);

        if (endAnchorX == endX) g.vLine(endX, endAnchorY, endY, -1);
        else g.hLine(endAnchorX, endX, endY, -1);

        if (!route.shouldShowArrow()) return;

        switch (route.entrySide()) {
            case TOP -> arrowDown(g, endX, endY);
            case BOTTOM -> arrowUp(g, endX, endY);
            case LEFT -> arrowRight(g, endX, endY);
            case RIGHT -> arrowLeft(g, endX, endY);
            case NONE -> {}
        }
    }

    static Route route(int startX, int startY, int endX, int endY, Side parentIncomingSide) {
        int dx = endX - startX;
        int dy = endY - startY;
        int absX = Math.abs(dx);
        int absY = Math.abs(dy);

        boolean verticalAnchors;
        if (absX < WIDGET_SIZE) {
            verticalAnchors = true;
        } else if (absY < WIDGET_SIZE) {
            verticalAnchors = false;
        } else {
            verticalAnchors = tieBreak(dx, dy, parentIncomingSide);
        }

        int endAnchorX = verticalAnchors ? endX : endX - bend(dx);
        int endAnchorY = verticalAnchors ? endY - bend(dy) : endY;

        int startAnchorX = verticalAnchors ? startX : endAnchorX;
        int startAnchorY = verticalAnchors ? endAnchorY : startY;

        Side exitSide;
        Side entrySide;
        if (verticalAnchors) {
            exitSide = compareSide(endAnchorY, startY, Side.BOTTOM, Side.TOP);
            entrySide = compareSide(endY, endAnchorY, Side.TOP, Side.BOTTOM);
        } else {
            exitSide = compareSide(endAnchorX, startX, Side.RIGHT, Side.LEFT);
            entrySide = compareSide(endX, endAnchorX, Side.LEFT, Side.RIGHT);
        }

        return new Route(startX, startY, startAnchorX, startAnchorY, endAnchorX, endAnchorY, endX, endY, verticalAnchors, exitSide, entrySide);
    }

    private static boolean tieBreak(int dx, int dy, Side parentIncomingSide) {
        switch (parentIncomingSide) {
            case TOP, BOTTOM -> {
                Side exitSide = dy > 0 ? Side.BOTTOM : Side.TOP;
                if (exitSide != parentIncomingSide) return true;
            }
            case LEFT, RIGHT -> {
                Side exit = dx > 0 ? Side.RIGHT : Side.LEFT;
                if (exit != parentIncomingSide) return false;
            }
            default -> {
            }
        }
        return Math.abs(dx) > Math.abs(dy);
    }

    private static int bend(int delta) {
        if (delta >= 0) {
            return Math.min(delta / 2, MAX_BEND_DISTANCE);
        }
        return -Math.min((-delta + 1) / 2, MAX_BEND_DISTANCE);
    }

    private static Side compareSide(int value, int against, Side ifGreater, Side ifLess) {
        if (value > against) return ifGreater;
        if (value < against) return ifLess;
        return Side.NONE;
    }

    public enum Side {
        NONE, LEFT, RIGHT, TOP, BOTTOM
    }

    public record Route(int startX, int startY,
                        int startAnchorX, int startAnchorY,
                        int endAnchorX, int endAnchorY,
                        int endX, int endY,
                        boolean verticalAnchors,
                        Side exitSide,
                        Side entrySide) {
        public boolean shouldShowArrow() {
            int alongStub = verticalAnchors
                ? Math.abs(endY - startY)
                : Math.abs(endX - startX);

            int acrossStub = verticalAnchors
                ? Math.abs(endX - startX)
                : Math.abs(endY - startY);

            if (alongStub > WIDGET_SIZE + 7) return true;
            return acrossStub == 0 && alongStub > WIDGET_SIZE + 2;
        }
    }
}
