package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.leaderboard.LeaderboardManager;
import dev.granite.graniteskills.common.leaderboard.SkillValue;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.command.FabricCommandLeaderboardText;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * 6×9 bounded Granite leaderboard browser (**Sprint 72**). Bukkit {@code LeaderboardMenu} is
 * Slate (10 template slots, skulls per row); this uses the same 10/ page as {@code /top} chat,
 * with paging and **refresh** (re-reads cached lists, no re-sort). Modes: **power**, **average**,
 * **per-skill**. **Sprint 103:** tries bundled {@link FabricLeaderboardTemplateSupport} first.
 */
public final class FabricLeaderboardMenuGui {

    public enum Mode {
        POWER,
        AVERAGE,
        SKILL
    }

    private static final int ENTRIES = FabricCommandLeaderboardText.ENTRIES_PER_PAGE;
    private static final int SLOT_BACK = 45;
    private static final int SLOT_POWER = 46;
    private static final int SLOT_AVERAGE = 47;
    private static final int SLOT_SKILL = 52;
    private static final int SLOT_PREV = 48;
    private static final int SLOT_REFRESH = 49;
    private static final int SLOT_NEXT = 50;

    /** Two rows of five (row0: 0-4, row1: 9-13) — 10 lines/page, matches chat. */
    private static final List<Integer> ENTRY_SLOTS = List.of(0, 1, 2, 3, 4, 9, 10, 11, 12, 13);

    private FabricLeaderboardMenuGui() {}

    /** Slot order for leaderboard entry rows; must match bundled {@code leaderboard.yml}. */
    public static List<Integer> builtinEntrySlots() {
        return ENTRY_SLOTS;
    }

    public static void open(
            ServerPlayer player,
            FabricGraniteSkills plugin,
            Mode mode,
            @Nullable Skill skill,
            int page) {
        LeaderboardManager lb = plugin.getLeaderboardManager();
        int total = totalSize(lb, mode, skill);
        int maxPage0 = FabricListMenuPaging.maxPageIndex(Math.max(total, 0), ENTRIES);
        /** 1-based page, aligned with {@link FabricLeaderboardCommands} and {@link LeaderboardManager}. */
        int page1 =
                FabricListMenuPaging.clampPage(Math.max(1, page) - 1, total, ENTRIES) + 1;
        int page0 = page1 - 1;
        String skillKey = skill == null ? "" : skill.getId().getSimpleName();
        FabricMenuNavigation.remember(player, FabricMenuKeys.LEADERBOARD, skillKey, page1, mode.name());
        var user = plugin.getUserManager().getUser(player.getUUID());
        Locale lo = user != null ? user.getLocale() : plugin.getDefaultLanguage();
        List<SkillValue> slice = fetchPage(lb, mode, skill, page1);
        if (FabricLeaderboardTemplateSupport.tryOpen(
                player, plugin, mode, skill, page1, page0, maxPage0, slice, lo)) {
            return;
        }
        openBuiltIn(player, plugin, mode, skill, page1, page0, maxPage0, slice, lo);
    }

