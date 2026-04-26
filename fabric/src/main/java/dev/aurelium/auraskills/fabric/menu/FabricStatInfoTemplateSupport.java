package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.common.message.MessageProvider;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import dev.granitelib.fabric.template.load.GraniteMenuTemplateYamlLoader;
import dev.granitelib.fabric.template.load.GraniteTemplateValidationException;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateFill;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Sprint 96: optional template-backed {@code stat_info} using GraniteLib v0 YAML. On any failure, returns
 * {@code false} so {@link FabricStatInfoMenuGui} uses the built-in path.
 */
public final class FabricStatInfoTemplateSupport {

    /** Bundled prototype template (classpath). */
    public static final String TEMPLATE_RESOURCE_PATH = "assets/auraskills/granite/templates/stat_info.yml";

    private FabricStatInfoTemplateSupport() {
    }

    /**
     * @return {@code true} if the template menu was opened; {@code false} to use built-in rendering.
     */
    public static boolean tryOpen(
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            FabricMenuContext context,
            Stat stat) {
        try {
            return tryOpenInner(player, plugin, context, stat);
        } catch (RuntimeException e) {
            plugin.logger().warn("stat_info template render failed; using built-in menu", e);
            return false;
        }
    }

    private static boolean tryOpenInner(
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            FabricMenuContext context,
            Stat stat) {
        GraniteMenuTemplate template;
        try (InputStream in = openBundledTemplate()) {
            if (in == null) {
                plugin.logger().warn(
                        "stat_info template missing from classpath (" + TEMPLATE_RESOURCE_PATH + "); using built-in menu");
                return false;
            }
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            template = GraniteMenuTemplateYamlLoader.loadString(yaml, TEMPLATE_RESOURCE_PATH);
        } catch (IOException e) {
            plugin.logger().warn("stat_info template read failed; using built-in menu", e);
            return false;
        } catch (GraniteTemplateValidationException e) {
            plugin.logger().warn(
                    "stat_info template invalid (" + String.join("; ", e.errors()) + "); using built-in menu");
            return false;
        }

        if (!"stat_info".equalsIgnoreCase(template.id())) {
            plugin.logger().warn(
                    "stat_info template id must be 'stat_info', got '"
                            + template.id()
                            + "'; using built-in menu");
            return false;
        }
        if (template.rows() != 6) {
            plugin.logger().warn(
                    "stat_info template rows must be 6 for 6×9 menu, got "
                            + template.rows()
                            + "; using built-in menu");
            return false;
        }

        User user = plugin.getUserManager().getUser(player.getUuid());
        Locale locale = user.getLocale();
        MessageProvider msg = plugin.getMessageProvider();
        Map<String, String> ph = buildPlaceholders(stat, user, plugin, msg, locale);

        Optional<Integer> backSlot = resolveBackSlot(template);
        if (backSlot.isEmpty()) {
            plugin.logger().warn(
                    "stat_info template has no back control (item key 'back' or action 'back'); using built-in menu");
            return false;
        }
        int back = backSlot.get();

        Text titleText = parseMenuText(expandTemplate(template.title(), ph), plugin, "title");
        if (titleText == null) {
            return false;
        }

        Optional<ItemStack> fillBase = buildFillStack(template, ph, plugin);
        if (template.fill().isPresent() && fillBase.isEmpty()) {
            return false;
        }

        Map<String, ItemStack> stacksByKey = new HashMap<>();
        for (Map.Entry<String, GraniteTemplateItem> e : template.items().entrySet()) {
            Optional<ItemStack> stack = buildItemStack(e.getValue(), ph, plugin, "items." + e.getKey());
            if (stack.isEmpty()) {
                return false;
            }
            stacksByKey.put(e.getKey(), stack.get());
        }

        GraniteContainerMenus.openClickable9x6(
                player,
                titleText,
                inv -> {
                    if (fillBase.isPresent()) {
                        ItemStack fill = fillBase.get();
                        for (int s = 0; s < 54; s++) {
                            inv.setStack(s, fill.copy());
                        }
                    }
                    for (Map.Entry<String, GraniteTemplateItem> e : template.items().entrySet()) {
                        ItemStack base = stacksByKey.get(e.getKey());
                        for (int slot : e.getValue().slots()) {
                            inv.setStack(slot, base.copy());
                        }
                    }
                },
                (topSlot, p, b, t) -> {
                    if (topSlot != back) {
                        return;
                    }
                    plugin.getScheduler()
                            .executeSync(() -> FabricAuraSkillsMenus.open(
                                    FabricAuraSkillsMenus.Id.STATS,
                                    p,
                                    plugin,
                                    FabricMenuContext.none().withListPage(context.listPage())));
                });
        return true;
    }

    private static @Nullable InputStream openBundledTemplate() {
        ClassLoader cl = FabricStatInfoTemplateSupport.class.getClassLoader();
        return cl.getResourceAsStream(TEMPLATE_RESOURCE_PATH);
    }

