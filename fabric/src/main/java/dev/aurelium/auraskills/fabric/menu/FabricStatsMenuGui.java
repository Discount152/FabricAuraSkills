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
import net.minecraft.inventory.Inventory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Stats: one book per enabled {@link Stat} (Bukkit {@code stats.yml} spirit). **Sprint 31:** when
 * {@link Option#MENUS_STAT_INFO_ENABLED} is on, top-slot click opens {@link FabricStatInfoMenuGui}.
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
        boolean statInfoClicks = plugin.configBoolean(Option.MENUS_STAT_INFO_ENABLED);
        if (!statInfoClicks) {
            GraniteContainerMenus.openReadOnly9x6(
                    player,
                    title,
                    inv -> fillStatSlots(
                            order, user, msg, plugin, locale, menus, util, listStart, listEnd, inv, false));
            return;
        }
        GraniteContainerMenus.openClickable9x6(
                player,
                title,
                inv -> fillStatSlots(
                        order, user, msg, plugin, locale, menus, util, listStart, listEnd, inv, true),
                (topSlot, p, b, t) -> {
                    if (topSlot < listStart || topSlot >= listEnd) {
                        return;
                    }
                    int idx = topSlot - listStart;
                    int cap = Math.min(order.size(), listEnd - listStart);
                    if (idx < 0 || idx >= cap) {
                        return;
                    }
                    Stat stat = order.get(idx);
                    if (!stat.isEnabled()) {
                        return;
                    }
                    plugin.getScheduler()
                            .executeSync(() -> FabricAuraSkillsMenus.openStatInfo(stat, p, plugin));
                });
    }

    private static void fillStatSlots(
            List<Stat> order,
            User user,
            MessageProvider msg,
            FabricAuraSkills plugin,
            Locale locale,
            MenuHelper menus,
            PlatformUtil util,
            int listStart,
            int listEndEx,
            Inventory inv,
            boolean addOpenDetailLore) {
        int slot = listStart;
        for (int i = 0; i < order.size() && slot < listEndEx && slot < inv.size() && slot < FabricMenuSlotLayout.TOP_SLOTS;
                i++) {
            Stat stat = order.get(i);
            ItemStack stack = new ItemStack(Items.BOOK);
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
            for (Trait trait : stat.getTraits()) {
                if (lore.size() >= 10) {
                    break;
                }
                addTraitLoreIfPresent(stat, user, trait, msg, plugin, locale, menus, util, lore);
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
            slot++;
        }
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
