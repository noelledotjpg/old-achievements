package com.nikohalfkino.achievements.render;

import net.minecraft.advancements.Advancement;

import java.util.LinkedHashSet;
import java.util.Set;

public record EditorOverlay(Advancement dragged, Set<Advancement> selected, int[] selectionRect, boolean showGrid) {

    public Set<Advancement> highlighted() {
        Set<Advancement> highlighted = new LinkedHashSet<>(selected);
        if (dragged != null) highlighted.add(dragged);
        return highlighted;
    }
}
