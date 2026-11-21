package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.ability.AbilityManager;
import dev.granite.graniteskills.common.mana.ManaAbilityManager;
import dev.granite.graniteskills.common.message.MessageProvider;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.math.RomanNumber;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import dev.granitelib.fabric.template.GraniteTemplateActionNames;
import dev.granitelib.fabric.template.GraniteTemplateItems;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Sprint 102: template-backed {@code abilities} menu. On failure returns {@code false} so
 * {@link FabricAbilitiesMenuGui} uses the built-in path.
 */
public final class FabricAbilitiesTemplateSupport {

    public static final String TEMPLATE_RESOURCE_PATH = "assets/graniteskills/granite/templates/abilities.yml";

    private static final String MENU_LOG = "abilities template";

    private FabricAbilitiesTemplateSupport() {
    }

    public static boolean tryOpen(
            ServerPlayer menuPlayer,
            FabricGraniteSkills plugin,
            Skill skill,
            int returnToSkillsListPage,
            User user) {
        try {
            return tryOpenInner(menuPlayer, plugin, skill, returnToSkillsListPage, user);
        } catch (RuntimeException e) {
            plugin.logger().warn("abilities template render failed; using built-in menu", e);
            return false;
        }
    }

    private static boolean tryOpenInner(
            ServerPlayer menuPlayer,
            FabricGraniteSkills plugin,
            Skill skill,
            int returnToSkillsListPage,
            User user) {
        Optional<FabricGraniteMenuTemplateLoader.LoadedTemplate> loaded =
                plugin.menuTemplateRegistry().getLoadedTemplate("abilities");
        if (loaded.isEmpty()) {
            return false;
        }
        GraniteMenuTemplate template = loaded.get().template();

        if (!"abilities".equalsIgnoreCase(template.id())) {
            plugin.logger().warn(
                    "abilities template id must be 'abilities', got '" + template.id() + "'; using built-in menu");
            return false;
        }
        if (template.rows() != 6) {
            plugin.logger().warn(
                    "abilities template rows must be 6 for 6×9 menu, got " + template.rows() + "; using built-in menu");
            return false;
        }

        Optional<String> actionContract =
                FabricGraniteMenuTemplateActionContract.validateTemplateActions("abilities", template);
        if (actionContract.isPresent()) {
            plugin.logger().warn(MENU_LOG + ": " + actionContract.get() + "; using built-in menu");
            return false;
        }

        GraniteTemplateItem abilityDef = GraniteTemplateItems.findAbilityGridDefinition(template);
        if (abilityDef == null) {
            plugin.logger().warn(
                    "abilities template: no ability row (key 'ability' or action ability_row); using built-in menu");
            return false;
        }
        List<Integer> builtinSlots = FabricAbilitiesMenuGui.builtinAbilityContentSlots();
        if (abilityDef.slots().size() != builtinSlots.size()) {
            plugin.logger().warn(
                    "abilities template: ability item must have exactly "
                            + builtinSlots.size()
                            + " slots; got "
                            + abilityDef.slots().size()
                            + "; using built-in menu");
            return false;
        }
        if (!abilityDef.slots().equals(builtinSlots)) {
            plugin.logger().warn(
                    "abilities template: ability slots must match built-in content order; using built-in menu");
            return false;
        }

        boolean abilityKeyed = abilityDef == template.items().get("ability");
        boolean abilityRowAction = GraniteTemplateItems.actionEquals(abilityDef, GraniteTemplateActionNames.ABILITY_ROW);
        if (!abilityKeyed && !abilityRowAction) {
            plugin.logger().warn(
                    "abilities template: ability grid must use YAML key 'ability' or action ability_row; using built-in menu");
            return false;
        }

        GraniteTemplateItem manaDef = GraniteTemplateItems.findManaAbilityItemDefinition(template);
        if (manaDef == null) {
            plugin.logger().warn(
                    "abilities template: missing mana_ability item (key or action mana_ability_row); using built-in menu");
            return false;
        }
        if (manaDef.slots().size() != 1) {
            plugin.logger().warn("abilities template: mana_ability must have exactly one slot; using built-in menu");
            return false;
        }
        boolean manaKeyed = manaDef == template.items().get("mana_ability");
        boolean manaRowAction = GraniteTemplateItems.actionEquals(manaDef, GraniteTemplateActionNames.MANA_ABILITY_ROW);
        if (!manaKeyed && !manaRowAction) {
            plugin.logger().warn(
                    "abilities template: mana item must use YAML key 'mana_ability' or action mana_ability_row; using built-in menu");
            return false;
        }
        int manaSlot = manaDef.slots().getFirst();

        Optional<Integer> backSlotOpt =
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                        template, "back", List.of(GraniteTemplateActionNames.BACK));
        if (backSlotOpt.isEmpty() || backSlotOpt.get() != FabricAbilitiesMenuGui.SLOT_BACK) {
            plugin.logger().warn(
                    "abilities template: back must be at slot " + FabricAbilitiesMenuGui.SLOT_BACK + "; using built-in menu");
            return false;
        }
        GraniteTemplateItem backItem = template.items().get("back");
        if (backItem == null || !GraniteTemplateItems.actionEquals(backItem, GraniteTemplateActionNames.BACK)) {
            plugin.logger().warn("abilities template: require YAML key back with action back; using built-in menu");
            return false;
        }

