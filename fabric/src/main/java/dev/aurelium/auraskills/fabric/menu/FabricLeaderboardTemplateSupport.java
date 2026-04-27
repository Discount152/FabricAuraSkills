package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.common.leaderboard.SkillValue;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.command.FabricCommandLeaderboardText;
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
 * Sprint 103: template-backed {@code leaderboard} (ranked rows + paging + refresh). On failure returns {@code false}
 * so {@link FabricLeaderboardMenuGui} uses the built-in path.
 */
public final class FabricLeaderboardTemplateSupport {

    public static final String TEMPLATE_RESOURCE_PATH = "assets/auraskills/granite/templates/leaderboard.yml";

    private static final int SLOT_BACK = 45;
    private static final int SLOT_PREV = 48;
    private static final int SLOT_REFRESH = 49;
    private static final int SLOT_NEXT = 50;

    private static final int ENTRIES = FabricCommandLeaderboardText.ENTRIES_PER_PAGE;

    private static final String MENU_LOG = "leaderboard template";

    private FabricLeaderboardTemplateSupport() {}

    public static boolean tryOpen(
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            FabricLeaderboardMenuGui.Mode mode,
            @Nullable Skill skill,
            int page1,
            int page0,
            int maxPage0,
            List<SkillValue> slice,
            Locale lo) {
        try {
            return tryOpenInner(player, plugin, mode, skill, page1, page0, maxPage0, slice, lo);
        } catch (RuntimeException e) {
            plugin.logger().warn("leaderboard template render failed; using built-in menu", e);
            return false;
        }
    }

