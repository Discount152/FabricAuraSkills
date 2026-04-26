package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.command.FabricSourceSortType;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import dev.aurelium.auraskills.fabric.command.FabricSourcesListSupport;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 6×9 “sources” list for one skill, opened from {@code /sources} for in-game players (**Sprint
 * 69**). Control row (back / page / sorter) matches Bukkit default {@code menus/sources.yml} slot
 * positions. **Sprint 98:** tries bundled GraniteLib template first; on failure uses this built-in path.
 */
final class FabricSourcesMenuGui {

    /** Bukkit default {@code sources} items: back row5 col0, prev 5,3, sorter 5,4, next 5,5. */
    private static final int SLOT_BACK = 45;
    private static final int SLOT_PREV = 48;
    private static final int SLOT_SORT = 49;
    private static final int SLOT_NEXT = 50;

    private static final int ITEMS_PER_PAGE = 28;
    private static final int GRID_COLS = 7;

    private FabricSourcesMenuGui() {
    }

    /**
     * Bukkit default grid: {@code source_start 1,1} → {@code source_end 4,8} (7×4, 28 slots), row-major
     * order. Exposed for template validation (**Sprint 98**).
     */
    static List<Integer> builtinSourceGridSlots() {
        List<Integer> out = new ArrayList<>(ITEMS_PER_PAGE);
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            out.add(sourceSlotInPage(i));
        }
        return List.copyOf(out);
    }

    /**
     * Bukkit default grid: {@code source_start 1,1} → {@code source_end 4,8} (7×4, 28 slots).
     */
    private static int sourceSlotInPage(int indexInPage) {
        int r = 1 + indexInPage / GRID_COLS;
        int c = 1 + indexInPage % GRID_COLS;
        return r * 9 + c;
    }

    static void open(
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            Skill skill,
            FabricSourceSortType sort,
            int page,
            int returnToSkillsListPage) {
        User user = plugin.getUserManager().getUser(player.getUuid());
        Locale lo = user.getLocale();
        var loaded = plugin.getSkillManager().getSkill(skill);
        List<XpSource> all = FabricSourcesListSupport.buildSorted(loaded, sort, lo);
        if (all.isEmpty()) {
            return;
        }
        if (FabricSourcesTemplateSupport.tryOpen(
                player, plugin, skill, sort, page, returnToSkillsListPage, all, lo)) {
            return;
        }
        openBuiltIn(player, plugin, skill, sort, page, returnToSkillsListPage, all, lo);
    }

    private static void openBuiltIn(
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            Skill skill,
            FabricSourceSortType sort,
            int page,
            int returnToSkillsListPage,
            List<XpSource> all,
            Locale lo) {
        var msg = plugin.getMessageProvider();
        int maxPage = FabricListMenuPaging.maxPageIndex(all.size(), ITEMS_PER_PAGE);
        int p = FabricListMenuPaging.clampPage(page, all.size(), ITEMS_PER_PAGE);
        int from = p * ITEMS_PER_PAGE;
        int to = Math.min(from + ITEMS_PER_PAGE, all.size());
        String skillName = msg.getSkillDisplayName(skill, lo, true);
        String sortLabel = sortTitle(sort).getString();
        Text baseTitle =
                FabricGraniteText.key("auraskills.granite.menu.sources_title", skillName, sortLabel);
        Text pageTitle = FabricListMenuPaging.appendPageToTitleIfNeeded(baseTitle, p, maxPage);
        var sub = all.subList(from, to);
        final int fPage = p;
        final int fMax = maxPage;
        final FabricSourceSortType fSort = sort;
        GraniteContainerMenus.openClickable9x6(
                player,
                pageTitle,
                inv -> {
                    for (int i = 0; i < FabricMenuSlotLayout.TOP_SLOTS; i++) {
                        inv.setStack(i, ItemStack.EMPTY);
                    }
                    for (int i = 0; i < sub.size(); i++) {
                        XpSource s = sub.get(i);
                        inv.setStack(sourceSlotInPage(i), sourceStack(s, lo));
                    }
                    putControl(inv, SLOT_BACK, true, "auraskills.granite.menu.sources_back", "auraskills.granite.menu.sources_back_lore", Items.ARROW);
                    if (fMax > 0) {
                        if (fPage > 0) {
                            putControl(
                                    inv,
                                    SLOT_PREV,
                                    true,
                                    "auraskills.granite.menu.paging_prev",
                                    "auraskills.granite.menu.paging_prev_lore",
                                    Items.ARROW);
                        }
                        if (fPage < fMax) {
                            putControl(
                                    inv,
                                    SLOT_NEXT,
                                    true,
                                    "auraskills.granite.menu.paging_next",
                                    "auraskills.granite.menu.paging_next_lore",
                                    Items.ARROW);
                        }
                    }
                    putControl(
                            inv,
                            SLOT_SORT,
                            true,
                            "auraskills.granite.menu.sources_sort",
                            "auraskills.granite.menu.sources_sort_lore",
                            Items.HOPPER);
                },
                (topSlot, pl, b, t) -> {
                    if (topSlot == SLOT_BACK) {
                        plugin.getScheduler()
                                .executeSync(
                                        () -> FabricAuraSkillsMenus.openLevelProgression(
                                                skill, pl, plugin, returnToSkillsListPage));
                        return;
                    }
                    if (fMax > 0) {
                        if (topSlot == SLOT_PREV) {
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
                        if (topSlot == SLOT_NEXT) {
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
                    if (topSlot == SLOT_SORT) {
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
    }

    private static Text sortTitle(FabricSourceSortType s) {
        return FabricGraniteText.key(
                "auraskills.granite.sort." + s.name().toLowerCase(Locale.ROOT));
    }

    private static void putControl(
            Inventory inv, int slot, boolean active, String nameKey, String loreKey, net.minecraft.item.Item item) {
        if (!active) {
            return;
        }
        ItemStack s = new ItemStack(item);
        s.set(DataComponentTypes.ITEM_NAME, FabricGraniteText.key(nameKey));
        s.set(
                DataComponentTypes.LORE,
                new LoreComponent(List.of(FabricGraniteText.key(loreKey))));
        inv.setStack(slot, s);
    }

    private static ItemStack sourceStack(XpSource s, Locale lo) {
        ItemStack st = new ItemStack(Items.GRAY_DYE);
        st.set(
                DataComponentTypes.ITEM_NAME,
                Text.literal(s.getDisplayName(lo)));
        List<Text> lore = new ArrayList<>();
        String unit = s.getUnitName(lo);
        if (unit == null) {
            lore.add(
                    FabricGraniteText.key(
                            "auraskills.granite.menu.sources_lore_xp", NumberUtil.format2(s.getXp())));
        } else {
            lore.add(
                    FabricGraniteText.key(
                            "auraskills.granite.menu.sources_lore_xp_per",
                            NumberUtil.format2(s.getXp()),
                            unit));
        }
        lore.add(
                FabricGraniteText.key(
                        "auraskills.granite.menu.sources_lore_id", s.getId().toString()));
        lore.add(
                FabricGraniteText.key(
                        "auraskills.granite.menu.sources_lore_type", s.getType().getId().toString()));
        st.set(DataComponentTypes.LORE, new LoreComponent(lore));
        return st;
    }
}
