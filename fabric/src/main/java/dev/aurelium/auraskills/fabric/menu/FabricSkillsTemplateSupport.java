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
 * Sprint 101: template-backed {@code skills} list (paged 28-slot grid). On failure returns {@code false} so
 * {@link FabricSkillsMenuGui} uses the built-in path.
 */
public final class FabricSkillsTemplateSupport {

    public static final String TEMPLATE_RESOURCE_PATH = "assets/auraskills/granite/templates/skills.yml";

    private static final int PAGE_SIZE = 28;

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
                    "skills template rows must be 6 for 6×9 menu, got " + template.rows() + "; using built-in menu");
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
        if (skillDef.slots().size() != PAGE_SIZE) {
            plugin.logger().warn(
                    "skills template: skill item must have exactly "
                            + PAGE_SIZE
                            + " slots; got "
                            + skillDef.slots().size()
                            + "; using built-in menu");
            return false;
        }
        if (!skillDef.slots().equals(FabricSourcesMenuGui.builtinSourceGridSlots())) {
            plugin.logger().warn(
                    "skills template: skill slots must match built-in 7×4 grid order; using built-in menu");
            return false;
        }

        boolean skillKeyed = skillDef == template.items().get("skill");
        boolean skillRowAction = GraniteTemplateItems.actionEquals(skillDef, GraniteTemplateActionNames.SKILL_ROW);
        if (!skillKeyed && !skillRowAction) {
            plugin.logger().warn(
                    "skills template: skill grid must use YAML key 'skill' or action skill_row; using built-in menu");
            return false;
        }

        Optional<Integer> prevSlotOpt =
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                        template, "previous", List.of(GraniteTemplateActionNames.PREVIOUS_PAGE));
        Optional<Integer> nextSlotOpt =
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                        template, "next", List.of(GraniteTemplateActionNames.NEXT_PAGE));
        if (prevSlotOpt.isEmpty() || nextSlotOpt.isEmpty()) {
            plugin.logger().warn("skills template: missing previous or next item; using built-in menu");
            return false;
        }
        int prev = prevSlotOpt.get();
        int next = nextSlotOpt.get();

        GraniteTemplateItem prevItem = template.items().get("previous");
        GraniteTemplateItem nextItem = template.items().get("next");
        if (prevItem == null || nextItem == null) {
            plugin.logger().warn(
                    "skills template: require YAML keys previous, next; using built-in menu");
            return false;
        }
        if (!GraniteTemplateItems.actionEquals(prevItem, GraniteTemplateActionNames.PREVIOUS_PAGE)
                || !GraniteTemplateItems.actionEquals(nextItem, GraniteTemplateActionNames.NEXT_PAGE)) {
            plugin.logger().warn(
                    "skills template: navigation actions must be previous_page, next_page; using built-in menu");
            return false;
        }

        Locale locale = user.getLocale();
        var msg = plugin.getMessageProvider();
        List<Skill> all = new ArrayList<>();
        for (Skill sk : plugin.getSkillManager().getEnabledSkills()) {
            all.add(sk);
        }
        int total = all.size();
        int maxPage = FabricListMenuPaging.maxPageIndex(total, PAGE_SIZE);
        int p = FabricListMenuPaging.clampPage(context.listPage(), total, PAGE_SIZE);
        int from = p * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, total);
        List<Skill> pageSkills = all.subList(from, to);

        Map<String, String> globalPh = buildGlobalPlaceholders(p, maxPage);

        String titleExpanded = FabricSkillsPlaceholders.expand(template.title(), globalPh);
        Optional<Text> titleBaseOpt =
                FabricGraniteTemplateStacks.toLiteralNoUnresolved(titleExpanded, plugin, MENU_LOG, "title");
        if (titleBaseOpt.isEmpty()) {
            return false;
        }
        Text pageTitle = FabricListMenuPaging.appendPageToTitleIfNeeded(titleBaseOpt.get(), p, maxPage);

        Optional<ItemStack> fillBase =
                FabricGraniteTemplateStacks.buildFillStack(template.fill(), globalPh, plugin, MENU_LOG);
        if (template.fill().isPresent() && fillBase.isEmpty()) {
            return false;
        }

        List<ItemStack> rowStacks = new ArrayList<>(pageSkills.size());
        for (Skill skill : pageSkills) {
            Map<String, String> rowPh = new HashMap<>(globalPh);
            addSkillRowPlaceholders(rowPh, skill, user, msg, locale);
            Optional<ItemStack> rowStack =
                    FabricGraniteTemplateStacks.buildTemplateItemStack(
                            skillDef, rowPh, plugin, MENU_LOG, "items.skill.row");
            if (rowStack.isEmpty()) {
                return false;
            }
            appendNoPermissionLoreIfNeeded(rowStack.get(), user, skill);
            rowStacks.add(rowStack.get());
        }

        Optional<ItemStack> prevStackOpt = Optional.empty();
        Optional<ItemStack> nextStackOpt = Optional.empty();
        if (maxPage > 0) {
            if (p > 0) {
                Optional<ItemStack> ps =
                        FabricGraniteTemplateStacks.buildTemplateItemStack(
                                prevItem, globalPh, plugin, MENU_LOG, "items.previous");
                if (ps.isEmpty()) {
                    return false;
                }
                prevStackOpt = ps;
            }
            if (p < maxPage) {
                Optional<ItemStack> ns =
                        FabricGraniteTemplateStacks.buildTemplateItemStack(
                                nextItem, globalPh, plugin, MENU_LOG, "items.next");
                if (ns.isEmpty()) {
                    return false;
                }
                nextStackOpt = ns;
            }
        }

        final int fPage = p;
        final int fMax = maxPage;
        final int fFrom = from;
        final int fTo = to;
        final int fTotal = total;
        final Optional<ItemStack> fPrevOpt = prevStackOpt;
        final Optional<ItemStack> fNextOpt = nextStackOpt;

        GraniteContainerMenus.openClickable9x6(
                player,
                pageTitle,
                inv -> {
                    if (fillBase.isPresent()) {
                        ItemStack fill = fillBase.get();
                        for (int s = 0; s < FabricMenuSlotLayout.TOP_SLOTS; s++) {
                            inv.setStack(s, fill.copy());
                        }
                    } else {
                        for (int s = 0; s < FabricMenuSlotLayout.TOP_SLOTS; s++) {
                            inv.setStack(s, ItemStack.EMPTY);
                        }
                    }
                    for (int i = 0; i < rowStacks.size(); i++) {
                        inv.setStack(skillDef.slots().get(i), rowStacks.get(i).copy());
                    }
                    if (fMax > 0) {
                        fPrevOpt.ifPresent(st -> inv.setStack(prev, st.copy()));
                        fNextOpt.ifPresent(st -> inv.setStack(next, st.copy()));
                    }
                },
                (topSlot, pl, b, t) -> {
                    if (fMax > 0) {
                        if (topSlot == prev) {
                            if (fPage > 0) {
                                plugin.getScheduler()
                                        .executeSync(
                                                () -> plugin
                                                        .getMenuRegistry()
                                                        .open(
                                                                FabricMenuKeys.SKILLS,
                                                                pl,
                                                                plugin,
                                                                FabricMenuContext.none()
                                                                        .withListPage(fPage - 1)));
                            }
                            return;
                        }
                        if (topSlot == next) {
                            if (fPage < fMax) {
                                plugin.getScheduler()
                                        .executeSync(
                                                () -> plugin
                                                        .getMenuRegistry()
                                                        .open(
                                                                FabricMenuKeys.SKILLS,
                                                                pl,
                                                                plugin,
                                                                FabricMenuContext.none()
                                                                        .withListPage(fPage + 1)));
                            }
                            return;
                        }
                    }
                    int idx = skillDef.slots().indexOf(topSlot);
                    if (idx < 0) {
                        return;
                    }
                    int gIdx = fFrom + idx;
                    if (gIdx < fFrom || gIdx >= fTo || gIdx < 0 || gIdx >= fTotal) {
                        return;
                    }
                    Skill skill = all.get(gIdx);
                    if (!user.hasSkillPermission(skill)) {
                        return;
                    }
                    final Skill s = skill;
                    plugin.getScheduler()
                            .executeSync(() -> FabricAuraSkillsMenus.openLevelProgression(s, pl, plugin, fPage));
                });
        return true;
    }

    private static Map<String, String> buildGlobalPlaceholders(int pageIndex, int maxPageIndex) {
        Map<String, String> m = new HashMap<>();
        m.put("page", String.valueOf(pageIndex + 1));
        m.put("total_pages", String.valueOf(maxPageIndex + 1));
        return m;
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
