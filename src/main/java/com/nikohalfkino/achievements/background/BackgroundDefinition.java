package com.nikohalfkino.achievements.background;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class BackgroundDefinition {

    public record Range(int min, int max) {
        static final Range ANY = new Range(Integer.MIN_VALUE, Integer.MAX_VALUE);

        boolean contains(int value) {
            return value >= min && value <= max;
        }
    }

    private record Layer(List<Block> blocks, Range depth, Range row, float chance) {}

    private final List<Layer> layers;
    private final float topBrightness;
    private final float bottomBrightness;
    private final int brightnessRows;

    private BackgroundDefinition(List<Layer> layers, float topBrightness, float bottomBrightness, int brightnessRows) {
        this.layers = layers;
        this.topBrightness = topBrightness;
        this.bottomBrightness = bottomBrightness;
        this.brightnessRows = brightnessRows;
    }

    public static BackgroundDefinition parse(JsonObject root) {
        float top = 0.6F;
        float bottom = 0.3F;
        int rows = 25;
        if (root.has("brightness")) {
            JsonObject brightness = GsonHelper.getAsJsonObject(root, "brightness");
            top = Mth.clamp(GsonHelper.getAsFloat(brightness, "top", top), 0F, 1F);
            bottom = Mth.clamp(GsonHelper.getAsFloat(brightness, "bottom", bottom), 0F, 1F);
            rows = Math.max(1, GsonHelper.getAsInt(brightness, "rows", rows));
        }

        JsonArray layerArray = GsonHelper.getAsJsonArray(root, "layers");
        List<Layer> layers = new ArrayList<>();
        for (int i = 0; i < layerArray.size(); i++) {
            layers.add(parseLayer(GsonHelper.convertToJsonObject(layerArray.get(i), "layers[" + i + "]"), i));
        }
        if (layers.isEmpty()) throw new JsonParseException("\"layers\" must not be empty");

        // later layers take priority
        Collections.reverse(layers);

        return new BackgroundDefinition(layers, top, bottom, rows);
    }

    private static Layer parseLayer(JsonObject json, int index) {
        List<Block> blocks = new ArrayList<>();
        if (json.has("block")) {
            blocks.add(block(GsonHelper.getAsString(json, "block"), index));
        } else if (json.has("blocks")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "blocks")) {
                blocks.add(block(GsonHelper.convertToString(element, "blocks"), index));
            }
        }
        if (blocks.isEmpty()) {
            throw new JsonParseException("layers[" + index + "] needs \"block\" or a non-empty \"blocks\"");
        }

        float chance = Mth.clamp(GsonHelper.getAsFloat(json, "chance", 1F), 0F, 1F);
        return new Layer(blocks, range(json, "depth"), range(json, "row"), chance);
    }

    private static Block block(String id, int layerIndex) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null || !ForgeRegistries.BLOCKS.containsKey(location)) {
            throw new JsonParseException("layers[" + layerIndex + "]: unknown block '" + id + "'");
        }
        return ForgeRegistries.BLOCKS.getValue(location);
    }

    private static Range range(JsonObject json, String key) {
        if (!json.has(key)) return Range.ANY;

        JsonElement element = json.get(key);
        if (element.isJsonPrimitive()) {
            int value = GsonHelper.convertToInt(element, key);
            return new Range(value, value);
        }

        JsonObject range = GsonHelper.convertToJsonObject(element, key);
        return new Range(
                GsonHelper.getAsInt(range, "min", Integer.MIN_VALUE),
                GsonHelper.getAsInt(range, "max", Integer.MAX_VALUE));
    }

    public float brightness(int tileY) {
        float value = topBrightness + (bottomBrightness - topBrightness) * tileY / brightnessRows;
        return Mth.clamp(value, Math.min(topBrightness, bottomBrightness), Math.max(topBrightness, bottomBrightness));
    }

    public Block pick(int tileY, int depth, Random random) {
        for (Layer layer : layers) {
            if (!layer.depth().contains(depth) || !layer.row().contains(tileY)) continue;
            if (layer.chance() < 1F && random.nextFloat() >= layer.chance()) continue;
            List<Block> blocks = layer.blocks();
            return blocks.size() == 1 ? blocks.get(0) : blocks.get(random.nextInt(blocks.size()));
        }
        return null;
    }
}