    private static boolean tryOpenInner(
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            FabricLeaderboardMenuGui.Mode mode,
            @Nullable Skill skill,
            int page1,
            int page0,
            int maxPage0,
            List<SkillValue> slice,
            Locale lo) {
        Optional<FabricGraniteMenuTemplateLoader.LoadedTemplate> loaded =
                plugin.menuTemplateRegistry().getLoadedTemplate("leaderboard");
        if (loaded.isEmpty()) {
            return false;
        }
        GraniteMenuTemplate template = loaded.get().template();

        if (!"leaderboard".equalsIgnoreCase(template.id())) {
            plugin.logger().warn(
                    "leaderboard template id must be 'leaderboard', got '" + template.id() + "'; using built-in menu");
            return false;
        }
        if (template.rows() != 6) {
            plugin.logger().warn(
                    "leaderboard template rows must be 6 for 6×9 menu, got " + template.rows() + "; using built-in menu");
            return false;
        }

        Optional<String> actionContract =
                FabricGraniteMenuTemplateActionContract.validateTemplateActions("leaderboard", template);
        if (actionContract.isPresent()) {
            plugin.logger().warn(MENU_LOG + ": " + actionContract.get() + "; using built-in menu");
            return false;
        }

        GraniteTemplateItem entryDef = GraniteTemplateItems.findLeaderboardEntryDefinition(template);
        if (entryDef == null) {
            plugin.logger().warn(
                    "leaderboard template: no entry row (key 'entry' or action leaderboard_row); using built-in menu");
            return false;
        }
        if (entryDef.slots().size() != ENTRIES) {
            plugin.logger().warn(
                    "leaderboard template: entry item must have exactly "
                            + ENTRIES
                            + " slots; got "
                            + entryDef.slots().size()
                            + "; using built-in menu");
            return false;
        }
        if (!entryDef.slots().equals(FabricLeaderboardMenuGui.builtinEntrySlots())) {
            plugin.logger().warn(
                    "leaderboard template: entry slots must match built-in two-row grid order; using built-in menu");
            return false;
        }

        boolean entryKeyed = entryDef == template.items().get("entry");
        boolean entryRowAction = GraniteTemplateItems.actionEquals(entryDef, GraniteTemplateActionNames.LEADERBOARD_ROW);
        if (!entryKeyed && !entryRowAction) {
            plugin.logger().warn(
                    "leaderboard template: entry grid must use YAML key 'entry' or action leaderboard_row; using built-in menu");
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
        Optional<Integer> refreshSlot =
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                        template, "refresh", List.of(GraniteTemplateActionNames.REFRESH));
        if (backSlot.isEmpty() || prevSlot.isEmpty() || nextSlot.isEmpty() || refreshSlot.isEmpty()) {
            plugin.logger().warn(
                    "leaderboard template: missing navigation item (back/previous/next/refresh); using built-in menu");
            return false;
        }
        if (backSlot.get() != SLOT_BACK
                || prevSlot.get() != SLOT_PREV
                || refreshSlot.get() != SLOT_REFRESH
                || nextSlot.get() != SLOT_NEXT) {
            plugin.logger().warn(
                    "leaderboard template: nav slots must match built-in (back "
                            + SLOT_BACK
                            + ", previous "
                            + SLOT_PREV
                            + ", refresh "
                            + SLOT_REFRESH
                            + ", next "
                            + SLOT_NEXT
                            + "); using built-in menu");
            return false;
        }

        GraniteTemplateItem backItem = template.items().get("back");
        GraniteTemplateItem prevItem = template.items().get("previous");
        GraniteTemplateItem nextItem = template.items().get("next");
        GraniteTemplateItem refreshItem = template.items().get("refresh");
        if (backItem == null || prevItem == null || nextItem == null || refreshItem == null) {
            plugin.logger().warn(
                    "leaderboard template: require YAML keys back, previous, next, refresh; using built-in menu");
            return false;
        }
        if (!GraniteTemplateItems.actionEquals(backItem, GraniteTemplateActionNames.BACK)
                || !GraniteTemplateItems.actionEquals(prevItem, GraniteTemplateActionNames.PREVIOUS_PAGE)
                || !GraniteTemplateItems.actionEquals(nextItem, GraniteTemplateActionNames.NEXT_PAGE)
                || !GraniteTemplateItems.actionEquals(refreshItem, GraniteTemplateActionNames.REFRESH)) {
            plugin.logger().warn(
                    "leaderboard template: navigation actions must be back, previous_page, next_page, refresh; using built-in menu");
            return false;
        }

        int back = backSlot.get();
        int prev = prevSlot.get();
        int next = nextSlot.get();
        int refreshS = refreshSlot.get();

        Map<String, String> globalPh = buildGlobalPlaceholders(mode, skill, page0, maxPage0, lo, plugin);

        String titleExpanded = FabricLeaderboardPlaceholders.expand(template.title(), globalPh);
        Optional<Text> titleBaseOpt =
                FabricGraniteTemplateStacks.toLiteralNoUnresolved(titleExpanded, plugin, MENU_LOG, "title");
        if (titleBaseOpt.isEmpty()) {
            return false;
        }
        Text pageTitle = FabricListMenuPaging.appendPageToTitleIfNeeded(titleBaseOpt.get(), page0, maxPage0);

        Optional<ItemStack> fillBase =
                FabricGraniteTemplateStacks.buildFillStack(template.fill(), globalPh, plugin, MENU_LOG);
        if (template.fill().isPresent() && fillBase.isEmpty()) {
            return false;
        }

        List<ItemStack> rowStacks = new ArrayList<>(slice.size());
        for (int i = 0; i < slice.size() && i < ENTRIES; i++) {
            SkillValue sv = slice.get(i);
            int rank = FabricCommandLeaderboardText.globalRank1Based(page1, i, ENTRIES);
            String name = FabricCommandLeaderboardText.resolvePlayerName(plugin.getServer(), sv.id());
            String value = FabricLeaderboardMenuGui.valueLine(sv, mode, lo, plugin);
            Map<String, String> rowPh = new HashMap<>(globalPh);
            rowPh.put("rank", String.valueOf(rank));
            rowPh.put("player", name);
            rowPh.put("value", value);
            Optional<ItemStack> rowStack =
                    FabricGraniteTemplateStacks.buildTemplateItemStack(
                            entryDef, rowPh, plugin, MENU_LOG, "items.entry.row");
            if (rowStack.isEmpty()) {
                return false;
            }
            rowStacks.add(rowStack.get());
        }

        Optional<ItemStack> backStack =
                FabricGraniteTemplateStacks.buildTemplateItemStack(
                        backItem, globalPh, plugin, MENU_LOG, "items.back");
        if (backStack.isEmpty()) {
            return false;
        }
        Optional<ItemStack> refreshStack =
                FabricGraniteTemplateStacks.buildTemplateItemStack(
                        refreshItem, globalPh, plugin, MENU_LOG, "items.refresh");
        if (refreshStack.isEmpty()) {
            return false;
        }

        Optional<ItemStack> prevStackOpt = Optional.empty();
        Optional<ItemStack> nextStackOpt = Optional.empty();
        if (maxPage0 > 0) {
            if (page0 > 0) {
                Optional<ItemStack> ps =
                        FabricGraniteTemplateStacks.buildTemplateItemStack(
                                prevItem, globalPh, plugin, MENU_LOG, "items.previous");
                if (ps.isEmpty()) {
                    return false;
                }
                prevStackOpt = ps;
            }
            if (page0 < maxPage0) {
                Optional<ItemStack> ns =
                        FabricGraniteTemplateStacks.buildTemplateItemStack(
                                nextItem, globalPh, plugin, MENU_LOG, "items.next");
                if (ns.isEmpty()) {
                    return false;
                }
                nextStackOpt = ns;
            }
        }

        final int fPage1 = page1;
        final int fPage0 = page0;
        final int fMax0 = maxPage0;
        final FabricLeaderboardMenuGui.Mode fMode = mode;
        @Nullable final Skill fSkill = skill;
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
                        inv.setStack(entryDef.slots().get(i), rowStacks.get(i).copy());
                    }
                    inv.setStack(back, backStack.get().copy());
                    inv.setStack(refreshS, refreshStack.get().copy());
                    if (fMax0 > 0) {
                        fPrevOpt.ifPresent(st -> inv.setStack(prev, st.copy()));
                        fNextOpt.ifPresent(st -> inv.setStack(next, st.copy()));
                    }
                },
                (topSlot, pl, b, t) -> {
                    if (topSlot == back) {
                        plugin.getScheduler()
                                .executeSync(
                                        () -> FabricAuraSkillsMenus.open(FabricMenuKeys.SKILLS, pl, plugin));
                        return;
                    }
                    if (topSlot == refreshS) {
                        plugin.getScheduler()
                                .executeSync(() -> FabricLeaderboardMenuGui.open(pl, plugin, fMode, fSkill, fPage1));
                        return;
                    }
                    if (fMax0 > 0) {
                        if (topSlot == prev) {
                            if (fPage0 > 0) {
                                plugin.getScheduler()
                                        .executeSync(
                                                () ->
                                                        FabricLeaderboardMenuGui.open(
                                                                pl, plugin, fMode, fSkill, fPage1 - 1));
                            }
                            return;
                        }
                        if (topSlot == next) {
                            if (fPage0 < fMax0) {
                                plugin.getScheduler()
                                        .executeSync(
                                                () ->
                                                        FabricLeaderboardMenuGui.open(
                                                                pl, plugin, fMode, fSkill, fPage1 + 1));
                            }
                        }
                    }
                });
        return true;
    }

    private static Map<String, String> buildGlobalPlaceholders(
            FabricLeaderboardMenuGui.Mode mode,
            @Nullable Skill skill,
            int page0,
            int maxPage0,
            Locale lo,
            FabricAuraSkills plugin) {
        Map<String, String> m = new HashMap<>();
        m.put(
                "leaderboard_title",
                leaderboardTitleBaseString(mode, skill, lo, plugin));
        m.put("leaderboard_type", leaderboardTypeLabel(mode, skill, lo, plugin));
        m.put("skill", skillKey(mode, skill));
        m.put("skill_display", skillDisplayPlaceholder(mode, skill, lo, plugin));
        m.put("page", String.valueOf(page0 + 1));
        m.put("total_pages", String.valueOf(maxPage0 + 1));
        return m;
    }

    private static String leaderboardTitleBaseString(
            FabricLeaderboardMenuGui.Mode mode,
            @Nullable Skill skill,
            Locale lo,
            FabricAuraSkills plugin) {
        return switch (mode) {
            case POWER ->
                    FabricGraniteText.key("auraskills.granite.menu.leaderboard_title_power").getString();
            case AVERAGE ->
                    FabricGraniteText.key("auraskills.granite.menu.leaderboard_title_average").getString();
            case SKILL -> {
                String sname =
                        skill == null
                                ? "?"
                                : plugin.getMessageProvider().getSkillDisplayName(skill, lo, true);
                yield FabricGraniteText.key("auraskills.granite.menu.leaderboard_title_skill", sname).getString();
            }
        };
    }

    private static String leaderboardTypeLabel(
            FabricLeaderboardMenuGui.Mode mode,
            @Nullable Skill skill,
            Locale lo,
            FabricAuraSkills plugin) {
        return switch (mode) {
            case POWER -> "Power";
            case AVERAGE -> "Skill average";
            case SKILL -> skill == null ? "?" : plugin.getMessageProvider().getSkillDisplayName(skill, lo, true);
        };
    }

    private static String skillKey(FabricLeaderboardMenuGui.Mode mode, @Nullable Skill skill) {
        if (mode != FabricLeaderboardMenuGui.Mode.SKILL || skill == null) {
            return "";
        }
        return skill.getId().getSimpleName();
    }

    private static String skillDisplayPlaceholder(
            FabricLeaderboardMenuGui.Mode mode,
            @Nullable Skill skill,
            Locale lo,
            FabricAuraSkills plugin) {
        return switch (mode) {
            case POWER -> "";
            case AVERAGE -> "Skill average";
            case SKILL -> skill == null ? "?" : plugin.getMessageProvider().getSkillDisplayName(skill, lo, true);
        };
    }
}
