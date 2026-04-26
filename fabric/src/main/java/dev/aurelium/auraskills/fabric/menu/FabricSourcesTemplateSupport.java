package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.message.MessageProvider;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.command.FabricSourceSortType;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

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
 * Sprint 98: template-backed {@code sources} (dynamic grid + sort/paging). On failure returns {@code false} so
 * {@link FabricSourcesMenuGui} uses the built-in path.
 */
public final class FabricSourcesTemplateSupport {

    public static final String TEMPLATE_RESOURCE_PATH = "assets/auraskills/granite/templates/sources.yml";

    private static final int BUILTIN_PAGE_SIZE = 28;

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
        GraniteMenuTemplate template;
        try (InputStream in = openBundledTemplate()) {
            if (in == null) {
                plugin.logger().warn(
                        "sources template missing from classpath (" + TEMPLATE_RESOURCE_PATH + "); using built-in menu");
                return false;
            }
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            template = GraniteMenuTemplateYamlLoader.loadString(yaml, TEMPLATE_RESOURCE_PATH);
        } catch (IOException e) {
            plugin.logger().warn("sources template read failed; using built-in menu", e);
            return false;
        } catch (GraniteTemplateValidationException e) {
            plugin.logger().warn(
                    "sources template invalid (" + String.join("; ", e.errors()) + "); using built-in menu");
            return false;
        }

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

        GraniteTemplateItem sourceDef = findSourceRowDef(template);
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

        Optional<Integer> backSlot = resolveNavSlot(template, "back", List.of("back"));
        Optional<Integer> prevSlot = resolveNavSlot(template, "previous", List.of("previous_page"));
        Optional<Integer> nextSlot = resolveNavSlot(template, "next", List.of("next_page"));
        Optional<Integer> sortSlot = resolveNavSlot(template, "sort", List.of("cycle_sort"));
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

        if (!navActionMatches(backItem, "back")
                || !navActionMatches(prevItem, "previous_page")
                || !navActionMatches(nextItem, "next_page")
                || !navActionMatches(sortItem, "cycle_sort")) {
            plugin.logger().warn(
                    "sources template: navigation actions must be back, previous_page, next_page, cycle_sort; using built-in menu");
            return false;
        }

        boolean sourceKeyed = sourceDef == template.items().get("source");
        boolean sourceRowAction =
                sourceDef.action().filter(a -> "source_row".equalsIgnoreCase(a)).isPresent();
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

        Text titleBase = parseTitleText(expandTemplate(template.title(), globalPh), plugin);
        if (titleBase == null) {
            return false;
        }
        Text pageTitle = FabricListMenuPaging.appendPageToTitleIfNeeded(titleBase, p, maxPage);

        Optional<ItemStack> fillBase = buildFillStack(template, globalPh, plugin);
        if (template.fill().isPresent() && fillBase.isEmpty()) {
            return false;
        }

        List<ItemStack> rowStacks = new ArrayList<>(sub.size());
        for (XpSource src : sub) {
            Map<String, String> rowPh = new HashMap<>(globalPh);
            addSourcePlaceholders(rowPh, src, locale);
            Optional<ItemStack> rowStack =
                    buildNavItemStack(sourceDef, rowPh, plugin, "items.source.row");
            if (rowStack.isEmpty()) {
                return false;
            }
            rowStacks.add(rowStack.get());
        }

        Optional<ItemStack> backStack = buildNavItemStack(backItem, globalPh, plugin, "items.back");
        Optional<ItemStack> sortStack = buildNavItemStack(sortItem, globalPh, plugin, "items.sort");
        if (backStack.isEmpty() || sortStack.isEmpty()) {
            return false;
        }

