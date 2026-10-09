package com.nikohalfkino.achievements.screen;

import com.nikohalfkino.achievements.state.AdvancementStateHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

final class AdvancementPages {

    private final ClientAdvancements clientAdvancements;
    private final AdvancementStateHelper stateHelper;
    private final List<Advancement> roots = new ArrayList<>();
    private int index;

    AdvancementPages(ClientAdvancements clientAdvancements, AdvancementStateHelper stateHelper) {
        this.clientAdvancements = clientAdvancements;
        this.stateHelper = stateHelper;
    }

    void refresh() {
        roots.clear();
        for (Advancement advancement : clientAdvancements.getAdvancements().getAllAdvancements()) {
            if (advancement.getParent() == null && !stateHelper.isHiddenFromScreen(advancement)) {
                roots.add(advancement);
            }
        }

        for (int i = 0; i < roots.size(); i++) {
            if (isStoryRoot(roots.get(i))) {
                index = i;
                break;
            }
        }

        if (index >= roots.size()) index = 0;
    }

    void refreshKeepingPage() {
        Advancement previous = current();
        refresh();
        int previousIndex = previous == null ? -1 : roots.indexOf(previous);
        if (previousIndex >= 0) index = previousIndex;
    }

    void next() {
        index = (index + 1) % roots.size();
    }

    int count() {
        return roots.size();
    }

    boolean isEmpty() {
        return roots.isEmpty();
    }

    Advancement current() {
        return roots.isEmpty() ? null : roots.get(index);
    }

    ResourceLocation currentId() {
        Advancement current = current();
        return current == null ? null : current.getId();
    }

    Component currentName() {
        Advancement current = current();
        if (current == null || isStoryRoot(current)) return Component.literal("Minecraft");
        return current.getDisplay().getTitle();
    }

    private static boolean isStoryRoot(Advancement advancement) {
        return "minecraft".equals(advancement.getId().getNamespace())
                && advancement.getId().getPath().contains("story/");
    }
}
