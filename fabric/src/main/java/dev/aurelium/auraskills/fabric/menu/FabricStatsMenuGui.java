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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Stats vertical slice: one book per enabled {@link Stat}, with level/value line and optional trait
 * lines (reference: Bukkit {@code StatsMenu} / {@code stats.yml} formats).
 */
final class FabricStatsMenuGui {

    private FabricStatsMenuGui() {
    }

    static void open(ServerPlayerEntity player, FabricAuraSkills plugin) {
        User user = plugin.getUserManager().getUser(player.getUuid());
        Locale locale = user.getLocale();
        MessageProvider msg = plugin.getMessageProvider();
        MenuHelper menus = plugin.getMenuHelper();
        var util = plugin.getPlatformUtil();

        GraniteContainerMenus.openReadOnly9x6(
                player,
                Text.translatable("auraskills.granite.menu.stats_title"),
                inv -> {
                    int i = 0;
                    for (Stat stat : plugin.getStatManager().getEnabledStats()) {
                        if (i >= inv.size()) {
                            break;
                        }
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
                        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
                        inv.setStack(i++, stack);
                    }
                });
    }

    private static String formatStatLevel(Stat stat, User user, FabricAuraSkills plugin, Locale locale) {
        if (stat.hasDirectTrait() && plugin.configBoolean(Option.MENUS_STATS_SHOW_TRAIT_VALUES_DIRECTLY)) {
            Trait trait = stat.getTraits().get(0);
            double value = user.getEffectiveTraitLevel(trait);
            return plugin.getTraitManager().getMenuDisplay(trait, value, locale, null);
        }
        return NumberUtil.format1(user.getStatLevel(stat));
    }

    private static void addTraitLoreIfPresent(
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
