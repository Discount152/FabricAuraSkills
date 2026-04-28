package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.message.MessageProvider;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import dev.granitelib.fabric.template.GraniteTemplateActionNames;
import dev.granitelib.fabric.template.GraniteTemplateItems;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Template-backed {@code skills} hub — Sprint 113 locked layout (11 skills + utilities). On failure returns
 * {@code false} so {@link FabricSkillsMenuGui} uses the built-in path.
 */
public final class FabricSkillsTemplateSupport {

    public static final String TEMPLATE_RESOURCE_PATH = "assets/auraskills/granite/templates/skills.yml";

    /** 6×9 menu: curated skill cells (archery…enchanting); row 5 kept empty. */
    public static final List<Integer> LOCKED_SKILL_GRID_SLOTS = List.of(
            12, 13, 14, 20, 21, 22, 23, 24, 30, 31, 32);

    public static final List<String> LOCKED_SKILL_IDS = List.of(
            "archery",
            "fighting",
            "defense",
            "farming",
            "foraging",
            "mining",
            "fishing",
            "excavation",
            "agility",
            "alchemy",
            "enchanting");

    public static final int EXPECTED_SKILL_COUNT = 11;

    private static final String MENU_LOG = "skills template";

    private FabricSkillsTemplateSupport() {
    }

    public static boolean tryOpen(ServerPlayerEntity player, FabricAuraSkills plugin, FabricMenuContext context, User user) {
        try {
            return tryOpenInner(player, plugin, context, user);
        } catch (RuntimeException e) {
            plugin.logger().warn("skills template render failed; using built-in menu", e);
            return false;
        }
    }

