package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.message.MessageProvider;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import dev.granitelib.fabric.template.GraniteTemplateActionNames;
import dev.granitelib.fabric.template.GraniteTemplateItems;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Template-backed {@code stats} — Sprint 113 locked 3×3 hub. On failure returns {@code false} so
 * {@link FabricStatsMenuGui} uses the built-in path.
 */
public final class FabricStatsTemplateSupport {

    public static final String TEMPLATE_RESOURCE_PATH = "assets/auraskills/granite/templates/stats.yml";

    public static final List<Integer> LOCKED_STAT_GRID_SLOTS =
            List.of(12, 13, 14, 21, 22, 23, 30, 31, 32);

    public static final List<String> LOCKED_STAT_IDS = List.of(
            "strength",
            "crit_chance",
            "crit_damage",
            "health",
            "regeneration",
            "toughness",
            "luck",
            "wisdom",
            "speed");

    public static final int EXPECTED_STAT_COUNT = 9;

    private static final String MENU_LOG = "stats template";

    private FabricStatsTemplateSupport() {
    }

    public static boolean tryOpen(ServerPlayerEntity player, FabricAuraSkills plugin, FabricMenuContext context, User user) {
        try {
            return tryOpenInner(player, plugin, context, user);
        } catch (RuntimeException e) {
            plugin.logger().warn("stats template render failed; using built-in menu", e);
            return false;
        }
    }

    private static boolean tryOpenInner(
            ServerPlayerEntity player, FabricAuraSkills plugin, FabricMenuContext context, User user) {
        Optional<FabricGraniteMenuTemplateLoader.LoadedTemplate> loaded =
                plugin.menuTemplateRegistry().getLoadedTemplate("stats");
        if (loaded.isEmpty()) {
            return false;
        }
        GraniteMenuTemplate template = loaded.get().template();

        if (!"stats".equalsIgnoreCase(template.id())) {
            plugin.logger().warn(
                    "stats template id must be 'stats', got '" + template.id() + "'; using built-in menu");
            return false;
        }
        if (template.rows() != 6) {
            plugin.logger().warn(
                    "stats template rows must be 6 for locked 6×9 layout, got "
                            + template.rows()
                            + "; using built-in menu");
            return false;
        }

        Optional<String> actionContract =
                FabricGraniteMenuTemplateActionContract.validateTemplateActions("stats", template);
        if (actionContract.isPresent()) {
            plugin.logger().warn(MENU_LOG + ": " + actionContract.get() + "; using built-in menu");
            return false;
        }

        GraniteTemplateItem statDef = GraniteTemplateItems.findStatGridDefinition(template);
        if (statDef == null) {
            plugin.logger().warn(
                    "stats template: no stat row (key 'stat' or action stat_row); using built-in menu");
            return false;
        }
        if (statDef.slots().size() != EXPECTED_STAT_COUNT || !statDef.slots().equals(LOCKED_STAT_GRID_SLOTS)) {
            plugin.logger().warn(
                    "stats template: stat item must use Sprint 113 locked slot list; using built-in menu");
            return false;
        }

        boolean statKeyed = statDef == template.items().get("stat");
        boolean statRowAction = GraniteTemplateItems.actionEquals(statDef, GraniteTemplateActionNames.STAT_ROW);
        if (!statKeyed && !statRowAction) {
            plugin.logger().warn(
                    "stats template: stat grid must use YAML key 'stat' or action stat_row; using built-in menu");
            return false;
        }

        int backSlot = requiredUtilitySlot(template, GraniteTemplateActionNames.BACK);
        int closeSlot = requiredUtilitySlot(template, GraniteTemplateActionNames.CLOSE);
        if (backSlot < 0 || closeSlot < 0) {
            plugin.logger().warn(MENU_LOG + ": back or close utility missing or multi-slot; using built-in menu");
            return false;
        }

        if (GraniteTemplateItems.findAllByAction(template, GraniteTemplateActionNames.BACK).size() != 1
                || GraniteTemplateItems.findAllByAction(template, GraniteTemplateActionNames.CLOSE).size() != 1) {
            plugin.logger().warn(MENU_LOG + ": duplicate back or close; using built-in menu");
            return false;
        }

        GraniteTemplateItem backItem =
                GraniteTemplateItems.findFirstByAction(template, GraniteTemplateActionNames.BACK).orElseThrow();
        GraniteTemplateItem closeItemT =
                GraniteTemplateItems.findFirstByAction(template, GraniteTemplateActionNames.CLOSE).orElseThrow();

        List<Stat> gridStats = resolveLockedStats(plugin);
        if (gridStats.size() != EXPECTED_STAT_COUNT) {
            plugin.logger().warn(
                    MENU_LOG + ": expected "
                            + EXPECTED_STAT_COUNT
                            + " enabled stats matching locked ids, got "
                            + gridStats.size()
                            + "; using built-in menu");
            return false;
        }

        boolean statInfoClicks = plugin.configBoolean(Option.MENUS_STAT_INFO_ENABLED);
        Locale locale = user.getLocale();
        MessageProvider msg = plugin.getMessageProvider();

        Map<String, String> globalPh = Map.of("page", "1", "total_pages", "1");

        String titleExpanded = FabricStatsPlaceholders.expand(template.title(), globalPh);
        Optional<Text> titleOpt =
                FabricGraniteTemplateStacks.toLiteralNoUnresolved(titleExpanded, plugin, MENU_LOG, "title");
        if (titleOpt.isEmpty()) {
            return false;
        }

        Optional<ItemStack> backStack =
                FabricGraniteTemplateStacks.buildTemplateItemStack(
                        backItem, globalPh, plugin, MENU_LOG, "items.back");
        if (backStack.isEmpty()) {
            return false;
        }
        Optional<ItemStack> closeStack =
                FabricGraniteTemplateStacks.buildTemplateItemStack(
                        closeItemT, globalPh, plugin, MENU_LOG, "items.close");
        if (closeStack.isEmpty()) {
            return false;
        }
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(backStack.get());
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(closeStack.get());

        List<ItemStack> rowStacks = new ArrayList<>(gridStats.size());
        for (Stat stat : gridStats) {
            Map<String, String> rowPh = new HashMap<>(globalPh);
            addStatRowPlaceholders(rowPh, stat, user, plugin, msg, locale);
            Optional<ItemStack> rowStack =
                    FabricGraniteTemplateStacks.buildTemplateItemStack(
                            statDef, rowPh, plugin, MENU_LOG, "items.stat.row");
            if (rowStack.isEmpty()) {
                return false;
            }
            ItemStack icon = FabricMenuPresentation.applyStatRowIcon(rowStack.get(), stat);
            FabricMenuPresentation.applyStatsHubRow(icon, stat, user, plugin, msg, locale);
            FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(icon);
            rowStacks.add(icon);
        }

        final int fBack = backSlot;
        final int fClose = closeSlot;
        final boolean fStatClicks = statInfoClicks;

        GraniteContainerMenus.openClickable9x6(
                player,
                titleOpt.get(),
                inv -> {
                    for (int s = 0; s < FabricMenuSlotLayout.TOP_SLOTS; s++) {
                        inv.setStack(s, ItemStack.EMPTY);
                    }
                    inv.setStack(fBack, backStack.get().copy());
                    inv.setStack(fClose, closeStack.get().copy());
                    for (int i = 0; i < rowStacks.size(); i++) {
                        inv.setStack(statDef.slots().get(i), rowStacks.get(i).copy());
                    }
                },
                (topSlot, pl, b, t) -> {
                    if (topSlot == fClose) {
                        pl.closeHandledScreen();
                        return;
                    }
                    if (topSlot == fBack) {
                        plugin.getScheduler()
                                .executeSync(
                                        () -> plugin
                                                .getMenuRegistry()
                                                .open(
                                                        FabricMenuKeys.SKILLS,
                                                        pl,
                                                        plugin,
                                                        FabricMenuContext.none()));
                        return;
                    }
                    if (!fStatClicks) {
                        return;
                    }
                    int idx = statDef.slots().indexOf(topSlot);
                    if (idx < 0 || idx >= gridStats.size()) {
                        return;
                    }
                    Stat st = gridStats.get(idx);
                    if (!st.isEnabled()) {
                        return;
                    }
                    final Stat stat = st;
                    plugin.getScheduler()
                            .executeSync(() -> FabricAuraSkillsMenus.openStatInfo(stat, pl, plugin, 0));
                });
        return true;
    }