        Locale lo = user.getLocale();
        MessageProvider msg = plugin.getMessageProvider();
        AbilityManager am = plugin.getAbilityManager();
        ManaAbilityManager mm = plugin.getManaAbilityManager();

        List<Ability> regular = new ArrayList<>();
        for (Ability a : skill.getAbilities()) {
            if (a.isEnabled()) {
                regular.add(a);
            }
        }
        ManaAbility mana = skill.getManaAbility();
        boolean showMana = mana != null && mana.isEnabled() && mm.isLoaded(mana);

        int maxPassive = showMana ? builtinSlots.size() - 1 : builtinSlots.size();
        if (regular.size() > maxPassive) {
            plugin.logger().warn(
                    "abilities template: too many passive abilities ("
                            + regular.size()
                            + ") for grid; using built-in menu");
            return false;
        }

        Set<Integer> passiveSlotsUsed = new HashSet<>();
        for (int i = 0; i < regular.size(); i++) {
            passiveSlotsUsed.add(abilityDef.slots().get(i));
        }
        if (showMana && passiveSlotsUsed.contains(manaSlot)) {
            plugin.logger().warn(
                    "abilities template: mana_ability slot overlaps a passive ability slot; using built-in menu");
            return false;
        }

        Map<String, String> globalPh = buildGlobalSkillPlaceholders(skill, lo, msg);

        Component title = FabricMenuTitles.abilitiesMenuTitle(msg, skill, lo);

        Optional<ItemStack> fillBase =
                FabricGraniteTemplateStacks.buildFillStack(template.fill(), globalPh, plugin, MENU_LOG);
        if (template.fill().isPresent() && fillBase.isEmpty()) {
            return false;
        }

        List<ItemStack> passiveStacks = new ArrayList<>(regular.size());
        for (Ability ability : regular) {
            Map<String, String> rowPh = new HashMap<>(globalPh);
            addPassivePlaceholders(rowPh, plugin, am, user, ability, lo, skill, msg);
            Optional<ItemStack> rowStack =
                    FabricGraniteTemplateStacks.buildTemplateItemStack(
                            abilityDef, rowPh, plugin, MENU_LOG, "items.ability.row");
            if (rowStack.isEmpty()) {
                return false;
            }
            ItemStack passiveBuilt = rowStack.get();
            appendWrappedGrayDescriptionLines(
                    plugin,
                    passiveBuilt,
                    passiveDescriptionPlain(plugin, am, user, ability, lo, skill));
            passiveStacks.add(passiveBuilt);
        }

        Optional<ItemStack> manaStack = Optional.empty();
        if (showMana) {
            Map<String, String> mPh = new HashMap<>(globalPh);
            addManaPlaceholders(mPh, plugin, mm, user, mana, lo, skill, msg);
            Optional<ItemStack> ms =
                    FabricGraniteTemplateStacks.buildTemplateItemStack(
                            manaDef, mPh, plugin, MENU_LOG, "items.mana_ability");
            if (ms.isEmpty()) {
                return false;
            }
            ItemStack manaBuilt = ms.get();
            appendWrappedGrayDescriptionLines(
                    plugin, manaBuilt, manaDescriptionPlain(plugin, mm, user, mana, lo, skill));
            manaStack = Optional.of(manaBuilt);
        }

        Optional<ItemStack> backStack =
                FabricGraniteTemplateStacks.buildTemplateItemStack(
                        backItem, globalPh, plugin, MENU_LOG, "items.back");
        if (backStack.isEmpty()) {
            return false;
        }

        final Skill fSkill = skill;
        final int fBackPage = returnToSkillsListPage;
        final GraniteTemplateItem fAbilityDef = abilityDef;
        final Optional<ItemStack> fMana = manaStack;
        final int fManaSlot = manaSlot;

