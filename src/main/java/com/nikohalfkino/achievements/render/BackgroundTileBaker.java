package com.nikohalfkino.achievements.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.nikohalfkino.achievements.background.BackgroundDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.Block;

import java.util.Random;

final class BackgroundTileBaker {

    static final int TILE = 16;

    private BackgroundTileBaker() {}

    static void bake(NativeImage image, int originTileX, int originTileY, int cols, int rows,
                     int worldSalt, BackgroundDefinition definition) {
        BlockRenderDispatcher blocks = Minecraft.getInstance().getBlockRenderer();

        for (int row = 0; row < rows; row++) {
            int tileY = originTileY + row;
            int brightness = Math.round(definition.brightness(tileY) * 255);

            for (int col = 0; col < cols; col++) {
                int tileX = originTileX + col;

                Random random = new Random(tileSeed(tileX, tileY, worldSalt));
                int depth = random.nextInt(1 + Math.max(0, tileY)) + tileY / 2;
                Block block = definition.pick(tileY, depth, random);

                if (block == null) {
                    fillSolid(image, col * TILE, row * TILE, brightness);
                } else {
                    TextureAtlasSprite sprite = blocks.getBlockModel(block.defaultBlockState()).getParticleIcon();
                    copySpriteTile(sprite, image, col * TILE, row * TILE, brightness);
                }
            }
        }
    }

    private static long tileSeed(int tileX, int tileY, int worldSalt) {
        // szudzik pairing, then xorshift64*
        long coord = tileX >= 0
                ? (tileX >= tileY ? (long) tileX * tileX + tileX + tileY
                : (long) tileY * tileY + tileX)
                : (tileY >= 0 ? (long) tileX * tileX - tileX + tileY
                : (long) (tileX - tileY) * (tileX - tileY) - tileX);
        long seed = coord ^ ((long) worldSalt << 32 | (worldSalt & 0xFFFFFFFFL));
        seed ^= seed >>> 12;
        seed ^= seed << 25;
        seed ^= seed >>> 27;
        return seed * 0x2545F4914F6CDD1DL;
    }

    private static void copySpriteTile(TextureAtlasSprite sprite, NativeImage destination,
                                       int destX, int destY, int brightness) {
        try {
            int spriteW = sprite.contents().width();
            int spriteH = sprite.contents().height();

            for (int py = 0; py < TILE; py++) {
                int srcY = py * spriteH / TILE;
                for (int px = 0; px < TILE; px++) {
                    int srcX = px * spriteW / TILE;
                    // pixels are ABGR
                    int abgr = sprite.getPixelRGBA(0, srcX, srcY);
                    int a = (abgr >>> 24) & 0xFF;
                    int b = (abgr >>> 16) & 0xFF;
                    int g = (abgr >>> 8) & 0xFF;
                    int r = abgr & 0xFF;
                    r = r * brightness / 255;
                    g = g * brightness / 255;
                    b = b * brightness / 255;
                    destination.setPixelRGBA(destX + px, destY + py, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }
        } catch (Exception e) {
            fillSolid(destination, destX, destY, brightness);
        }
    }

    private static void fillSolid(NativeImage destination, int destX, int destY, int brightness) {
        int grey = (0xFF << 24) | (brightness << 16) | (brightness << 8) | brightness;
        for (int py = 0; py < TILE; py++) {
            for (int px = 0; px < TILE; px++) {
                destination.setPixelRGBA(destX + px, destY + py, grey);
            }
        }
    }
}
