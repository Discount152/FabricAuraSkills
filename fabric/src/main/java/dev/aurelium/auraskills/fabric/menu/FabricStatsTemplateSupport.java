package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.message.MessageProvider;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.PlatformUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import dev.granitelib.fabric.template.GraniteTemplateActionNames;
import dev.granitelib.fabric.template.GraniteTemplateItems;
import dev.granitelib.fabric.template.GraniteTemplateResources;
import dev.granitelib.fabric.template.load.GraniteMenuTemplateYamlLoader;
import dev.granitelib.fabric.template.load.GraniteTemplateValidationException;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Sprint 101: template-backed {@code stats} list (paged 28-slot grid). On failure returns {@code false} so
 * {@link FabricStatsMenuGui} uses the built-in path.
 */
public final class FabricStatsTemplateSupport {

    public static final String TEMPLATE_RESOURCE_PATH = "assets/auraskills/granite/templates/stats.yml";

    private static final int PAGE_SIZE = 28;

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
        GraniteMenuTemplate template;
        try (InputStream in =
                GraniteTemplateResources.openClasspath(FabricStatsTemplateSupport.class, TEMPLATE_RESOURCE_PATH)) {
            if (in == null) {
                plugin.logger().warn(
                        "stats template missing from classpath (" + TEMPLATE_RESOURCE_PATH + "); using built-in menu");
                return false;
            }
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            template = GraniteMenuTemplateYamlLoader.loadString(yaml, TEMPLATE_RESOURCE_PATH);
        } catch (IOException e) {
            plugin.logger().warn("stats template read failed; using built-in menu", e);
            return false;
        } catch (GraniteTemplateValidationException e) {
            plugin.logger().warn(
                    "stats template invalid (" + String.join("; ", e.errors()) + "); using built-in menu");
            return false;
        }

        if (!"stats".equalsIgnoreCase(template.id())) {
            plugin.logger().warn(
                    "stats template id must be 'stats', got '" + template.id() + "'; using built-in menu");
            return false;
        }
        if (template.rows() != 6) {
            plugin.logger().warn(
                    "stats template rows must be 6 for 6×9 menu, got " + template.rows() + "; using built-in menu");
            return false;
        }

        GraniteTemplateItem statDef = GraniteTemplateItems.findStatGridDefinition(template);
        if (statDef == null) {
            plugin.logger().warn(
                    "stats template: no stat row (key 'stat' or action stat_row); using built-in menu");
            return false;
        }
        if (statDef.slots().size() != PAGE_SIZE) {
            plugin.logger().warn(
                    "stats template: stat item must have exactly "
                            + PAGE_SIZE
                            + " slots; got "
                            + statDef.slots().size()
                            + "; using built-in menu");
            return false;
        }
        if (!statDef.slots().equals(FabricSourcesMenuGui.builtinSourceGridSlots())) {
            plugin.logger().warn(
                    "stats template: stat slots must match built-in 7×4 grid order; using built-in menu");
            return false;
        }

        boolean statKeyed = statDef == template.items().get("stat");
        boolean statRowAction = GraniteTemplateItems.actionEquals(statDef, GraniteTemplateActionNames.STAT_ROW);
        if (!statKeyed && !statRowAction) {
            plugin.logger().warn(
                    "stats template: stat grid must use YAML key 'stat' or action stat_row; using built-in menu");
            return false;
        }

