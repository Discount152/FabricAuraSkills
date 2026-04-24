package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.menu.MenuHelper;
import dev.aurelium.auraskills.common.message.MessageProvider;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.PlatformUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricTraitManager;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Stats: one entry per enabled {@link Stat} (Bukkit {@code stats.yml} spirit). **Sprint 31:** when
 * {@link Option#MENUS_STAT_INFO_ENABLED} is on, list click opens {@link FabricStatInfoMenuGui}. **Sprint
 * 36:** list row item from {@code granite.items.list}. **Sprint 37:** paged list + {@code
 * granite.paging}.
 */
final class FabricStatsMenuGui {

    private FabricStatsMenuGui() {
    }

    static void open(
            ServerPlayerEntity player, FabricAuraSkills plugin, Text title, FabricMenuContext context) {
        User user = plugin.getUserManager().getUser(player.getUuid());
        Locale locale = user.getLocale();
        MessageProvider msg = plugin.getMessageProvider();
        MenuHelper menus = plugin.getMenuHelper();
        var util = plugin.getPlatformUtil();
        List<Stat> order = new ArrayList<>();
        for (Stat s : plugin.getStatManager().getEnabledStats()) {
            order.add(s);
        }
        FabricMenuYamlSlice cfg = plugin
                .getBuiltInMenuConfig()
                .getSlice(FabricMenuKeys.STATS, FabricMenuYamlSlice.defaultStats());
        int listStart = cfg.listStartSlot();
        int listEnd = cfg.listEndSlotExclusive();
        FabricListMenuPaging.ListContentRegion region =
                FabricListMenuPaging.listContentRegion(listStart, listEnd);
        int pageSize = region.size();
        Item listItem = FabricMenuItemDefinitions.resolveItem(
                cfg.listItemId(), Items.BOOK, plugin, "menus/stats.yml", "list");
        int prevSlot = cfg.pagingPrevSlot();
        int nextSlot = cfg.pagingNextSlot();
        boolean statInfoClicks = plugin.configBoolean(Option.MENUS_STAT_INFO_ENABLED);
        int total = order.size();
        int maxPage = FabricListMenuPaging.maxPageIndex(total, pageSize);
        int page = FabricListMenuPaging.clampPage(context.listPage(), total, pageSize);
        int from = page * pageSize;
        int to = pageSize == 0 ? 0 : Math.min(from + pageSize, total);
        int contentStart = region.startInclusive();
        int contentEndEx = region.endExclusive();
        Text pageTitle = FabricListMenuPaging.appendPageToTitleIfNeeded(title, page, maxPage);
        final int fFrom = from;
        final int fTo = to;
        final int fPage = page;
        final int fMaxPage = maxPage;
        final int fPageSize = pageSize;
        final int fTotal = total;
        final boolean fStatClicks = statInfoClicks;
        GraniteContainerMenus.openClickable9x6(
                player,
                pageTitle,
                inv -> {
                    for (int i = 0; i < FabricMenuSlotLayout.TOP_SLOTS; i++) {
                        inv.setStack(i, ItemStack.EMPTY);
                    }
                    if (pageSize > 0) {
                        for (int g = fFrom; g < fTo; g++) {
                            int slot = contentStart + (g - fFrom);
                            if (slot >= contentEndEx) {
                                break;
                            }
                            fillOneStatStack(
                                    order.get(g),
                                    user,
                                    msg,
                                    plugin,
                                    locale,
                                    menus,
                                    util,
                                    listItem,
                                    fStatClicks,
                                    inv,
                                    slot);
                        }
                    }
                    if (fMaxPage > 0) {
                        if (fPage > 0) {
                            putNav(
                                    inv,
                                    prevSlot,
                                    true,
                                    "auraskills.granite.menu.paging_prev",
                                    "auraskills.granite.menu.paging_prev_lore");
                        } else {
                            inv.setStack(prevSlot, ItemStack.EMPTY);
                        }
                        if (fPage < fMaxPage) {
                            putNav(
                                    inv,
                                    nextSlot,
                                    true,
                                    "auraskills.granite.menu.paging_next",
                                    "auraskills.granite.menu.paging_next_lore");
                        } else {
                            inv.setStack(nextSlot, ItemStack.EMPTY);
                        }
                    } else {
                        inv.setStack(prevSlot, ItemStack.EMPTY);
                        inv.setStack(nextSlot, ItemStack.EMPTY);
                    }
                },
                (topSlot, p, b, t) -> {
                    if (fMaxPage > 0) {
                        if (topSlot == prevSlot) {
                            if (fPage > 0) {
                                plugin.getScheduler()
                                        .executeSync(() -> plugin
                                                .getMenuRegistry()
                                                .open(
                                                        FabricMenuKeys.STATS,
                                                        p,
                                                        plugin,
                                                        FabricMenuContext.none()
                                                                .withListPage(fPage - 1)));
                            }
                            return;
                        }
                        if (topSlot == nextSlot) {
                            if (fPage < fMaxPage) {
                                plugin.getScheduler()
                                        .executeSync(() -> plugin
                                                .getMenuRegistry()
                                                .open(
                                                        FabricMenuKeys.STATS,
                                                        p,
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
                    if (fPageSize <= 0) {
                        return;
                    }
                    if (topSlot < contentStart || topSlot >= contentEndEx) {
                        return;
                    }
                    int gIdx = fFrom + (topSlot - contentStart);
                    if (gIdx < fFrom || gIdx >= fTo) {
                        return;
                    }
                    if (gIdx < 0 || gIdx >= fTotal) {
                        return;
                    }
                    Stat st = order.get(gIdx);
                    if (!st.isEnabled()) {
                        return;
                    }
                    final Stat stat = st;
                    plugin.getScheduler()
                            .executeSync(() -> FabricAuraSkillsMenus.openStatInfo(stat, p, plugin));
                });
    }

    private static void putNav(
            Inventory inv, int slot, boolean active, String nameKey, String loreKey) {
        if (!active) {
            inv.setStack(slot, ItemStack.EMPTY);
            return;
        }
        ItemStack s = new ItemStack(Items.ARROW);
        s.set(DataComponentTypes.ITEM_NAME, Text.translatable(nameKey));
        s.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.translatable(loreKey))));
        inv.setStack(slot, s);
    }

    private static void fillOneStatStack(
            Stat stat,
            User user,
            MessageProvider msg,
            FabricAuraSkills plugin,
            Locale locale,
            MenuHelper menus,
            PlatformUtil util,
            Item listItem,
            boolean addOpenDetailLore,
            Inventory inv,
            int slot) {
        ItemStack stack = new ItemStack(listItem);
        String levelText = formatStatLevel(stat, user, plugin, locale);
        String nameTemplate = menus.getFormat("stats", "player_stat_entry");
        String rawName = TextUtil.replace(
                nameTemplate,
                "{color}", msg.getStatColor(stat, locale),
                "{symbol}", msg.getStatSymbol(stat, locale),
                "{stat}", msg.getStatDisplayName(stat, locale, false),
                "{level}", levelText);
        String itemName = util.toString(util.toComponent(rawName));
        stack.set(DataComponentTypes.ITEM_NAME, Text.literal(itemName));
        List<Text> lore = new ArrayList<>();
        String desc = msg.getStatDescription(stat, locale, false);
        if (desc != null && !desc.isEmpty()) {
            String oneLine = util.toString(util.toComponent(desc));
            if (oneLine.length() > 200) {
                oneLine = oneLine.substring(0, 197) + "...";
            }
            lore.add(Text.literal(oneLine));
        }
        for (Trait tr : stat.getTraits()) {
            if (lore.size() >= 10) {
                break;
            }
            addTraitLoreIfPresent(stat, user, tr, msg, plugin, locale, menus, util, lore);
        }
        if (addOpenDetailLore) {
            String openDetail = plugin
                    .getBuiltInMenuConfig()
                    .resolveListOpenDetailKey(
                            FabricMenuKeys.STATS,
                            FabricMenuYamlSlice.defaultStats(),
                            "auraskills.granite.menu.stats_lore_open_detail");
            lore.add(Text.translatable(openDetail));
        }
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        inv.setStack(slot, stack);
    }

    static String formatStatLevel(Stat stat, User user, FabricAuraSkills plugin, Locale locale) {
        if (stat.hasDirectTrait() && plugin.configBoolean(Option.MENUS_STATS_SHOW_TRAIT_VALUES_DIRECTLY)) {
            Trait trait = stat.getTraits().get(0);
            double value = user.getEffectiveTraitLevel(trait);
            return plugin.getTraitManager().getMenuDisplay(trait, value, locale, null);
        }
        return NumberUtil.format1(user.getStatLevel(stat));
    }

    static void addTraitLoreIfPresent(
            Stat stat,
            User user,
            Trait trait,
            MessageProvider msg,
            FabricAuraSkills plugin,
            Locale locale,
            MenuHelper menus,
            PlatformUtil util,
            List<Text> lore) {
        if (!trait.isEnabled()) {
            return;
        }
        String line = formatTraitLine(stat, user, trait, msg, plugin, locale, menus);
        if (line == null) {
            return;
        }
        String plain = util.toString(util.toComponent(line));
        lore.add(Text.literal(plain));
    }

    private static @Nullable String formatTraitLine(
            Stat stat,
            User user,
            Trait trait,
            MessageProvider msg,
            FabricAuraSkills plugin,
            Locale locale,
            MenuHelper menus) {
        if (plugin.getTraitManager() instanceof FabricTraitManager fabricTraits
                && fabricTraits.getTraitImpl(trait) == null) {
            return null;
        }
        String template = menus.getFormat("stats", "trait_entry");
        double eff = user.getEffectiveTraitLevel(trait);
        String levelPart = plugin.getTraitManager().getMenuDisplay(trait, eff, locale, null);
        return TextUtil.replace(
                template,
                "{trait}", trait.getDisplayName(locale),
                "{color}", msg.getStatColor(stat, locale),
                "{level}", levelPart);
    }
}
