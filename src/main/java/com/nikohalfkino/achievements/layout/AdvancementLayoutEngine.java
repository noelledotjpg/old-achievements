package com.nikohalfkino.achievements.layout;

import com.nikohalfkino.achievements.state.AdvancementStateHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AdvancementLayoutEngine {

    private static final int SCROLL_MARGIN_X = 112;
    private static final int SCROLL_MARGIN_Y = 250;
    private static final int MIN_NODE_DISTANCE = 45;
    private static final int MIN_VERTICAL_OFFSET = 35;
    private static final int PLACEMENT_ATTEMPTS = 8;

    private final AdvancementLayoutConfig layoutConfig;
    private final AdvancementStateHelper stateHelper;

    private final Map<Advancement, int[]> positions = new LinkedHashMap<>();

    private int minX, maxX, minY, maxY;

    public AdvancementLayoutEngine(AdvancementLayoutConfig layoutConfig, AdvancementStateHelper stateHelper) {
        this.layoutConfig = layoutConfig;
        this.stateHelper = stateHelper;
    }

    public Map<Advancement, int[]> positions() {
        return positions;
    }

    public double clampX(double x) {
        return Mth.clamp(x, minX, maxX);
    }

    public double clampY(double y) {
        return Mth.clamp(y, minY, maxY);
    }

    public void buildLayout(Advancement root) {
        if (root == null) {
            positions.clear();
            return;
        }

        Map<Advancement, int[]> autoLayout = new LinkedHashMap<>();
        growBranch(root, 0, 0, 0, 0, autoLayout);

        layoutConfig.sync(autoLayout);

        positions.clear();
        for (Map.Entry<Advancement, int[]> entry : autoLayout.entrySet()) {
            int[] saved = layoutConfig.getPosition(entry.getKey());
            positions.put(entry.getKey(), saved != null ? saved : entry.getValue());
        }

        recalcScrollBounds();
    }

    public void recalcScrollBounds() {
        if (positions.isEmpty()) return;

        int lowX = Integer.MAX_VALUE, highX = Integer.MIN_VALUE;
        int lowY = Integer.MAX_VALUE, highY = Integer.MIN_VALUE;
        boolean anyDrawn = false;

        for (Map.Entry<Advancement, int[]> entry : positions.entrySet()) {
            if (!stateHelper.isNodeDrawn(entry.getKey())) continue;

            int[] p = entry.getValue();
            lowX = Math.min(lowX, p[0]);
            highX = Math.max(highX, p[0]);
            lowY = Math.min(lowY, p[1]);
            highY = Math.max(highY, p[1]);
            anyDrawn = true;
        }

        if (!anyDrawn) {
            lowX = highX = lowY = highY = 0;
        }

        minX = lowX - SCROLL_MARGIN_X;
        maxX = highX + SCROLL_MARGIN_X;
        minY = lowY - SCROLL_MARGIN_Y;
        maxY = highY + SCROLL_MARGIN_Y;
    }

    private void growBranch(Advancement advancement, int x, int y, double parentAngle, int depth,
                            Map<Advancement, int[]> out) {
        out.put(advancement, new int[]{x, y});

        Random random = new Random(advancement.toString().hashCode());
        double spread = Math.toRadians(depth == 0 ? 360 : 160);
        double startAngle = parentAngle - spread / 2.0;

        List<Advancement> children = new ArrayList<>();
        AdvancementNode advancementNode = Minecraft.getInstance().getConnection().getAdvancements().getTree().nodes().stream().filter(node -> node.holder().value() == advancement).findFirst().orElse(null);
        if(advancementNode != null) {
            for (AdvancementNode childNode : advancementNode.children()) {
                Advancement child = childNode.holder().value();
                if (!stateHelper.isHiddenFromScreen(child)) {
                    children.add(child);
                }
            }
        }

        for (int i = 0; i < children.size(); i++) {
            double angle = startAngle + (i + 0.5) * (spread / children.size());

            int childX = x;
            int childY = y;
            float distanceScale = 1.0f;
            for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
                int distance = (int) ((100 + random.nextInt(30)) * distanceScale);
                childX = x + (int) (Math.cos(angle) * distance);
                childY = y + (int) (Math.sin(angle) * distance);
                if (Math.abs(childY - y) < MIN_VERTICAL_OFFSET) {
                    childY = y + (childY >= y ? MIN_VERTICAL_OFFSET : -MIN_VERTICAL_OFFSET);
                }
                if (isPositionClear(childX, childY, out)) break;
                distanceScale += 0.3f;
                angle += (random.nextBoolean() ? 0.1 : -0.1);
            }

            growBranch(children.get(i), childX, childY, angle, depth + 1, out);
        }
    }

    private static boolean isPositionClear(int x, int y, Map<Advancement, int[]> layout) {
        int thresholdSq = MIN_NODE_DISTANCE * MIN_NODE_DISTANCE;
        for (int[] pos : layout.values()) {
            int dx = x - pos[0];
            int dy = y - pos[1];
            if (dx * dx + dy * dy < thresholdSq) return false;
        }
        return true;
    }
}
