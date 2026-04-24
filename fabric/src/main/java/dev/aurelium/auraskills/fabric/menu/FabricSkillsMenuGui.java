package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.user.User;
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

/**
 * 6×9 chest: enabled skills with level/XP; top-slot click opens
 * {@link FabricLevelProgressionMenuGui}. **Sprint 34:** list slot range from {@code granite.layout}.
 */
final class FabricSkillsMenuGui {

    private FabricSkillsMenuGui() {
    }

    static void open(
            ServerPlayerEntity player, FabricAuraSkills plugin, Text title, FabricMenuContext context) {
        User user = plugin.getUserManager().getUser(player.getUuid());
        var locale = user.getLocale();
        var msg = plugin.getMessageProvider();
        FabricMenuYamlSlice cfg = plugin
                .getBuiltInMenuConfig()
                .getSlice(FabricMenuKeys.SKILLS, FabricMenuYamlSlice.defaultSkills());
        int listStart = cfg.listStartSlot();
        int listEnd = cfg.listEndSlotExclusive();
        List<Skill> skillsInOrder = new ArrayList<>();
        GraniteContainerMenus.openClickable9x6(
                player,
                title,
                inv -> {
                    int slot = listStart;
                    for (Skill skill : plugin.getSkillManager().getEnabledSkills()) {
                        if (slot >= listEnd || slot >= FabricMenuSlotLayout.TOP_SLOTS) {
                            break;
                        }
                        skillsInOrder.add(skill);
                        ItemStack stack = new ItemStack(Items.BOOK);
                        String name = msg.getSkillDisplayName(skill, locale, true);
                        stack.set(DataComponentTypes.ITEM_NAME, Text.literal(name));
                        int level = user.getSkillLevel(skill);
                        double xp = user.getSkillXp(skill);
                        List<Text> lore = new ArrayList<>();
                        lore.add(Text.translatable("auraskills.granite.menu.skills_lore_level", level));
                        lore.add(Text.translatable("auraskills.granite.menu.skills_lore_xp", NumberUtil.format2(xp)));
                        String openDetail = plugin
                                .getBuiltInMenuConfig()
                                .resolveListOpenDetailKey(
                                        FabricMenuKeys.SKILLS,
                                        FabricMenuYamlSlice.defaultSkills(),
                                        "auraskills.granite.menu.skills_lore_open_detail");
                        lore.add(Text.translatable(openDetail));
                        if (!user.hasSkillPermission(skill)) {
                            lore.add(Text.translatable("auraskills.granite.menu.skills_lore_no_permission"));
                        }
                        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
                        inv.setStack(slot, stack);
                        slot++;
                    }
                },
                (topSlot, p, b, t) -> {
                    if (topSlot < listStart || topSlot >= listEnd) {
                        return;
                    }
                    int idx = topSlot - listStart;
                    if (idx < 0 || idx >= skillsInOrder.size()) {
                        return;
                    }
                    Skill skill = skillsInOrder.get(idx);
                    if (!user.hasSkillPermission(skill)) {
                        return;
                    }
                    plugin.getScheduler()
                            .executeSync(() -> FabricAuraSkillsMenus.openLevelProgression(skill, p, plugin));
                });
    }
}
