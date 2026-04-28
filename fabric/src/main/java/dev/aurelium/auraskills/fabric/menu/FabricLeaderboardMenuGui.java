package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.leaderboard.LeaderboardManager;
import dev.aurelium.auraskills.common.leaderboard.SkillValue;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.command.FabricCommandLeaderboardText;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
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
        var user = plugin.getUserManager().getUser(player.getUuid());
        Locale lo = user != null ? user.getLocale() : plugin.getDefaultLanguage();
        List<SkillValue> slice = fetchPage(lb, mode, skill, page1);
        if (FabricLeaderboardTemplateSupport.tryOpen(
                player, plugin, mode, skill, page1, page0, maxPage0, slice, lo)) {
            return;
        }
        openBuiltIn(player, plugin, mode, skill, page1, page0, maxPage0, slice, lo);
    }

    private static void openBuiltIn(
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            Mode mode,
            @Nullable Skill skill,
            int page1,
            int page0,
            int maxPage0,
            List<SkillValue> slice,
            Locale lo) {
        Text title = makeTitle(mode, skill, page0, maxPage0, lo, plugin);
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
                        inv.setStack(i, ItemStack.EMPTY);
                    }
                    for (int i = 0; i < slice.size() && i < ENTRY_SLOTS.size(); i++) {
                        SkillValue sv = slice.get(i);
                        int rank = FabricCommandLeaderboardText.globalRank1Based(fPage1, i, ENTRIES);
                        String name =
                                FabricCommandLeaderboardText.resolvePlayerName(
                                        plugin.getServer(), sv.id());
                        String value = valueLine(sv, mode, lo, plugin);
                        inv.setStack(ENTRY_SLOTS.get(i), entryStack(rank, name, value));
                    }
                    putControl(
                            inv, SLOT_BACK, true, "auraskills.granite.menu.leaderboard_back", "auraskills.granite.menu.leaderboard_back_lore", Items.IRON_DOOR);
                    if (fMax0 > 0) {
                        putControl(
                                inv,
                                SLOT_PREV,
                                fPage0 > 0,
                                "auraskills.granite.menu.paging_prev",
                                "auraskills.granite.menu.paging_prev_lore",
                                Items.ARROW);
                        putControl(
                                inv,
                                SLOT_NEXT,
                                fPage0 < fMax0,
                                "auraskills.granite.menu.paging_next",
                                "auraskills.granite.menu.paging_next_lore",
                                Items.ARROW);
                    }
                    putControl(
                            inv,
                            SLOT_REFRESH,
                            true,
                            "auraskills.granite.menu.leaderboard_refresh",
                            "auraskills.granite.menu.leaderboard_refresh_lore",
                            Items.COMPASS);
                },
                (topSlot, pl, b, t) -> {
                    if (topSlot == SLOT_BACK) {
                        plugin.getScheduler()
                                .executeSync(
                                        () -> FabricAuraSkillsMenus.open(
                                                FabricMenuKeys.SKILLS, pl, plugin));
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

    private static Text makeTitle(
            Mode mode,
            @Nullable Skill skill,
            int page0,
            int maxPage0,
            Locale lo,
            FabricAuraSkills plugin) {
        String line =
                switch (mode) {
                    case POWER ->
                            FabricGraniteText.key("auraskills.granite.menu.leaderboard_title_power").getString();
                    case AVERAGE ->
                            FabricGraniteText.key("auraskills.granite.menu.leaderboard_title_average").getString();
                    case SKILL -> {
                        String sname =
                                skill == null
                                        ? "?"
                                        : plugin.getMessageProvider()
                                                .getSkillDisplayName(skill, lo, true);
                        yield FabricGraniteText.key(
                                        "auraskills.granite.menu.leaderboard_title_skill", sname)
                                .getString();
                    }
                };
        Text base = FabricMenuTitles.fromLeaderboardContextLine(line);
        return FabricListMenuPaging.appendPageToTitleIfNeeded(base, page0, maxPage0);
    }

    /** Formatted value column; shared with {@link FabricLeaderboardTemplateSupport}. */
    static String valueLine(
            SkillValue sv, Mode mode, Locale lo, FabricAuraSkills plugin) {
        return switch (mode) {
            case POWER, SKILL -> String.valueOf(sv.level());
            case AVERAGE -> NumberUtil.format2(sv.xp());
        };
    }

    private static ItemStack entryStack(int rank, String name, String value) {
        ItemStack s = new ItemStack(Items.PAPER);
        s.set(
                DataComponentTypes.ITEM_NAME,
                FabricGraniteText.key("auraskills.granite.menu.leaderboard_entry_name", rank, name));
        s.set(
                DataComponentTypes.LORE,
                new LoreComponent(
                        List.of(
                                FabricGraniteText.key(
                                        "auraskills.granite.menu.leaderboard_entry_value", value))));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(s);
        return s;
    }

    private static void putControl(
            Inventory inv,
            int slot,
            boolean active,
            String nameKey,
            String loreKey,
            net.minecraft.item.Item item) {
        if (!active) {
            return;
        }
        ItemStack s = new ItemStack(item);
        s.set(DataComponentTypes.ITEM_NAME, FabricGraniteText.key(nameKey));
        s.set(
                DataComponentTypes.LORE,
                new LoreComponent(List.of(FabricGraniteText.key(loreKey))));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(s);
        inv.setStack(slot, s);
    }
}
