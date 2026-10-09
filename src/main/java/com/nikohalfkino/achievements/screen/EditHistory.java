package com.nikohalfkino.achievements.screen;

import net.minecraft.advancements.Advancement;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;

final class EditHistory {

    private static final int MAX_ENTRIES = 200;
    private static final long NUDGE_MERGE_MS = 800;

    private static final class Edit {
        final Map<Advancement, int[]> before;
        final boolean nudge;
        Map<Advancement, int[]> after;
        long time;

        Edit(Map<Advancement, int[]> before, Map<Advancement, int[]> after, boolean nudge) {
            this.before = before;
            this.after = after;
            this.nudge = nudge;
            this.time = System.currentTimeMillis();
        }
    }

    private final Deque<Edit> undoStack = new ArrayDeque<>();
    private final Deque<Edit> redoStack = new ArrayDeque<>();

    void record(Map<Advancement, int[]> before, Map<Advancement, int[]> after) {
        push(new Edit(before, after, false));
    }

    void recordNudge(Map<Advancement, int[]> before, Map<Advancement, int[]> after) {
        Edit top = undoStack.peek();
        long now = System.currentTimeMillis();
        if (top != null && top.nudge && top.before.keySet().equals(after.keySet())
                && now - top.time < NUDGE_MERGE_MS) {
            top.after = after;
            top.time = now;
            redoStack.clear();
        } else {
            push(new Edit(before, after, true));
        }
    }

    Map<Advancement, int[]> undo() {
        Edit edit = undoStack.poll();
        if (edit == null) return null;
        redoStack.push(edit);
        return edit.before;
    }

    Map<Advancement, int[]> redo() {
        Edit edit = redoStack.poll();
        if (edit == null) return null;
        undoStack.push(edit);
        return edit.after;
    }

    void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    private void push(Edit edit) {
        if (samePositions(edit.before, edit.after)) return;
        undoStack.push(edit);
        while (undoStack.size() > MAX_ENTRIES) undoStack.removeLast();
        redoStack.clear();
    }

    private static boolean samePositions(Map<Advancement, int[]> a, Map<Advancement, int[]> b) {
        if (!a.keySet().equals(b.keySet())) return false;
        for (Map.Entry<Advancement, int[]> entry : a.entrySet()) {
            if (!Arrays.equals(entry.getValue(), b.get(entry.getKey()))) return false;
        }
        return true;
    }
}
