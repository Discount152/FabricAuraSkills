package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.command.FabricSourceSortType;
import dev.granite.graniteskills.fabric.command.FabricSourcesListSupport;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * One-skill level progression: tries {@link FabricLevelProgressionTemplateSupport} first; built-in path matches
 * {@link FabricLevelProgressionLayout} (Sprint 118).
 */
final class FabricLevelProgressionMenuGui {

    private FabricLevelProgressionMenuGui() {
    }

    static void open(
            ServerPlayer player, FabricGraniteSkills plugin, Component titleUnused, FabricMenuContext context) {
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
            player.sendSystemMessage(
                    FabricGraniteText.key(
                            "graniteskills.granite.menu.built_in_disabled", FabricMenuKeys.LEVEL_PROGRESSION),
                    false);
            plugin.getScheduler()
                    .executeSync(() -> FabricGraniteSkillsMenus.open(
                            FabricGraniteSkillsMenus.Id.SKILLS,
                            player,
                            plugin,
                            FabricMenuContext.none().withListPage(context.listPage())));
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user == null) {
            return;
        }
        if (FabricLevelProgressionTemplateSupport.tryOpen(player, plugin, context, user)) {
            return;
        }
        openBuiltInSnake(player, plugin, context, skill, user);
    }

    private static void openBuiltInSnake(
            ServerPlayer player,
            FabricGraniteSkills plugin,
            FabricMenuContext context,
            Skill skill,
            User user) {
        Locale locale = user.getLocale();
        var msg = plugin.getMessageProvider();
        int maxLevel = skill.getMaxLevel();
        List<Integer> levels = new ArrayList<>(maxLevel);
        for (int l = 1; l <= maxLevel; l++) {
            levels.add(l);
        }
        if (levels.isEmpty()) {
            return;
        }

        int maxPage = FabricListMenuPaging.maxPageIndex(levels.size(), FabricLevelProgressionLayout.PAGE_SIZE);
        int p = FabricListMenuPaging.clampPage(
                context.progressionPage(), levels.size(), FabricLevelProgressionLayout.PAGE_SIZE);
        int from = p * FabricLevelProgressionLayout.PAGE_SIZE;
        int to = Math.min(from + FabricLevelProgressionLayout.PAGE_SIZE, levels.size());
        List<Integer> pageLevels = levels.subList(from, to);

        Component title = FabricMenuTitles.levelProgressionTitle(msg, skill, locale, p, maxPage);

        int userLevel = user.getSkillLevel(skill);
        double currentXp = user.getSkillXp(skill);
        var xpReqs = plugin.getXpRequirements();

        List<ItemStack> rowStacks = new ArrayList<>(pageLevels.size());
        for (Integer displayLevel : pageLevels) {
            Map<String, String> rowPh =
                    FabricLevelProgressionMenuContent.globalPlaceholders(skill, p, maxPage, locale, msg);
            FabricLevelProgressionMenuContent.addLevelRowPlaceholders(
                    rowPh, userLevel, displayLevel, currentXp, skill, maxLevel, xpReqs);
            ItemStack st = new ItemStack(Items.DYE.lime());
            setMiniMessageItemName(
                    plugin,
                    st,
                    "<white>Level </white><yellow>" + displayLevel + "</yellow>",
                    "items.level.name");
            List<Component> loreLines = new ArrayList<>();
            loreLines.add(FabricMenuLoreFormatting.formatMenuLine(plugin, rowPh.get("level_status")));
            loreLines.add(Component.empty());
            loreLines.add(FabricMenuLoreFormatting.formatMenuLine(plugin, rowPh.get("level_detail")));
            st.set(DataComponents.LORE, new ItemLore(loreLines));
            FabricMenuPresentation.levelProgressionRowIcon(st, userLevel, displayLevel);
            FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(st);
            rowStacks.add(st);
        }

        int back = FabricLevelProgressionLayout.SLOT_BACK;
        int sourcesS = FabricLevelProgressionLayout.SLOT_SOURCES;
        int abilitiesS = FabricLevelProgressionLayout.SLOT_ABILITIES;
        int pSlot = FabricLevelProgressionLayout.SLOT_PREVIOUS_PAGE;
        int nSlot = FabricLevelProgressionLayout.SLOT_NEXT_PAGE;

        ItemStack backStack = new ItemStack(Items.IRON_DOOR);
        backStack.set(
                DataComponents.ITEM_NAME,
                FabricGraniteText.key("graniteskills.granite.menu.level_progression_back"));
        backStack.set(
                DataComponents.LORE,
                new ItemLore(
                        List.of(FabricGraniteText.key(
                                "graniteskills.granite.menu.level_progression_back_lore"))));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(backStack);

        ItemStack sourcesStack = new ItemStack(Items.KNOWLEDGE_BOOK);
        setMiniMessageItemName(plugin, sourcesStack, "<aqua>Sources</aqua>", "items.sources");
        sourcesStack.set(
                DataComponents.LORE,
                new ItemLore(
                        List.of(FabricGraniteText.key(
                                "graniteskills.granite.menu.level_progression_open_sources_lore"))));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(sourcesStack);

        ItemStack abilitiesStack = new ItemStack(Items.AMETHYST_SHARD);
        setMiniMessageItemName(plugin, abilitiesStack, "<light_purple>Abilities</light_purple>", "items.abilities");
        List<Component> abLore = new ArrayList<>();
        FabricGraniteTemplateStacks.toLiteralNoUnresolved(
                        "<gray>Passives and mana abilities</gray>",
                        plugin,
                        "level_progression.builtin",
                        "items.abilities.lore")
                .ifPresent(abLore::add);
        abilitiesStack.set(DataComponents.LORE, new ItemLore(abLore));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(abilitiesStack);

        Optional<ItemStack> prevOpt = Optional.empty();
        Optional<ItemStack> nextOpt = Optional.empty();
        if (maxPage > 0) {
            if (p > 0) {
                Component prevName =
                        FabricGraniteTemplateStacks.toLiteralNoUnresolved(
                                        "<yellow>Previous</yellow>",
                                        plugin,
                                        "level_progression.builtin",
                                        "items.previous")
                                .orElseGet(() -> Component.literal("Previous"));
                ItemStack ps =
                        FabricMenuItemPresentation.pageNavArrow(
                                prevName,
                                FabricGraniteText.key("graniteskills.granite.menu.paging_prev_lore"));
                prevOpt = Optional.of(ps);
            }
            if (p < maxPage) {
                Component nextName =
                        FabricGraniteTemplateStacks.toLiteralNoUnresolved(
                                        "<yellow>Next</yellow>",
                                        plugin,
                                        "level_progression.builtin",
                                        "items.next")
                                .orElseGet(() -> Component.literal("Next"));
                ItemStack ns =
                        FabricMenuItemPresentation.pageNavArrow(
                                nextName,
                                FabricGraniteText.key("graniteskills.granite.menu.paging_next_lore"));
                nextOpt = Optional.of(ns);
            }
        }

        final int pageIndex = p;
        final int maxPageIndex = maxPage;
        final Optional<ItemStack> prevStackOpt = prevOpt;
        final Optional<ItemStack> nextStackOpt = nextOpt;
        final int returnSkillsPage = context.listPage();

        GraniteContainerMenus.openClickable9x6(
                player,
                title,
                inv -> {
                    for (int s = 0; s < 45; s++) {
                        inv.setItem(s, ItemStack.EMPTY);
                    }
                    for (int i = 0; i < rowStacks.size(); i++) {
                        inv.setItem(FabricLevelProgressionLayout.LEVEL_PATH_SLOTS.get(i), rowStacks.get(i).copy());
                    }
                    for (int u = rowStacks.size(); u < FabricLevelProgressionLayout.PAGE_SIZE; u++) {
                        int pathSlot = FabricLevelProgressionLayout.LEVEL_PATH_SLOTS.get(u);
                        inv.setItem(pathSlot, ItemStack.EMPTY);
                    }
                    for (int s = 45; s <= 53; s++) {
                        inv.setItem(s, ItemStack.EMPTY);
                    }
                    inv.setItem(back, backStack.copy());
                    inv.setItem(sourcesS, sourcesStack.copy());
                    inv.setItem(abilitiesS, abilitiesStack.copy());
                    if (maxPageIndex > 0) {
                        prevStackOpt.ifPresent(st -> inv.setItem(pSlot, st.copy()));
                        nextStackOpt.ifPresent(st -> inv.setItem(nSlot, st.copy()));
                    }
                },
                (topSlot, pl, b, t) -> {
                    if (topSlot == back) {
                        plugin.getScheduler()
                                .executeSync(
                                        () ->
                                                FabricGraniteSkillsMenus.open(
                                                        FabricGraniteSkillsMenus.Id.SKILLS,
                                                        pl,
                                                        plugin,
                                                        FabricMenuContext.none()
                                                                .withListPage(returnSkillsPage)));
                        return;
                    }
                    if (topSlot == sourcesS) {
                        final Skill sk = skill;
                        plugin.getScheduler()
                                .executeSync(
                                        () -> {
                                            User u = plugin.getUserManager().getUser(pl.getUUID());
                                            Locale lo = u != null ? u.getLocale() : plugin.getDefaultLanguage();
                                            if (FabricSourcesListSupport.buildSorted(
                                                            plugin,
                                                            plugin.getSkillManager().getSkill(sk),
                                                            FabricSourceSortType.ASCENDING,
                                                            lo)
                                                    .isEmpty()) {
                                                pl.sendSystemMessage(
                                                        FabricGraniteText.key(
                                                                "graniteskills.granite.command.sources.empty",
                                                                sk.getDisplayName(lo, false)),
                                                        false);
                                                return;
                                            }
                                            FabricGraniteSkillsMenus.openSources(
                                                    sk,
                                                    pl,
                                                    plugin,
                                                    FabricSourceSortType.ASCENDING,
                                                    0,
                                                    returnSkillsPage);
                                        });
                        return;
                    }
                    if (topSlot == abilitiesS) {
                        final Skill sk = skill;
                        plugin.getScheduler()
                                .executeSync(
                                        () ->
                                                FabricGraniteSkillsMenus.openAbilities(
                                                        sk, pl, plugin, returnSkillsPage));
                        return;
                    }
                    if (maxPageIndex > 0) {
                        if (topSlot == pSlot && pageIndex > 0) {
                            final Skill sk = skill;
                            plugin.getScheduler()
                                    .executeSync(
                                            () ->
                                                    FabricGraniteSkillsMenus.open(
                                                            FabricMenuKeys.LEVEL_PROGRESSION,
                                                            pl,
                                                            plugin,
                                                            FabricMenuContext.ofSkill(sk)
                                                                    .withListPage(returnSkillsPage)
                                                                    .withProgressionPage(pageIndex - 1)));
                            return;
                        }
                        if (topSlot == nSlot && pageIndex < maxPageIndex) {
                            final Skill sk = skill;
                            plugin.getScheduler()
                                    .executeSync(
                                            () ->
                                                    FabricGraniteSkillsMenus.open(
                                                            FabricMenuKeys.LEVEL_PROGRESSION,
                                                            pl,
                                                            plugin,
                                                            FabricMenuContext.ofSkill(sk)
                                                                    .withListPage(returnSkillsPage)
                                                                    .withProgressionPage(pageIndex + 1)));
                        }
                    }
                });
    }

    private static void setMiniMessageItemName(
            FabricGraniteSkills plugin, ItemStack stack, String miniMessage, String fieldLabel) {
        FabricGraniteTemplateStacks.toLiteralNoUnresolved(
                        miniMessage, plugin, "level_progression.builtin", fieldLabel)
                .ifPresent(text -> stack.set(DataComponents.ITEM_NAME, text));
    }
}