    private static void openBuiltIn(
            ServerPlayer player,
            FabricGraniteSkills plugin,
            Mode mode,
            @Nullable Skill skill,
            int page1,
            int page0,
            int maxPage0,
            List<SkillValue> slice,
            Locale lo) {
        Component title = makeTitle(mode, skill, page0, maxPage0, lo, plugin);
        final int fPage1 = page1;
        final int fPage0 = page0;
        final int fMax0 = maxPage0;
        final Mode fMode = mode;
        @Nullable final Skill fSkill = skill;
        GraniteContainerMenus.openClickable9x6(
                player,
                title,
                inv -> {
                    for (int i = 0; i < FabricMenuSlotLayout.TOP_SLOTS; i++) {
                        inv.setItem(i, ItemStack.EMPTY);
                    }
                    for (int i = 0; i < slice.size() && i < ENTRY_SLOTS.size(); i++) {
                        SkillValue sv = slice.get(i);
                        int rank = FabricCommandLeaderboardText.globalRank1Based(fPage1, i, ENTRIES);
                        String name =
                                FabricCommandLeaderboardText.resolvePlayerName(
                                        plugin.getServer(), sv.id());
                        String value = valueLine(sv, mode, lo, plugin);
                        inv.setItem(ENTRY_SLOTS.get(i), entryStack(rank, name, value));
                    }
                    putControl(
                            inv, SLOT_BACK, true, "graniteskills.granite.menu.leaderboard_back", "graniteskills.granite.menu.leaderboard_back_lore", Items.IRON_DOOR);
                    putControl(
                            inv, SLOT_POWER, fMode != Mode.POWER, "graniteskills.granite.menu.leaderboard_power", "graniteskills.granite.menu.leaderboard_power_lore", Items.GOLD_INGOT);
                    putControl(
                            inv, SLOT_AVERAGE, fMode != Mode.AVERAGE, "graniteskills.granite.menu.leaderboard_average", "graniteskills.granite.menu.leaderboard_average_lore", Items.IRON_INGOT);
                    putControl(
                            inv, SLOT_SKILL, fMode != Mode.SKILL, "graniteskills.granite.menu.leaderboard_skill", "graniteskills.granite.menu.leaderboard_skill_lore", Items.BOOK);
                    if (fMax0 > 0) {
                        putControl(
                                inv,
                                SLOT_PREV,
                                fPage0 > 0,
                                "graniteskills.granite.menu.paging_prev",
                                "graniteskills.granite.menu.paging_prev_lore",
                                Items.ARROW);
                        putControl(
                                inv,
                                SLOT_NEXT,
                                fPage0 < fMax0,
                                "graniteskills.granite.menu.paging_next",
                                "graniteskills.granite.menu.paging_next_lore",
                                Items.ARROW);
                    }
                    putControl(
                            inv,
                            SLOT_REFRESH,
                            true,
                            "graniteskills.granite.menu.leaderboard_refresh",
                            "graniteskills.granite.menu.leaderboard_refresh_lore",
                            Items.COMPASS);
                },
                (topSlot, pl, b, t) -> {
                    if (!FabricMenuClicks.activates(t)) {
                        return;
                    }
                    if (topSlot == SLOT_POWER) {
                        plugin.getScheduler().executeSync(() -> FabricLeaderboardMenuGui.open(pl, plugin, Mode.POWER, null, 1));
                        return;
                    }
                    if (topSlot == SLOT_AVERAGE) {
                        plugin.getScheduler().executeSync(() -> FabricLeaderboardMenuGui.open(pl, plugin, Mode.AVERAGE, null, 1));
                        return;
                    }
                    if (topSlot == SLOT_SKILL) {
                        Skill next = nextListedSkill(plugin, fMode == Mode.SKILL ? fSkill : null);
                        if (next != null) {
                            plugin.getScheduler().executeSync(() -> FabricLeaderboardMenuGui.open(pl, plugin, Mode.SKILL, next, 1));
                        }
                        return;
                    }
                    if (topSlot == SLOT_BACK) {
                        plugin.getScheduler()
                                .executeSync(
                                        () -> {
                                            if (!FabricMenuNavigation.back(pl, plugin)) {
                                                FabricGraniteSkillsMenus.open(FabricMenuKeys.SKILLS, pl, plugin);
                                            }
                                        });
                        return;
                    }
                    if (topSlot == SLOT_REFRESH) {
                        plugin.getScheduler()
                                .executeSync(
                                        () ->
                                                FabricLeaderboardMenuGui.open(
                                                        pl, plugin, fMode, fSkill, fPage1));
                        return;
                    }
                    if (fMax0 > 0) {
                        if (topSlot == SLOT_PREV) {
                            if (fPage0 > 0) {
                                plugin.getScheduler()
                                        .executeSync(
                                                () ->
                                                        FabricLeaderboardMenuGui.open(
                                                                pl,
                                                                plugin,
                                                                fMode,
                                                                fSkill,
                                                                fPage1 - 1));
                            }
                            return;
                        }
                        if (topSlot == SLOT_NEXT) {
                            if (fPage0 < fMax0) {
                                plugin.getScheduler()
                                        .executeSync(
                                                () ->
                                                        FabricLeaderboardMenuGui.open(
                                                                pl,
                                                                plugin,
                                                                fMode,
                                                                fSkill,
                                                                fPage1 + 1));
                            }
                        }
                    }
                });
    }

