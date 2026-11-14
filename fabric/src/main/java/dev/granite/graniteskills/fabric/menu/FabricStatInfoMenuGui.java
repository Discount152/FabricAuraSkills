package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.menu.MenuHelper;
import dev.granite.graniteskills.common.message.MessageProvider;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One-stat “stat_info” view: live name, value/level, description, and trait lines — not Bukkit
 * {@code stat_info.yml} + modifier grids. **Sprint 33:** titles/format keys from
 * {@link FabricBuiltInMenuConfig} / {@code menus/stat_info.yml} {@code granite:}. **Sprint 34:** main/back
 * slots from {@code granite.layout}. **Sprint 35:** opened via {@link FabricMenuRegistry} with
 * {@link FabricMenuContext#ofStat(Stat)}. **Sprint 36:** main/back items from {@code granite.items.main} /
 * {@code granite.items.back}. **Sprint 38:** <strong>Back</strong> reopens <strong>Stats</strong> with the
 * same {@link FabricMenuContext#listPage()} the player had when opening this menu (clamped in the list GUI);
 * early exits when {@code stat_info} is disabled also preserve that page.
 *
 * <p><strong>Sprint 96–97:</strong> tries bundled GraniteLib v0 YAML at
 * {@link FabricStatInfoTemplateSupport#TEMPLATE_RESOURCE_PATH} (trait lore + {@code custom_model_data} on template
 * path per Sprint 97); on any failure, uses this built-in path (safe fallback).
 */
final class FabricStatInfoMenuGui {

    private FabricStatInfoMenuGui() {
    }

    static void open(
            ServerPlayer player, FabricGraniteSkills plugin, Component titleUnused, FabricMenuContext context) {
        Stat stat = context.stat();
        if (stat == null) {
            if (plugin != null) {
                plugin.logger().warn("stat_info menu: missing Stat in FabricMenuContext");
            }
            return;
        }
        if (!plugin.configBoolean(Option.MENUS_STAT_INFO_ENABLED)) {
            FabricGraniteSkillsMenus.open(
                    FabricGraniteSkillsMenus.Id.STATS,
                    player,
                    plugin,
                    FabricMenuContext.none().withListPage(context.listPage()));
            return;
        }
        FabricMenuYamlSlice def = FabricMenuYamlSlice.defaultStatInfo();
        FabricMenuYamlSlice cfg = plugin.getBuiltInMenuConfig().getSlice(FabricMenuKeys.STAT_INFO, def);
        if (!cfg.enabled()) {
            player.sendSystemMessage(
                    FabricGraniteText.key(
                            "graniteskills.granite.menu.built_in_disabled", FabricMenuKeys.STAT_INFO),
                    false);
            plugin.getScheduler()
                    .executeSync(() -> FabricGraniteSkillsMenus.open(FabricGraniteSkillsMenus.Id.STATS, player, plugin));
            return;
        }
        if (FabricStatInfoTemplateSupport.tryOpen(player, plugin, context, stat)) {
            return;
        }
        openBuiltIn(player, plugin, context, stat, cfg, def);
    }

    private static void openBuiltIn(
            ServerPlayer player,
            FabricGraniteSkills plugin,
            FabricMenuContext context,
            Stat stat,
            FabricMenuYamlSlice cfg,
            FabricMenuYamlSlice def) {
        User user = plugin.getUserManager().getUser(player.getUUID());
        Locale locale = user.getLocale();
        MessageProvider msg = plugin.getMessageProvider();
        MenuHelper menus = plugin.getMenuHelper();
        String displayName = msg.getStatDisplayName(stat, locale, true);
        Component title = FabricMenuTitles.statInfoTitle(displayName);
        int mainSlot = cfg.mainSlot();
        int backSlot = cfg.backSlot();
        Item mainItem = FabricMenuItemDefinitions.resolveItem(
                cfg.mainItemId(), Items.BOOK, plugin, "menus/stat_info.yml", "main");
        Item backItem = FabricMenuItemDefinitions.resolveItem(
                cfg.backItemId(), Items.IRON_DOOR, plugin, "menus/stat_info.yml", "back");

        GraniteContainerMenus.openClickable9x6(
                player,
                title,
                inv -> {
                    ItemStack main = new ItemStack(mainItem);
                    main.set(
                            DataComponents.ITEM_NAME,
                            FabricGraniteText.key(
                                    cfg.resolveFormatKey(
                                            "main_title",
                                            def,
                                            "graniteskills.granite.menu.stat_info_main_title"),
                                    displayName));
                    List<Component> lore = new ArrayList<>();
                    String levelText = FabricStatsMenuGui.formatStatLevel(stat, user, plugin, locale);
                    lore.add(
                            FabricGraniteText.key(
                                    cfg.resolveFormatKey(
                                            "lore_value", def, "graniteskills.granite.menu.stat_info_lore_value"),
                                    levelText));
                    addDescriptionLore(plugin, stat, locale, msg, lore);
                    for (Trait trait : stat.getTraits()) {
                        if (lore.size() >= 16) {
                            break;
                        }
                        FabricStatsMenuGui.addTraitLoreIfPresent(
                                stat, user, trait, msg, plugin, locale, menus, lore);
                    }
                    main.set(DataComponents.LORE, new ItemLore(lore));
                    FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(main);
                    inv.setItem(mainSlot, main);
                    ItemStack back = new ItemStack(backItem);
                    back.set(
                            DataComponents.ITEM_NAME,
                            FabricGraniteText.key(
                                    cfg.resolveFormatKey("back", def, "graniteskills.granite.menu.stat_info_back")));
                    back.set(
                            DataComponents.LORE,
                            new ItemLore(
                                    List.of(
                                            FabricGraniteText.key(
                                                    cfg.resolveFormatKey(
                                                            "back_lore",
                                                            def,
                                                            "graniteskills.granite.menu.stat_info_back_lore")))));
                    FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(back);
                    inv.setItem(backSlot, back);
                },
                (topSlot, p, b, t) -> {
                    if (topSlot != backSlot) {
                        return;
                    }
                    plugin.getScheduler()
                            .executeSync(() -> FabricGraniteSkillsMenus.open(
                                    FabricGraniteSkillsMenus.Id.STATS,
                                    p,
                                    plugin,
                                    FabricMenuContext.none().withListPage(context.listPage())));
                });
    }

    private static void addDescriptionLore(
            FabricGraniteSkills plugin,
            Stat stat,
            Locale locale,
            MessageProvider msg,
            List<Component> lore) {
        String desc = msg.getStatDescription(stat, locale, false);
        if (desc == null || desc.isEmpty()) {
            return;
        }
        FabricMenuLoreFormatting.appendWrappedFormattedLore(plugin, lore, desc, 12, 800);
    }
}
