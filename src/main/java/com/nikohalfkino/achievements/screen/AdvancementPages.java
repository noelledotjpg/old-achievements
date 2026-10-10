package com.nikohalfkino.achievements.screen;

import com.nikohalfkino.achievements.state.AdvancementStateHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

final class AdvancementPages {

    private final ClientAdvancements clientAdvancements;
    private final AdvancementStateHelper stateHelper;
    private final List<AdvancementHolder> roots = new ArrayList<>();
    private int index;

    AdvancementPages(ClientAdvancements clientAdvancements, AdvancementStateHelper stateHelper) {
        this.clientAdvancements = clientAdvancements;
        this.stateHelper = stateHelper;
    }

    void refresh() {
        roots.clear();
        clientAdvancements.getTree().roots().forEach(advancementNode -> {
            AdvancementHolder holder = advancementNode.holder();
            Advancement advancement = holder.value();
            if (advancement.parent().isEmpty() && !stateHelper.isHiddenFromScreen(advancement)) {
                roots.add(holder);
            }
        });

        for (int i = 0; i < roots.size(); i++) {
            if (isStoryRoot(clientAdvancements.getTree().get(roots.get(i).id()).holder())) {
                index = i;
                break;
            }
        }

        if (index >= roots.size()) index = 0;
    }

    void refreshKeepingPage() {
        AdvancementHolder previous = current();
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

    AdvancementHolder current() {
        return roots.isEmpty() ? null : roots.get(index);
    }

    ResourceLocation currentId() {
        AdvancementHolder current = current();
        return current == null ? null : current.id();
    }

    Component currentName() {
        AdvancementHolder current = current();
        if (current == null || isStoryRoot(current)) return Component.literal("Minecraft");
        return current.value().display().get().getTitle();
    }

    private static boolean isStoryRoot(AdvancementHolder advancement) {
        return "minecraft".equals(advancement.id().getNamespace())
                && advancement.id().getPath().contains("story/");
    }
}
