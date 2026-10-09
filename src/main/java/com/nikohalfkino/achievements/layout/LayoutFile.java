package com.nikohalfkino.achievements.layout;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.nikohalfkino.achievements.ClassicAchievementsMod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

final class LayoutFile {

    private static final String LEGACY_EDGE_PREFIX = "_edge.";

    private final Path path;

    LayoutFile(Path path) {
        this.path = path;
    }

    Map<String, int[]> read() {
        Map<String, int[]> positions = new LinkedHashMap<>();
        if (!Files.exists(path)) return positions;

        try (CommentedFileConfig config = CommentedFileConfig.builder(path).preserveInsertionOrder().build()) {
            config.load();

            for (Map.Entry<String, Object> entry : config.valueMap().entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(LEGACY_EDGE_PREFIX)) continue; // legacy waypoint sections

                if (!(entry.getValue() instanceof Config section)) {
                    ClassicAchievementsMod.LOGGER.warn("Unexpected entry type for key '{}', skipping", key);
                    continue;
                }

                Object x = section.valueMap().get("x");
                Object y = section.valueMap().get("y");
                if (!(x instanceof Number) || !(y instanceof Number)) {
                    ClassicAchievementsMod.LOGGER.warn("Missing or non-numeric x/y for key '{}', skipping", key);
                    continue;
                }
                positions.put(key, new int[]{((Number) x).intValue(), ((Number) y).intValue()});
            }

            ClassicAchievementsMod.LOGGER.info("Loaded {} layout overrides from {}", positions.size(), path.getFileName());
        } catch (Exception e) {
            ClassicAchievementsMod.LOGGER.warn("Failed to read layout config, starting fresh: {}", e.getMessage());
            positions.clear();
        }
        return positions;
    }

    void write(Map<String, int[]> positions, Map<String, int[]> autoPositions) {
        try {
            Files.createDirectories(path.getParent());

            StringBuilder sb = new StringBuilder();
            sb.append("# Classic Achievements - Advancement Layout Config\n");
            sb.append("# Edit x/y values to reposition nodes on the achievement screen.\n");
            sb.append("# Entries marked '# auto' were placed by the layout algorithm.\n");
            sb.append("# Entries marked '# user' differ from the auto position (you edited them).\n");
            sb.append("# Positions are in virtual screen pixels; the root node starts at x=0, y=0.\n");
            sb.append("\n");

            for (Map.Entry<String, int[]> entry : positions.entrySet()) {
                String id = entry.getKey();
                int[] pos = entry.getValue();
                int[] auto = autoPositions.get(id);
                boolean userEdited = auto == null || auto[0] != pos[0] || auto[1] != pos[1];

                sb.append(String.format("[\"%s\"] # %s%n", escape(id), userEdited ? "user" : "auto"));
                sb.append(String.format("  x = %d%n", pos[0]));
                sb.append(String.format("  y = %d%n", pos[1]));
                sb.append("\n");
            }

            Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
            ClassicAchievementsMod.LOGGER.info("Wrote {} layout entries to {}", positions.size(), path.getFileName());
        } catch (IOException e) {
            ClassicAchievementsMod.LOGGER.error("Failed to write layout config: {}", e.getMessage());
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
