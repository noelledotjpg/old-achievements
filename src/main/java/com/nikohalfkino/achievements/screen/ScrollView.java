package com.nikohalfkino.achievements.screen;

import com.nikohalfkino.achievements.layout.AdvancementLayoutEngine;
import net.minecraft.util.Mth;

final class ScrollView {

    private static final float ZOOM_STEP = 0.25F;
    private static final float MIN_ZOOM = 1.0F;
    private static final float MAX_ZOOM = 2.0F;
    private static final double SMOOTHING = 0.2;

    private double x;
    private double y;
    private double targetX;
    private double targetY;
    private float zoom = 1.0F;

    double x() {
        return x;
    }

    double y() {
        return y;
    }

    float zoom() {
        return zoom;
    }

    void animate() {
        x += (targetX - x) * SMOOTHING;
        y += (targetY - y) * SMOOTHING;
    }

    void jumpTo(double worldX, double worldY) {
        x = targetX = worldX;
        y = targetY = worldY;
    }

    void panBy(double screenDx, double screenDy) {
        targetX -= screenDx * zoom;
        targetY -= screenDy * zoom;
    }

    void clampTarget(AdvancementLayoutEngine layoutEngine) {
        targetX = layoutEngine.clampX(targetX);
        targetY = layoutEngine.clampY(targetY);
    }

    void zoomBy(double wheelDelta, int viewportW, int viewportH) {
        float previous = zoom;
        zoom = Mth.clamp(zoom + (wheelDelta < 0 ? ZOOM_STEP : -ZOOM_STEP), MIN_ZOOM, MAX_ZOOM);
        if (zoom == previous) return;

        x -= (zoom * viewportW - previous * viewportW) * 0.5;
        y -= (zoom * viewportH - previous * viewportH) * 0.5;
        targetX = x;
        targetY = y;
    }
}
