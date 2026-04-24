package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.menu.MenuHelper;
import dev.aurelium.auraskills.common.message.MessageProvider;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.PlatformUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One-stat “stat_info” view: live name, value/level, description, and trait lines — not Bukkit
 * {@code stat_info.yml} + modifier grids. **Sprint 33:** titles/format keys from
 * {@link FabricBuiltInMenuConfig} / {@code menus/stat_info.yml} {@code granite:}.
 */
final class FabricStatInfoMenuGui {

    private static final int INFO_SLOT = 4;
    private static final int BACK_SLOT = 49;

    private FabricStatInfoMenuGui() {
    }

    static void open(ServerPlayerEntity player, FabricAuraSkills plugin, Stat stat) {
        if (!plugin.configBoolean(Option.MENUS_STAT_INFO_ENABLED)) {
            FabricAuraSkillsMenus.open(FabricAuraSkillsMenus.Id.STATS, player, plugin);
            return;
        }
        FabricMenuYamlSlice def = FabricMenuYamlSlice.defaultStatInfo();
        FabricMenuYamlSlice cfg = plugin.getBuiltInMenuConfig().getSlice(FabricMenuKeys.STAT_INFO, def);
        if (!cfg.enabled()) {
            player.sendMessage(
                    Text.translatable("auraskills.granite.menu.built_in_disabled", FabricMenuKeys.STAT_INFO), false);
            plugin.getScheduler()
                    .executeSync(() -> FabricAuraSkillsMenus.open(FabricAuraSkillsMenus.Id.STATS, player, plugin));
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        Locale locale = user.getLocale();
        MessageProvider msg = plugin.getMessageProvider();
        MenuHelper menus = plugin.getMenuHelper();
        var util = plugin.getPlatformUtil();
        String displayName = msg.getStatDisplayName(stat, locale, true);
        Text title = Text.translatable(cfg.titleTranslationKey(), displayName);

        GraniteContainerMenus.openClickable9x6(
                player,
                title,
                inv -> {
                    ItemStack main = new ItemStack(Items.BOOK);
                    main.set(
                            DataComponentTypes.ITEM_NAME,
                            Text.translatable(
                                    cfg.resolveFormatKey(
                                            "main_title",
                                            def,
                                            "auraskills.granite.menu.stat_info_main_title"),
                                    displayName));
                    List<Text> lore = new ArrayList<>();
                    String levelText = FabricStatsMenuGui.formatStatLevel(stat, user, plugin, locale);
                    lore.add(Text.translatable(
                            cfg.resolveFormatKey(
                                    "lore_value", def, "auraskills.granite.menu.stat_info_lore_value"),
                            levelText));
                    addDescriptionLore(msg, util, stat, locale, lore);
                    for (Trait trait : stat.getTraits()) {
                        if (lore.size() >= 16) {
                            break;
                        }
                        FabricStatsMenuGui.addTraitLoreIfPresent(
                                stat, user, trait, msg, plugin, locale, menus, util, lore);
                    }
                    main.set(DataComponentTypes.LORE, new LoreComponent(lore));
                    inv.setStack(INFO_SLOT, main);
                    ItemStack back = new ItemStack(Items.ARROW);
                    back.set(
                            DataComponentTypes.ITEM_NAME,
                            Text.translatable(
                                    cfg.resolveFormatKey("back", def, "auraskills.granite.menu.stat_info_back")));
                    back.set(
                            DataComponentTypes.LORE,
                            new LoreComponent(List.of(Text.translatable(
                                    cfg.resolveFormatKey(
                                            "back_lore",
                                            def,
                                            "auraskills.granite.menu.stat_info_back_lore")))));
                    inv.setStack(BACK_SLOT, back);
                },
                (topSlot, p, b, t) -> {
                    if (topSlot != BACK_SLOT) {
                        return;
                    }
                    plugin.getScheduler()
                            .executeSync(() -> FabricAuraSkillsMenus.open(FabricAuraSkillsMenus.Id.STATS, p, plugin));
                });
    }

    private static void addDescriptionLore(
            MessageProvider msg,
            PlatformUtil util,
            Stat stat,
            Locale locale,
            List<Text> lore) {
        String desc = msg.getStatDescription(stat, locale, false);
        if (desc == null || desc.isEmpty()) {
            return;
        }
        for (String line : desc.split("\n", -1)) {
            if (line.isEmpty()) {
                continue;
            }
            String shown = util.toString(util.toComponent(line));
            if (shown.length() <= 100) {
                lore.add(Text.literal(shown));
            } else {
                for (int at = 0; at < shown.length(); at += 90) {
                    if (lore.size() >= 12) {
                        return;
                    }
                    int end = Math.min(at + 90, shown.length());
                    lore.add(Text.literal(shown.substring(at, end)));
                }
            }
        }
    }
}