    private static int totalSize(LeaderboardManager lb, Mode mode, @Nullable Skill skill) {
        return switch (mode) {
            case POWER -> lb.getPowerLeaderboard().size();
            case AVERAGE -> lb.getAverageLeaderboard().size();
            case SKILL -> skill == null ? 0 : lb.getLeaderboard(skill).size();
        };
    }

    /** @param page1 1-based page (same as {@link LeaderboardManager#getPowerLeaderboard(int, int)}). */
    private static List<SkillValue> fetchPage(
            LeaderboardManager lb, Mode mode, @Nullable Skill skill, int page1) {
        return switch (mode) {
            case POWER -> lb.getPowerLeaderboard(page1, ENTRIES);
            case AVERAGE -> lb.getAverageLeaderboard(page1, ENTRIES);
            case SKILL -> skill == null
                    ? List.of()
                    : lb.getLeaderboard(skill, page1, ENTRIES);
        };
    }

    private static Component makeTitle(
            Mode mode,
            @Nullable Skill skill,
            int page0,
            int maxPage0,
            Locale lo,
            FabricGraniteSkills plugin) {
        String line =
                switch (mode) {
                    case POWER ->
                            FabricGraniteText.key("graniteskills.granite.menu.leaderboard_title_power").getString();
                    case AVERAGE ->
                            FabricGraniteText.key("graniteskills.granite.menu.leaderboard_title_average").getString();
                    case SKILL -> {
                        String sname =
                                skill == null
                                        ? "?"
                                        : plugin.getMessageProvider()
                                                .getSkillDisplayName(skill, lo, true);
                        yield FabricGraniteText.key(
                                        "graniteskills.granite.menu.leaderboard_title_skill", sname)
                                .getString();
                    }
                };
        Component base = FabricMenuTitles.fromLeaderboardContextLine(line);
        return FabricListMenuPaging.appendPageToTitleIfNeeded(base, page0, maxPage0);
    }

    static Skill nextListedSkill(FabricGraniteSkills plugin, @Nullable Skill current) {
        Skill first = null;
        boolean takeNext = current == null;
        for (Skill skill : plugin.getSkillManager().getEnabledSkills()) {
            if (first == null) {
                first = skill;
            }
            if (takeNext) {
                return skill;
            }
            if (skill.equals(current)) {
                takeNext = true;
            }
        }
        return first;
    }

    /** Formatted value column; shared with {@link FabricLeaderboardTemplateSupport}. */
    static String valueLine(
            SkillValue sv, Mode mode, Locale lo, FabricGraniteSkills plugin) {
        return switch (mode) {
            case POWER, SKILL -> String.valueOf(sv.level());
            case AVERAGE -> NumberUtil.format2(sv.xp());
        };
    }

    private static ItemStack entryStack(int rank, String name, String value) {
        ItemStack s = new ItemStack(Items.PAPER);
        s.set(
                DataComponents.ITEM_NAME,
                FabricGraniteText.key("graniteskills.granite.menu.leaderboard_entry_name", rank, name));
        s.set(
                DataComponents.LORE,
                new ItemLore(
                        List.of(
                                FabricGraniteText.key(
                                        "graniteskills.granite.menu.leaderboard_entry_value", value))));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(s);
        return s;
    }

    private static void putControl(
            Container inv,
            int slot,
            boolean active,
            String nameKey,
            String loreKey,
            net.minecraft.world.item.Item item) {
        if (!active) {
            return;
        }
        ItemStack s = new ItemStack(item);
        s.set(DataComponents.ITEM_NAME, FabricGraniteText.key(nameKey));
        s.set(
                DataComponents.LORE,
                new ItemLore(List.of(FabricGraniteText.key(loreKey))));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(s);
        inv.setItem(slot, s);
    }
}
