package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.command.FabricSourceSortType;
import dev.aurelium.auraskills.fabric.command.FabricSourcesListSupport;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
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
        openBuiltInSnake(player, plugin, context, skill, user);
    }

    private static void openBuiltInSnake(
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
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

        Text title = FabricLevelProgressionMenuContent.builtInMenuTitle(msg, skill, locale, p, maxPage);

        int userLevel = user.getSkillLevel(skill);
        double currentXp = user.getSkillXp(skill);
        var xpReqs = plugin.getXpRequirements();

        List<ItemStack> rowStacks = new ArrayList<>(pageLevels.size());
        for (Integer displayLevel : pageLevels) {
            Map<String, String> rowPh =
                    FabricLevelProgressionMenuContent.globalPlaceholders(skill, p, maxPage, locale, msg);
            FabricLevelProgressionMenuContent.addLevelRowPlaceholders(
                    rowPh, userLevel, displayLevel, currentXp, skill, maxLevel, xpReqs);
            ItemStack st = new ItemStack(Items.LIME_DYE);
            setMiniMessageItemName(
                    plugin,
                    st,
                    "<white>Level </white><yellow>" + displayLevel + "</yellow>",
                    "items.level.name");
            List<Text> loreLines = new ArrayList<>();
            loreLines.add(Text.literal(rowPh.get("level_status")));
            loreLines.add(Text.empty());
            loreLines.add(Text.literal(rowPh.get("level_detail")));
            st.set(DataComponentTypes.LORE, new LoreComponent(loreLines));
            FabricMenuPresentation.levelProgressionRowIcon(st, userLevel, displayLevel);
            FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(st);
            rowStacks.add(st);
        }

        int back = FabricLevelProgressionLayout.SLOT_BACK;
        int sourcesS = FabricLevelProgressionLayout.SLOT_SOURCES;
        int abilitiesS = FabricLevelProgressionLayout.SLOT_ABILITIES;
        int pSlot = FabricLevelProgressionLayout.SLOT_PREVIOUS_PAGE;
        int nSlot = FabricLevelProgressionLayout.SLOT_NEXT_PAGE;

        ItemStack backStack = new ItemStack(Items.ARROW);
        setMiniMessageItemName(plugin, backStack, "<white>Back</white>", "items.back");
        backStack.set(
                DataComponentTypes.LORE,
                new LoreComponent(
                        List.of(FabricGraniteText.key(
                                "auraskills.granite.menu.level_progression_back_lore"))));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(backStack);

        ItemStack sourcesStack = new ItemStack(Items.KNOWLEDGE_BOOK);
        setMiniMessageItemName(plugin, sourcesStack, "<aqua>Sources</aqua>", "items.sources");
        sourcesStack.set(
                DataComponentTypes.LORE,
                new LoreComponent(
                        List.of(FabricGraniteText.key(
                                "auraskills.granite.menu.level_progression_open_sources_lore"))));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(sourcesStack);

        ItemStack abilitiesStack = new ItemStack(Items.AMETHYST_SHARD);
        setMiniMessageItemName(plugin, abilitiesStack, "<light_purple>Abilities</light_purple>", "items.abilities");
        List<Text> abLore = new ArrayList<>();
        FabricGraniteTemplateStacks.toLiteralNoUnresolved(
                        "<gray>Passives and mana abilities</gray>",
                        plugin,
                        "level_progression.builtin",
                        "items.abilities.lore")
                .ifPresent(abLore::add);
        abilitiesStack.set(DataComponentTypes.LORE, new LoreComponent(abLore));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(abilitiesStack);

        Optional<ItemStack> prevOpt = Optional.empty();
        Optional<ItemStack> nextOpt = Optional.empty();
        if (maxPage > 0) {
            if (p > 0) {
                ItemStack ps = new ItemStack(Items.ARROW);
                setMiniMessageItemName(plugin, ps, "<yellow>Previous</yellow>", "items.previous");
                ps.set(
                        DataComponentTypes.LORE,
                        new LoreComponent(
                                List.of(FabricGraniteText.key("auraskills.granite.menu.paging_prev_lore"))));
                FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(ps);
                prevOpt = Optional.of(ps);
            }
            if (p < maxPage) {
                ItemStack ns = new ItemStack(Items.ARROW);
                setMiniMessageItemName(plugin, ns, "<yellow>Next</yellow>", "items.next");
                ns.set(
                        DataComponentTypes.LORE,
                        new LoreComponent(
                                List.of(FabricGraniteText.key("auraskills.granite.menu.paging_next_lore"))));
                FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(ns);
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
                        inv.setStack(s, ItemStack.EMPTY);
                    }
                    for (int i = 0; i < rowStacks.size(); i++) {
                        inv.setStack(FabricLevelProgressionLayout.LEVEL_PATH_SLOTS.get(i), rowStacks.get(i).copy());
                    }
                    for (int u = rowStacks.size(); u < FabricLevelProgressionLayout.PAGE_SIZE; u++) {
                        int pathSlot = FabricLevelProgressionLayout.LEVEL_PATH_SLOTS.get(u);
                        inv.setStack(pathSlot, ItemStack.EMPTY);
                    }
                    for (int s = 45; s <= 53; s++) {
                        inv.setStack(s, ItemStack.EMPTY);
                    }
                    inv.setStack(back, backStack.copy());
                    inv.setStack(sourcesS, sourcesStack.copy());
                    inv.setStack(abilitiesS, abilitiesStack.copy());
                    if (maxPageIndex > 0) {
                        prevStackOpt.ifPresent(st -> inv.setStack(pSlot, st.copy()));
                        nextStackOpt.ifPresent(st -> inv.setStack(nSlot, st.copy()));
                    }
                },
                (topSlot, pl, b, t) -> {
                    if (topSlot == back) {
                        plugin.getScheduler()
                                .executeSync(
                                        () ->
                                                FabricAuraSkillsMenus.open(
                                                        FabricAuraSkillsMenus.Id.SKILLS,
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
                                            User u = plugin.getUserManager().getUser(pl.getUuid());
                                            Locale lo = u != null ? u.getLocale() : plugin.getDefaultLanguage();
                                            if (FabricSourcesListSupport.buildSorted(
                                                            plugin.getSkillManager().getSkill(sk),
                                                            FabricSourceSortType.ASCENDING,
                                                            lo)
                                                    .isEmpty()) {
                                                pl.sendMessage(
                                                        FabricGraniteText.key(
                                                                "auraskills.granite.command.sources.empty",
                                                                sk.getDisplayName(lo, false)),
                                                        false);
                                                return;
                                            }
                                            FabricAuraSkillsMenus.openSources(
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
                                                FabricAuraSkillsMenus.openAbilities(
                                                        sk, pl, plugin, returnSkillsPage));
                        return;
                    }
                    if (maxPageIndex > 0) {
                        if (topSlot == pSlot && pageIndex > 0) {
                            final Skill sk = skill;
                            plugin.getScheduler()
                                    .executeSync(
                                            () ->
                                                    FabricAuraSkillsMenus.open(
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
                                                    FabricAuraSkillsMenus.open(
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
            FabricAuraSkills plugin, ItemStack stack, String miniMessage, String fieldLabel) {
        FabricGraniteTemplateStacks.toLiteralNoUnresolved(
                        miniMessage, plugin, "level_progression.builtin", fieldLabel)
                .ifPresent(text -> stack.set(DataComponentTypes.ITEM_NAME, text));
    }
}
