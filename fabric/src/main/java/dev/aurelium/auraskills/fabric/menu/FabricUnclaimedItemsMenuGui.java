package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.common.message.type.MenuMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.data.KeyIntPair;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import dev.aurelium.auraskills.fabric.item.FabricItemRegistry;
import dev.aurelium.auraskills.fabric.item.FabricUnclaimedItemsMerge;
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
    static ItemStack baseStackForUnclaimedEntry(FabricAuraSkills plugin, KeyIntPair pair, Locale lo) {
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
    public static void open(ServerPlayerEntity player, FabricAuraSkills plugin, int page) {
        User user = plugin.getUserManager().getUser(player.getUuid());
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
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            User user,
            int p,
            int maxPage,
            List<KeyIntPair> sub,
            Locale lo) {
        String rawTitle = plugin.getMsg(MenuMessage.UNCLAIMED_ITEMS_TITLE, lo);
        Text base = Text.literal(plugin.getMessageProvider().applyFormatting(rawTitle));
        Text title = FabricListMenuPaging.appendPageToTitleIfNeeded(base, p, maxPage);
        final int fPage = p;
        final int fMax = maxPage;
        GraniteContainerMenus.openClickable9x6(
                player,
                title,
                inv -> {
                    for (int i = 0; i < FabricMenuSlotLayout.TOP_SLOTS; i++) {
                        inv.setStack(i, ItemStack.EMPTY);
                    }
                    for (int i = 0; i < sub.size(); i++) {
                        int slot = i;
                        if (slot >= ITEMS_PER_PAGE) {
                            break;
                        }
                        inv.setStack(slot, displayStackForEntry(plugin, sub.get(i), lo));
                    }
                    if (fMax > 0) {
                        if (fPage > 0) {
                            putControl(
                                    inv,
                                    SLOT_PREV,
                                    "auraskills.granite.menu.paging_prev",
                                    "auraskills.granite.menu.paging_prev_lore",
                                    Items.ARROW);
                        }
                        if (fPage < fMax) {
                            putControl(
                                    inv,
                                    SLOT_NEXT,
                                    "auraskills.granite.menu.paging_next",
                                    "auraskills.granite.menu.paging_next_lore",
                                    Items.ARROW);
                        }
                    }
                    putControl(
                            inv,
                            SLOT_CLAIM_ALL,
                            "auraskills.granite.menu.unclaimed_claim_all",
                            "auraskills.granite.menu.unclaimed_claim_all_lore",
                            Items.CHEST);
                },
                (topSlot, pl, b, t) ->
                        handleUnclaimedClick(pl, plugin, fPage, fMax, topSlot));
    }

    static void handleUnclaimedClick(
            ServerPlayerEntity pl, FabricAuraSkills plugin, int fPage, int fMax, int topSlot) {
        User u = plugin.getUserManager().getUser(pl.getUuid());
        if (u == null) {
            return;
        }
        u.clearInvalidItems();
        if (u.getUnclaimedItems().isEmpty()) {
            pl.closeHandledScreen();
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
            pl.closeHandledScreen();
            return;
        }
        u = plugin.getUserManager().getUser(pl.getUuid());
        if (u == null) {
            pl.closeHandledScreen();
            return;
        }
        if (u.getUnclaimedItems().isEmpty()) {
            pl.closeHandledScreen();
            return;
        }
        int newSize = u.getUnclaimedItems().size();
        int newPage = FabricListMenuPaging.clampPage(fPage, newSize, ITEMS_PER_PAGE);
        plugin.getScheduler()
                .executeSync(() -> FabricUnclaimedItemsMenuGui.open(pl, plugin, newPage));
    }

    private static void doClaimAllAndRefresh(
            ServerPlayerEntity pl, FabricAuraSkills plugin, User user, int fromPage) {
        FabricUnclaimedItemsMerge.mergeAllInOrder(plugin, user, pl);
        User u2 = plugin.getUserManager().getUser(pl.getUuid());
        if (u2 == null) {
            pl.closeHandledScreen();
            return;
        }
        u2.clearInvalidItems();
        if (u2.getUnclaimedItems().isEmpty()) {
            pl.closeHandledScreen();
            return;
        }
        int newSize = u2.getUnclaimedItems().size();
        int newPage = FabricListMenuPaging.clampPage(fromPage, newSize, ITEMS_PER_PAGE);
        plugin.getScheduler()
                .executeSync(() -> FabricUnclaimedItemsMenuGui.open(pl, plugin, newPage));
    }

    private static void putControl(
            Inventory inv, int slot, String nameKey, String loreKey, net.minecraft.item.Item item) {
        ItemStack s = new ItemStack(item);
        s.set(DataComponentTypes.ITEM_NAME, FabricGraniteText.key(nameKey));
        s.set(
                DataComponentTypes.LORE,
                new LoreComponent(List.of(FabricGraniteText.key(loreKey))));
        inv.setStack(slot, s);
    }

    private static ItemStack displayStackForEntry(FabricAuraSkills plugin, KeyIntPair pair, Locale lo) {
        ItemStack show = baseStackForUnclaimedEntry(plugin, pair, lo);
        List<Text> lore = new ArrayList<>();
        if (show.contains(DataComponentTypes.LORE)) {
            var existing = show.get(DataComponentTypes.LORE);
            if (existing != null) {
                lore.addAll(existing.lines());
            }
        }
        lore.add(Text.empty());
        String click =
                plugin.getMessageProvider()
                        .applyFormatting(plugin.getMsg(MenuMessage.CLICK_TO_CLAIM, lo));
        lore.add(Text.literal(click));
        show.set(DataComponentTypes.LORE, new LoreComponent(lore));
        return show;
    }

    private static ItemStack unknownPlaceholder() {
        ItemStack s = new ItemStack(Items.BARRIER);
        s.set(
                DataComponentTypes.ITEM_NAME,
                FabricGraniteText.key("auraskills.granite.menu.unclaimed_unknown"));
        return s;
    }
}