        Optional<ItemStack> prevOpt = Optional.empty();
        Optional<ItemStack> nextOpt = Optional.empty();
        if (maxPage > 0) {
            if (p > 0) {
                Optional<ItemStack> ps = buildNavItemStack(prevItem, globalPh, plugin, "items.previous");
                if (ps.isEmpty()) {
                    return false;
                }
                prevOpt = ps;
            }
            if (p < maxPage) {
                Optional<ItemStack> ns = buildNavItemStack(nextItem, globalPh, plugin, "items.next");
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

    private static boolean navActionMatches(GraniteTemplateItem item, String expected) {
        return item.action().map(a -> expected.equalsIgnoreCase(a.trim())).orElse(false);
    }

    private static @Nullable InputStream openBundledTemplate() {
        return FabricSourcesTemplateSupport.class.getClassLoader().getResourceAsStream(TEMPLATE_RESOURCE_PATH);
    }

    static @Nullable GraniteTemplateItem findSourceRowDef(GraniteMenuTemplate template) {
        GraniteTemplateItem s = template.items().get("source");
        if (s != null) {
            return s;
        }
        for (Map.Entry<String, GraniteTemplateItem> e : template.items().entrySet()) {
            if (e.getValue().action().filter(a -> "source_row".equalsIgnoreCase(a)).isPresent()) {
                return e.getValue();
            }
        }
        return null;
    }

    static Optional<Integer> resolveNavSlot(
            GraniteMenuTemplate template, String preferredKey, List<String> actionNames) {
        GraniteTemplateItem byKey = template.items().get(preferredKey);
        if (byKey != null && !byKey.slots().isEmpty()) {
            return Optional.of(byKey.slots().getFirst());
        }
        for (GraniteTemplateItem item : template.items().values()) {
            for (String a : actionNames) {
                if (item.action().filter(x -> a.equalsIgnoreCase(x)).isPresent() && !item.slots().isEmpty()) {
                    return Optional.of(item.slots().getFirst());
                }
            }
        }
        return Optional.empty();
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

    private static String expandTemplate(String raw, Map<String, String> ph) {
        return FabricSourcesPlaceholders.expand(raw, ph);
    }

    private static @Nullable Text parseTitleText(String expanded, FabricAuraSkills plugin) {
        if (FabricSourcesPlaceholders.hasUnresolvedBraceSyntax(expanded)) {
            plugin.logger().warn(
                    "sources template title still contains unresolved placeholders; using built-in menu");
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
        Optional<Item> item = FabricStatInfoTemplateSupport.resolveStrict(fill.material(), plugin, "fill.material");
        if (item.isEmpty()) {
            return Optional.empty();
        }
        ItemStack stack = new ItemStack(item.get(), 1);
        if (fill.displayName().isPresent()) {
            String name = expandTemplate(fill.displayName().get(), ph);
            if (FabricSourcesPlaceholders.hasUnresolvedBraceSyntax(name)) {
                plugin.logger().warn("sources template fill.name unresolved; using built-in menu");
                return Optional.empty();
            }
            stack.set(DataComponentTypes.ITEM_NAME, Text.literal(name));
        }
        return Optional.of(stack);
    }

    static Optional<ItemStack> buildNavItemStack(
            GraniteTemplateItem item, Map<String, String> ph, FabricAuraSkills plugin, String logCtx) {
        Optional<Item> resolved =
                FabricStatInfoTemplateSupport.resolveStrict(item.material(), plugin, logCtx + ".material");
        if (resolved.isEmpty()) {
            return Optional.empty();
        }
        int count = item.amount().orElse(1);
        ItemStack stack = new ItemStack(resolved.get(), Math.max(1, count));
        if (item.displayName().isPresent()) {
            String name = expandTemplate(item.displayName().get(), ph);
            if (FabricSourcesPlaceholders.hasUnresolvedBraceSyntax(name)) {
                plugin.logger().warn(
                        "sources template " + logCtx + ".name unresolved; using built-in menu");
                return Optional.empty();
            }
            stack.set(DataComponentTypes.ITEM_NAME, Text.literal(name));
        }
        List<Text> loreLines = new ArrayList<>();
        for (String loreRaw : item.lore()) {
            if (loreRaw.isEmpty()) {
                loreLines.add(Text.empty());
                continue;
            }
            String line = expandTemplate(loreRaw, ph);
            if (FabricSourcesPlaceholders.hasUnresolvedBraceSyntax(line)) {
                plugin.logger().warn(
                        "sources template " + logCtx + ".lore unresolved; using built-in menu");
                return Optional.empty();
            }
            loreLines.add(Text.literal(line));
        }
        if (!loreLines.isEmpty()) {
            stack.set(DataComponentTypes.LORE, new LoreComponent(loreLines));
        }
        FabricStatInfoItemComponents.applyCustomModelData(stack, item.customModelData());
        return Optional.of(stack);
    }
}
