package com.nikohalfkino.achievements.screen;

import com.nikohalfkino.achievements.ClassicAchievementsConfig;
import com.nikohalfkino.achievements.layout.AdvancementLayoutConfig;
import com.nikohalfkino.achievements.layout.AdvancementLayoutEngine;
import com.nikohalfkino.achievements.render.EditorOverlay;
import com.nikohalfkino.achievements.state.AdvancementStateHelper;
import net.minecraft.advancements.Advancement;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.nikohalfkino.achievements.layout.NodeMetrics.CENTER;
import static com.nikohalfkino.achievements.layout.NodeMetrics.GRID_SIZE;
import static com.nikohalfkino.achievements.layout.NodeMetrics.SIZE;

final class NodeEditor {

    private static boolean enabled = true;
    private static boolean showGrid = true;
    private static boolean snapEnabled = true;

    private final AdvancementLayoutEngine layoutEngine;
    private final AdvancementLayoutConfig layoutConfig;
    private final AdvancementStateHelper stateHelper;
    private final ScrollView scroll;
    private final EditHistory history = new EditHistory();

    private final Set<Advancement> selected = new LinkedHashSet<>();
    private Advancement dragged;
    private double dragWorldX;
    private double dragWorldY;
    private int[] dragStartPosition;
    private Map<Advancement, int[]> groupDragOrigins;
    private int[] selectionRect;

    NodeEditor(AdvancementLayoutEngine layoutEngine, AdvancementLayoutConfig layoutConfig,
               AdvancementStateHelper stateHelper, ScrollView scroll) {
        this.layoutEngine = layoutEngine;
        this.layoutConfig = layoutConfig;
        this.stateHelper = stateHelper;
        this.scroll = scroll;
    }

    boolean isActive() {
        return ClassicAchievementsConfig.DEBUG_MODE.get() && enabled;
    }

    void toggle() {
        enabled = !enabled;
        dragged = null;
        groupDragOrigins = null;
        selectionRect = null;
        selected.clear();
    }

    void clearSelection() {
        selected.clear();
    }

    void clearHistory() {
        history.clear();
        dragStartPosition = null;
    }

    boolean isDragging() {
        return dragged != null || selectionRect != null;
    }

    int selectedCount() {
        return selected.size();
    }

    boolean isSnapEnabled() {
        return snapEnabled;
    }

    EditorOverlay overlay() {
        return new EditorOverlay(dragged, selected, selectionRect, showGrid);
    }

    boolean keyPressed(int keyCode, boolean ctrl, boolean shift) {
        if (ctrl && (keyCode == GLFW.GLFW_KEY_Z || keyCode == GLFW.GLFW_KEY_Y)) {
            if (!isDragging()) {
                if (keyCode == GLFW.GLFW_KEY_Y || shift) applyHistory(history.redo());
                else applyHistory(history.undo());
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_G) {
            if (shift) snapEnabled = !snapEnabled;
            else showGrid = !showGrid;
            return true;
        }

        int dx = 0;
        int dy = 0;
        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> dx = -1;
            case GLFW.GLFW_KEY_RIGHT -> dx = 1;
            case GLFW.GLFW_KEY_UP -> dy = -1;
            case GLFW.GLFW_KEY_DOWN -> dy = 1;
            default -> { }
        }
        return (dx != 0 || dy != 0) && nudgeSelection(dx, dy);
    }

    boolean mouseClicked(int viewX, int viewY, boolean ctrl, boolean shift) {
        Map<Advancement, int[]> positions = layoutEngine.positions();
        Advancement clicked = nodeAt(positions, viewX, viewY);

        if (clicked != null) {
            if (ctrl) {
                if (!selected.remove(clicked)) selected.add(clicked);
                groupDragOrigins = null;
                return true;
            }

            int[] position = positions.get(clicked);
            if (selected.contains(clicked) && selected.size() > 1) {
                groupDragOrigins = new HashMap<>();
                for (Advancement node : selected) {
                    int[] nodePosition = positions.get(node);
                    if (nodePosition != null) groupDragOrigins.put(node, nodePosition.clone());
                }
            } else {
                selected.clear();
                groupDragOrigins = null;
                dragStartPosition = position.clone();
            }
            dragged = clicked;
            dragWorldX = position[0];
            dragWorldY = position[1];
            return true;
        }

        if (shift) {
            selectionRect = new int[]{viewX, viewY, viewX, viewY};
            selected.clear();
            groupDragOrigins = null;
            return true;
        }

        groupDragOrigins = null;
        return false;
    }

    boolean mouseDragged(int viewX, int viewY, double dx, double dy) {
        if (selectionRect != null) {
            selectionRect[2] = viewX;
            selectionRect[3] = viewY;
            return true;
        }
        if (dragged == null) return false;

        dragWorldX += dx * scroll.zoom();
        dragWorldY += dy * scroll.zoom();
        Map<Advancement, int[]> positions = layoutEngine.positions();

        if (groupDragOrigins != null) {
            int[] origin = groupDragOrigins.get(dragged);
            double deltaX = dragWorldX - origin[0];
            double deltaY = dragWorldY - origin[1];
            for (Advancement node : selected) {
                int[] nodeOrigin = groupDragOrigins.get(node);
                int[] position = positions.get(node);
                if (nodeOrigin == null || position == null) continue;
                position[0] = snap((int) Math.round(nodeOrigin[0] + deltaX));
                position[1] = snap((int) Math.round(nodeOrigin[1] + deltaY));
            }
        } else {
            int[] position = positions.get(dragged);
            position[0] = snap((int) Math.round(dragWorldX));
            position[1] = snap((int) Math.round(dragWorldY));
        }
        return true;
    }

