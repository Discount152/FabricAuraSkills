package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.api.ability.AbstractAbility;
import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.ability.AbilityManager;
import dev.granite.graniteskills.common.mana.ManaAbilityManager;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.math.RomanNumber;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One-skill **abilities** view (Bukkit {@code AbilitiesMenu} Slate builder). **Sprint 73:** bounded
 * Granite 6×9 — lists enabled {@link Ability} rows + optional {@link ManaAbility}; **Back** →
 * **level_progression** for the same skill (Bukkit: {@code previous_menu} / global back). Not YAML
 * layout parity. **Sprint 102:** tries bundled GraniteLib template first.
 */
public final class FabricAbilitiesMenuGui {

    static final int SLOT_BACK = 45;
    /** Fills row 1 cols 2–8 then row 2 — enough for reference skills (≤3 abilities + mana). */
    private static final int[] CONTENT_SLOTS = {
            11, 12, 13, 14, 15, 16, 17, 20, 21, 22, 23, 24, 25, 26
    };

    private static final class PassiveIconsUnlocked {
        private static final net.minecraft.world.item.Item[] ITEMS = {
                Items.DYE.lime(),
                Items.EXPERIENCE_BOTTLE,
                Items.LAPIS_LAZULI,
                Items.EMERALD,
                Items.AMETHYST_SHARD,
                Items.SUNFLOWER
        };
    }

    private FabricAbilitiesMenuGui() {}

    /** Same order as built-in passive ability placement (Sprint 102 template validation). */
    static List<Integer> builtinAbilityContentSlots() {
        List<Integer> out = new ArrayList<>(CONTENT_SLOTS.length);
        for (int s : CONTENT_SLOTS) {
            out.add(s);
        }
        return List.copyOf(out);
    }

    public static void open(
            ServerPlayer menuPlayer, FabricGraniteSkills plugin, Skill skill, int returnToSkillsListPage) {
        if (!skill.isEnabled()) {
            return;
        }
        User user = plugin.getUserManager().getUser(menuPlayer.getUUID());
        if (user == null) {
            return;
        }
        if (FabricAbilitiesTemplateSupport.tryOpen(menuPlayer, plugin, skill, returnToSkillsListPage, user)) {
            return;
        }
        openBuiltIn(menuPlayer, plugin, skill, returnToSkillsListPage, user);
    }

    static void openBuiltIn(
            ServerPlayer menuPlayer,
            FabricGraniteSkills plugin,
            Skill skill,
            int returnToSkillsListPage,
            User user) {
        Locale lo = user.getLocale();
        AbilityManager am = plugin.getAbilityManager();
        ManaAbilityManager mm = plugin.getManaAbilityManager();
        Component title = FabricMenuTitles.abilitiesMenuTitle(plugin.getMessageProvider(), skill, lo);
        List<Ability> regular = new ArrayList<>();
        for (Ability a : skill.getAbilities()) {
            if (a.isEnabled()) {
                regular.add(a);
            }
        }
        ManaAbility mana = skill.getManaAbility();
        boolean showMana = mana != null && mana.isEnabled() && mm.isLoaded(mana);
        List<Placed> placed = new ArrayList<>();
        int si = 0;
        for (Ability a : regular) {
            if (si >= CONTENT_SLOTS.length - (showMana ? 1 : 0)) {
                break;
            }
            placed.add(
                    new Placed(
                            CONTENT_SLOTS[si++],
                            stackForAbility(plugin, am, user, a, lo, skill)));
        }
        if (showMana && si < CONTENT_SLOTS.length) {
            placed.add(
                    new Placed(
                            CONTENT_SLOTS[si],
                            stackForManaAbility(plugin, mm, user, mana, lo, skill)));
        }
        final int fBackPage = returnToSkillsListPage;
        final Skill fSkill = skill;
        GraniteContainerMenus.openClickable9x6(
                menuPlayer,
                title,
                inv -> {
                    for (int i = 0; i < FabricMenuSlotLayout.TOP_SLOTS; i++) {
                        inv.setItem(i, ItemStack.EMPTY);
                    }
                    for (Placed p : placed) {
                        if (p.slot() >= 0 && p.slot() < 54) {
                            inv.setItem(p.slot(), p.stack());
                        }
                    }
                    putControl(
                            inv,
                            SLOT_BACK,
                            true,
                            "graniteskills.granite.menu.abilities_back",
                            "graniteskills.granite.menu.abilities_back_lore",
                            Items.IRON_DOOR);
                },
                (topSlot, pl, b, t) -> {
                    if (topSlot == SLOT_BACK) {
                        plugin.getScheduler()
                                .executeSync(
                                        () ->
                                                FabricGraniteSkillsMenus.openLevelProgression(
                                                        fSkill, pl, plugin, fBackPage));
                    }
                });
    }

