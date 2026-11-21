package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.common.message.type.MenuMessage;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.data.KeyIntPair;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.item.FabricItemRegistry;
import dev.granite.graniteskills.fabric.item.FabricUnclaimedItemsMerge;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

/**
 * 6×9 bounded Granite menu for Bukkit {@code UnclaimedItemsMenu} parity (**Sprint 71**). Uses {@link
 * GraniteContainerMenus#openClickable9x6} only: rows 0–3 show up to 36 unclaimed stacks per page; row 5
 * has prev (48), claim all (49), next (50). **Sprint 105:** tries bundled {@link FabricUnclaimedItemsTemplateSupport}
 * first.
 */
public final class FabricUnclaimedItemsMenuGui {

    static final int ITEMS_PER_PAGE = 36;
    static final int SLOT_PREV = 48;
    static final int SLOT_CLAIM_ALL = 49;
    static final int SLOT_NEXT = 50;

    private static final List<Integer> BUILTIN_CLAIMABLE_SLOTS =
            IntStream.range(0, ITEMS_PER_PAGE).boxed().toList();

    private FabricUnclaimedItemsMenuGui() {}

    /** Slot indices 0..35 in order; must match bundled {@code unclaimed_items.yml}. */
    public static List<Integer> builtinClaimableSlots() {
        return BUILTIN_CLAIMABLE_SLOTS;
    }

    /**
     * Registered-item stack for display/claim (count from pair); preserves template item lore from registry.
     * Unknown keys use barrier placeholder — same as built-in menu.
     */
    static ItemStack baseStackForUnclaimedEntry(FabricGraniteSkills plugin, KeyIntPair pair, Locale lo) {
        FabricItemRegistry reg = plugin.getFabricItemRegistry();
        NamespacedId id;
        try {
            id = NamespacedId.fromDefault(pair.getKey());
        } catch (IllegalArgumentException e) {
            return unknownPlaceholder();
        }
        if (!reg.containsItem(id)) {
            return unknownPlaceholder();
        }
        ItemStack base = reg.getItem(id);
        if (base == null || base.isEmpty()) {
            return unknownPlaceholder();
        }
        ItemStack show = base.copy();
        show.setCount(Math.max(1, pair.getValue()));
        return show;
    }

