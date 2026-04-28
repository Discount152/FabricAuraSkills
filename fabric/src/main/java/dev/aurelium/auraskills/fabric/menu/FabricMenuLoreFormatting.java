package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.text.FabricTextAdapter;
import dev.aurelium.auraskills.fabric.text.FabricTextFormatter;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Player-facing menu lore: MiniMessage/legacy via {@link FabricTextFormatter} / {@link FabricTextAdapter}.
 */
public final class FabricMenuLoreFormatting {

    /** Owner rule (Sprint 121D): description/body lore wraps at this width. */
    public static final int DEFAULT_MENU_LORE_WRAP_WIDTH = 42;

    private FabricMenuLoreFormatting() {}

    /**
     * Word-wrap plain text at {@code maxLen}. Preserves explicit newlines (paragraphs). If input looks like
     * MiniMessage ({@code <...>}), returns one chunk. Overlong single words are hard-split.
     */
    public static List<String> wordWrapPlain(String input, int maxLen) {
        List<String> out = new ArrayList<>();
        if (input == null || input.isEmpty() || maxLen < 1) {
            return out;
        }
        if (input.contains("<") && input.contains(">")) {
            out.add(input);
            return out;
        }
        String[] words = input.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) {
                continue;
            }
            if (w.length() > maxLen) {
                if (!line.isEmpty()) {
                    out.add(line.toString());
                    line = new StringBuilder();
                }
                for (int i = 0; i < w.length(); i += maxLen) {
                    out.add(w.substring(i, Math.min(i + maxLen, w.length())));
                }
                continue;
            }
            if (line.isEmpty()) {
                line.append(w);
            } else if (line.length() + 1 + w.length() <= maxLen) {
                line.append(' ').append(w);
            } else {
                out.add(line.toString());
                line = new StringBuilder(w);
            }
        }
        if (!line.isEmpty()) {
            out.add(line.toString());
        }
        return out;
    }

    public static Text formatMenuLine(FabricAuraSkills plugin, String rawLine) {
        if (rawLine == null || rawLine.isEmpty()) {
            return Text.empty();
        }
        return FabricTextAdapter.toNativeText(FabricTextFormatter.toComponent(rawLine), plugin.getServer());
    }

    public static void appendWrappedFormattedLore(
            FabricAuraSkills plugin,
            List<Text> lore,
            String multilineInput,
            int maxLines,
            int capSoft) {
        appendWrappedFormattedLore(
                plugin, lore, multilineInput, maxLines, DEFAULT_MENU_LORE_WRAP_WIDTH, capSoft);
    }

    public static void appendWrappedFormattedLore(
            FabricAuraSkills plugin,
            List<Text> lore,
            String multilineInput,
            int maxLines,
            int maxCharsPerLine,
            int capSoft) {
        if (multilineInput == null || multilineInput.isEmpty()) {
            return;
        }
        String work = multilineInput;
        if (capSoft > 0 && work.length() > capSoft) {
            work = work.substring(0, capSoft) + "\u2026";
        }
        String[] paragraphs = work.split("\n");
        for (String para : paragraphs) {
            String p = para.trim();
            if (p.isEmpty()) {
                continue;
            }
            List<String> chunks = wordWrapPlain(p, maxCharsPerLine);
            for (String chunk : chunks) {
                if (lore.size() >= maxLines) {
                    return;
                }
                lore.add(formatMenuLine(plugin, chunk));
            }
        }
    }
}