    static boolean isAbilityNotMaxed(User user, AbstractAbility ability) {
        return isNotMaxed(user, ability);
    }

    private record Placed(int slot, ItemStack stack) {}

    private static ItemStack stackForAbility(
            FabricGraniteSkills plugin,
            AbilityManager am,
            User user,
            Ability ability,
            Locale lo,
            Skill skill) {
        int lvl = user.getAbilityLevel(ability);
        boolean unlocked = lvl >= 1;
        ItemStack stack = passiveAbilityIcon(ability, unlocked);
        String name = ability.getDisplayName(lo, false);
        stack.set(DataComponents.ITEM_NAME, FabricMenuLoreFormatting.formatMenuLine(plugin, name));
        List<Component> lore = new ArrayList<>();
        if (!unlocked) {
            lore.add(FabricGraniteText.key("graniteskills.granite.menu.abilities_state_locked"));
            String desc =
                    FabricAbilityMenuLore.passiveAbilityDescriptionBodyPlain(
                            plugin,
                            am,
                            user,
                            ability,
                            FabricAbilityMenuLore.PassiveBodyMode.LOCKED);
            lore.addAll(splitDesc(plugin, desc, 6, FabricMenuLoreFormatting.DEFAULT_MENU_LORE_WRAP_WIDTH));
            lore.add(
                    FabricGraniteText.key(
                            "graniteskills.granite.menu.abilities_unlocks_at",
                            RomanNumber.toRoman(ability.getUnlock(), plugin),
                            plugin.getMessageProvider().getSkillDisplayName(skill, lo, true)));
        } else {
            lore.add(
                    FabricGraniteText.key(
                            "graniteskills.granite.menu.abilities_state_level",
                            RomanNumber.toRoman(lvl, plugin)));
            if (isNotMaxed(user, ability)) {
                String desc =
                        FabricAbilityMenuLore.passiveAbilityDescriptionBodyPlain(
                                plugin,
                                am,
                                user,
                                ability,
                                FabricAbilityMenuLore.PassiveBodyMode.UPGRADING);
                lore.addAll(splitDesc(plugin, desc, 5, FabricMenuLoreFormatting.DEFAULT_MENU_LORE_WRAP_WIDTH));
            } else {
                String desc =
                        FabricAbilityMenuLore.passiveAbilityDescriptionBodyPlain(
                                plugin,
                                am,
                                user,
                                ability,
                                FabricAbilityMenuLore.PassiveBodyMode.MAXED);
                lore.addAll(splitDesc(plugin, desc, 5, FabricMenuLoreFormatting.DEFAULT_MENU_LORE_WRAP_WIDTH));
                lore.add(FabricGraniteText.key("graniteskills.granite.menu.abilities_maxed"));
            }
        }
        stack.set(
                DataComponents.LORE, new ItemLore(lore.stream().toList()));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(stack);
        return stack;
    }

