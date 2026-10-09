package com.nikohalfkino.achievements.render;

import com.nikohalfkino.achievements.state.AdvancementStateHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.nikohalfkino.achievements.layout.NodeMetrics.GRID_SIZE;

final class EditorOverlayRenderer {

    static final int SELECT_BLUE = 0xFF3399FF;

    private static final int GRID_COLOR = 0x22FFFFFF;
    private static final int RECT_FILL = 0x3300AAFF;
    private static final int RECT_BORDER = 0xFF00AAFF;

    private final Font font;
    private final AdvancementStateHelper stateHelper;

    EditorOverlayRenderer(Font font, AdvancementStateHelper stateHelper) {
        this.font = font;
        this.stateHelper = stateHelper;
    }

    void renderGrid(GuiGraphics g, TreeView view) {
        int gridW = (int) (view.viewportW() * view.zoom());
        int gridH = (int) (view.viewportH() * view.zoom());
        int offsetX = Math.floorMod((int) view.scrollX(), GRID_SIZE);
        int offsetY = Math.floorMod((int) view.scrollY(), GRID_SIZE);

        for (int x = -offsetX; x < gridW; x += GRID_SIZE) g.vLine(x, 0, gridH, GRID_COLOR);
        for (int y = -offsetY; y < gridH; y += GRID_SIZE) g.hLine(0, gridW, y, GRID_COLOR);
    }

    void renderDistanceLabels(GuiGraphics g, TreeView view, Set<Advancement> highlighted) {
        Map<Advancement, int[]> positions = view.positions();

        for (Advancement advancement : highlighted) {
            Advancement parent = advancement.getParent();
            int[] pos = positions.get(advancement);
            int[] parentPos = parent == null ? null : positions.get(parent);
            if (pos == null || parentPos == null || stateHelper.isHiddenFromScreen(advancement)) continue;

            int dx = Math.abs(pos[0] - parentPos[0]);
            int dy = Math.abs(pos[1] - parentPos[1]);
            double tiles = (dx + dy) / (double) GRID_SIZE;
            String label = tiles == Math.rint(tiles)
                    ? String.valueOf((int) tiles)
                    : String.format(Locale.ROOT, "%.1f", tiles);

            g.drawString(font, label, view.screenX(pos) + 30, view.screenY(pos) + 9, SELECT_BLUE, true);
        }
    }

    void renderSelectionRect(GuiGraphics g, int[] rect) {
        int x1 = Math.min(rect[0], rect[2]);
        int y1 = Math.min(rect[1], rect[3]);
        int x2 = Math.max(rect[0], rect[2]);
        int y2 = Math.max(rect[1], rect[3]);

        g.fill(x1, y1, x2, y2, RECT_FILL);
        g.hLine(x1, x2, y1, RECT_BORDER);
        g.hLine(x1, x2, y2, RECT_BORDER);
        g.vLine(x1, y1, y2, RECT_BORDER);
        g.vLine(x2, y1, y2, RECT_BORDER);
    }
}