    private static int requiredUtilitySlot(GraniteMenuTemplate template, String normalizedAction) {
        return GraniteTemplateItems.findFirstByAction(template, normalizedAction)
                .filter(item -> item.slots().size() == 1)
                .map(item -> item.slots().getFirst())
                .orElse(-1);
    }

    static List<Stat> resolveLockedStats(FabricAuraSkills plugin) {
        List<Stat> out = new ArrayList<>(EXPECTED_STAT_COUNT);
        for (String id : LOCKED_STAT_IDS) {
            Stat found = null;
            for (Stat st : plugin.getStatManager().getEnabledStats()) {
                if (id.equalsIgnoreCase(st.getId().getSimpleName())) {
                    found = st;
                    break;
                }
            }
            if (found != null && found.isEnabled()) {
                out.add(found);
            }
        }
        return out;
    }

    private static void addStatRowPlaceholders(
            Map<String, String> m,
            Stat stat,
            User user,
            FabricAuraSkills plugin,
            MessageProvider msg,
            Locale locale) {
        m.put("stat", stat.getId().getSimpleName());
        m.put("stat_display", msg.getStatDisplayName(stat, locale, true));
        m.put("stat_value", FabricStatsMenuGui.formatStatLevel(stat, user, plugin, locale));
        // Sprint 114: hub rows overwrite lore in code; YAML placeholders need a dummy for expansion.
        m.put("stat_description", "");
    }
}
