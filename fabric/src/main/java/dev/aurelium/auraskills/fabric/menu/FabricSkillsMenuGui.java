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
 * First Vertical slice: read-only 6×9 chest showing enabled skills, level, and XP (server state)
 * for the opening player. Future: Slate templates, click → level_progression, placeholder pipeline.
 */
final class FabricSkillsMenuGui {

    private FabricSkillsMenuGui() {
    }

    static void open(ServerPlayerEntity player, FabricAuraSkills plugin) {
        User user = plugin.getUserManager().getUser(player.getUuid());
        var locale = user.getLocale();
        var msg = plugin.getMessageProvider();
        GraniteContainerMenus.openReadOnly9x6(
                player,
                Text.translatable("auraskills.granite.menu.skills_title"),
                inv -> {
                    int i = 0;
                    for (Skill skill : plugin.getSkillManager().getEnabledSkills()) {
                        if (i >= inv.size()) {
                            break;
                        }
                        ItemStack stack = new ItemStack(Items.BOOK);
                        String name = msg.getSkillDisplayName(skill, locale, true);
                        stack.set(DataComponentTypes.ITEM_NAME, Text.literal(name));
                        int level = user.getSkillLevel(skill);
                        double xp = user.getSkillXp(skill);
                        List<Text> lore = new ArrayList<>();
                        lore.add(Text.translatable("auraskills.granite.menu.skills_lore_level", level));
                        lore.add(Text.translatable("auraskills.granite.menu.skills_lore_xp", NumberUtil.format2(xp)));
                        if (!user.hasSkillPermission(skill)) {
                            lore.add(Text.translatable("auraskills.granite.menu.skills_lore_no_permission"));
                        }
                        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
                        inv.setStack(i++, stack);
                    }
                });
    }
}
