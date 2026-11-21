package dev.granitelib.fabric.template.load;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateFill;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Writes a loaded menu back to the v0 YAML schema. The text is the menu object that was passed in,
 * not a copy of an earlier file.
 */
public final class GraniteMenuExporter {

    private GraniteMenuExporter() {
    }

    public static void write(GraniteMenuTemplate template, Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(path, toYaml(template), StandardCharsets.UTF_8);
    }

    public static String toYaml(GraniteMenuTemplate template) {
        StringBuilder out = new StringBuilder();
        line(out, 0, "id", template.id());
        line(out, 0, "title", template.title());
        out.append("rows: ").append(template.rows()).append('\n');
        template.fill().ifPresent(fill -> writeFill(out, fill));
        if (!template.items().isEmpty()) {
            out.append("items:\n");
            for (Map.Entry<String, GraniteTemplateItem> entry : template.items().entrySet()) {
                out.append("  ").append(scalar(entry.getKey())).append(":\n");
                writeItem(out, entry.getValue());
            }
        }
        return out.toString();
    }

    private static void writeFill(StringBuilder out, GraniteTemplateFill fill) {
        out.append("fill:\n");
        line(out, 1, "material", fill.material());
        fill.displayName().ifPresent(name -> line(out, 1, "name", name));
    }

    private static void writeItem(StringBuilder out, GraniteTemplateItem item) {
        line(out, 2, "material", item.material());
        if (item.slots().size() == 1) {
            out.append("    slot: ").append(item.slots().getFirst()).append('\n');
        } else {
            out.append("    slots:\n");
            for (int slot : item.slots()) {
                out.append("      - ").append(slot).append('\n');
            }
        }
        item.displayName().ifPresent(name -> line(out, 2, "name", name));
        if (!item.lore().isEmpty()) {
            out.append("    lore:\n");
            for (String lore : item.lore()) {
                out.append("      - ").append(scalar(lore)).append('\n');
            }
        }
        if (item.amount().isPresent()) {
            out.append("    amount: ").append(item.amount().getAsInt()).append('\n');
        }
        if (item.customModelData().isPresent()) {
            out.append("    custom_model_data: ").append(item.customModelData().getAsInt()).append('\n');
        }
        item.action().ifPresent(action -> line(out, 2, "action", action));
    }

    private static void line(StringBuilder out, int indent, String key, String value) {
        out.append("  ".repeat(indent)).append(key).append(": ").append(scalar(value)).append('\n');
    }

    private static String scalar(String value) {
        if (value.matches("[A-Za-z0-9_./+-]+")) {
            return value;
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
