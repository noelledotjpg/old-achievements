package com.nikohalfkino.achievements.layout;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.loading.FMLPaths;

import java.util.LinkedHashMap;
import java.util.Map;

public class AdvancementLayoutConfig {

    private static final String CONFIG_FILE_NAME = "achievements_layout.toml";

    private final Map<String, int[]> savedPositions = new LinkedHashMap<>();
    private final Map<String, int[]> autoPositions = new LinkedHashMap<>();
    private final LayoutFile file;

    public AdvancementLayoutConfig() {
        file = new LayoutFile(FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE_NAME));
        savedPositions.putAll(file.read());
    }

    public void sync(Map<Advancement, int[]> autoLayout) {
        autoPositions.clear();
        boolean dirty = false;

        for (Map.Entry<Advancement, int[]> entry : autoLayout.entrySet()) {
            String id = getHolder(entry.getKey()).id().toString();
            autoPositions.put(id, entry.getValue().clone());
            if (savedPositions.putIfAbsent(id, entry.getValue().clone()) == null) {
                dirty = true;
            }
        }

        if (dirty) save();
    }

    public int[] getPosition(Advancement advancement) {
        int[] position = savedPositions.get(getHolder(advancement).id().toString());
        return position == null ? null : position.clone();
    }

    public void setPosition(Advancement advancement, int[] position) {
        savedPositions.put(getHolder(advancement).id().toString(), position.clone());
        save();
    }

    public void setPositions(Map<Advancement, int[]> positions) {
        for (Map.Entry<Advancement, int[]> entry : positions.entrySet()) {
            savedPositions.put(getHolder(entry.getKey()).id().toString(), entry.getValue().clone());
        }
        save();
    }

    private void save() {
        file.write(savedPositions, autoPositions);
    }

    private AdvancementHolder getHolder(Advancement advancement) {
        return Minecraft.getInstance().getConnection().getAdvancements().getTree().nodes().stream().filter(node -> node.holder().value() == advancement).findFirst().orElse(null).holder();
    }
}
