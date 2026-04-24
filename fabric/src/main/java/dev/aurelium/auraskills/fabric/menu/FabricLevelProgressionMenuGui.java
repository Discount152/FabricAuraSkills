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
import java.util.Locale;

/**
 * One-skill “level progression” view: live level + XP from {@link User}, not full Bukkit
 * {@code level_progression.yml} / reward tables.
 */
final class FabricLevelProgressionMenuGui {

    /** Main info stack (row 0, center). */
    private static final int INFO_SLOT = 4;
    /** “Back” — matches common large-chest bottom-row center patterns. */
    private static final int BACK_SLOT = 49;

    private FabricLevelProgressionMenuGui() {
    }

    static void open(ServerPlayerEntity player, FabricAuraSkills plugin, Skill skill) {
        User user = plugin.getUserManager().getUser(player.getUuid());
        Locale locale = user.getLocale();
        var msg = plugin.getMessageProvider();
        String skillName = msg.getSkillDisplayName(skill, locale, true);
        Text title = Text.translatable("auraskills.granite.menu.level_progression_title", skillName);
        int level = user.getSkillLevel(skill);
        double currentXp = user.getSkillXp(skill);
        var xpReqs = plugin.getXpRequirements();
        boolean maxed = user.getSkillLevel(skill) >= skill.getMaxLevel()
                || xpReqs.getListSize(skill) <= level - 1;
        double xpToNext = !maxed ? xpReqs.getXpRequired(skill, level + 1) : 0.0;

        GraniteContainerMenus.openClickable9x6(
                player,
                title,
                inv -> {
                    ItemStack main = new ItemStack(Items.BOOK);
                    main.set(
                            DataComponentTypes.ITEM_NAME,
                            Text.translatable("auraskills.granite.menu.level_progression_main_title", skillName));
                    List<Text> lore = new ArrayList<>();
                    lore.add(Text.translatable("auraskills.granite.menu.level_progression_lore_level", level));
                    lore.add(Text.translatable("auraskills.granite.menu.level_progression_lore_xp", NumberUtil.format2(currentXp)));
                    if (maxed) {
                        lore.add(Text.translatable("auraskills.granite.menu.level_progression_lore_maxed"));
                    } else {
                        lore.add(Text.translatable(
                                "auraskills.granite.menu.level_progression_lore_to_next",
                                NumberUtil.format2(currentXp),
                                NumberUtil.format2(xpToNext)));
                    }
                    if (!user.hasSkillPermission(skill)) {
                        lore.add(Text.translatable("auraskills.granite.menu.skills_lore_no_permission"));
                    }
                    main.set(DataComponentTypes.LORE, new LoreComponent(lore));
                    inv.setStack(INFO_SLOT, main);

                    ItemStack back = new ItemStack(Items.ARROW);
                    back.set(
                            DataComponentTypes.ITEM_NAME,
                            Text.translatable("auraskills.granite.menu.level_progression_back"));
                    back.set(
                            DataComponentTypes.LORE,
                            new LoreComponent(List.of(
                                    Text.translatable("auraskills.granite.menu.level_progression_back_lore"))));
                    inv.setStack(BACK_SLOT, back);
                },
                (topSlot, p, button, actionType) -> {
                    if (topSlot != BACK_SLOT) {
                        return;
                    }
                    plugin.getScheduler().executeSync(() -> FabricAuraSkillsMenus.open(FabricAuraSkillsMenus.Id.SKILLS, p, plugin));
                });
    }
}
