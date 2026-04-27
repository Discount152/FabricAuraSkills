package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.common.menu.MenuHelper;
import dev.aurelium.auraskills.common.message.MessageProvider;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.PlatformUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import dev.granitelib.fabric.template.GraniteTemplateActionNames;
import dev.granitelib.fabric.template.GraniteTemplateItems;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Sprint 96–97: optional template-backed {@code stat_info} using GraniteLib v0 YAML. On any failure, returns
 * {@code false} so {@link FabricStatInfoMenuGui} uses the built-in path. **Sprint 99:** shared loaders/helpers in
 * GraniteLib + {@link FabricGraniteTemplateStacks}.
 */
public final class FabricStatInfoTemplateSupport {

    /** Bundled prototype template (classpath). */
    public static final String TEMPLATE_RESOURCE_PATH = "assets/auraskills/granite/templates/stat_info.yml";

    private static final String MENU_LOG = "stat_info template";

    private FabricStatInfoTemplateSupport() {
    }

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
        Optional<FabricGraniteMenuTemplateLoader.LoadedTemplate> loaded =
                plugin.menuTemplateRegistry().getLoadedTemplate("stat_info");
        if (loaded.isEmpty()) {
            return false;
        }
        GraniteMenuTemplate template = loaded.get().template();

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

        Optional<Integer> backSlot =
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                        template, "back", List.of(GraniteTemplateActionNames.BACK));
        if (backSlot.isEmpty()) {
            plugin.logger().warn(
                    "stat_info template has no back control (item key 'back' or action 'back'); using built-in menu");
            return false;
        }
        int back = backSlot.get();

        String titleExpanded = FabricStatInfoPlaceholders.expand(template.title(), ph);
        Optional<Text> titleText =
                FabricGraniteTemplateStacks.toLiteralNoUnresolved(titleExpanded, plugin, MENU_LOG, "title");
        if (titleText.isEmpty()) {
            return false;
        }

        Optional<ItemStack> fillBase =
                FabricGraniteTemplateStacks.buildFillStack(template.fill(), ph, plugin, MENU_LOG);
        if (template.fill().isPresent() && fillBase.isEmpty()) {
            return false;
        }

        Map<String, ItemStack> stacksByKey = new HashMap<>();
        MenuHelper menus = plugin.getMenuHelper();
        PlatformUtil util = plugin.getPlatformUtil();
        for (Map.Entry<String, GraniteTemplateItem> e : template.items().entrySet()) {
            String itemKey = e.getKey();
            Optional<ItemStack> stack =
                    FabricGraniteTemplateStacks.buildTemplateItemStack(
                            e.getValue(), ph, plugin, MENU_LOG, "items." + itemKey);
            if (stack.isEmpty()) {
                return false;
            }
            ItemStack built = stack.get();
            if (isStatPanelItem(itemKey, e.getValue())) {
                try {
                    appendStatPanelTraitLore(built, stat, user, plugin, msg, locale, menus, util);
                } catch (RuntimeException ex) {
                    plugin.logger().warn(
                            "stat_info template trait lore generation failed; using built-in menu", ex);
                    return false;
                }
            }
            stacksByKey.put(itemKey, built);
        }

        GraniteContainerMenus.openClickable9x6(
                player,
                titleText.get(),
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

    /** Stat panel = YAML key {@code stat} or {@code action: stat_panel} (Sprint 97 trait lore). */
    static boolean isStatPanelItem(String itemKey, GraniteTemplateItem def) {
        if ("stat".equalsIgnoreCase(itemKey)) {
            return true;
        }
        return GraniteTemplateItems.actionEquals(def, GraniteTemplateActionNames.STAT_PANEL);
    }

    static void appendStatPanelTraitLore(
            ItemStack stack,
            Stat stat,
            User user,
            FabricAuraSkills plugin,
            MessageProvider msg,
            Locale locale,
            MenuHelper menus,
            PlatformUtil util) {
        java.util.List<Text> lore = new java.util.ArrayList<>();
        var existing = stack.get(DataComponentTypes.LORE);
        if (existing != null) {
            lore.addAll(existing.lines());
        }
        for (Trait trait : stat.getTraits()) {
            if (lore.size() >= 16) {
                break;
            }
            FabricStatsMenuGui.addTraitLoreIfPresent(stat, user, trait, msg, plugin, locale, menus, util, lore);
        }
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
    }
}