        GraniteContainerMenus.openClickable9x6(
                menuPlayer,
                title,
                inv -> {
                    if (fillBase.isPresent()) {
                        ItemStack fill = fillBase.get();
                        for (int s = 0; s < FabricMenuSlotLayout.TOP_SLOTS; s++) {
                            inv.setItem(s, fill.copy());
                        }
                    } else {
                        for (int s = 0; s < FabricMenuSlotLayout.TOP_SLOTS; s++) {
                            inv.setItem(s, ItemStack.EMPTY);
                        }
                    }
                    for (int i = 0; i < passiveStacks.size(); i++) {
                        inv.setItem(fAbilityDef.slots().get(i), passiveStacks.get(i).copy());
                    }
                    fMana.ifPresent(st -> inv.setItem(fManaSlot, st.copy()));
                    inv.setItem(FabricAbilitiesMenuGui.SLOT_BACK, backStack.get().copy());
                },
                (topSlot, pl, b, t) -> {
                    if (!FabricMenuClicks.activates(t)) {
                        return;
                    }
                    if (topSlot == FabricAbilitiesMenuGui.SLOT_BACK) {
                        plugin.getScheduler()
                                .executeSync(
                                        () ->
                                                FabricGraniteSkillsMenus.openLevelProgression(
                                                        fSkill, pl, plugin, fBackPage));
                    }
                });
        return true;
    }

    private static Map<String, String> buildGlobalSkillPlaceholders(Skill skill, Locale lo, MessageProvider msg) {
        Map<String, String> m = new HashMap<>();
        m.put("skill", skill.getId().getSimpleName());
        m.put("skill_display", msg.getSkillDisplayName(skill, lo, true));
        return m;
    }

    private static void addPassivePlaceholders(
            Map<String, String> m,
            FabricGraniteSkills plugin,
            AbilityManager am,
            User user,
            Ability ability,
            Locale lo,
            Skill skill,
            MessageProvider msg) {
        m.put("ability", ability.getId().getSimpleName());
        m.put("ability_display", ability.getDisplayName(lo, false));
        m.put("ability_status", passiveStatus(plugin, user, ability));
        m.put("ability_unlock_level", passiveUnlockLine(plugin, am, user, ability, lo, skill, msg));
        m.put("ability_description", passiveDescriptionPlain(plugin, am, user, ability, lo, skill));
    }

    private static void addManaPlaceholders(
            Map<String, String> m,
            FabricGraniteSkills plugin,
            ManaAbilityManager mm,
            User user,
            ManaAbility mana,
            Locale lo,
            Skill skill,
            MessageProvider msg) {
        m.put("mana_ability", mana.getId().getSimpleName());
        m.put("mana_ability_display", mana.getDisplayName(lo, false));
        m.put("mana_ability_status", manaStatus(plugin, user, mana));
        m.put("mana_ability_description", manaDescriptionPlain(plugin, mm, user, mana, lo, skill));
    }

    private static String passiveStatus(FabricGraniteSkills plugin, User user, Ability ability) {
        int lvl = user.getAbilityLevel(ability);
        if (lvl < 1) {
            return FabricGraniteText.key("graniteskills.granite.menu.abilities_state_locked").getString();
        }
        if (!FabricAbilitiesMenuGui.isAbilityNotMaxed(user, ability)) {
            return FabricGraniteText.key("graniteskills.granite.menu.abilities_maxed").getString();
        }
        return FabricGraniteText.key("graniteskills.granite.menu.abilities_state_level", RomanNumber.toRoman(lvl, plugin))
                .getString();
    }

    private static String manaStatus(FabricGraniteSkills plugin, User user, ManaAbility mana) {
        int lvl = user.getManaAbilityLevel(mana);
        if (lvl < 1) {
            return FabricGraniteText.key("graniteskills.granite.menu.abilities_state_locked").getString();
        }
        if (!FabricAbilitiesMenuGui.isAbilityNotMaxed(user, mana)) {
            return FabricGraniteText.key("graniteskills.granite.menu.abilities_maxed").getString();
        }
        return FabricGraniteText.key(
                        "graniteskills.granite.menu.abilities_state_level", RomanNumber.toRoman(lvl, plugin))
                .getString();
    }

    private static String passiveUnlockLine(
            FabricGraniteSkills plugin,
            AbilityManager am,
            User user,
            Ability ability,
            Locale lo,
            Skill skill,
            MessageProvider msg) {
        int lvl = user.getAbilityLevel(ability);
        if (lvl < 1) {
            return FabricGraniteText.key(
                            "graniteskills.granite.menu.abilities_unlocks_at",
                            RomanNumber.toRoman(ability.getUnlock(), plugin),
                            msg.getSkillDisplayName(skill, lo, true))
                    .getString();
        }
        if (!FabricAbilitiesMenuGui.isAbilityNotMaxed(user, ability)) {
            return FabricGraniteText.key("graniteskills.granite.menu.abilities_maxed").getString();
        }
        return FabricGraniteText.key(
                        "graniteskills.granite.menu.abilities_state_level", RomanNumber.toRoman(lvl, plugin))
                .getString();
    }

    private static String passiveDescriptionPlain(
            FabricGraniteSkills plugin,
            AbilityManager am,
            User user,
            Ability ability,
            Locale lo,
            Skill skill) {
        int lvl = user.getAbilityLevel(ability);
        if (lvl < 1) {
            String desc =
                    FabricAbilityMenuLore.passiveAbilityDescriptionBodyPlain(
                            plugin, am, user, ability, FabricAbilityMenuLore.PassiveBodyMode.LOCKED);
            return descToPlain(desc, 6);
        }
        if (FabricAbilitiesMenuGui.isAbilityNotMaxed(user, ability)) {
            String desc =
                    FabricAbilityMenuLore.passiveAbilityDescriptionBodyPlain(
                            plugin, am, user, ability, FabricAbilityMenuLore.PassiveBodyMode.UPGRADING);
            return descToPlain(desc, 5);
        }
        String desc =
                FabricAbilityMenuLore.passiveAbilityDescriptionBodyPlain(
                        plugin, am, user, ability, FabricAbilityMenuLore.PassiveBodyMode.MAXED);
        String base = descToPlain(desc, 5);
        String maxed = FabricGraniteText.key("graniteskills.granite.menu.abilities_maxed").getString();
        return base.isEmpty() ? maxed : base + "\n" + maxed;
    }

    private static String manaDescriptionPlain(
            FabricGraniteSkills plugin,
            ManaAbilityManager mm,
            User user,
            ManaAbility mana,
            Locale lo,
            Skill skill) {
        int lvl = user.getManaAbilityLevel(mana);
        if (lvl < 1) {
            String desc =
                    FabricAbilityMenuLore.manaAbilityDescriptionBodyPlain(
                            plugin,
                            mm,
                            user,
                            mana,
                            FabricAbilityMenuLore.ManaBodyMode.LOCKED);
            String plain = descToPlain(desc, 5);
            String unlock =
                    FabricGraniteText.key(
                                    "graniteskills.granite.menu.abilities_unlocks_at",
                                    RomanNumber.toRoman(mana.getUnlock(), plugin),
                                    plugin.getMessageProvider().getSkillDisplayName(skill, lo, true))
                            .getString();
            return plain.isEmpty() ? unlock : plain + "\n" + unlock;
        }
        if (FabricAbilitiesMenuGui.isAbilityNotMaxed(user, mana)) {
            String desc =
                    FabricAbilityMenuLore.manaAbilityDescriptionBodyPlain(
                            plugin,
                            mm,
                            user,
                            mana,
                            FabricAbilityMenuLore.ManaBodyMode.UPGRADING);
            return descToPlain(desc, 5);
        }
        String desc =
                FabricAbilityMenuLore.manaAbilityDescriptionBodyPlain(
                        plugin,
                        mm,
                        user,
                        mana,
                        FabricAbilityMenuLore.ManaBodyMode.MAXED);
        String base = descToPlain(desc, 5);
        String maxed = FabricGraniteText.key("graniteskills.granite.menu.abilities_maxed").getString();
        return base.isEmpty() ? maxed : base + "\n" + maxed;
    }

    /**
     * Sprint 122: body descriptions are not in Granite YAML lore rows — appended here so
     * {@link FabricMenuLoreFormatting#appendWrappedFormattedLore} can wrap at 42 characters.
     */
    private static void appendWrappedGrayDescriptionLines(
            FabricGraniteSkills plugin, ItemStack stack, String plainDescription) {
        if (plainDescription == null || plainDescription.isBlank()) {
            return;
        }
        List<Component> lore = new ArrayList<>();
        var existing = stack.get(DataComponents.LORE);
        if (existing != null) {
            lore.addAll(existing.lines());
        }
        FabricMenuLoreFormatting.appendWrappedFormattedLore(
                plugin, lore, "§8" + plainDescription, 16, 8000);
        stack.set(DataComponents.LORE, new ItemLore(lore));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(stack);
    }

    private static String descToPlain(String desc, int maxLines) {
        if (desc == null || desc.isEmpty()) {
            return "";
        }
        String[] parts = desc.split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(parts.length, maxLines); i++) {
            String line = parts[i].trim();
            if (!line.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(line);
            }
        }
        if (parts.length > maxLines) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(FabricGraniteText.key("graniteskills.granite.menu.abilities_lore_truncated").getString());
        }
        return sb.toString();
    }
}