        Optional<Integer> prevSlotOpt =
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                        template, "previous", List.of(GraniteTemplateActionNames.PREVIOUS_PAGE));
        Optional<Integer> nextSlotOpt =
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                        template, "next", List.of(GraniteTemplateActionNames.NEXT_PAGE));
        if (prevSlotOpt.isEmpty() || nextSlotOpt.isEmpty()) {
            plugin.logger().warn("stats template: missing previous or next item; using built-in menu");
            return false;
        }
        int prev = prevSlotOpt.get();
        int next = nextSlotOpt.get();

        GraniteTemplateItem prevItem = template.items().get("previous");
        GraniteTemplateItem nextItem = template.items().get("next");
        if (prevItem == null || nextItem == null) {
            plugin.logger().warn("stats template: require YAML keys previous, next; using built-in menu");
            return false;
        }
        if (!GraniteTemplateItems.actionEquals(prevItem, GraniteTemplateActionNames.PREVIOUS_PAGE)
                || !GraniteTemplateItems.actionEquals(nextItem, GraniteTemplateActionNames.NEXT_PAGE)) {
            plugin.logger().warn(
                    "stats template: navigation actions must be previous_page, next_page; using built-in menu");
            return false;
        }

        Locale locale = user.getLocale();
        MessageProvider msg = plugin.getMessageProvider();
        List<Stat> order = new ArrayList<>();
        for (Stat s : plugin.getStatManager().getEnabledStats()) {
            order.add(s);
        }
        int total = order.size();
        int maxPage = FabricListMenuPaging.maxPageIndex(total, PAGE_SIZE);
        int p = FabricListMenuPaging.clampPage(context.listPage(), total, PAGE_SIZE);
        int from = p * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, total);
        List<Stat> pageStats = order.subList(from, to);
        boolean statInfoClicks = plugin.configBoolean(Option.MENUS_STAT_INFO_ENABLED);

        Map<String, String> globalPh = buildGlobalPlaceholders(p, maxPage);

        String titleExpanded = FabricStatsPlaceholders.expand(template.title(), globalPh);
        Optional<Text> titleBaseOpt =
                FabricGraniteTemplateStacks.toLiteralNoUnresolved(titleExpanded, plugin, MENU_LOG, "title");
        if (titleBaseOpt.isEmpty()) {
            return false;
        }
        Text pageTitle = FabricListMenuPaging.appendPageToTitleIfNeeded(titleBaseOpt.get(), p, maxPage);

        Optional<ItemStack> fillBase =
                FabricGraniteTemplateStacks.buildFillStack(template.fill(), globalPh, plugin, MENU_LOG);
        if (template.fill().isPresent() && fillBase.isEmpty()) {
            return false;
        }

        var util = plugin.getPlatformUtil();
        List<ItemStack> rowStacks = new ArrayList<>(pageStats.size());
        for (Stat stat : pageStats) {
            Map<String, String> rowPh = new HashMap<>(globalPh);
            addStatRowPlaceholders(rowPh, stat, user, plugin, msg, locale, util);
            Optional<ItemStack> rowStack =
                    FabricGraniteTemplateStacks.buildTemplateItemStack(
                            statDef, rowPh, plugin, MENU_LOG, "items.stat.row");
            if (rowStack.isEmpty()) {
                return false;
            }
            rowStacks.add(rowStack.get());
        }

        Optional<ItemStack> prevStackOpt = Optional.empty();
        Optional<ItemStack> nextStackOpt = Optional.empty();
        if (maxPage > 0) {
            if (p > 0) {
                Optional<ItemStack> ps =
                        FabricGraniteTemplateStacks.buildTemplateItemStack(
                                prevItem, globalPh, plugin, MENU_LOG, "items.previous");
                if (ps.isEmpty()) {
                    return false;
                }
                prevStackOpt = ps;
            }
            if (p < maxPage) {
                Optional<ItemStack> ns =
                        FabricGraniteTemplateStacks.buildTemplateItemStack(
                                nextItem, globalPh, plugin, MENU_LOG, "items.next");
                if (ns.isEmpty()) {
                    return false;
                }
                nextStackOpt = ns;
            }
        }

        final int fPage = p;
        final int fMax = maxPage;
        final int fFrom = from;
        final int fTo = to;
        final int fTotal = total;
        final boolean fStatClicks = statInfoClicks;
        final Optional<ItemStack> fPrevOpt = prevStackOpt;
        final Optional<ItemStack> fNextOpt = nextStackOpt;

        GraniteContainerMenus.openClickable9x6(
                player,
                pageTitle,
                inv -> {
                    if (fillBase.isPresent()) {
                        ItemStack fill = fillBase.get();
                        for (int s = 0; s < FabricMenuSlotLayout.TOP_SLOTS; s++) {
                            inv.setStack(s, fill.copy());
                        }
                    } else {
                        for (int s = 0; s < FabricMenuSlotLayout.TOP_SLOTS; s++) {
                            inv.setStack(s, ItemStack.EMPTY);
                        }
                    }
                    for (int i = 0; i < rowStacks.size(); i++) {
                        inv.setStack(statDef.slots().get(i), rowStacks.get(i).copy());
                    }
                    if (fMax > 0) {
                        fPrevOpt.ifPresent(st -> inv.setStack(prev, st.copy()));
                        fNextOpt.ifPresent(st -> inv.setStack(next, st.copy()));
                    }
                },
                (topSlot, pl, b, t) -> {
                    if (fMax > 0) {
                        if (topSlot == prev) {
                            if (fPage > 0) {
                                plugin.getScheduler()
                                        .executeSync(
                                                () -> plugin
                                                        .getMenuRegistry()
                                                        .open(
                                                                FabricMenuKeys.STATS,
                                                                pl,
                                                                plugin,
                                                                FabricMenuContext.none()
                                                                        .withListPage(fPage - 1)));
                            }
                            return;
                        }
                        if (topSlot == next) {
                            if (fPage < fMax) {
                                plugin.getScheduler()
                                        .executeSync(
                                                () -> plugin
                                                        .getMenuRegistry()
                                                        .open(
                                                                FabricMenuKeys.STATS,
                                                                pl,
                                                                plugin,
                                                                FabricMenuContext.none()
                                                                        .withListPage(fPage + 1)));
                            }
                            return;
                        }
                    }
                    if (!fStatClicks) {
                        return;
                    }
                    int idx = statDef.slots().indexOf(topSlot);
                    if (idx < 0) {
                        return;
                    }
                    int gIdx = fFrom + idx;
                    if (gIdx < fFrom || gIdx >= fTo || gIdx < 0 || gIdx >= fTotal) {
                        return;
                    }
                    Stat st = order.get(gIdx);
                    if (!st.isEnabled()) {
                        return;
                    }
                    final Stat stat = st;
                    plugin.getScheduler()
                            .executeSync(() -> FabricAuraSkillsMenus.openStatInfo(stat, pl, plugin, fPage));
                });
        return true;
    }

    private static Map<String, String> buildGlobalPlaceholders(int pageIndex, int maxPageIndex) {
        Map<String, String> m = new HashMap<>();
        m.put("page", String.valueOf(pageIndex + 1));
        m.put("total_pages", String.valueOf(maxPageIndex + 1));
        return m;
    }

    private static void addStatRowPlaceholders(
            Map<String, String> m,
            Stat stat,
            User user,
            FabricAuraSkills plugin,
            MessageProvider msg,
            Locale locale,
            PlatformUtil util) {
        m.put("stat", stat.getId().getSimpleName());
        m.put("stat_display", msg.getStatDisplayName(stat, locale, true));
        m.put("stat_value", FabricStatsMenuGui.formatStatLevel(stat, user, plugin, locale));
        String desc = msg.getStatDescription(stat, locale, false);
        if (desc == null) {
            desc = "";
        }
        String oneLine = util.toString(util.toComponent(desc.replace('\n', ' ').trim()));
        if (oneLine.length() > 200) {
            oneLine = oneLine.substring(0, 197) + "...";
        }
        m.put("stat_description", oneLine);
    }
}
