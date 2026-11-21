package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * 6×9 chest: enabled skills with level/XP; top-slot click opens
 * {@link FabricLevelProgressionMenuGui}. **Sprint 34:** list slot range from {@code granite.layout}.
 * **Sprint 36:** list row item from {@code granite.items.list}. **Sprint 37:** paged list + {@code
 * granite.paging} prev/next. **Sprint 101:** tries bundled GraniteLib template first.
 */
final class FabricSkillsMenuGui {

    private FabricSkillsMenuGui() {
    }

    static void open(
            ServerPlayer player, FabricGraniteSkills plugin, Component title, FabricMenuContext context) {
        User user = FabricMenuUserReadiness.loadedUserOrWarn(plugin, player);
        if (user == null) {
            return;
        }
        if (FabricSkillsTemplateSupport.tryOpen(player, plugin, context, user)) {
            return;
        }
        openBuiltIn(player, plugin, title, context, user);
    }

    private static void openBuiltIn(
            ServerPlayer player,
            FabricGraniteSkills plugin,
            Component title,
            FabricMenuContext context,
            User user) {
        var locale = user.getLocale();
        var msg = plugin.getMessageProvider();
        FabricMenuYamlSlice cfg = plugin
                .getBuiltInMenuConfig()
                .getSlice(FabricMenuKeys.SKILLS, FabricMenuYamlSlice.defaultSkills());
        int listStart = cfg.listStartSlot();
        int listEnd = cfg.listEndSlotExclusive();
        FabricListMenuPaging.ListContentRegion region =
                FabricListMenuPaging.listContentRegion(listStart, listEnd);
        int pageSize = region.size();
        Item listItem = FabricMenuItemDefinitions.resolveItem(
                cfg.listItemId(), Items.BOOK, plugin, "menus/skills.yml", "list");
        int prevSlot = cfg.pagingPrevSlot();
        int nextSlot = cfg.pagingNextSlot();
        List<Skill> all = new ArrayList<>();
        for (Skill sk : plugin.getSkillManager().getEnabledSkills()) {
            all.add(sk);
        }
        int total = all.size();
        int maxPage = FabricListMenuPaging.maxPageIndex(total, pageSize);
        int page = FabricListMenuPaging.clampPage(context.listPage(), total, pageSize);
        int from = page * pageSize;
        int to = pageSize == 0 ? 0 : Math.min(from + pageSize, total);
        int contentStart = region.startInclusive();
        int contentEndEx = region.endExclusive();
        Component pageTitle = FabricListMenuPaging.appendPageToTitleIfNeeded(title, page, maxPage);
        final int fFrom = from;
        final int fTo = to;
        final int fPage = page;
        final int fMaxPage = maxPage;
        final int fPageSize = pageSize;
        final int fTotal = total;
        GraniteContainerMenus.openClickable9x6(
                player,
                pageTitle,
                inv -> {
                    for (int i = 0; i < FabricMenuSlotLayout.TOP_SLOTS; i++) {
                        inv.setItem(i, ItemStack.EMPTY);
                    }
                    if (pageSize > 0) {
                        for (int g = fFrom; g < fTo; g++) {
                            int slot = contentStart + (g - fFrom);
                            if (slot >= contentEndEx) {
                                break;
                            }
                            Skill skill = all.get(g);
                            ItemStack stack = new ItemStack(listItem);
                            String name = msg.getSkillDisplayName(skill, locale, true);
                            stack.set(DataComponents.ITEM_NAME, Component.literal(name));
                            int level = user.getSkillLevel(skill);
                            double xp = user.getSkillXp(skill);
                            List<Component> lore = new ArrayList<>();
                            lore.add(
                                    FabricGraniteText.key("graniteskills.granite.menu.skills_lore_level", level));
                            lore.add(
                                    FabricGraniteText.key(
                                            "graniteskills.granite.menu.skills_lore_xp",
                                            NumberUtil.format2(xp)));
                            String openDetail = plugin
                                    .getBuiltInMenuConfig()
                                    .resolveListOpenDetailKey(
                                            FabricMenuKeys.SKILLS,
                                            FabricMenuYamlSlice.defaultSkills(),
                                            "graniteskills.granite.menu.skills_lore_open_detail");
                            lore.add(FabricGraniteText.key(openDetail));
                            if (!user.hasSkillPermission(skill)) {
                                lore.add(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.menu.skills_lore_no_permission"));
                            }
                            stack.set(DataComponents.LORE, new ItemLore(lore));
                            inv.setItem(slot, stack);
                        }
                    }
                    if (fMaxPage > 0) {
                        if (fPage > 0) {
                            putNav(inv, prevSlot, true, "graniteskills.granite.menu.paging_prev", "graniteskills.granite.menu.paging_prev_lore");
                        } else {
                            inv.setItem(prevSlot, ItemStack.EMPTY);
                        }
                        if (fPage < fMaxPage) {
                            putNav(
                                    inv,
                                    nextSlot,
                                    true,
                                    "graniteskills.granite.menu.paging_next",
                                    "graniteskills.granite.menu.paging_next_lore");
                        } else {
                            inv.setItem(nextSlot, ItemStack.EMPTY);
                        }
                    } else {
                        inv.setItem(prevSlot, ItemStack.EMPTY);
                        inv.setItem(nextSlot, ItemStack.EMPTY);
                    }
                },
                (topSlot, p, b, t) -> {
                    if (!FabricMenuClicks.activates(t)) {
                        return;
                    }
                    if (fMaxPage > 0) {
                        if (topSlot == prevSlot) {
                            if (fPage > 0) {
                                plugin.getScheduler()
                                        .executeSync(() -> plugin
                                                .getMenuRegistry()
                                                .open(
                                                        FabricMenuKeys.SKILLS,
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
                                                        FabricMenuKeys.SKILLS,
                                                        p,
                                                        plugin,
                                                        FabricMenuContext.none()
                                                                .withListPage(fPage + 1)));
                            }
                            return;
                        }
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
                    Skill skill = all.get(gIdx);
                    if (!user.hasSkillPermission(skill)) {
                        return;
                    }
                    final Skill s = skill;
                    plugin.getScheduler()
                            .executeSync(
                                    () -> FabricGraniteSkillsMenus.openLevelProgression(s, p, plugin, fPage));
                });
    }

    private static void putNav(
            Container inv,
            int slot,
            boolean active,
            String nameKey,
            String loreKey) {
        if (!active) {
            inv.setItem(slot, ItemStack.EMPTY);
            return;
        }
        ItemStack s = new ItemStack(Items.ARROW);
        s.set(DataComponents.ITEM_NAME, FabricGraniteText.key(nameKey));
        s.set(DataComponents.LORE, new ItemLore(List.of(FabricGraniteText.key(loreKey))));
        inv.setItem(slot, s);
    }
}
