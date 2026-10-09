package com.nikohalfkino.achievements.background;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.nikohalfkino.achievements.ClassicAchievementsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class BackgroundDataLoader extends SimpleJsonResourceReloadListener {

    public static final ResourceLocation DEFAULT_ID = new ResourceLocation(ClassicAchievementsMod.MODID, "default");

    private static final Gson GSON = new GsonBuilder().setLenient().create();

    private static volatile Map<ResourceLocation, String> activeJsons = Map.of();

    public BackgroundDataLoader() {
        super(GSON, "advancements/background");
    }

    public static Map<ResourceLocation, String> activeJsons() {
        return activeJsons;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, String> valid = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            try {
                BackgroundDefinition.parse(entry.getValue().getAsJsonObject()); // validate only
                valid.put(entry.getKey(), entry.getValue().toString());
            } catch (Exception e) {
                ClassicAchievementsMod.LOGGER.error("Invalid achievement background {}: {}", entry.getKey(), e.getMessage());
            }
        }
        activeJsons = Map.copyOf(valid);
    }
}