    private static boolean tryOpenInner(
            ServerPlayerEntity player, FabricAuraSkills plugin, FabricMenuContext context, User user) {
        Optional<FabricGraniteMenuTemplateLoader.LoadedTemplate> loaded =
                plugin.menuTemplateRegistry().getLoadedTemplate("skills");
        if (loaded.isEmpty()) {
            return false;
        }
        GraniteMenuTemplate template = loaded.get().template();

        if (!"skills".equalsIgnoreCase(template.id())) {
            plugin.logger().warn(
                    "skills template id must be 'skills', got '" + template.id() + "'; using built-in menu");
            return false;
        }
        if (template.rows() != 6) {
            plugin.logger().warn(
                    "skills template rows must be 6 for locked 6×9 layout, got "
                            + template.rows()
                            + "; using built-in menu");
            return false;
        }

        Optional<String> actionContract =
                FabricGraniteMenuTemplateActionContract.validateTemplateActions("skills", template);
        if (actionContract.isPresent()) {
            plugin.logger().warn(MENU_LOG + ": " + actionContract.get() + "; using built-in menu");
            return false;
        }

        GraniteTemplateItem skillDef = GraniteTemplateItems.findSkillGridDefinition(template);
        if (skillDef == null) {
            plugin.logger().warn(
                    "skills template: no skill row (key 'skill' or action skill_row); using built-in menu");
            return false;
        }
        if (skillDef.slots().size() != EXPECTED_SKILL_COUNT || !skillDef.slots().equals(LOCKED_SKILL_GRID_SLOTS)) {
            plugin.logger().warn(
                    "skills template: skill item must use Sprint 113 locked slot list; using built-in menu");
            return false;
        }

        boolean skillKeyed = skillDef == template.items().get("skill");
        boolean skillRowAction = GraniteTemplateItems.actionEquals(skillDef, GraniteTemplateActionNames.SKILL_ROW);
        if (!skillKeyed && !skillRowAction) {
            plugin.logger().warn(
                    "skills template: skill grid must use YAML key 'skill' or action skill_row; using built-in menu");
            return false;
        }

        int lbSlot = requiredUtilitySlot(template, GraniteTemplateActionNames.OPEN_LEADERBOARD);
        int statsSlot = requiredUtilitySlot(template, GraniteTemplateActionNames.OPEN_STATS);
        int unclaimedSlot = requiredUtilitySlot(template, GraniteTemplateActionNames.OPEN_UNCLAIMED_ITEMS);
        int closeSlot = requiredUtilitySlot(template, GraniteTemplateActionNames.CLOSE);
        if (lbSlot < 0 || statsSlot < 0 || unclaimedSlot < 0 || closeSlot < 0) {
            plugin.logger().warn(MENU_LOG + ": utility item missing or multi-slot; using built-in menu");
            return false;
        }

        GraniteTemplateItem leaderboardItem =
                GraniteTemplateItems.findFirstByAction(template, GraniteTemplateActionNames.OPEN_LEADERBOARD)
                        .orElseThrow();
        GraniteTemplateItem statsItem =
                GraniteTemplateItems.findFirstByAction(template, GraniteTemplateActionNames.OPEN_STATS)
                        .orElseThrow();
        GraniteTemplateItem unclaimedItem =
                GraniteTemplateItems.findFirstByAction(template, GraniteTemplateActionNames.OPEN_UNCLAIMED_ITEMS)
                        .orElseThrow();
        GraniteTemplateItem closeItem =
                GraniteTemplateItems.findFirstByAction(template, GraniteTemplateActionNames.CLOSE).orElseThrow();

        if (GraniteTemplateItems.findAllByAction(template, GraniteTemplateActionNames.OPEN_LEADERBOARD).size() != 1
                || GraniteTemplateItems.findAllByAction(template, GraniteTemplateActionNames.OPEN_STATS).size() != 1
                || GraniteTemplateItems.findAllByAction(template, GraniteTemplateActionNames.OPEN_UNCLAIMED_ITEMS).size()
                        != 1
                || GraniteTemplateItems.findAllByAction(template, GraniteTemplateActionNames.CLOSE).size() != 1) {
            plugin.logger().warn(MENU_LOG + ": duplicate utility action; using built-in menu");
            return false;
        }

        Locale locale = user.getLocale();
        MessageProvider msg = plugin.getMessageProvider();
        List<Skill> gridSkills = resolveLockedSkills(plugin);
        if (gridSkills.size() != EXPECTED_SKILL_COUNT) {
            plugin.logger().warn(
                    MENU_LOG + ": expected "
                            + EXPECTED_SKILL_COUNT
                            + " enabled skills matching locked ids, got "
                            + gridSkills.size()
                            + "; using built-in menu");
            return false;
        }

        Map<String, String> globalPh = Map.of("page", "1", "total_pages", "1");

        String titleExpanded = FabricSkillsPlaceholders.expand(template.title(), globalPh);
        Optional<Text> titleOpt =
                FabricGraniteTemplateStacks.toLiteralNoUnresolved(titleExpanded, plugin, MENU_LOG, "title");
        if (titleOpt.isEmpty()) {
            return false;
        }

        Optional<ItemStack> leaderboardStack =
                FabricGraniteTemplateStacks.buildTemplateItemStack(
                        leaderboardItem, globalPh, plugin, MENU_LOG, "items.leaderboard");
        if (leaderboardStack.isEmpty()) {
            return false;
        }

        Optional<ItemStack> statsTemplate =
                FabricGraniteTemplateStacks.buildTemplateItemStack(
                        statsItem, globalPh, plugin, MENU_LOG, "items.open_stats");
        if (statsTemplate.isEmpty()) {
            return false;
        }
        ItemStack statsStack = FabricMenuPresentation.playerHeadWithTemplateDecor(statsTemplate.get(), player);

        Optional<ItemStack> unclaimedTemplate =
                FabricGraniteTemplateStacks.buildTemplateItemStack(
                        unclaimedItem, globalPh, plugin, MENU_LOG, "items.unclaimed");
        if (unclaimedTemplate.isEmpty()) {
            return false;
        }

        Optional<ItemStack> closeStack =
                FabricGraniteTemplateStacks.buildTemplateItemStack(
                        closeItem, globalPh, plugin, MENU_LOG, "items.close");
        if (closeStack.isEmpty()) {
            return false;
        }

        boolean hasUnclaimed = !user.getUnclaimedItems().isEmpty();

        List<ItemStack> rowStacks = new ArrayList<>(gridSkills.size());
        for (Skill skill : gridSkills) {
            Map<String, String> rowPh = new HashMap<>(globalPh);
            addSkillRowPlaceholders(rowPh, skill, user, msg, locale);
            Optional<ItemStack> rowStack =
                    FabricGraniteTemplateStacks.buildTemplateItemStack(
                            skillDef, rowPh, plugin, MENU_LOG, "items.skill.row");
            if (rowStack.isEmpty()) {
                return false;
            }
            appendNoPermissionLoreIfNeeded(rowStack.get(), user, skill);
            rowStacks.add(FabricMenuPresentation.applySkillRowIcon(rowStack.get(), skill));
        }

        final int fLb = lbSlot;
        final int fSt = statsSlot;
        final int fUn = unclaimedSlot;
        final int fCl = closeSlot;
        final boolean fHasUnclaimed = hasUnclaimed;

        GraniteContainerMenus.openClickable9x6(
                player,
                titleOpt.get(),
                inv -> {
                    for (int s = 0; s < FabricMenuSlotLayout.TOP_SLOTS; s++) {
                        inv.setStack(s, ItemStack.EMPTY);
                    }
                    inv.setStack(fLb, leaderboardStack.get().copy());
                    inv.setStack(fSt, statsStack.copy());
                    if (fHasUnclaimed) {
                        inv.setStack(fUn, unclaimedTemplate.get().copy());
                    }
                    inv.setStack(fCl, closeStack.get().copy());
                    for (int i = 0; i < rowStacks.size(); i++) {
                        inv.setStack(skillDef.slots().get(i), rowStacks.get(i).copy());
                    }
                },
                (topSlot, pl, b, t) -> {
                    if (topSlot == fCl) {
                        pl.closeHandledScreen();
                        return;
                    }
                    if (topSlot == fLb) {
                        plugin.getScheduler()
                                .executeSync(
                                        () -> FabricLeaderboardMenuGui.open(
                                                pl, plugin, FabricLeaderboardMenuGui.Mode.POWER, null, 1));
                        return;
                    }
                    if (topSlot == fSt) {
                        plugin.getScheduler()
                                .executeSync(
                                        () -> plugin
                                                .getMenuRegistry()
                                                .open(
                                                        FabricMenuKeys.STATS,
                                                        pl,
                                                        plugin,
                                                        FabricMenuContext.none()));
                        return;
                    }
                    if (topSlot == fUn) {
                        if (!fHasUnclaimed) {
                            return;
                        }
                        plugin.getScheduler()
                                .executeSync(() -> FabricAuraSkillsMenus.openUnclaimedItems(pl, plugin, 0));
                        return;
                    }
                    int idx = skillDef.slots().indexOf(topSlot);
                    if (idx < 0 || idx >= gridSkills.size()) {
                        return;
                    }
                    Skill skill = gridSkills.get(idx);
                    if (!user.hasSkillPermission(skill)) {
                        return;
                    }
                    final Skill s = skill;
                    plugin.getScheduler()
                            .executeSync(() -> FabricAuraSkillsMenus.openLevelProgression(s, pl, plugin, 0));
                });
        return true;
    }

