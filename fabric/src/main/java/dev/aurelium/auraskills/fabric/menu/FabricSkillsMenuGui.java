package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import net.minecraft.inventory.Inventory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

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
            ServerPlayerEntity player, FabricAuraSkills plugin, Text title, FabricMenuContext context) {
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (FabricSkillsTemplateSupport.tryOpen(player, plugin, context, user)) {
            return;
        }
        openBuiltIn(player, plugin, title, context, user);
    }

    private static void openBuiltIn(
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            Text title,
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
        Text pageTitle = FabricListMenuPaging.appendPageToTitleIfNeeded(title, page, maxPage);
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
                        inv.setStack(i, ItemStack.EMPTY);
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
                            stack.set(DataComponentTypes.ITEM_NAME, Text.literal(name));
                            int level = user.getSkillLevel(skill);
                            double xp = user.getSkillXp(skill);
                            List<Text> lore = new ArrayList<>();
                            lore.add(
                                    FabricGraniteText.key("auraskills.granite.menu.skills_lore_level", level));
                            lore.add(
                                    FabricGraniteText.key(
                                            "auraskills.granite.menu.skills_lore_xp",
                                            NumberUtil.format2(xp)));
                            String openDetail = plugin
                                    .getBuiltInMenuConfig()
                                    .resolveListOpenDetailKey(
                                            FabricMenuKeys.SKILLS,
                                            FabricMenuYamlSlice.defaultSkills(),
                                            "auraskills.granite.menu.skills_lore_open_detail");
                            lore.add(FabricGraniteText.key(openDetail));
                            if (!user.hasSkillPermission(skill)) {
                                lore.add(
                                        FabricGraniteText.key(
                                                "auraskills.granite.menu.skills_lore_no_permission"));
                            }
                            stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
                            inv.setStack(slot, stack);
                        }
                    }
                    if (fMaxPage > 0) {
                        if (fPage > 0) {
                            putNav(inv, prevSlot, true, "auraskills.granite.menu.paging_prev", "auraskills.granite.menu.paging_prev_lore");
                        } else {
                            inv.setStack(prevSlot, ItemStack.EMPTY);
                        }
                        if (fPage < fMaxPage) {
                            putNav(
                                    inv,
                                    nextSlot,
                                    true,
                                    "auraskills.granite.menu.paging_next",
                                    "auraskills.granite.menu.paging_next_lore");
                        } else {
                            inv.setStack(nextSlot, ItemStack.EMPTY);
                        }
                    } else {
                        inv.setStack(prevSlot, ItemStack.EMPTY);
                        inv.setStack(nextSlot, ItemStack.EMPTY);
                    }
                },
                (topSlot, p, b, t) -> {
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
                                    () -> FabricAuraSkillsMenus.openLevelProgression(s, p, plugin, fPage));
                });
    }

    private static void putNav(
            Inventory inv,
            int slot,
            boolean active,
            String nameKey,
            String loreKey) {
        if (!active) {
            inv.setStack(slot, ItemStack.EMPTY);
            return;
        }
        ItemStack s = new ItemStack(Items.ARROW);
        s.set(DataComponentTypes.ITEM_NAME, FabricGraniteText.key(nameKey));
        s.set(DataComponentTypes.LORE, new LoreComponent(List.of(FabricGraniteText.key(loreKey))));
        inv.setStack(slot, s);
    }
}
