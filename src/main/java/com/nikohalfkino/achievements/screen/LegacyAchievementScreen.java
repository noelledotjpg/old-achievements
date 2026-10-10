package com.nikohalfkino.achievements.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nikohalfkino.achievements.OldAchievementsConfig;
import com.nikohalfkino.achievements.layout.AdvancementLayoutConfig;
import com.nikohalfkino.achievements.layout.AdvancementLayoutEngine;
import com.nikohalfkino.achievements.render.AchievementRenderer;
import com.nikohalfkino.achievements.render.AchievementTextures;
import com.nikohalfkino.achievements.render.EditorOverlay;
import com.nikohalfkino.achievements.render.TreeView;
import com.nikohalfkino.achievements.state.AdvancementStateHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class LegacyAchievementScreen extends Screen implements ClientAdvancements.Listener {

    private static final String[] HINTS = {"F: Fullscreen", "Scroll: Zoom", "Drag: Move"};
    private static final int HINT_GAP = 12;

    private final ClientAdvancements clientAdvancements;
    private final AdvancementStateHelper stateHelper = new AdvancementStateHelper();
    private final AdvancementLayoutConfig layoutConfig = new AdvancementLayoutConfig();
    private final AdvancementLayoutEngine layoutEngine = new AdvancementLayoutEngine(layoutConfig, stateHelper);
    private final AdvancementPages pages;
    private final ScrollView scroll = new ScrollView();
    private final NodeEditor editor;
    private final AchievementRenderer renderer;

    private boolean fullscreen;
    private boolean panning;
    private boolean panMoved;

    public LegacyAchievementScreen(ClientAdvancements clientAdvancements) {
        super(Component.literal("Achievements"));
        this.clientAdvancements = clientAdvancements;
        this.pages = new AdvancementPages(clientAdvancements, stateHelper);
        this.editor = new NodeEditor(layoutEngine, layoutConfig, stateHelper, scroll);
        this.renderer = new AchievementRenderer(Minecraft.getInstance().font, stateHelper, id -> Minecraft.getInstance().getConnection().getAdvancements().get(id));
    }

    private FrameGeometry frame() {
        return new FrameGeometry(width, height, fullscreen);
    }

    @Override
    protected void init() {
        clientAdvancements.setListener(this);
        stateHelper.setDebugReveal(editor.isActive());
        pages.refresh();
        rebuildButtons();
        updateLayout();
    }

    @Override
    public void removed() {
        clientAdvancements.setListener(null);
        renderer.close();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    private void rebuildButtons() {
        clearWidgets();
        FrameGeometry frame = frame();

        addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), button -> onClose())
                        .bounds(frame.left() + frame.width() - 115, frame.top() + frame.height() - 28, 100, 20)
                        .build());

        if (pages.count() > 1 && !OldAchievementsConfig.HIDE_PAGE.get()) {
            addRenderableWidget(
                    Button.builder(pages.currentName(), button -> {
                        pages.next();
                        editor.clearHistory();
                        button.setMessage(pages.currentName());
                        updateLayout();
                    }).bounds(frame.left() + 15, frame.top() + frame.height() - 28, 125, 20).build());
        }
    }

    private void updateLayout() {
        layoutEngine.buildLayout(pages.current().value());
        centerOnRoot();
    }

    private void centerOnRoot() {
        if (pages.isEmpty()) return;

        int[] position = layoutEngine.positions().get(pages.current());
        if (position == null) return;

        FrameGeometry frame = frame();
        scroll.jumpTo(position[0] - frame.viewportW() / 2.0, position[1] - frame.viewportH() / 2.0);
        scroll.clampTarget(layoutEngine);
    }

    private void toggleEditor() {
        editor.toggle();
        panning = false;
        stateHelper.setDebugReveal(editor.isActive());
        pages.refreshKeepingPage();
        rebuildButtons();
        layoutEngine.buildLayout(pages.current().value());
        scroll.clampTarget(layoutEngine);
    }

    private int toViewportX(FrameGeometry frame, double mouseX) {
        return (int) ((mouseX - frame.viewportX()) * scroll.zoom());
    }

    private int toViewportY(FrameGeometry frame, double mouseY) {
        return (int) ((mouseY - frame.viewportY()) * scroll.zoom());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

        if (keyCode == GLFW.GLFW_KEY_D && ctrl && shift && OldAchievementsConfig.DEBUG_MODE.get()) {
            toggleEditor();
            return true;
        }

        if (editor.isActive() && editor.keyPressed(keyCode, ctrl, shift)) return true;

        if (keyCode == GLFW.GLFW_KEY_F) {
            fullscreen = !fullscreen;
            renderer.close();
            rebuildButtons();
            updateLayout();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double ScrollY) {
        FrameGeometry frame = frame();
        scroll.zoomBy(ScrollY, frame.viewportW(), frame.viewportH());
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        FrameGeometry frame = frame();
        if (!frame.containsInViewport(mouseX, mouseY)) return false;

        if (button == 0 && editor.isActive()
                && editor.mouseClicked(toViewportX(frame, mouseX), toViewportY(frame, mouseY),
                hasControlDown(), hasShiftDown())) {
            return true;
        }

        if (button == 0 || button == 1) {
            panning = true;
            panMoved = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            if (panning && !panMoved && button == 0 && editor.isActive() && !hasControlDown()) {
                editor.clearSelection();
            }
            panning = false;
        }

        if (button == 0 && editor.mouseReleased()) return true;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        FrameGeometry frame = frame();

        if (button == 0 && editor.isActive()
                && editor.mouseDragged(toViewportX(frame, mouseX), toViewportY(frame, mouseY), dx, dy)) {
            return true;
        }

        if (panning && (button == 0 || button == 1)) {
            panMoved = true;
            scroll.panBy(dx, dy);
            scroll.clampTarget(layoutEngine);
            return true;
        }
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        scroll.animate();
        FrameGeometry frame = frame();

        Advancement hovered = renderViewport(g, frame, mouseX, mouseY);
        renderFrameAndTitle(g, frame);

        g.pose().pushPose();
        g.pose().translate(0, 0, 210);
        super.render(g, mouseX, mouseY, partialTick);
        g.pose().popPose();

        if (editor.isActive() && !editor.isSnapEnabled()) {
            String message = "Snap: OFF";
            g.pose().pushPose();
            g.pose().translate(0, 0, 210);
            g.drawString(font, message, frame.left() + frame.width() - 15 - font.width(message),
                    frame.top() + 5, 0xFFFFAA00, true);
            g.pose().popPose();
        }

        if (hovered != null && !editor.isDragging()) {
            renderer.renderTooltip(g, hovered, mouseX, mouseY);
        }

        renderStatusLines(g, frame);
    }

    private Advancement renderViewport(GuiGraphics g, FrameGeometry frame, int mouseX, int mouseY) {
        int viewportX = frame.viewportX();
        int viewportY = frame.viewportY();
        int viewportW = frame.viewportW();
        int viewportH = frame.viewportH();
        float zoom = scroll.zoom();

        g.pose().pushPose();
        g.pose().translate(viewportX, viewportY, 0);
        g.enableScissor(viewportX, viewportY, viewportX + viewportW, viewportY + viewportH);
        g.pose().pushPose();
        g.pose().scale(1f / zoom, 1f / zoom, 1f);

        renderer.renderBackground(g, scroll.x(), scroll.y(), zoom, viewportW, viewportH, pages.currentId());

        EditorOverlay overlay = editor.isActive() ? editor.overlay() : null;
        Advancement hovered = renderer.renderTree(g, new TreeView(
                layoutEngine.positions(),
                scroll.x(), scroll.y(), zoom,
                toViewportX(frame, mouseX), toViewportY(frame, mouseY),
                viewportW, viewportH,
                overlay));

        g.pose().popPose();
        g.disableScissor();
        g.pose().popPose();
        return hovered;
    }

    private void renderFrameAndTitle(GuiGraphics g, FrameGeometry frame) {
        RenderSystem.enableBlend();
        g.pose().pushPose();
        g.pose().translate(0, 0, 200);

        if (!frame.fullscreen()) {
            g.blit(AchievementTextures.ACHIEVEMENT_BG, frame.left(), frame.top(), 0, 0,
                    FrameGeometry.WIDTH, FrameGeometry.HEIGHT);
        }

        String title = Component.translatable("gui.achievements").getString();
        if (OldAchievementsConfig.SHOW_ACHIEVEMENT_COUNT.get()) {
            AdvancementStateHelper.AchievementCount count =
                    stateHelper.countAchievements(clientAdvancements.getTree().nodes().stream().map(advancementNode -> advancementNode.holder().value()).toList());
            title += " - " + count.unlocked() + "/" + count.total();
        }

        if (frame.fullscreen()) {
            g.drawString(font, title, frame.left() + 15, frame.top() + 5, 0xFFFFFFFF, true);
        } else {
            g.drawString(font, title, frame.left() + 15, frame.top() + 5, 0x404040, false);
        }
        g.pose().popPose();
    }

    private void renderStatusLines(GuiGraphics g, FrameGeometry frame) {
        int y = frame.top() + frame.height() + 4;

        if (!frame.fullscreen() && OldAchievementsConfig.SHOW_HINTS.get()) {
            int totalWidth = HINT_GAP * (HINTS.length - 1);
            for (String hint : HINTS) totalWidth += font.width(hint);

            int x = frame.left() + (FrameGeometry.WIDTH - totalWidth) / 2;
            for (String hint : HINTS) {
                g.drawString(font, hint, x, y, 0xFFFFFFFF, true);
                x += font.width(hint) + HINT_GAP;
            }
            y += font.lineHeight + 2;
        }

        if (editor.isActive() && editor.selectedCount() > 0) {
            String message = editor.selectedCount() == 1
                    ? "1 node selected"
                    : editor.selectedCount() + " nodes selected";
            g.drawString(font, message, frame.left() + 15, y, 0xFFFFFF00, true);
        }
    }

    @Override
    public void onUpdateAdvancementProgress(AdvancementNode advancement, AdvancementProgress progress) {
        stateHelper.onProgress(advancement.advancement(), progress);
    }

    @Override
    public void onSelectedTabChanged(AdvancementHolder advancement) {}

    @Override
    public void onAddAdvancementRoot(AdvancementNode advancement) {
        pages.refresh();
        updateLayout();
    }

    @Override
    public void onRemoveAdvancementRoot(AdvancementNode advancement) {
        pages.refresh();
        updateLayout();
    }

    @Override
    public void onAddAdvancementTask(AdvancementNode advancement) {}

    @Override
    public void onRemoveAdvancementTask(AdvancementNode advancement) {}

    @Override
    public void onAdvancementsCleared() {
        stateHelper.clear();
        pages.refresh();
        updateLayout();
    }
}
