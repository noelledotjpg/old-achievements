package com.nikohalfkino.achievements.render;

import com.nikohalfkino.achievements.state.AdvancementStateHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;
import java.util.function.Function;

public class AchievementRenderer {

    private final BackgroundRenderer background = new BackgroundRenderer();
    private final ConnectorRenderer connectors;
    private final NodeRenderer nodes;
    private final EditorOverlayRenderer overlays;
    private final TooltipRenderer tooltips;

    public AchievementRenderer(Font font, AdvancementStateHelper stateHelper, Function<ResourceLocation, AdvancementHolder> advHolderFunction) {
        connectors = new ConnectorRenderer(stateHelper, advHolderFunction);
        nodes = new NodeRenderer(stateHelper);
        overlays = new EditorOverlayRenderer(font, stateHelper, advHolderFunction);
        tooltips = new TooltipRenderer(font, stateHelper, advHolderFunction);
    }

    public void close() {
        background.close();
    }

    public void renderBackground(GuiGraphics g, double scrollX, double scrollY, float zoom,
                                 int viewportW, int viewportH, ResourceLocation rootId) {
        background.render(g, scrollX, scrollY, zoom, viewportW, viewportH, rootId);
    }

    public Advancement renderTree(GuiGraphics g, TreeView view) {
        EditorOverlay overlay = view.overlay();
        Set<Advancement> highlighted = overlay == null ? Set.of() : overlay.highlighted();

        if (overlay != null && overlay.showGrid()) overlays.renderGrid(g, view);

        connectors.render(g, view, highlighted);
        Advancement hovered = nodes.render(g, view);

        if (overlay != null) {
            overlays.renderDistanceLabels(g, view, highlighted);
            if (overlay.selectionRect() != null) overlays.renderSelectionRect(g, overlay.selectionRect());
        }

        return hovered;
    }

    public void renderTooltip(GuiGraphics g, Advancement advancement, int mouseX, int mouseY) {
        tooltips.render(g, advancement, mouseX, mouseY);
    }
}
