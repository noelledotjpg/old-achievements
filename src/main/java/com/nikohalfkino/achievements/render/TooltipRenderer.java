package com.nikohalfkino.achievements.render;

import com.nikohalfkino.achievements.state.AdvancementStateHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

final class TooltipRenderer {

    private static final int UNKNOWN_TITLE_DEPTH = 3;
    private static final int DESCRIPTION_WIDTH = 120;

    private static final int DESCRIPTION_COLOR = 0xFFA0A0A0;
    private static final int TAKEN_COLOR = 0xFF9090FF;
    private static final int REQUIRES_COLOR = 0xFF705050;

    private final Font font;
    private final AdvancementStateHelper stateHelper;

    TooltipRenderer(Font font, AdvancementStateHelper stateHelper) {
        this.font = font;
        this.stateHelper = stateHelper;
    }

    void render(GuiGraphics g, Advancement advancement, int mouseX, int mouseY) {
        DisplayInfo display = advancement.getDisplay();
        boolean done = stateHelper.isUnlocked(advancement);
        boolean available = done || stateHelper.canUnlock(advancement);
        boolean challenge = display.getFrame() == FrameType.CHALLENGE;
        int depth = stateHelper.getRequirementCount(advancement);

        int titleColor = available ? (challenge ? 0xFFFFFF80 : 0xFFFFFFFF) : (challenge ? 0xFF808040 : 0xFF808080);
        String title = (depth == UNKNOWN_TITLE_DEPTH && !available)
                ? Component.translatable("achievement.unknown").getString()
                : display.getTitle().getString();

        List<FormattedCharSequence> descriptionLines =
                available ? font.split(display.getDescription(), DESCRIPTION_WIDTH) : List.of();

        String parentTitle = null;
        Advancement parent = advancement.getParent();
        if (parent != null && parent.getDisplay() != null) {
            parentTitle = parent.getDisplay().getTitle().getString();
        }
        boolean showRequires = !available && parentTitle != null && depth <= UNKNOWN_TITLE_DEPTH;

        int height = 12 + descriptionLines.size() * 9 + (done ? 12 : (showRequires ? 22 : 0));
        int width = Math.max(font.width(title), DESCRIPTION_WIDTH);

        g.pose().pushPose();
        g.pose().translate(0, 0, 400);
        g.fillGradient(mouseX + 9, mouseY - 7, mouseX + width + 15, mouseY + height + 3, 0xC0101010, 0xC0101010);
        g.drawString(font, title, mouseX + 12, mouseY, titleColor, true);

        int lineY = mouseY + 12;
        for (FormattedCharSequence line : descriptionLines) {
            g.drawString(font, line, mouseX + 12, lineY, DESCRIPTION_COLOR, false);
            lineY += 9;
        }

        if (done) {
            g.drawString(font, Component.translatable("achievement.taken"), mouseX + 12, lineY + 4, TAKEN_COLOR, false);
        } else if (showRequires) {
            g.drawString(font, Component.translatable("achievement.requires.text"), mouseX + 12, lineY + 4, REQUIRES_COLOR, false);
            g.drawString(font, "'" + parentTitle + "'", mouseX + 12, lineY + 13, REQUIRES_COLOR, false);
        }
        g.pose().popPose();
    }
}