    private static int requiredUtilitySlot(GraniteMenuTemplate template, String normalizedAction) {
        return GraniteTemplateItems.findFirstByAction(template, normalizedAction)
                .filter(item -> item.slots().size() == 1)
                .map(item -> item.slots().getFirst())
                .orElse(-1);
    }

    /** Enabled skills matching {@link #LOCKED_SKILL_IDS} in order; skips disabled or missing registry entries. */
    static List<Skill> resolveLockedSkills(FabricAuraSkills plugin) {
        List<Skill> out = new ArrayList<>(EXPECTED_SKILL_COUNT);
        for (String id : LOCKED_SKILL_IDS) {
            Skill found = null;
            for (Skill sk : plugin.getSkillManager().getEnabledSkills()) {
                if (id.equalsIgnoreCase(sk.getId().getSimpleName())) {
                    found = sk;
                    break;
                }
            }
            if (found != null) {
                out.add(found);
            }
        }
        return out;
    }

    private static void addSkillRowPlaceholders(
            Map<String, String> m, Skill skill, User user, MessageProvider msg, Locale locale) {
        m.put("skill", skill.getId().getSimpleName());
        m.put("skill_display", msg.getSkillDisplayName(skill, locale, true));
        m.put("skill_level", String.valueOf(user.getSkillLevel(skill)));
        String xp = NumberUtil.format2(user.getSkillXp(skill));
        m.put("skill_xp", xp);
        m.put("skill_xp_in_level", xp);
    }

    private static void appendNoPermissionLoreIfNeeded(ItemStack stack, User user, Skill skill) {
        if (user.hasSkillPermission(skill)) {
            return;
        }
        List<Text> lore = new ArrayList<>();
        var existing = stack.get(DataComponentTypes.LORE);
        if (existing != null) {
            lore.addAll(existing.lines());
        }
        lore.add(FabricGraniteText.key("auraskills.granite.menu.skills_lore_no_permission"));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
    }
}
