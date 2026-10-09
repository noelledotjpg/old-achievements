package com.nikohalfkino.achievements.background;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nikohalfkino.achievements.ClassicAchievementsMod;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class BackgroundManager {

    private static final String BUNDLED_PATH = "/data/achievements/advancements/background/default.json";
    private static final String FALLBACK_JSON = "{\"layers\":[{\"block\":\"minecraft:stone\"}]}";

    private static volatile Map<ResourceLocation, BackgroundDefinition> synced = Map.of();
    private static BackgroundDefinition bundled;

    private BackgroundManager() {}

    public static BackgroundDefinition get(ResourceLocation rootId) {
        Map<ResourceLocation, BackgroundDefinition> definitions = synced;
        if (rootId != null) {
            BackgroundDefinition forRoot = definitions.get(rootId);
            if (forRoot != null) return forRoot;
        }
        BackgroundDefinition syncedDefault = definitions.get(BackgroundDataLoader.DEFAULT_ID);
        return syncedDefault != null ? syncedDefault : bundled();
    }

    public static void setSynced(Map<ResourceLocation, String> jsons) {
        Map<ResourceLocation, BackgroundDefinition> parsed = new HashMap<>();
        for (Map.Entry<ResourceLocation, String> entry : jsons.entrySet()) {
            try {
                parsed.put(entry.getKey(), BackgroundDefinition.parse(
                        JsonParser.parseString(entry.getValue()).getAsJsonObject()));
            } catch (Exception e) {
                ClassicAchievementsMod.LOGGER.error("Invalid synced achievement background {}", entry.getKey(), e);
            }
        }
        synced = parsed;
    }

    public static void clearSynced() {
        synced = Map.of();
    }

    private static synchronized BackgroundDefinition bundled() {
        if (bundled == null) {
            try (InputStream in = BackgroundManager.class.getResourceAsStream(BUNDLED_PATH)) {
                if (in == null) throw new IllegalStateException("missing " + BUNDLED_PATH);
                JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                bundled = BackgroundDefinition.parse(json);
            } catch (Exception e) {
                ClassicAchievementsMod.LOGGER.error("Failed to load bundled achievement background", e);
                bundled = BackgroundDefinition.parse(JsonParser.parseString(FALLBACK_JSON).getAsJsonObject());
            }
        }
        return bundled;
    }
}
