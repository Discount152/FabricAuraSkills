package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.command.FabricSourceSortType;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import dev.aurelium.auraskills.fabric.command.FabricSourcesListSupport;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One-skill “level progression” view: live level + XP from {@link User}, not full Bukkit
 * {@code level_progression.yml} / reward tables. **Sprint 100:** tries bundled GraniteLib template first; on
 * failure uses the built-in path below. **Sprint 117:** the template uses a 24-slot snake path; this built-in path
 * remains a compact legacy layout (book + back + hopper sources at slot 50 — not the template path).
 */
final class FabricLevelProgressionMenuGui {

    /** “View sources” (distinct from default main/back slots in typical YAML). */
    private static final int SLOT_OPEN_SOURCES = 50;

    private FabricLevelProgressionMenuGui() {
    }

    static void open(
            ServerPlayerEntity player, FabricAuraSkills plugin, Text titleUnused, FabricMenuContext context) {
        Skill skill = context.skill();
        if (skill == null) {
            if (plugin != null) {
                plugin.logger().warn("level_progression menu: missing Skill in FabricMenuContext");
            }
            return;
        }
        FabricMenuYamlSlice def = FabricMenuYamlSlice.defaultLevelProgression();
        FabricMenuYamlSlice cfg = plugin
                .getBuiltInMenuConfig()
                .getSlice(FabricMenuKeys.LEVEL_PROGRESSION, def);
        if (!cfg.enabled()) {
            player.sendMessage(
                    FabricGraniteText.key(
                            "auraskills.granite.menu.built_in_disabled", FabricMenuKeys.LEVEL_PROGRESSION),
                    false);
            plugin.getScheduler()
                    .executeSync(() -> FabricAuraSkillsMenus.open(
                            FabricAuraSkillsMenus.Id.SKILLS,
                            player,
                            plugin,
                            FabricMenuContext.none().withListPage(context.listPage())));
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (user == null) {
            return;
        }
        if (FabricLevelProgressionTemplateSupport.tryOpen(player, plugin, context, user)) {
            return;
        }
        openBuiltIn(player, plugin, context, skill, cfg, def, user);
    }

    private static void openBuiltIn(
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            FabricMenuContext context,
            Skill skill,
            FabricMenuYamlSlice cfg,
            FabricMenuYamlSlice def,
            User user) {
        Locale locale = user.getLocale();
        var msg = plugin.getMessageProvider();
        String skillName = msg.getSkillDisplayName(skill, locale, true);
        Text title = FabricGraniteText.key(cfg.titleTranslationKey(), skillName);
        int level = user.getSkillLevel(skill);
        double currentXp = user.getSkillXp(skill);
        var xpReqs = plugin.getXpRequirements();
        boolean maxed = user.getSkillLevel(skill) >= skill.getMaxLevel()
                || xpReqs.getListSize(skill) <= level - 1;
        double xpToNext = !maxed ? xpReqs.getXpRequired(skill, level + 1) : 0.0;
        int mainSlot = cfg.mainSlot();
        int backSlot = cfg.backSlot();
        Item mainItem = FabricMenuItemDefinitions.resolveItem(
                cfg.mainItemId(), Items.BOOK, plugin, "menus/level_progression.yml", "main");
        Item backItem = FabricMenuItemDefinitions.resolveItem(
                cfg.backItemId(), Items.ARROW, plugin, "menus/level_progression.yml", "back");

        GraniteContainerMenus.openClickable9x6(
                player,
                title,
                inv -> {
                    ItemStack main = new ItemStack(mainItem);
                    main.set(
                            DataComponentTypes.ITEM_NAME,
                            FabricGraniteText.key(
                                    cfg.resolveFormatKey(
                                            "main_title",
                                            def,
                                            "auraskills.granite.menu.level_progression_main_title"),
                                    skillName));
                    List<Text> lore = new ArrayList<>();
                    lore.add(
                            FabricGraniteText.key(
                                    cfg.resolveFormatKey(
                                            "lore_level",
                                            def,
                                            "auraskills.granite.menu.level_progression_lore_level"),
                                    level));
                    lore.add(
                            FabricGraniteText.key(
                                    cfg.resolveFormatKey(
                                            "lore_xp", def, "auraskills.granite.menu.level_progression_lore_xp"),
                                    NumberUtil.format2(currentXp)));
                    if (maxed) {
                        lore.add(
                                FabricGraniteText.key(
                                        cfg.resolveFormatKey(
                                                "lore_maxed",
                                                def,
                                                "auraskills.granite.menu.level_progression_lore_maxed")));
                    } else {
                        lore.add(
                                FabricGraniteText.key(
                                        cfg.resolveFormatKey(
                                                "lore_to_next",
                                                def,
                                                "auraskills.granite.menu.level_progression_lore_to_next"),
                                        NumberUtil.format2(currentXp),
                                        NumberUtil.format2(xpToNext)));
                    }
                    if (!user.hasSkillPermission(skill)) {
                        lore.add(
                                FabricGraniteText.key(
                                        cfg.resolveFormatKey(
                                                "no_permission",
                                                def,
                                                "auraskills.granite.menu.skills_lore_no_permission")));
                    }
                    main.set(DataComponentTypes.LORE, new LoreComponent(lore));
                    FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(main);
                    inv.setStack(mainSlot, main);

                    ItemStack back = new ItemStack(backItem);
                    back.set(
                            DataComponentTypes.ITEM_NAME,
                            FabricGraniteText.key(
                                    cfg.resolveFormatKey(
                                            "back", def, "auraskills.granite.menu.level_progression_back")));
                    back.set(
                            DataComponentTypes.LORE,
                            new LoreComponent(
                                    List.of(
                                            FabricGraniteText.key(
                                                    cfg.resolveFormatKey(
                                                            "back_lore",
                                                            def,
                                                            "auraskills.granite.menu.level_progression_back_lore")))));
                    FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(back);
                    inv.setStack(backSlot, back);

                    ItemStack openSources = new ItemStack(Items.KNOWLEDGE_BOOK);
                    openSources.set(
                            DataComponentTypes.ITEM_NAME,
                            FabricGraniteText.key("auraskills.granite.menu.level_progression_open_sources"));
                    openSources.set(
                            DataComponentTypes.LORE,
                            new LoreComponent(
                                    List.of(
                                            FabricGraniteText.key(
                                                    "auraskills.granite.menu.level_progression_open_sources_lore"))));
                    FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(openSources);
                    inv.setStack(SLOT_OPEN_SOURCES, openSources);
                },
                (topSlot, p, button, actionType) -> {
                    if (topSlot == backSlot) {
                        plugin.getScheduler()
                                .executeSync(
                                        () -> FabricAuraSkillsMenus.open(
                                                FabricAuraSkillsMenus.Id.SKILLS,
                                                p,
                                                plugin,
                                                FabricMenuContext.none()
                                                        .withListPage(context.listPage())));
                    } else if (topSlot == SLOT_OPEN_SOURCES) {
                        final Skill s = skill;
                        final int returnSkillsPage = context.listPage();
                        plugin.getScheduler()
                                .executeSync(
                                        () -> {
                                            User u = plugin
                                                    .getUserManager()
                                                    .getUser(p.getUuid());
                                            Locale lo =
                                                    u != null
                                                            ? u.getLocale()
                                                            : plugin.getDefaultLanguage();
                                            if (FabricSourcesListSupport.buildSorted(
                                                            plugin
                                                                    .getSkillManager()
                                                                    .getSkill(s),
                                                            FabricSourceSortType.ASCENDING,
                                                            lo)
                                                    .isEmpty()) {
                                                p.sendMessage(
                                                        FabricGraniteText.key(
                                                                "auraskills.granite.command.sources.empty",
                                                                s.getDisplayName(lo, false)),
                                                        false);
                                                return;
                                            }
                                            FabricAuraSkillsMenus.openSources(
                                                    s,
                                                    p,
                                                    plugin,
                                                    FabricSourceSortType.ASCENDING,
                                                    0,
                                                    returnSkillsPage);
                                        });
                    }
                });
    }
}
