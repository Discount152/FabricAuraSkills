package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.message.MessageProvider;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.command.FabricSourceSortType;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import dev.granitelib.fabric.template.GraniteTemplateActionNames;
import dev.granitelib.fabric.template.GraniteTemplateItems;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Sprint 98: template-backed {@code sources} (dynamic grid + sort/paging). **Sprint 99:** shared GraniteLib
 * helpers + {@link FabricGraniteTemplateStacks}.
 */
public final class FabricSourcesTemplateSupport {

    public static final String TEMPLATE_RESOURCE_PATH = "assets/auraskills/granite/templates/sources.yml";

    private static final int BUILTIN_PAGE_SIZE = 28;

    private static final String MENU_LOG = "sources template";

    private FabricSourcesTemplateSupport() {
    }

    public static boolean tryOpen(
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            Skill skill,
            FabricSourceSortType sort,
            int page,
            int returnToSkillsListPage,
            List<XpSource> allSources,
            Locale locale) {
        try {
            return tryOpenInner(
                    player, plugin, skill, sort, page, returnToSkillsListPage, allSources, locale);
        } catch (RuntimeException e) {
            plugin.logger().warn("sources template render failed; using built-in menu", e);
            return false;
        }
    }

    private static boolean tryOpenInner(
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            Skill skill,
            FabricSourceSortType sort,
            int page,
            int returnToSkillsListPage,
            List<XpSource> allSources,
            Locale locale) {
        Optional<FabricGraniteMenuTemplateLoader.LoadedTemplate> loaded =
                plugin.menuTemplateRegistry().getLoadedTemplate("sources");
        if (loaded.isEmpty()) {
            return false;
        }
        GraniteMenuTemplate template = loaded.get().template();

        if (!"sources".equalsIgnoreCase(template.id())) {
            plugin.logger().warn(
                    "sources template id must be 'sources', got '" + template.id() + "'; using built-in menu");
            return false;
        }
        if (template.rows() != 6) {
            plugin.logger().warn(
                    "sources template rows must be 6 for 6×9 menu, got "
                            + template.rows()
                            + "; using built-in menu");
            return false;
        }

        GraniteTemplateItem sourceDef = GraniteTemplateItems.findSourceGridDefinition(template);
        if (sourceDef == null) {
            plugin.logger().warn(
                    "sources template: no source row (key 'source' or action source_row); using built-in menu");
            return false;
        }
        if (sourceDef.slots().size() != BUILTIN_PAGE_SIZE) {
            plugin.logger().warn(
                    "sources template: source item must have exactly "
                            + BUILTIN_PAGE_SIZE
                            + " slots; got "
                            + sourceDef.slots().size()
                            + "; using built-in menu");
            return false;
        }
        if (!sourceDef.slots().equals(FabricSourcesMenuGui.builtinSourceGridSlots())) {
            plugin.logger().warn(
                    "sources template: source slots must match built-in 7×4 grid order; using built-in menu");
            return false;
        }

        Optional<Integer> backSlot =
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                        template, "back", List.of(GraniteTemplateActionNames.BACK));
        Optional<Integer> prevSlot =
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                        template, "previous", List.of(GraniteTemplateActionNames.PREVIOUS_PAGE));
        Optional<Integer> nextSlot =
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                        template, "next", List.of(GraniteTemplateActionNames.NEXT_PAGE));
        Optional<Integer> sortSlot =
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                        template, "sort", List.of(GraniteTemplateActionNames.CYCLE_SORT));
        if (backSlot.isEmpty() || prevSlot.isEmpty() || nextSlot.isEmpty() || sortSlot.isEmpty()) {
            plugin.logger().warn(
                    "sources template: missing navigation item (back/previous/next/sort); using built-in menu");
            return false;
        }
        int back = backSlot.get();
        int prev = prevSlot.get();
        int next = nextSlot.get();
        int sortS = sortSlot.get();

        GraniteTemplateItem backItem = template.items().get("back");
        GraniteTemplateItem prevItem = template.items().get("previous");
        GraniteTemplateItem nextItem = template.items().get("next");
        GraniteTemplateItem sortItem = template.items().get("sort");
        if (backItem == null || prevItem == null || nextItem == null || sortItem == null) {
            plugin.logger().warn(
                    "sources template: require YAML keys back, previous, next, sort; using built-in menu");
            return false;
        }

        if (!GraniteTemplateItems.actionEquals(backItem, GraniteTemplateActionNames.BACK)
                || !GraniteTemplateItems.actionEquals(prevItem, GraniteTemplateActionNames.PREVIOUS_PAGE)
                || !GraniteTemplateItems.actionEquals(nextItem, GraniteTemplateActionNames.NEXT_PAGE)
                || !GraniteTemplateItems.actionEquals(sortItem, GraniteTemplateActionNames.CYCLE_SORT)) {
            plugin.logger().warn(
                    "sources template: navigation actions must be back, previous_page, next_page, cycle_sort; using built-in menu");
            return false;
        }

        boolean sourceKeyed = sourceDef == template.items().get("source");
        boolean sourceRowAction = GraniteTemplateItems.actionEquals(sourceDef, GraniteTemplateActionNames.SOURCE_ROW);
        if (!sourceKeyed && !sourceRowAction) {
            plugin.logger().warn(
                    "sources template: source grid must use YAML key 'source' or action source_row; using built-in menu");
            return false;
        }

        MessageProvider msg = plugin.getMessageProvider();
        int maxPage = FabricListMenuPaging.maxPageIndex(allSources.size(), BUILTIN_PAGE_SIZE);
        int p = FabricListMenuPaging.clampPage(page, allSources.size(), BUILTIN_PAGE_SIZE);
        int from = p * BUILTIN_PAGE_SIZE;
        int to = Math.min(from + BUILTIN_PAGE_SIZE, allSources.size());
        List<XpSource> sub = allSources.subList(from, to);

        Map<String, String> globalPh = buildGlobalPlaceholders(skill, sort, p, maxPage, locale, msg);

        String titleExpanded = FabricSourcesPlaceholders.expand(template.title(), globalPh);
        Optional<Text> titleBaseOpt =
                FabricGraniteTemplateStacks.toLiteralNoUnresolved(titleExpanded, plugin, MENU_LOG, "title");
        if (titleBaseOpt.isEmpty()) {
            return false;
        }
        Text pageTitle = FabricListMenuPaging.appendPageToTitleIfNeeded(titleBaseOpt.get(), p, maxPage);

        Optional<ItemStack> fillBase = FabricGraniteTemplateStacks.buildFillStack(template.fill(), globalPh, plugin, MENU_LOG);
        if (template.fill().isPresent() && fillBase.isEmpty()) {
            return false;
        }

        List<ItemStack> rowStacks = new ArrayList<>(sub.size());
        for (XpSource src : sub) {
            Map<String, String> rowPh = new HashMap<>(globalPh);
            addSourcePlaceholders(rowPh, src, locale);
            Optional<ItemStack> rowStack =
                    FabricGraniteTemplateStacks.buildTemplateItemStack(
                            sourceDef, rowPh, plugin, MENU_LOG, "items.source.row");
            if (rowStack.isEmpty()) {
                return false;
            }
            rowStacks.add(rowStack.get());
        }

        Optional<ItemStack> backStack =
                FabricGraniteTemplateStacks.buildTemplateItemStack(backItem, globalPh, plugin, MENU_LOG, "items.back");
        Optional<ItemStack> sortStack =
                FabricGraniteTemplateStacks.buildTemplateItemStack(
                        sortItem, globalPh, plugin, MENU_LOG, "items.sort");
        if (backStack.isEmpty() || sortStack.isEmpty()) {
            return false;
        }

        Optional<ItemStack> prevOpt = Optional.empty();
        Optional<ItemStack> nextOpt = Optional.empty();
        if (maxPage > 0) {
            if (p > 0) {
                Optional<ItemStack> ps =
                        FabricGraniteTemplateStacks.buildTemplateItemStack(
                                prevItem, globalPh, plugin, MENU_LOG, "items.previous");
                if (ps.isEmpty()) {
                    return false;
                }
                prevOpt = ps;
            }
            if (p < maxPage) {
                Optional<ItemStack> ns =
                        FabricGraniteTemplateStacks.buildTemplateItemStack(
                                nextItem, globalPh, plugin, MENU_LOG, "items.next");
                if (ns.isEmpty()) {
                    return false;
                }
                nextOpt = ns;
            }
        }

        final int fPage = p;
        final int fMax = maxPage;
        final FabricSourceSortType fSort = sort;
        final Optional<ItemStack> fPrevOpt = prevOpt;
        final Optional<ItemStack> fNextOpt = nextOpt;

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
                        inv.setStack(sourceDef.slots().get(i), rowStacks.get(i).copy());
                    }
                    inv.setStack(back, backStack.get().copy());
                    inv.setStack(sortS, sortStack.get().copy());
                    if (fMax > 0) {
                        fPrevOpt.ifPresent(st -> inv.setStack(prev, st.copy()));
                        fNextOpt.ifPresent(st -> inv.setStack(next, st.copy()));
                    }
                },
                (topSlot, pl, b, t) -> {
                    if (topSlot == back) {
                        plugin.getScheduler()
                                .executeSync(
                                        () -> FabricAuraSkillsMenus.openLevelProgression(
                                                skill, pl, plugin, returnToSkillsListPage));
                        return;
                    }
                    if (fMax > 0) {
                        if (topSlot == prev) {
                            if (fPage > 0) {
                                plugin.getScheduler()
                                        .executeSync(
                                                () -> FabricSourcesMenuGui.open(
                                                        pl,
                                                        plugin,
                                                        skill,
                                                        fSort,
                                                        fPage - 1,
                                                        returnToSkillsListPage));
                            }
                            return;
                        }
                        if (topSlot == next) {
                            if (fPage < fMax) {
                                plugin.getScheduler()
                                        .executeSync(
                                                () -> FabricSourcesMenuGui.open(
                                                        pl,
                                                        plugin,
                                                        skill,
                                                        fSort,
                                                        fPage + 1,
                                                        returnToSkillsListPage));
                            }
                            return;
                        }
                    }
                    if (topSlot == sortS) {
                        plugin.getScheduler()
                                .executeSync(
                                        () -> FabricSourcesMenuGui.open(
                                                pl,
                                                plugin,
                                                skill,
                                                fSort.nextCyclic(),
                                                0,
                                                returnToSkillsListPage));
                    }
                });
        return true;
    }

    /** For tests and callers that need the Sprint 98 discovery rule. */
    static @Nullable GraniteTemplateItem findSourceRowDef(GraniteMenuTemplate template) {
        return GraniteTemplateItems.findSourceGridDefinition(template);
    }

    static Optional<Integer> resolveNavSlot(
            GraniteMenuTemplate template, String preferredKey, List<String> actionNames) {
        return GraniteTemplateItems.firstSlotByPreferredKeyOrActions(template, preferredKey, actionNames);
    }

    private static Map<String, String> buildGlobalPlaceholders(
            Skill skill,
            FabricSourceSortType sort,
            int pageIndex,
            int maxPageIndex,
            Locale locale,
            MessageProvider msg) {
        Map<String, String> m = new HashMap<>();
        m.put("skill", skill.getId().getSimpleName());
        m.put("skill_display", msg.getSkillDisplayName(skill, locale, true));
        m.put(
                "sort",
                FabricGraniteText.key(
                                "auraskills.granite.sort." + sort.name().toLowerCase(Locale.ROOT))
                        .getString());
        m.put("page", String.valueOf(pageIndex + 1));
        m.put("total_pages", String.valueOf(maxPageIndex + 1));
        return m;
    }

    private static void addSourcePlaceholders(Map<String, String> m, XpSource s, Locale locale) {
        m.put("source_name", s.getDisplayName(locale));
        m.put("source_type", s.getType().getId().toString());
        String unit = s.getUnitName(locale);
        if (unit == null) {
            m.put("source_xp", NumberUtil.format2(s.getXp()));
        } else {
            m.put("source_xp", NumberUtil.format2(s.getXp()) + " / " + unit);
        }
    }
}
