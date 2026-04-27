package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.message.MessageProvider;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.command.FabricSourceSortType;
import dev.aurelium.auraskills.fabric.command.FabricSourcesListSupport;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
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
 * Sprint 100: template-backed {@code level_progression} (paged level grid). On failure returns {@code false} so
 * {@link FabricLevelProgressionMenuGui} uses the built-in path.
 */
public final class FabricLevelProgressionTemplateSupport {

    public static final String TEMPLATE_RESOURCE_PATH = "assets/auraskills/granite/templates/level_progression.yml";

    private static final int PAGE_SIZE = 28;

    private static final String MENU_LOG = "level_progression template";

    private FabricLevelProgressionTemplateSupport() {
    }

    public static boolean tryOpen(
            ServerPlayerEntity player, FabricAuraSkills plugin, FabricMenuContext context, User user) {
        try {
            return tryOpenInner(player, plugin, context, user);
        } catch (RuntimeException e) {
            plugin.logger().warn("level_progression template render failed; using built-in menu", e);
            return false;
        }
    }

    private static boolean tryOpenInner(
            ServerPlayerEntity player, FabricAuraSkills plugin, FabricMenuContext context, User user) {
        Skill skill = context.skill();
        if (skill == null) {
            return false;
        }
        Optional<FabricGraniteMenuTemplateLoader.LoadedTemplate> loaded =
                plugin.menuTemplateRegistry().getLoadedTemplate("level_progression");
        if (loaded.isEmpty()) {
            return false;
        }
        GraniteMenuTemplate template = loaded.get().template();

        if (!"level_progression".equalsIgnoreCase(template.id())) {
            plugin.logger().warn(
                    "level_progression template id must be 'level_progression', got '"
                            + template.id()
                            + "'; using built-in menu");
            return false;
        }
        if (template.rows() != 6) {
            plugin.logger().warn(
                    "level_progression template rows must be 6 for 6×9 menu, got "
                            + template.rows()
                            + "; using built-in menu");
            return false;
        }

        GraniteTemplateItem levelDef = GraniteTemplateItems.findLevelGridDefinition(template);
        if (levelDef == null) {
            plugin.logger().warn(
                    "level_progression template: no level grid (key 'level' or action level_row); using built-in menu");
            return false;
        }
        if (levelDef.slots().size() != PAGE_SIZE) {
            plugin.logger().warn(
                    "level_progression template: level item must have exactly "
                            + PAGE_SIZE
                            + " slots; got "
                            + levelDef.slots().size()
                            + "; using built-in menu");
            return false;
        }
        if (!levelDef.slots().equals(FabricSourcesMenuGui.builtinSourceGridSlots())) {
            plugin.logger().warn(
                    "level_progression template: level slots must match built-in 7×4 grid order; using built-in menu");
            return false;
        }

        boolean levelKeyed = levelDef == template.items().get("level");
        boolean levelRowAction = GraniteTemplateItems.actionEquals(levelDef, GraniteTemplateActionNames.LEVEL_ROW);
        if (!levelKeyed && !levelRowAction) {
            plugin.logger().warn(
                    "level_progression template: level grid must use YAML key 'level' or action level_row; using built-in menu");
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
        Optional<Integer> sourcesSlot =
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                        template, "sources", List.of(GraniteTemplateActionNames.OPEN_SOURCES));
        if (backSlot.isEmpty() || prevSlot.isEmpty() || nextSlot.isEmpty() || sourcesSlot.isEmpty()) {
            plugin.logger().warn(
                    "level_progression template: missing navigation (back/previous/next/sources); using built-in menu");
            return false;
        }
        int back = backSlot.get();
        int prev = prevSlot.get();
        int next = nextSlot.get();
        int sourcesS = sourcesSlot.get();

        GraniteTemplateItem backItem = template.items().get("back");
        GraniteTemplateItem prevItem = template.items().get("previous");
        GraniteTemplateItem nextItem = template.items().get("next");
        GraniteTemplateItem sourcesItem = template.items().get("sources");
        if (backItem == null || prevItem == null || nextItem == null || sourcesItem == null) {
            plugin.logger().warn(
                    "level_progression template: require YAML keys back, previous, next, sources; using built-in menu");
            return false;
        }
        if (!GraniteTemplateItems.actionEquals(backItem, GraniteTemplateActionNames.BACK)
                || !GraniteTemplateItems.actionEquals(prevItem, GraniteTemplateActionNames.PREVIOUS_PAGE)
                || !GraniteTemplateItems.actionEquals(nextItem, GraniteTemplateActionNames.NEXT_PAGE)
                || !GraniteTemplateItems.actionEquals(sourcesItem, GraniteTemplateActionNames.OPEN_SOURCES)) {
            plugin.logger().warn(
                    "level_progression template: navigation actions must be back, previous_page, next_page, open_sources; using built-in menu");
            return false;
        }

        GraniteTemplateItem abilitiesItem = template.items().get("abilities");
        Optional<Integer> abilitiesSlot = Optional.empty();
        if (abilitiesItem != null) {
            if (!GraniteTemplateItems.actionEquals(abilitiesItem, GraniteTemplateActionNames.OPEN_ABILITIES)
                    || abilitiesItem.slots().isEmpty()) {
                plugin.logger().warn(
                        "level_progression template: abilities item must use action open_abilities with a slot; using built-in menu");
                return false;
            }
            abilitiesSlot = Optional.of(abilitiesItem.slots().getFirst());
        }

        Locale locale = user.getLocale();
        MessageProvider msg = plugin.getMessageProvider();
        int maxLevel = skill.getMaxLevel();
        List<Integer> levels = new ArrayList<>(maxLevel);
        for (int L = 1; L <= maxLevel; L++) {
            levels.add(L);
        }
        if (levels.isEmpty()) {
            return false;
        }

        int maxPage = FabricListMenuPaging.maxPageIndex(levels.size(), PAGE_SIZE);
        int p = FabricListMenuPaging.clampPage(context.progressionPage(), levels.size(), PAGE_SIZE);
        int from = p * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, levels.size());
        List<Integer> pageLevels = levels.subList(from, to);