    static Map<String, String> buildPlaceholders(
            Stat stat, User user, FabricAuraSkills plugin, MessageProvider msg, Locale locale) {
        Map<String, String> m = new HashMap<>();
        m.put("stat", stat.getId().getSimpleName());
        m.put("stat_display", msg.getStatDisplayName(stat, locale, true));
        String desc = msg.getStatDescription(stat, locale, false);
        if (desc == null) {
            desc = "";
        }
        m.put("stat_description", desc.replace('\n', ' ').trim());
        m.put("stat_value", FabricStatsMenuGui.formatStatLevel(stat, user, plugin, locale));
        return m;
    }

    private static Optional<Integer> resolveBackSlot(GraniteMenuTemplate template) {
        for (Map.Entry<String, GraniteTemplateItem> e : template.items().entrySet()) {
            if ("back".equalsIgnoreCase(e.getKey())) {
                return Optional.of(e.getValue().slots().getFirst());
            }
            if (e.getValue().action().filter(a -> "back".equalsIgnoreCase(a)).isPresent()) {
                return Optional.of(e.getValue().slots().getFirst());
            }
        }
        return Optional.empty();
    }

    private static String expandTemplate(String raw, Map<String, String> ph) {
        return FabricStatInfoPlaceholders.expand(raw, ph);
    }

    /**
     * @return {@code null} if expansion left unresolved {@code {...}} (forces built-in fallback).
     */
    private static @Nullable Text parseMenuText(String expanded, FabricAuraSkills plugin, String label) {
        if (FabricStatInfoPlaceholders.hasUnresolvedBraceSyntax(expanded)) {
            plugin.logger().warn(
                    "stat_info template "
                            + label
                            + " still contains unresolved placeholders after expansion; using built-in menu");
            return null;
        }
        return Text.literal(expanded);
    }

    private static Optional<ItemStack> buildFillStack(
            GraniteMenuTemplate template, Map<String, String> ph, FabricAuraSkills plugin) {
        if (template.fill().isEmpty()) {
            return Optional.empty();
        }
        GraniteTemplateFill fill = template.fill().get();
        Optional<Item> item = resolveStrict(fill.material(), plugin, "fill.material");
        if (item.isEmpty()) {
            return Optional.empty();
        }
        ItemStack stack = new ItemStack(item.get(), 1);
        if (fill.displayName().isPresent()) {
            String name = expandTemplate(fill.displayName().get(), ph);
            Text line = parseMenuText(name, plugin, "fill.name");
            if (line == null) {
                return Optional.empty();
            }
            stack.set(DataComponentTypes.ITEM_NAME, line);
        }
        return Optional.of(stack);
    }

    private static Optional<ItemStack> buildItemStack(
            GraniteTemplateItem item, Map<String, String> ph, FabricAuraSkills plugin, String logCtx) {
        Optional<Item> resolved = resolveStrict(item.material(), plugin, logCtx + ".material");
        if (resolved.isEmpty()) {
            return Optional.empty();
        }
        int count = item.amount().orElse(1);
        ItemStack stack = new ItemStack(resolved.get(), Math.max(1, count));

        if (item.displayName().isPresent()) {
            String name = expandTemplate(item.displayName().get(), ph);
            Text line = parseMenuText(name, plugin, logCtx + ".name");
            if (line == null) {
                return Optional.empty();
            }
            stack.set(DataComponentTypes.ITEM_NAME, line);
        }

        List<Text> loreLines = new java.util.ArrayList<>();
        for (String loreRaw : item.lore()) {
            if (loreRaw.isEmpty()) {
                loreLines.add(Text.empty());
                continue;
            }
            String line = expandTemplate(loreRaw, ph);
            Text text = parseMenuText(line, plugin, logCtx + ".lore");
            if (text == null) {
                return Optional.empty();
            }
            loreLines.add(text);
        }
        if (!loreLines.isEmpty()) {
            stack.set(DataComponentTypes.LORE, new LoreComponent(loreLines));
        }

        // custom_model_data: parsed in v0 YAML but not applied here until a stable 1.21.11 component ctor is wired.
        return Optional.of(stack);
    }

    /**
     * Strict item resolution for template path: unknown ids abort template (built-in fallback). Logs a warning.
     */
    static Optional<Item> resolveStrict(String materialId, FabricAuraSkills plugin, String logCtx) {
        if (materialId == null || materialId.isBlank()) {
            plugin.logger().warn("stat_info template " + logCtx + ": blank material; using built-in menu");
            return Optional.empty();
        }
        try {
            Identifier id = materialId.contains(":")
                    ? Identifier.of(materialId)
                    : Identifier.ofVanilla(materialId.toLowerCase(Locale.ROOT));
            if (!Registries.ITEM.containsId(id)) {
                plugin.logger().warn(
                        "stat_info template "
                                + logCtx
                                + ": unknown item '"
                                + materialId
                                + "'; using built-in menu");
                return Optional.empty();
            }
            Item it = Registries.ITEM.get(id);
            if (it == null || it == Items.AIR) {
                plugin.logger().warn(
                        "stat_info template "
                                + logCtx
                                + ": item '"
                                + materialId
                                + "' resolved to air; using built-in menu");
                return Optional.empty();
            }
            return Optional.of(it);
        } catch (Exception e) {
            plugin.logger().warn(
                    "stat_info template "
                            + logCtx
                            + ": could not parse item '"
                            + materialId
                            + "'; using built-in menu",
                    e);
            return Optional.empty();
        }
    }
}