    private static ItemStack stackForManaAbility(
            FabricGraniteSkills plugin,
            ManaAbilityManager mm,
            User user,
            ManaAbility mana,
            Locale lo,
            Skill skill) {
        int lvl = user.getManaAbilityLevel(mana);
        boolean unlocked = lvl >= 1;
        var stack = new ItemStack(unlocked ? Items.NETHER_STAR : Items.PRISMARINE_SHARD);
        String name = mana.getDisplayName(lo, false);
        stack.set(DataComponents.ITEM_NAME, FabricMenuLoreFormatting.formatMenuLine(plugin, name));
        List<Component> lore = new ArrayList<>();
        lore.add(
                FabricGraniteText.key(
                        "graniteskills.granite.menu.abilities_mana_ability",
                        plugin.getMessageProvider().getSkillDisplayName(skill, lo, true)));
        if (!unlocked) {
            lore.add(FabricGraniteText.key("graniteskills.granite.menu.abilities_state_locked"));
            String desc =
                    FabricAbilityMenuLore.manaAbilityDescriptionBodyPlain(
                            plugin,
                            mm,
                            user,
                            mana,
                            FabricAbilityMenuLore.ManaBodyMode.LOCKED);
            lore.addAll(splitDesc(plugin, desc, 5, FabricMenuLoreFormatting.DEFAULT_MENU_LORE_WRAP_WIDTH));
            lore.add(
                    FabricGraniteText.key(
                            "graniteskills.granite.menu.abilities_unlocks_at",
                            RomanNumber.toRoman(mana.getUnlock(), plugin),
                            plugin.getMessageProvider().getSkillDisplayName(skill, lo, true)));
        } else {
            lore.add(
                    FabricGraniteText.key(
                            "graniteskills.granite.menu.abilities_state_level",
                            RomanNumber.toRoman(lvl, plugin)));
            if (isNotMaxed(user, mana)) {
                String desc =
                        FabricAbilityMenuLore.manaAbilityDescriptionBodyPlain(
                                plugin,
                                mm,
                                user,
                                mana,
                                FabricAbilityMenuLore.ManaBodyMode.UPGRADING);
                lore.addAll(splitDesc(plugin, desc, 5, FabricMenuLoreFormatting.DEFAULT_MENU_LORE_WRAP_WIDTH));
            } else {
                String desc =
                        FabricAbilityMenuLore.manaAbilityDescriptionBodyPlain(
                                plugin,
                                mm,
                                user,
                                mana,
                                FabricAbilityMenuLore.ManaBodyMode.MAXED);
                lore.addAll(splitDesc(plugin, desc, 5, FabricMenuLoreFormatting.DEFAULT_MENU_LORE_WRAP_WIDTH));
                lore.add(FabricGraniteText.key("graniteskills.granite.menu.abilities_maxed"));
            }
        }
        stack.set(
                DataComponents.LORE, new ItemLore(lore.stream().toList()));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(stack);
        return stack;
    }

    private static ItemStack passiveAbilityIcon(Ability ability, boolean unlocked) {
        if (!unlocked) {
            return new ItemStack(Items.DYE.gray());
        }
        int i = Math.floorMod(ability.hashCode(), PassiveIconsUnlocked.ITEMS.length);
        return new ItemStack(PassiveIconsUnlocked.ITEMS[i]);
    }

    private static List<Component> splitDesc(FabricGraniteSkills plugin, String desc, int maxLines, int wrapWidth) {
        if (desc == null || desc.isEmpty()) {
            return List.of();
        }
        List<Component> out = new ArrayList<>();
        int safety = 0;
        for (String part : desc.split("\n")) {
            String t = part.trim();
            if (t.isEmpty()) {
                continue;
            }
            for (String chunk : FabricMenuLoreFormatting.wordWrapPlain(t, wrapWidth)) {
                if (out.size() >= maxLines) {
                    out.add(FabricGraniteText.key("graniteskills.granite.menu.abilities_lore_truncated"));
                    return out;
                }
                out.add(FabricMenuLoreFormatting.formatMenuLine(plugin, chunk));
                safety++;
                if (safety > 40) {
                    out.add(FabricGraniteText.key("graniteskills.granite.menu.abilities_lore_truncated"));
                    return out;
                }
            }
        }
        return out;
    }

    private static boolean isNotMaxed(User user, AbstractAbility ability) {
        int maxLevel = ability.getMaxLevel();
        int unlock = ability.getUnlock();
        int levelUp = ability.getLevelUp();
        int maxAllowedBySkill = (ability.getSkill().getMaxLevel() - unlock) / levelUp + 1;
        if (maxLevel == 0 || maxLevel > maxAllowedBySkill) {
            maxLevel = maxAllowedBySkill;
        }
        return user.getAbstractAbilityLevel(ability) < maxLevel;
    }

    private static void putControl(
            Container inv,
            int slot,
            boolean active,
            String nameKey,
            String loreKey,
            net.minecraft.world.item.Item item) {
        if (!active) {
            return;
        }
        ItemStack s = new ItemStack(item);
        s.set(DataComponents.ITEM_NAME, FabricGraniteText.key(nameKey));
        s.set(
                DataComponents.LORE,
                new ItemLore(List.of(FabricGraniteText.key(loreKey))));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(s);
        inv.setItem(slot, s);
    }
}