        Map<String, String> globalPh = buildGlobalPlaceholders(skill, p, maxPage, locale, msg);

        String titleExpanded = FabricLevelProgressionPlaceholders.expand(template.title(), globalPh);
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

        int userLevel = user.getSkillLevel(skill);
        double currentXp = user.getSkillXp(skill);
        var xpReqs = plugin.getXpRequirements();

        List<ItemStack> rowStacks = new ArrayList<>(pageLevels.size());
        for (Integer displayLevel : pageLevels) {
            Map<String, String> rowPh = new HashMap<>(globalPh);
            addLevelRowPlaceholders(rowPh, userLevel, displayLevel, currentXp, skill, maxLevel, xpReqs, plugin);
            Optional<ItemStack> rowStack =
                    FabricGraniteTemplateStacks.buildTemplateItemStack(
                            levelDef, rowPh, plugin, MENU_LOG, "items.level.row");
            if (rowStack.isEmpty()) {
                return false;
            }
            rowStacks.add(rowStack.get());
        }

        Optional<ItemStack> backStack =
                FabricGraniteTemplateStacks.buildTemplateItemStack(
                        backItem, globalPh, plugin, MENU_LOG, "items.back");
        Optional<ItemStack> sourcesStack =
                FabricGraniteTemplateStacks.buildTemplateItemStack(
                        sourcesItem, globalPh, plugin, MENU_LOG, "items.sources");
        if (backStack.isEmpty() || sourcesStack.isEmpty()) {
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

        Optional<ItemStack> abilitiesStack = Optional.empty();
        if (abilitiesItem != null) {
            Optional<ItemStack> ab =
                    FabricGraniteTemplateStacks.buildTemplateItemStack(
                            abilitiesItem, globalPh, plugin, MENU_LOG, "items.abilities");
            if (ab.isEmpty()) {
                return false;
            }
            abilitiesStack = ab;
        }

        final int fPage = p;
        final int fMax = maxPage;
        final Optional<ItemStack> fPrevOpt = prevOpt;
        final Optional<ItemStack> fNextOpt = nextOpt;
        final int returnSkillsPage = context.listPage();
        final Optional<Integer> fAbilitiesSlot = abilitiesSlot;
        final Optional<ItemStack> fAbilitiesStack = abilitiesStack;

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
                        inv.setStack(levelDef.slots().get(i), rowStacks.get(i).copy());
                    }
                    inv.setStack(back, backStack.get().copy());
                    inv.setStack(sourcesS, sourcesStack.get().copy());
                    if (fMax > 0) {
                        fPrevOpt.ifPresent(st -> inv.setStack(prev, st.copy()));
                        fNextOpt.ifPresent(st -> inv.setStack(next, st.copy()));
                    }
                    fAbilitiesSlot.ifPresent(
                            as -> fAbilitiesStack.ifPresent(st -> inv.setStack(as, st.copy())));
                },
                (topSlot, pl, b, t) -> {
                    if (topSlot == back) {
                        plugin.getScheduler()
                                .executeSync(
                                        () ->
                                                FabricAuraSkillsMenus.open(
                                                        FabricAuraSkillsMenus.Id.SKILLS,
                                                        pl,
                                                        plugin,
                                                        FabricMenuContext.none()
                                                                .withListPage(returnSkillsPage)));
                        return;
                    }
                    if (topSlot == sourcesS) {
                        final Skill sk = skill;
                        plugin.getScheduler()
                                .executeSync(
                                        () -> {
                                            User u = plugin.getUserManager().getUser(pl.getUuid());
                                            Locale lo = u != null ? u.getLocale() : plugin.getDefaultLanguage();
                                            if (FabricSourcesListSupport.buildSorted(
                                                            plugin.getSkillManager().getSkill(sk),
                                                            FabricSourceSortType.ASCENDING,
                                                            lo)
                                                    .isEmpty()) {
                                                pl.sendMessage(
                                                        FabricGraniteText.key(
                                                                "auraskills.granite.command.sources.empty",
                                                                sk.getDisplayName(lo, false)),
                                                        false);
                                                return;
                                            }
                                            FabricAuraSkillsMenus.openSources(
                                                    sk,
                                                    pl,
                                                    plugin,
                                                    FabricSourceSortType.ASCENDING,
                                                    0,
                                                    returnSkillsPage);
                                        });
                        return;
                    }
                    if (fAbilitiesSlot.isPresent() && topSlot == fAbilitiesSlot.get()) {
                        final Skill sk = skill;
                        plugin.getScheduler()
                                .executeSync(
                                        () ->
                                                FabricAuraSkillsMenus.openAbilities(
                                                        sk, pl, plugin, returnSkillsPage));
                        return;
                    }
                    if (fMax > 0) {
                        if (topSlot == prev && fPage > 0) {
                            final Skill sk = skill;
                            plugin.getScheduler()
                                    .executeSync(
                                            () ->
                                                    FabricAuraSkillsMenus.open(
                                                            FabricMenuKeys.LEVEL_PROGRESSION,
                                                            pl,
                                                            plugin,
                                                            FabricMenuContext.ofSkill(sk)
                                                                    .withListPage(returnSkillsPage)
                                                                    .withProgressionPage(fPage - 1)));
                            return;
                        }
                        if (topSlot == next && fPage < fMax) {
                            final Skill sk = skill;
                            plugin.getScheduler()
                                    .executeSync(
                                            () ->
                                                    FabricAuraSkillsMenus.open(
                                                            FabricMenuKeys.LEVEL_PROGRESSION,
                                                            pl,
                                                            plugin,
                                                            FabricMenuContext.ofSkill(sk)
                                                                    .withListPage(returnSkillsPage)
                                                                    .withProgressionPage(fPage + 1)));
                        }
                    }
                });
        return true;
    }

    private static Map<String, String> buildGlobalPlaceholders(
            Skill skill, int pageIndex, int maxPageIndex, Locale locale, MessageProvider msg) {
        Map<String, String> m = new HashMap<>();
        m.put("skill", skill.getId().getSimpleName());
        m.put("skill_display", msg.getSkillDisplayName(skill, locale, true));
        m.put("page", String.valueOf(pageIndex + 1));
        m.put("total_pages", String.valueOf(maxPageIndex + 1));
        return m;
    }

    private static void addLevelRowPlaceholders(
            Map<String, String> m,
            int userLevel,
            int displayLevel,
            double currentXp,
            Skill skill,
            int maxLevel,
            dev.aurelium.auraskills.common.level.XpRequirements xpReqs,
            FabricAuraSkills plugin) {
        m.put("level", String.valueOf(displayLevel));
        m.put("current_xp", NumberUtil.format2(currentXp));
        if (displayLevel >= maxLevel) {
            m.put("level_xp_required", "-");
        } else {
            int req = xpReqs.getXpRequired(skill, displayLevel + 1);
            m.put("level_xp_required", NumberUtil.format2(req));
        }
        m.put("level_status", statusLabel(plugin, userLevel, displayLevel));
    }

    private static String statusLabel(FabricAuraSkills plugin, int userLevel, int displayLevel) {
        if (displayLevel < userLevel) {
            return FabricGraniteText.key("auraskills.granite.menu.level_progression_status_unlocked")
                    .getString();
        }
        if (displayLevel == userLevel) {
            return FabricGraniteText.key("auraskills.granite.menu.level_progression_status_current")
                    .getString();
        }
        return FabricGraniteText.key("auraskills.granite.menu.level_progression_status_locked").getString();
    }
}
