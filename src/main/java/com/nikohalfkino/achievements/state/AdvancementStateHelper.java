package com.nikohalfkino.achievements.state;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AdvancementStateHelper {

    // locked nodes deeper than this are not drawn
    public static final int MAX_VISIBLE_DEPTH = 3;

    public record AchievementCount(int unlocked, int total) {}

    private final Map<Advancement, AdvancementProgress> progressMap = new HashMap<>();
    private boolean debugReveal;

    public void setDebugReveal(boolean debugReveal) {
        this.debugReveal = debugReveal;
    }

    public void onProgress(Advancement advancement, AdvancementProgress progress) {
        progressMap.put(advancement, progress);
    }

    public void clear() {
        progressMap.clear();
    }

    public boolean isUnlocked(Advancement advancement) {
        return debugReveal || isUnlockedReal(advancement);
    }

    public boolean isUnlockedReal(Advancement advancement) {
        AdvancementProgress progress = progressMap.get(advancement);
        return progress != null && progress.isDone();
    }

    public boolean canUnlock(Advancement advancement) {
        return advancement.getParent() == null || isUnlocked(advancement.getParent());
    }

    public boolean isHiddenFromScreen(Advancement advancement) {
        if (advancement.getDisplay() == null) return true;
        if (debugReveal) return false;
        return isHiddenReal(advancement);
    }

    public boolean isHiddenReal(Advancement advancement) {
        if (advancement.getDisplay() == null) return true;
        if (!advancement.getDisplay().isHidden()) return false;
        return !isUnlockedReal(advancement);
    }

    public boolean isNodeDrawn(Advancement advancement) {
        return !isHiddenFromScreen(advancement)
                && (getRequirementCount(advancement) <= MAX_VISIBLE_DEPTH || isUnlocked(advancement));
    }

    public int getRequirementCount(Advancement advancement) {
        if (debugReveal) return 0;
        int count = 0;
        for (Advancement current = advancement; current != null && !isUnlocked(current); current = current.getParent()) {
            count++;
        }
        return count;
    }

    public AchievementCount countAchievements(Collection<Advancement> advancements) {
        int unlocked = 0;
        int total = 0;
        for (Advancement advancement : advancements) {
            if (advancement.getDisplay() == null || advancement.getParent() == null) continue;

            boolean done = isUnlockedReal(advancement);
            if (done) unlocked++;
            if (done || !isHiddenReal(advancement)) total++;
        }
        return new AchievementCount(unlocked, total);
    }
}
