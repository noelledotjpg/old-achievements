package com.nikohalfkino.achievements.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.nikohalfkino.achievements.ClassicAchievementsMod;
import com.nikohalfkino.achievements.background.BackgroundDefinition;
import com.nikohalfkino.achievements.background.BackgroundManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import static com.nikohalfkino.achievements.render.BackgroundTileBaker.TILE;

final class BackgroundRenderer {

    private static final ResourceLocation TEXTURE_ID =
            new ResourceLocation(ClassicAchievementsMod.MODID, "background_baked");

    private static final int WORLD_OFFSET = 288;
    private static final int PAD_TILES = 2;
    private static final int MAX_TILES = 512;
    private static final double MAX_ZOOM = 2.0;

    private DynamicTexture texture;
    private int textureW;
    private int textureH;
    private int originTileX;
    private int originTileY;
    private int bakedSalt;
    private BackgroundDefinition bakedDefinition;
    private int bakedViewportW = -1;
    private int bakedViewportH = -1;

    void close() {
        if (texture != null) {
            texture.close();
            texture = null;
        }
    }

    void render(GuiGraphics g, double scrollX, double scrollY, float zoom,
                int viewportW, int viewportH, ResourceLocation rootId) {
        Minecraft mc = Minecraft.getInstance();
        BackgroundDefinition definition = BackgroundManager.get(rootId);
        int worldSalt = mc.getUser().getUuid().hashCode();

        if (texture == null
                || viewportW != bakedViewportW || viewportH != bakedViewportH
                || worldSalt != bakedSalt
                || definition != bakedDefinition) {
            rebuild(scrollX, scrollY, viewportW, viewportH, worldSalt, definition);
        }

        int worldX = (int) Math.floor(scrollX) + WORLD_OFFSET;
        int worldY = (int) Math.floor(scrollY) + WORLD_OFFSET;
        int tileX = worldX >> 4;
        int tileY = worldY >> 4;
        int subTileX = Math.floorMod(worldX, TILE);
        int subTileY = Math.floorMod(worldY, TILE);

        int colOffset = tileX - originTileX;
        int rowOffset = tileY - originTileY;
        int visibleCols = (int) Math.ceil(viewportW * zoom / (double) TILE) + 1;
        int visibleRows = (int) Math.ceil(viewportH * zoom / (double) TILE) + 1;

        if (colOffset < 0 || rowOffset < 0
                || colOffset + visibleCols > textureW / TILE
                || rowOffset + visibleRows > textureH / TILE) {
            rebuild(scrollX, scrollY, viewportW, viewportH, worldSalt, definition);
            colOffset = tileX - originTileX;
            rowOffset = tileY - originTileY;
        }

        mc.getTextureManager().bindForSetup(TEXTURE_ID);
        RenderSystem.setShaderTexture(0, texture.getId());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int blitW = (int) Math.ceil(viewportW * zoom);
        int blitH = (int) Math.ceil(viewportH * zoom);
        g.blit(TEXTURE_ID, -subTileX, -subTileY, colOffset * TILE, rowOffset * TILE,
                blitW + TILE, blitH + TILE, textureW, textureH);
    }

    private void rebuild(double scrollX, double scrollY, int viewportW, int viewportH,
                         int worldSalt, BackgroundDefinition definition) {
        Minecraft mc = Minecraft.getInstance();

        int tileX = ((int) Math.floor(scrollX) + WORLD_OFFSET) >> 4;
        int tileY = ((int) Math.floor(scrollY) + WORLD_OFFSET) >> 4;

        int visibleCols = (int) Math.ceil(viewportW * MAX_ZOOM / TILE) + 1;
        int visibleRows = (int) Math.ceil(viewportH * MAX_ZOOM / TILE) + 1;
        int cols = Math.min(visibleCols + PAD_TILES * 2, MAX_TILES);
        int rows = Math.min(visibleRows + PAD_TILES * 2, MAX_TILES);

        int newOriginX = tileX - PAD_TILES;
        int newOriginY = tileY - PAD_TILES;
        int newTextureW = cols * TILE;
        int newTextureH = rows * TILE;

        if (texture != null && (textureW != newTextureW || textureH != newTextureH)) {
            texture.close();
            texture = null;
        }

        NativeImage image = new NativeImage(newTextureW, newTextureH, false);
        BackgroundTileBaker.bake(image, newOriginX, newOriginY, cols, rows, worldSalt, definition);

        if (texture == null) {
            texture = new DynamicTexture(image);
            mc.getTextureManager().register(TEXTURE_ID, texture);
        } else {
            texture.setPixels(image);
            texture.upload();
        }

        originTileX = newOriginX;
        originTileY = newOriginY;
        textureW = newTextureW;
        textureH = newTextureH;
        bakedSalt = worldSalt;
        bakedDefinition = definition;
        bakedViewportW = viewportW;
        bakedViewportH = viewportH;
    }
}