    /**
     * @param page 0-based page into the unclaimed list
     */
    public static void open(ServerPlayer player, FabricGraniteSkills plugin, int page) {
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user == null) {
            return;
        }
        user.clearInvalidItems();
        List<KeyIntPair> all = user.getUnclaimedItems();
        if (all.isEmpty()) {
            return;
        }
        int maxPage = FabricListMenuPaging.maxPageIndex(all.size(), ITEMS_PER_PAGE);
        int p = FabricListMenuPaging.clampPage(page, all.size(), ITEMS_PER_PAGE);
        int from = p * ITEMS_PER_PAGE;
        int to = Math.min(from + ITEMS_PER_PAGE, all.size());
        Locale lo = user.getLocale();
        List<KeyIntPair> sub = all.subList(from, to);
        if (FabricUnclaimedItemsTemplateSupport.tryOpen(player, plugin, user, p, maxPage, sub, lo)) {
            return;
        }
        openBuiltIn(player, plugin, user, p, maxPage, sub, lo);
    }

    private static void openBuiltIn(
            ServerPlayer player,
            FabricGraniteSkills plugin,
            User user,
            int p,
            int maxPage,
            List<KeyIntPair> sub,
            Locale lo) {
        Component title = FabricMenuTitles.unclaimedItemsTitle(p, maxPage);
        final int fPage = p;
        final int fMax = maxPage;
        GraniteContainerMenus.openClickable9x6(
                player,
                title,
                inv -> {
                    for (int i = 0; i < FabricMenuSlotLayout.TOP_SLOTS; i++) {
                        inv.setItem(i, ItemStack.EMPTY);
                    }
                    for (int i = 0; i < sub.size(); i++) {
                        int slot = i;
                        if (slot >= ITEMS_PER_PAGE) {
                            break;
                        }
                        inv.setItem(slot, displayStackForEntry(plugin, sub.get(i), lo));
                    }
                    if (fMax > 0) {
                        if (fPage > 0) {
                            putControl(
                                    inv,
                                    SLOT_PREV,
                                    "graniteskills.granite.menu.paging_prev",
                                    "graniteskills.granite.menu.paging_prev_lore",
                                    Items.ARROW);
                        }
                        if (fPage < fMax) {
                            putControl(
                                    inv,
                                    SLOT_NEXT,
                                    "graniteskills.granite.menu.paging_next",
                                    "graniteskills.granite.menu.paging_next_lore",
                                    Items.ARROW);
                        }
                    }
                    putControl(
                            inv,
                            SLOT_CLAIM_ALL,
                            "graniteskills.granite.menu.unclaimed_claim_all",
                            "graniteskills.granite.menu.unclaimed_claim_all_lore",
                            Items.CHEST);
                },
                (topSlot, pl, b, t) -> {
                    if (!FabricMenuClicks.activates(t)) {
                        return;
                    }
                    handleUnclaimedClick(pl, plugin, fPage, fMax, topSlot);
                });
    }

    static void handleUnclaimedClick(
            ServerPlayer pl, FabricGraniteSkills plugin, int fPage, int fMax, int topSlot) {
        User u = plugin.getUserManager().getUser(pl.getUUID());
        if (u == null) {
            return;
        }
        u.clearInvalidItems();
        if (u.getUnclaimedItems().isEmpty()) {
            pl.closeContainer();
            return;
        }
        if (topSlot == SLOT_CLAIM_ALL) {
            doClaimAllAndRefresh(pl, plugin, u, fPage);
            return;
        }
        if (fMax > 0) {
            if (topSlot == SLOT_PREV) {
                if (fPage > 0) {
                    plugin.getScheduler()
                            .executeSync(
                                    () -> FabricUnclaimedItemsMenuGui.open(pl, plugin, fPage - 1));
                }
                return;
            }
            if (topSlot == SLOT_NEXT) {
                if (fPage < fMax) {
                    plugin.getScheduler()
                            .executeSync(
                                    () -> FabricUnclaimedItemsMenuGui.open(pl, plugin, fPage + 1));
                }
                return;
            }
        }
        if (topSlot < 0 || topSlot >= ITEMS_PER_PAGE) {
            return;
        }
        int listIndex = fPage * ITEMS_PER_PAGE + topSlot;
        FabricUnclaimedItemsMerge.MergeOne outcome =
                FabricUnclaimedItemsMerge.mergeOneAtListIndex(plugin, u, pl, listIndex);
        if (outcome == FabricUnclaimedItemsMerge.MergeOne.INVENTORY_FULL) {
            String line =
                    plugin.getPrefix(u.getLocale())
                            + plugin.getMessageProvider()
                                    .applyFormatting(
                                            plugin.getMsg(MenuMessage.INVENTORY_FULL, u.getLocale()));
            u.sendMessage(line);
            pl.closeContainer();
            return;
        }
        u = plugin.getUserManager().getUser(pl.getUUID());
        if (u == null) {
            pl.closeContainer();
            return;
        }
        if (u.getUnclaimedItems().isEmpty()) {
            pl.closeContainer();
            return;
        }
        int newSize = u.getUnclaimedItems().size();
        int newPage = FabricListMenuPaging.clampPage(fPage, newSize, ITEMS_PER_PAGE);
        plugin.getScheduler()
                .executeSync(() -> FabricUnclaimedItemsMenuGui.open(pl, plugin, newPage));
    }

    private static void doClaimAllAndRefresh(
            ServerPlayer pl, FabricGraniteSkills plugin, User user, int fromPage) {
        FabricUnclaimedItemsMerge.mergeAllInOrder(plugin, user, pl);
        User u2 = plugin.getUserManager().getUser(pl.getUUID());
        if (u2 == null) {
            pl.closeContainer();
            return;
        }
        u2.clearInvalidItems();
        if (u2.getUnclaimedItems().isEmpty()) {
            pl.closeContainer();
            return;
        }
        int newSize = u2.getUnclaimedItems().size();
        int newPage = FabricListMenuPaging.clampPage(fromPage, newSize, ITEMS_PER_PAGE);
        plugin.getScheduler()
                .executeSync(() -> FabricUnclaimedItemsMenuGui.open(pl, plugin, newPage));
    }

    private static void putControl(
            Container inv, int slot, String nameKey, String loreKey, net.minecraft.world.item.Item item) {
        ItemStack s = new ItemStack(item);
        s.set(DataComponents.ITEM_NAME, FabricGraniteText.key(nameKey));
        s.set(
                DataComponents.LORE,
                new ItemLore(List.of(FabricGraniteText.key(loreKey))));
        inv.setItem(slot, s);
    }

    private static ItemStack displayStackForEntry(FabricGraniteSkills plugin, KeyIntPair pair, Locale lo) {
        ItemStack show = baseStackForUnclaimedEntry(plugin, pair, lo);
        List<Component> lore = new ArrayList<>();
        if (show.has(DataComponents.LORE)) {
            var existing = show.get(DataComponents.LORE);
            if (existing != null) {
                lore.addAll(existing.lines());
            }
        }
        lore.add(Component.empty());
        String click =
                plugin.getMessageProvider()
                        .applyFormatting(plugin.getMsg(MenuMessage.CLICK_TO_CLAIM, lo));
        lore.add(Component.literal(click));
        show.set(DataComponents.LORE, new ItemLore(lore));
        return show;
    }

    private static ItemStack unknownPlaceholder() {
        ItemStack s = new ItemStack(Items.BARRIER);
        s.set(
                DataComponents.ITEM_NAME,
                FabricGraniteText.key("graniteskills.granite.menu.unclaimed_unknown"));
        return s;
    }
}
