package com.nikohalfkino.achievements.screen;

record FrameGeometry(int screenWidth, int screenHeight, boolean fullscreen) {

    static final int WIDTH = 256;
    static final int HEIGHT = 202;

    int width() {
        return fullscreen ? screenWidth : WIDTH;
    }

    int height() {
        return fullscreen ? screenHeight : HEIGHT;
    }

    int left() {
        return fullscreen ? 0 : (screenWidth - WIDTH) / 2;
    }

    int top() {
        return fullscreen ? 0 : (screenHeight - HEIGHT) / 2;
    }

    int viewportX() {
        return fullscreen ? 0 : left() + 16;
    }

    int viewportY() {
        return fullscreen ? 0 : top() + 17;
    }

    int viewportW() {
        return fullscreen ? screenWidth : WIDTH - 32;
    }

    int viewportH() {
        return fullscreen ? screenHeight : HEIGHT - 47;
    }

    boolean containsInViewport(double x, double y) {
        return x >= viewportX() && x <= viewportX() + viewportW()
                && y >= viewportY() && y <= viewportY() + viewportH();
    }
}