    boolean mouseReleased() {
        if (dragged != null && groupDragOrigins != null) {
            finishGroupDrag();
            return true;
        }
        if (dragged != null) {
            finishSingleDrag();
            return true;
        }
        if (selectionRect != null) {
            applySelectionRect();
            selectionRect = null;
            return true;
        }
        return false;
    }

    private void finishGroupDrag() {
        Map<Advancement, int[]> positions = layoutEngine.positions();

        Map<Advancement, int[]> moved = new HashMap<>();
        for (Advancement node : selected) {
            int[] position = positions.get(node);
            if (position == null) continue;
            position[0] = snap(position[0]);
            position[1] = snap(position[1]);
            moved.put(node, position.clone());
        }
        layoutConfig.setPositions(moved);

        Map<Advancement, int[]> before = new HashMap<>();
        Map<Advancement, int[]> after = new HashMap<>();
        for (Map.Entry<Advancement, int[]> origin : groupDragOrigins.entrySet()) {
            int[] position = positions.get(origin.getKey());
            if (position == null) continue;
            before.put(origin.getKey(), origin.getValue().clone());
            after.put(origin.getKey(), position.clone());
        }
        history.record(before, after);

        refreshBounds();
        groupDragOrigins = null;
        dragged = null;
    }

    private void finishSingleDrag() {
        int[] finalPosition = {snap((int) Math.round(dragWorldX)), snap((int) Math.round(dragWorldY))};
        int[] position = layoutEngine.positions().get(dragged);
        position[0] = finalPosition[0];
        position[1] = finalPosition[1];
        layoutConfig.setPosition(dragged, finalPosition);

        if (dragStartPosition != null) {
            history.record(Map.of(dragged, dragStartPosition), Map.of(dragged, finalPosition));
            dragStartPosition = null;
        }

        refreshBounds();
        selected.clear();
        selected.add(dragged);
        dragged = null;
    }

    private void applySelectionRect() {
        int x1 = Math.min(selectionRect[0], selectionRect[2]);
        int y1 = Math.min(selectionRect[1], selectionRect[3]);
        int x2 = Math.max(selectionRect[0], selectionRect[2]);
        int y2 = Math.max(selectionRect[1], selectionRect[3]);

        selected.clear();
        for (Map.Entry<Advancement, int[]> entry : layoutEngine.positions().entrySet()) {
            if (!stateHelper.isNodeDrawn(entry.getKey())) continue;

            int centerX = entry.getValue()[0] - (int) scroll.x() + CENTER;
            int centerY = entry.getValue()[1] - (int) scroll.y() + CENTER;
            if (centerX >= x1 && centerX <= x2 && centerY >= y1 && centerY <= y2) {
                selected.add(entry.getKey());
            }
        }
    }

    private Advancement nodeAt(Map<Advancement, int[]> positions, int viewX, int viewY) {
        for (Map.Entry<Advancement, int[]> entry : positions.entrySet()) {
            if (stateHelper.isHiddenFromScreen(entry.getKey())) continue;

            int nodeX = entry.getValue()[0] - (int) scroll.x();
            int nodeY = entry.getValue()[1] - (int) scroll.y();
            if (viewX >= nodeX && viewX <= nodeX + SIZE && viewY >= nodeY && viewY <= nodeY + SIZE) {
                return entry.getKey();
            }
        }
        return null;
    }

    private boolean nudgeSelection(int dx, int dy) {
        if (selected.isEmpty()) return false;

        Map<Advancement, int[]> positions = layoutEngine.positions();
        Map<Advancement, int[]> before = new HashMap<>();
        Map<Advancement, int[]> moved = new HashMap<>();
        int step = snapEnabled ? GRID_SIZE : 1;

        for (Advancement node : selected) {
            int[] position = positions.get(node);
            if (position == null) continue;
            before.put(node, position.clone());
            position[0] = snap(position[0] + dx * step);
            position[1] = snap(position[1] + dy * step);
            moved.put(node, position.clone());
        }
        if (moved.isEmpty()) return false;

        history.recordNudge(before, moved);
        layoutConfig.setPositions(moved);
        refreshBounds();
        return true;
    }

    private void applyHistory(Map<Advancement, int[]> restored) {
        if (restored == null) return;

        Map<Advancement, int[]> positions = layoutEngine.positions();
        selected.clear();
        groupDragOrigins = null;
        for (Map.Entry<Advancement, int[]> entry : restored.entrySet()) {
            int[] position = positions.get(entry.getKey());
            if (position == null) continue;
            position[0] = entry.getValue()[0];
            position[1] = entry.getValue()[1];
            selected.add(entry.getKey());
        }
        layoutConfig.setPositions(restored);
        refreshBounds();
    }

    private void refreshBounds() {
        layoutEngine.recalcScrollBounds();
        scroll.clampTarget(layoutEngine);
    }

    private static int snap(int value) {
        if (!snapEnabled) return value;
        return Math.round((float) value / GRID_SIZE) * GRID_SIZE;
    }
}
