package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.granite.graniteskills.api.item.ModifierType;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.skill.Multiplier;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.stat.StatModifier;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.TraitModifier;
import dev.granite.graniteskills.api.util.GraniteSkillsModifier;
import dev.granite.graniteskills.common.message.type.CommandMessage;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.item.FabricItemIgnoreTags;
import dev.granite.graniteskills.fabric.item.FabricItemModifierLore;
import dev.granite.graniteskills.fabric.item.FabricSkillsItemReader;
import dev.granite.graniteskills.fabric.item.FabricSkillsItemWriter;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static net.minecraft.commands.Commands.literal;

/**
 * **Sprint 51+** — Bukkit-shaped {@code /item} and {@code /armor} stat/trait <strong>modifier</strong> subtrees
 * (main-hand held item; {@code item.modifier} / {@code armor.modifier}) plus {@code /item ignore …}
 * (Sprint 52, {@code item.ignore}); {@code /item requirement|multiplier …} (Sprint 53, {@code
 * item.requirement} / {@code item.multiplier}); {@code /armor requirement|multiplier …} (Sprint 54,
 * {@code armor.requirement} / {@code armor.multiplier} — shared executors with {@link ModifierType}).
 * {@code /item register|unregister|give …} (Sprint 55, {@code item.register} / {@code item.give}).
 */
public final class FabricItemArmorModifierCommands {

    private static final String ARG_STAT = "stat";
    private static final String ARG_TRAIT = "trait";
    private static final String ARG_SKILL = "skill";
    private static final String ARG_LEVEL = "level";
    private static final String ARG_TARGET = "target";
    private static final String ARG_VALUE = "value";
    private static final String ARG_OP = "operation";
    private static final String ARG_LORE = "lore";
    private static final String LIT_ADD = "add";
    private static final String LIT_REMOVE = "remove";
    private static final String LIT_LIST = "list";
    private static final String LIT_REMOVEALL = "removeall";
    private static final String LIT_MODIFIER = "modifier";
    private static final String LIT_TRAIT = "trait";
    private static final String LIT_IGNORE = "ignore";
    private static final String LIT_REQUIREMENT = "requirement";
    private static final String LIT_MULTIPLIER = "multiplier";
    private static final String LIT_REGISTER = "register";
    private static final String LIT_UNREGISTER = "unregister";
    private static final String LIT_GIVE = "give";
    private static final String ARG_KEY = "key";
    private static final String ARG_GIVE_COUNT = "amount";
    private static final String ARG_GIVE_TARGET = "player";

    private static final SuggestionProvider<CommandSourceStack> OPERATIONS =
            (c, b) -> {
                b.suggest("ADD");
                b.suggest("ADD_PERCENT");
                b.suggest("MULTIPLY");
                return b.buildFuture();
            };

    /** Pairs {@link CommandMessage} keys for {@code /item|/armor requirement …} by {@link ModifierType}. */
    private record RequirementMsg(
            CommandMessage addAlready,
            CommandMessage addAdded,
            CommandMessage removeNot,
            CommandMessage removeOk,
            CommandMessage listHeader,
            CommandMessage listEntry,
            CommandMessage removeAll) {
        static RequirementMsg of(ModifierType type) {
            if (type == ModifierType.ITEM) {
                return new RequirementMsg(
                        CommandMessage.ITEM_REQUIREMENT_ADD_ALREADY_EXISTS,
                        CommandMessage.ITEM_REQUIREMENT_ADD_ADDED,
                        CommandMessage.ITEM_REQUIREMENT_REMOVE_DOES_NOT_EXIST,
                        CommandMessage.ITEM_REQUIREMENT_REMOVE_REMOVED,
                        CommandMessage.ITEM_REQUIREMENT_LIST_HEADER,
                        CommandMessage.ITEM_REQUIREMENT_LIST_ENTRY,
                        CommandMessage.ITEM_REQUIREMENT_REMOVEALL_REMOVED);
            }
            return new RequirementMsg(
                    CommandMessage.ARMOR_REQUIREMENT_ADD_ALREADY_EXISTS,
                    CommandMessage.ARMOR_REQUIREMENT_ADD_ADDED,
                    CommandMessage.ARMOR_REQUIREMENT_REMOVE_DOES_NOT_EXIST,
                    CommandMessage.ARMOR_REQUIREMENT_REMOVE_REMOVED,
                    CommandMessage.ARMOR_REQUIREMENT_LIST_HEADER,
                    CommandMessage.ARMOR_REQUIREMENT_LIST_ENTRY,
                    CommandMessage.ARMOR_REQUIREMENT_REMOVEALL_REMOVED);
        }
    }

    /** Pairs {@link CommandMessage} keys for {@code /item|/armor multiplier …} by {@link ModifierType}. */
    private record MultiplierMsg(
            CommandMessage addAlready,
            CommandMessage addAdded,
            CommandMessage removeNot,
            CommandMessage removeOk,
            CommandMessage listHeader,
            CommandMessage listEntry,
            CommandMessage removeAll) {
        static MultiplierMsg of(ModifierType type) {
            if (type == ModifierType.ITEM) {
                return new MultiplierMsg(
                        CommandMessage.ITEM_MULTIPLIER_ADD_ALREADY_EXISTS,
                        CommandMessage.ITEM_MULTIPLIER_ADD_ADDED,
                        CommandMessage.ITEM_MULTIPLIER_REMOVE_DOES_NOT_EXIST,
                        CommandMessage.ITEM_MULTIPLIER_REMOVE_REMOVED,
                        CommandMessage.ITEM_MULTIPLIER_LIST_HEADER,
                        CommandMessage.ITEM_MULTIPLIER_LIST_ENTRY,
                        CommandMessage.ITEM_MULTIPLIER_REMOVEALL_REMOVED);
            }
            return new MultiplierMsg(
                    CommandMessage.ARMOR_MULTIPLIER_ADD_ALREADY_EXISTS,
                    CommandMessage.ARMOR_MULTIPLIER_ADD_ADDED,
                    CommandMessage.ARMOR_MULTIPLIER_REMOVE_DOES_NOT_EXIST,
                    CommandMessage.ARMOR_MULTIPLIER_REMOVE_REMOVED,
                    CommandMessage.ARMOR_MULTIPLIER_LIST_HEADER,
                    CommandMessage.ARMOR_MULTIPLIER_LIST_ENTRY,
                    CommandMessage.ARMOR_MULTIPLIER_REMOVEALL_REMOVED);
        }
    }

    private FabricItemArmorModifierCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> itemCommandRoot() {
        return literal("item")
                .requires(FabricCommandRequirements.anyItemSubcommandPermission())
                .executes(FabricItemArmorModifierCommands::executeItemRootUsage)
                .then(
                        literal(LIT_MODIFIER)
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.ITEM_MODIFIER, false))
                                .then(literal(LIT_ADD).then(itemOrArmorStatAdd(ModifierType.ITEM)))
                                .then(literal(LIT_REMOVE).then(itemOrArmorStatRemove(ModifierType.ITEM)))
                                .then(
                                        literal(LIT_LIST)
                                                .executes(
                                                        ctx ->
                                                                listStat(
                                                                        ctx, ModifierType.ITEM)))
                                .then(
                                        literal(LIT_REMOVEALL)
                                                .executes(
                                                        ctx ->
                                                                removeAllStat(
                                                                        ctx,
                                                                        ModifierType.ITEM))))
                .then(
                        literal(LIT_TRAIT)
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.ITEM_MODIFIER, false))
                                .then(literal(LIT_ADD).then(itemOrArmorTraitAdd(ModifierType.ITEM)))
                                .then(literal(LIT_REMOVE).then(itemOrArmorTraitRemove(ModifierType.ITEM)))
                                .then(
                                        literal(LIT_LIST)
                                                .executes(
                                                        ctx ->
                                                                listTrait(
                                                                        ctx, ModifierType.ITEM)))
                                .then(
                                        literal(LIT_REMOVEALL)
                                                .executes(
                                                        ctx ->
                                                                removeAllTrait(
                                                                        ctx, ModifierType.ITEM))))
                .then(
                        literal(LIT_IGNORE)
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.ITEM_IGNORE, false))
                                .then(literal(LIT_ADD).executes(FabricItemArmorModifierCommands::runItemIgnoreAdd))
                                .then(
                                        literal(LIT_REMOVE)
                                                .executes(
                                                        FabricItemArmorModifierCommands
                                                                ::runItemIgnoreRemove)))
                .then(
                        literal(LIT_REQUIREMENT)
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.ITEM_REQUIREMENT, false))
                                .then(
                                        literal(LIT_ADD)
                                                .then(
                                                        Commands.argument(
                                                                        ARG_SKILL,
                                                                        StringArgumentType
                                                                                .word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .enabledSkillKeys())
                                                                .then(
                                                                        Commands
                                                                                .argument(
                                                                                        ARG_LEVEL,
                                                                                        IntegerArgumentType
                                                                                                .integer(
                                                                                                        1))
                                                                                .executes(
                                                                                        ctx ->
                                                                                                runItemRequirementAdd(
                                                                                                        ctx,
                                                                                                        true))
                                                                                .then(
                                                                                        Commands
                                                                                                .argument(
                                                                                                        ARG_LORE,
                                                                                                        BoolArgumentType
                                                                                                                .bool())
                                                                                                .executes(
                                                                                                        ctx ->
                                                                                                                runItemRequirementAdd(
                                                                                                                        ctx,
                                                                                                                        BoolArgumentType
                                                                                                                                .getBool(
                                                                                                                                        ctx,
                                                                                                                                        ARG_LORE)))))))
                                .then(
                                        literal(LIT_REMOVE)
                                                .then(
                                                        Commands.argument(
                                                                        ARG_SKILL,
                                                                        StringArgumentType
                                                                                .word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .enabledSkillKeys())
                                                                .executes(
                                                                        ctx ->
                                                                                runItemRequirementRemove(
                                                                                        ctx,
                                                                                        true))
                                                                .then(
                                                                        Commands
                                                                                .argument(
                                                                                        ARG_LORE,
                                                                                        BoolArgumentType
                                                                                                .bool())
                                                                                .executes(
                                                                                        ctx ->
                                                                                                runItemRequirementRemove(
                                                                                                        ctx,
                                                                                                        BoolArgumentType
                                                                                                                .getBool(
                                                                                                                        ctx,
                                                                                                                        ARG_LORE))))))
                                .then(
                                        literal(LIT_LIST)
                                                .executes(
                                                        FabricItemArmorModifierCommands
                                                                ::listItemRequirement))
                                .then(
                                        literal(LIT_REMOVEALL)
                                                .executes(
                                                        FabricItemArmorModifierCommands
                                                                ::removeAllItemRequirement)))
                .then(
                        literal(LIT_MULTIPLIER)
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.ITEM_MULTIPLIER, false))
                                .then(
                                        literal(LIT_ADD)
                                                .then(
                                                        Commands.argument(
                                                                        ARG_TARGET,
                                                                        StringArgumentType
                                                                                .word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .enabledSkillKeysOrGlobal())
                                                                .then(
                                                                        Commands
                                                                                .argument(
                                                                                        ARG_VALUE,
                                                                                        DoubleArgumentType
                                                                                                .doubleArg())
                                                                                .executes(
                                                                                        ctx ->
                                                                                                runItemMultiplierAdd(
                                                                                                        ctx,
                                                                                                        true))
                                                                                .then(
                                                                                        Commands
                                                                                                .argument(
                                                                                                        ARG_LORE,
                                                                                                        BoolArgumentType
                                                                                                                .bool())
                                                                                                .executes(
                                                                                                        ctx ->
                                                                                                                runItemMultiplierAdd(
                                                                                                                        ctx,
                                                                                                                        BoolArgumentType
                                                                                                                                .getBool(
                                                                                                                                        ctx,
                                                                                                                                        ARG_LORE)))))))
                                .then(
                                        literal(LIT_REMOVE)
                                                .then(
                                                        Commands.argument(
                                                                        ARG_TARGET,
                                                                        StringArgumentType
                                                                                .word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .enabledSkillKeysOrGlobal())
                                                                .executes(
                                                                        FabricItemArmorModifierCommands
                                                                                ::runItemMultiplierRemove)))
                                .then(
                                        literal(LIT_LIST)
                                                .executes(
                                                        FabricItemArmorModifierCommands
                                                                ::listItemMultiplier))
                                .then(
                                        literal(LIT_REMOVEALL)
                                                .executes(
                                                        FabricItemArmorModifierCommands
                                                                ::removeAllItemMultiplier)))
                .then(
                        literal(LIT_REGISTER)
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.ITEM_REGISTER, false))
                                .then(
                                        Commands.argument(ARG_KEY, StringArgumentType.word())
                                                .executes(FabricItemArmorModifierCommands::runItemRegister)))
                .then(
                        literal(LIT_UNREGISTER)
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.ITEM_REGISTER, false))
                                .then(
                                        Commands.argument(ARG_KEY, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .registeredItemKeys())
                                                .executes(
                                                        FabricItemArmorModifierCommands
                                                                ::runItemUnregister)))
                .then(
                        literal(LIT_GIVE)
                                .requires(
                                        FabricCommandRequirements.permissionOrOpLevel(
                                                FabricCommandPermissions.ITEM_GIVE, 2))
                                .then(
                                        Commands.argument(
                                                        ARG_GIVE_TARGET, StringArgumentType.word())
                                                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                                                .then(
                                                        Commands.argument(
                                                                        ARG_KEY, StringArgumentType
                                                                                .word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .registeredItemKeys())
                                                                .executes(
                                                                        ctx ->
                                                                                runItemGive(
                                                                                        ctx, -1))
                                                                .then(
                                                                        Commands
                                                                                .argument(
                                                                                        ARG_GIVE_COUNT,
                                                                                        IntegerArgumentType
                                                                                                .integer(
                                                                                                        1))
                                                                                .executes(
                                                                                        ctx ->
                                                                                                runItemGive(
                                                                                                        ctx,
                                                                                                        IntegerArgumentType
                                                                                                                .getInteger(
                                                                                                                        ctx,
                                                                                                                        ARG_GIVE_COUNT)))))));
    }

    private static int executeItemRootUsage(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource()
                .sendSuccess(
                        () -> FabricGraniteText.key("graniteskills.granite.command.item.usage_root"),
                        false);
        return 1;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> armorCommandRoot() {
        return literal("armor")
                .then(
                        literal(LIT_MODIFIER)
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.ARMOR_MODIFIER, false))
                                .then(
                                        literal(LIT_ADD)
                                                .then(itemOrArmorStatAdd(ModifierType.ARMOR)))
                                .then(
                                        literal(LIT_REMOVE)
                                                .then(
                                                        itemOrArmorStatRemove(
                                                                ModifierType.ARMOR)))
                                .then(
                                        literal(LIT_LIST)
                                                .executes(
                                                        ctx ->
                                                                listStat(
                                                                        ctx,
                                                                        ModifierType.ARMOR)))
                                .then(
                                        literal(LIT_REMOVEALL)
                                                .executes(
                                                        ctx ->
                                                                removeAllStat(
                                                                        ctx,
                                                                        ModifierType.ARMOR))))
                .then(
                        literal(LIT_TRAIT)
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.ARMOR_MODIFIER, false))
                                .then(
                                        literal(LIT_ADD)
                                                .then(itemOrArmorTraitAdd(ModifierType.ARMOR)))
                                .then(
                                        literal(LIT_REMOVE)
                                                .then(
                                                        itemOrArmorTraitRemove(
                                                                ModifierType.ARMOR)))
                                .then(
                                        literal(LIT_LIST)
                                                .executes(
                                                        ctx ->
                                                                listTrait(
                                                                        ctx,
                                                                        ModifierType.ARMOR)))
                                .then(
                                        literal(LIT_REMOVEALL)
                                                .executes(
                                                        ctx ->
                                                                removeAllTrait(
                                                                        ctx,
                                                                        ModifierType.ARMOR))))
                .then(
                        literal(LIT_REQUIREMENT)
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.ARMOR_REQUIREMENT, false))
                                .then(
                                        literal(LIT_ADD)
                                                .then(
                                                        Commands.argument(
                                                                        ARG_SKILL,
                                                                        StringArgumentType
                                                                                .word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .enabledSkillKeys())
                                                                .then(
                                                                        Commands
                                                                                .argument(
                                                                                        ARG_LEVEL,
                                                                                        IntegerArgumentType
                                                                                                .integer(
                                                                                                        1))
                                                                                .executes(
                                                                                        ctx ->
                                                                                                runArmorRequirementAdd(
                                                                                                        ctx,
                                                                                                        true))
                                                                                .then(
                                                                                        Commands
                                                                                                .argument(
                                                                                                        ARG_LORE,
                                                                                                        BoolArgumentType
                                                                                                                .bool())
                                                                                                .executes(
                                                                                                        ctx ->
                                                                                                                runArmorRequirementAdd(
                                                                                                                        ctx,
                                                                                                                        BoolArgumentType
                                                                                                                                .getBool(
                                                                                                                                        ctx,
                                                                                                                                        ARG_LORE)))))))
                                .then(
                                        literal(LIT_REMOVE)
                                                .then(
                                                        Commands.argument(
                                                                        ARG_SKILL,
                                                                        StringArgumentType
                                                                                .word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .enabledSkillKeys())
                                                                .executes(
                                                                        ctx ->
                                                                                runArmorRequirementRemove(
                                                                                        ctx,
                                                                                        true))
                                                                .then(
                                                                        Commands
                                                                                .argument(
                                                                                        ARG_LORE,
                                                                                        BoolArgumentType
                                                                                                .bool())
                                                                                .executes(
                                                                                        ctx ->
                                                                                                runArmorRequirementRemove(
                                                                                                        ctx,
                                                                                                        BoolArgumentType
                                                                                                                .getBool(
                                                                                                                        ctx,
                                                                                                                        ARG_LORE))))))
                                .then(
                                        literal(LIT_LIST)
                                                .executes(
                                                        FabricItemArmorModifierCommands
                                                                ::listArmorRequirement))
                                .then(
                                        literal(LIT_REMOVEALL)
                                                .executes(
                                                        FabricItemArmorModifierCommands
                                                                ::removeAllArmorRequirement)))
                .then(
                        literal(LIT_MULTIPLIER)
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.ARMOR_MULTIPLIER, false))
                                .then(
                                        literal(LIT_ADD)
                                                .then(
                                                        Commands.argument(
                                                                        ARG_TARGET,
                                                                        StringArgumentType
                                                                                .word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .enabledSkillKeysOrGlobal())
                                                                .then(
                                                                        Commands
                                                                                .argument(
                                                                                        ARG_VALUE,
                                                                                        DoubleArgumentType
                                                                                                .doubleArg())
                                                                                .executes(
                                                                                        ctx ->
                                                                                                runArmorMultiplierAdd(
                                                                                                        ctx,
                                                                                                        true))
                                                                                .then(
                                                                                        Commands
                                                                                                .argument(
                                                                                                        ARG_LORE,
                                                                                                        BoolArgumentType
                                                                                                                .bool())
                                                                                                .executes(
                                                                                                        ctx ->
                                                                                                                runArmorMultiplierAdd(
                                                                                                                        ctx,
                                                                                                                        BoolArgumentType
                                                                                                                                .getBool(
                                                                                                                                        ctx,
                                                                                                                                        ARG_LORE)))))))
                                .then(
                                        literal(LIT_REMOVE)
                                                .then(
                                                        Commands.argument(
                                                                        ARG_TARGET,
                                                                        StringArgumentType
                                                                                .word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .enabledSkillKeysOrGlobal())
                                                                .executes(
                                                                        FabricItemArmorModifierCommands
                                                                                ::runArmorMultiplierRemove)))
                                .then(
                                        literal(LIT_LIST)
                                                .executes(
                                                        FabricItemArmorModifierCommands
                                                                ::listArmorMultiplier))
                                .then(
                                        literal(LIT_REMOVEALL)
                                                .executes(
                                                        FabricItemArmorModifierCommands
                                                                ::removeAllArmorMultiplier)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> itemOrArmorStatAdd(ModifierType type) {
        return Commands.argument(ARG_STAT, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.enabledStatKeys())
                .then(
                        Commands.argument(ARG_VALUE, DoubleArgumentType.doubleArg())
                                .executes(
                                        ctx ->
                                                runStatAdd(
                                                        ctx,
                                                        type,
                                                        GraniteSkillsModifier.Operation.ADD,
                                                        true))
                                .then(
                                        Commands.argument(
                                                        ARG_OP, StringArgumentType.word())
                                                .suggests(OPERATIONS)
                                                .executes(
                                                        ctx ->
                                                                runStatAdd(
                                                                        ctx,
                                                                        type,
                                                                        GraniteSkillsModifier
                                                                                .Operation
                                                                                .parse(
                                                                                        StringArgumentType
                                                                                                .getString(
                                                                                                        ctx,
                                                                                                        ARG_OP)),
                                                                        true))
                                                .then(
                                                        Commands.argument(
                                                                        ARG_LORE,
                                                                        BoolArgumentType.bool())
                                                                .executes(
                                                                        ctx ->
                                                                                runStatAdd(
                                                                                        ctx,
                                                                                        type,
                                                                                        GraniteSkillsModifier
                                                                                                .Operation
                                                                                                .parse(
                                                                                                        StringArgumentType
                                                                                                                .getString(
                                                                                                                        ctx,
                                                                                                                        ARG_OP)),
                                                                                        BoolArgumentType
                                                                                                .getBool(
                                                                                                        ctx,
                                                                                                        ARG_LORE))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> itemOrArmorStatRemove(ModifierType type) {
        return Commands.argument(ARG_STAT, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.enabledStatKeys())
                .executes(ctx -> runStatRemove(ctx, type, true))
                .then(
                        Commands.argument(ARG_LORE, BoolArgumentType.bool())
                                .executes(
                                        ctx ->
                                                runStatRemove(
                                                        ctx,
                                                        type,
                                                        BoolArgumentType.getBool(
                                                                ctx, ARG_LORE))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> itemOrArmorTraitAdd(ModifierType type) {
        return Commands.argument(ARG_TRAIT, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.enabledTraitKeys())
                .then(
                        Commands.argument(ARG_VALUE, DoubleArgumentType.doubleArg())
                                .executes(
                                        ctx ->
                                                runTraitAdd(
                                                        ctx,
                                                        type,
                                                        GraniteSkillsModifier.Operation.ADD,
                                                        true))
                                .then(
                                        Commands.argument(
                                                        ARG_OP, StringArgumentType.word())
                                                .suggests(OPERATIONS)
                                                .executes(
                                                        ctx ->
                                                                runTraitAdd(
                                                                        ctx,
                                                                        type,
                                                                        GraniteSkillsModifier
                                                                                .Operation
                                                                                .parse(
                                                                                        StringArgumentType
                                                                                                .getString(
                                                                                                        ctx,
                                                                                                        ARG_OP)),
                                                                        true))
                                                .then(
                                                        Commands.argument(
                                                                        ARG_LORE,
                                                                        BoolArgumentType.bool())
                                                                .executes(
                                                                        ctx ->
                                                                                runTraitAdd(
                                                                                        ctx,
                                                                                        type,
                                                                                        GraniteSkillsModifier
                                                                                                .Operation
                                                                                                .parse(
                                                                                                        StringArgumentType
                                                                                                                .getString(
                                                                                                                        ctx,
                                                                                                                        ARG_OP)),
                                                                                        BoolArgumentType
                                                                                                .getBool(
                                                                                                        ctx,
                                                                                                        ARG_LORE))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> itemOrArmorTraitRemove(ModifierType type) {
        return Commands.argument(ARG_TRAIT, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.enabledTraitKeys())
                .executes(ctx -> runTraitRemove(ctx, type));
    }

    private static int notPlayer(CommandSourceStack s) {
        s.sendFailure(
                FabricGraniteText.key("graniteskills.granite.command.item_armor_mod.players_only"));
        return 0;
    }

    private static int emptyHand(CommandSourceStack s) {
        s.sendFailure(
                FabricGraniteText.key("graniteskills.granite.command.item_armor_mod.empty_hand"));
        return 0;
    }

    private static int notReady(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendFailure(Component.literal("GraniteSkills is still starting."));
        return 0;
    }

    private static int runStatAdd(
            CommandContext<CommandSourceStack> ctx,
            ModifierType type,
            GraniteSkillsModifier.Operation op,
            boolean lore) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String raw = StringArgumentType.getString(ctx, ARG_STAT);
                            Stat stat = FabricCommandStatParser.parse(p, raw);
                            if (stat == null) {
                                ctx.getSource()
                                        .sendFailure(
                                                Component.literal(
                                                        p.getMsg(
                                                                CommandMessage.UNKNOWN_STAT,
                                                                p.getDefaultLanguage())));
                                return 0;
                            }
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            double value = DoubleArgumentType.getDouble(ctx, ARG_VALUE);
                            boolean off = false;
                            ItemStack work = player.getMainHandItem().copy();
                            if (!FabricSkillsItemWriter.addStatModifier(
                                    p, work, type, off, stat, value, op)) {
                                sendStatExists(ctx, p, type, stat);
                                return 0;
                            }
                            if (lore) {
                                User u = p.getUserManager().getUser(player.getUUID());
                                Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                                FabricItemModifierLore.addStatLore(
                                        p, work, type, stat, value, op, lo);
                            }
                            player.setItemSlot(EquipmentSlot.MAINHAND, work);
                            refreshModifiers(p, player);
                            String line = formatStatAdded(p, type, stat, value, op, player);
                            ctx.getSource()
                                    .sendSuccess(() -> Component.literal(line), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static void sendStatExists(
            CommandContext<CommandSourceStack> ctx,
            FabricGraniteSkills p,
            ModifierType type,
            Stat stat) {
        User u = null;
        if (ctx.getSource().getEntity() instanceof ServerPlayer sp) {
            u = p.getUserManager().getUser(sp.getUUID());
        }
        Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
        String out =
                p.getMessageProvider()
                        .applyFormatting(
                                p.getMsg(
                                                type == ModifierType.ITEM
                                                        ? CommandMessage.ITEM_MODIFIER_ADD_ALREADY_EXISTS
                                                        : CommandMessage.ARMOR_MODIFIER_ADD_ALREADY_EXISTS,
                                                lo)
                                        .replace("{stat}", stat.getDisplayName(lo, false)));
        ctx.getSource().sendFailure(Component.literal(out));
    }

    private static String formatStatAdded(
            FabricGraniteSkills p,
            ModifierType type,
            Stat stat,
            double value,
            GraniteSkillsModifier.Operation op,
            ServerPlayer player) {
        User u = p.getUserManager().getUser(player.getUUID());
        Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
        StatModifier fake = new StatModifier("cmd", stat, value, op);
        CommandMessage key =
                type == ModifierType.ITEM
                        ? CommandMessage.ITEM_MODIFIER_ADD_ADDED
                        : CommandMessage.ARMOR_MODIFIER_ADD_ADDED;
        return FabricCommandModifierText.applyStatModifierItem(
                p, p.getMsg(key, lo), fake, lo);
    }

    private static int runStatRemove(
            CommandContext<CommandSourceStack> ctx, ModifierType type, boolean lore) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String raw = StringArgumentType.getString(ctx, ARG_STAT);
                            Stat stat = FabricCommandStatParser.parse(p, raw);
                            if (stat == null) {
                                ctx.getSource()
                                        .sendFailure(
                                                Component.literal(
                                                        p.getMsg(
                                                                CommandMessage.UNKNOWN_STAT,
                                                                p.getDefaultLanguage())));
                                return 0;
                            }
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            boolean off = false;
                            ItemStack work = player.getMainHandItem().copy();
                            if (!FabricSkillsItemWriter.removeStatModifier(
                                    p, work, type, off, stat)) {
                                String line = statRemoveLine(p, type, stat, false, player);
                                ctx.getSource()
                                        .sendFailure(
                                                Component.literal(
                                                        p.getMessageProvider()
                                                                .applyFormatting(line)));
                                return 0;
                            }
                            if (lore) {
                                User u = p.getUserManager().getUser(player.getUUID());
                                Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                                FabricItemModifierLore.removeStatLore(
                                        p, work, stat, lo);
                            }
                            player.setItemSlot(EquipmentSlot.MAINHAND, work);
                            refreshModifiers(p, player);
                            String ok = statRemoveLine(p, type, stat, true, player);
                            ctx.getSource()
                                    .sendSuccess(
                                            () -> Component.literal(
                                                    p.getMessageProvider()
                                                            .applyFormatting(ok)),
                                            false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static String statRemoveLine(
            FabricGraniteSkills p, ModifierType type, Stat stat, boolean removed, ServerPlayer player) {
        User u = p.getUserManager().getUser(player.getUUID());
        Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
        CommandMessage key =
                removed
                        ? (type == ModifierType.ITEM
                                ? CommandMessage.ITEM_MODIFIER_REMOVE_REMOVED
                                : CommandMessage.ARMOR_MODIFIER_REMOVE_REMOVED)
                        : (type == ModifierType.ITEM
                                ? CommandMessage.ITEM_MODIFIER_REMOVE_DOES_NOT_EXIST
                                : CommandMessage.ARMOR_MODIFIER_REMOVE_DOES_NOT_EXIST);
        return p.getMsg(key, lo).replace("{stat}", stat.getDisplayName(lo, false));
    }

    private static int listStat(CommandContext<CommandSourceStack> ctx, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            User u = p.getUserManager().getUser(player.getUUID());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            CommandMessage headerKey =
                                    type == ModifierType.ITEM
                                            ? CommandMessage.ITEM_MODIFIER_LIST_HEADER
                                            : CommandMessage.ARMOR_MODIFIER_LIST_HEADER;
                            var mods =
                                    FabricSkillsItemReader.getStatModifiers(
                                            p, player.getMainHandItem(), type, false);
                            StringBuilder sb = new StringBuilder(p.getMsg(headerKey, lo));
                            for (StatModifier mod : mods) {
                                String entry =
                                        type == ModifierType.ITEM
                                                ? p.getMsg(
                                                        CommandMessage.ITEM_MODIFIER_LIST_ENTRY, lo)
                                                : p.getMsg(
                                                        CommandMessage.ARMOR_MODIFIER_LIST_ENTRY,
                                                        lo);
                                sb.append("\n")
                                        .append(
                                                FabricCommandModifierText.applyStatModifierItem(
                                                        p, entry, mod, lo));
                            }
                            String out = sb.toString();
                            ctx.getSource()
                                    .sendSuccess(() -> Component.literal(out), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int removeAllStat(CommandContext<CommandSourceStack> ctx, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            ItemStack work = player.getMainHandItem().copy();
                            FabricSkillsItemWriter.removeAllStatModifiers(
                                    p, work, type, false);
                            player.setItemSlot(EquipmentSlot.MAINHAND, work);
                            refreshModifiers(p, player);
                            User u = p.getUserManager().getUser(player.getUUID());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            CommandMessage k =
                                    type == ModifierType.ITEM
                                            ? CommandMessage.ITEM_MODIFIER_REMOVEALL_REMOVED
                                            : CommandMessage.ARMOR_MODIFIER_REMOVEALL_REMOVED;
                            String out = p.getMsg(k, lo);
                            ctx.getSource()
                                    .sendSuccess(
                                            () -> Component.literal(
                                                    p.getMessageProvider().applyFormatting(out)),
                                            false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runTraitAdd(
            CommandContext<CommandSourceStack> ctx,
            ModifierType type,
            GraniteSkillsModifier.Operation op,
            boolean lore) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            String raw = StringArgumentType.getString(ctx, ARG_TRAIT);
                            Trait trait = FabricCommandTraitParser.parse(p, raw);
                            if (trait == null) {
                                ctx.getSource()
                                        .sendFailure(
                                                Component.literal(
                                                        p.getMsg(
                                                                CommandMessage.UNKNOWN_TRAIT,
                                                                p.getDefaultLanguage())));
                                return 0;
                            }
                            double value = DoubleArgumentType.getDouble(ctx, ARG_VALUE);
                            ItemStack work = player.getMainHandItem().copy();
                            if (!FabricSkillsItemWriter.addTraitModifier(
                                    p, work, type, false, trait, value, op)) {
                                sendTraitExists(p, ctx, type, trait);
                                return 0;
                            }
                            if (lore) {
                                User u = p.getUserManager().getUser(player.getUUID());
                                Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                                FabricItemModifierLore.addTraitLore(
                                        p, work, type, trait, value, op, lo);
                            }
                            player.setItemSlot(EquipmentSlot.MAINHAND, work);
                            refreshModifiers(p, player);
                            String line = formatTraitAdded(p, type, trait, value, op, player);
                            ctx.getSource()
                                    .sendSuccess(() -> Component.literal(line), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static void sendTraitExists(
            FabricGraniteSkills p, CommandContext<CommandSourceStack> ctx, ModifierType type, Trait t) {
        User u = null;
        if (ctx.getSource().getEntity() instanceof ServerPlayer sp) {
            u = p.getUserManager().getUser(sp.getUUID());
        }
        Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
        String out =
                p.getMessageProvider()
                        .applyFormatting(
                                p.getMsg(
                                                type == ModifierType.ITEM
                                                        ? CommandMessage.ITEM_TRAIT_ADD_ALREADY_EXISTS
                                                        : CommandMessage.ARMOR_TRAIT_ADD_ALREADY_EXISTS,
                                                lo)
                                        .replace("{stat}", t.getDisplayName(lo, false)));
        ctx.getSource().sendFailure(Component.literal(out));
    }

    private static String formatTraitAdded(
            FabricGraniteSkills p,
            ModifierType type,
            Trait trait,
            double value,
            GraniteSkillsModifier.Operation op,
            ServerPlayer player) {
        User u = p.getUserManager().getUser(player.getUUID());
        Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
        TraitModifier m = new TraitModifier("c", trait, value, op);
        m.setNonPersistent();
        CommandMessage k =
                type == ModifierType.ITEM
                        ? CommandMessage.ITEM_TRAIT_ADD_ADDED
                        : CommandMessage.ARMOR_TRAIT_ADD_ADDED;
        return FabricCommandModifierText.applyTraitModifierItem(
                p, p.getMsg(k, lo), m, lo);
    }

    private static int runTraitRemove(CommandContext<CommandSourceStack> ctx, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            String raw = StringArgumentType.getString(ctx, ARG_TRAIT);
                            Trait trait = FabricCommandTraitParser.parse(p, raw);
                            if (trait == null) {
                                ctx.getSource()
                                        .sendFailure(
                                                Component.literal(
                                                        p.getMsg(
                                                                CommandMessage.UNKNOWN_TRAIT,
                                                                p.getDefaultLanguage())));
                                return 0;
                            }
                            ItemStack work = player.getMainHandItem().copy();
                            boolean removed =
                                    FabricSkillsItemWriter.removeTraitModifier(
                                            p, work, type, false, trait);
                            player.setItemSlot(EquipmentSlot.MAINHAND, work);
                            refreshModifiers(p, player);
                            User u = p.getUserManager().getUser(player.getUUID());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            CommandMessage key =
                                    removed
                                            ? (type == ModifierType.ITEM
                                                    ? CommandMessage.ITEM_MODIFIER_REMOVE_REMOVED
                                                    : CommandMessage.ARMOR_MODIFIER_REMOVE_REMOVED)
                                            : (type == ModifierType.ITEM
                                                    ? CommandMessage.ITEM_MODIFIER_REMOVE_DOES_NOT_EXIST
                                                    : CommandMessage.ARMOR_MODIFIER_REMOVE_DOES_NOT_EXIST);
                            String out =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    p.getMsg(key, lo)
                                                            .replace(
                                                                    "{stat}",
                                                                    trait.getDisplayName(
                                                                            lo, false)));
                            if (removed) {
                                ctx.getSource()
                                        .sendSuccess(() -> Component.literal(out), false);
                            } else {
                                ctx.getSource().sendFailure(Component.literal(out));
                            }
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int listTrait(CommandContext<CommandSourceStack> ctx, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            User u = p.getUserManager().getUser(player.getUUID());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            CommandMessage headerKey =
                                    type == ModifierType.ITEM
                                            ? CommandMessage.ITEM_MODIFIER_LIST_HEADER
                                            : CommandMessage.ARMOR_MODIFIER_LIST_HEADER;
                            var mods =
                                    FabricSkillsItemReader.getTraitModifiers(
                                            p, player.getMainHandItem(), type, false);
                            StringBuilder sb = new StringBuilder(p.getMsg(headerKey, lo));
                            for (TraitModifier mod : mods) {
                                String entry =
                                        type == ModifierType.ITEM
                                                ? p.getMsg(
                                                        CommandMessage.ITEM_MODIFIER_LIST_ENTRY, lo)
                                                : p.getMsg(
                                                        CommandMessage.ARMOR_MODIFIER_LIST_ENTRY,
                                                        lo);
                                sb.append("\n")
                                        .append(
                                                FabricCommandModifierText
                                                        .applyTraitModifierItem(
                                                                p, entry, mod, lo));
                            }
                            ctx.getSource()
                                    .sendSuccess(
                                            () -> Component.literal(sb.toString()), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int removeAllTrait(CommandContext<CommandSourceStack> ctx, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            ItemStack work = player.getMainHandItem().copy();
                            FabricSkillsItemWriter.removeAllTraitModifiers(
                                    p, work, type, false);
                            player.setItemSlot(EquipmentSlot.MAINHAND, work);
                            refreshModifiers(p, player);
                            User u = p.getUserManager().getUser(player.getUUID());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            CommandMessage k =
                                    type == ModifierType.ITEM
                                            ? CommandMessage.ITEM_MODIFIER_REMOVEALL_REMOVED
                                            : CommandMessage.ARMOR_MODIFIER_REMOVEALL_REMOVED;
                            String out = p.getMsg(k, lo);
                            ctx.getSource()
                                    .sendSuccess(
                                            () -> Component.literal(
                                                    p.getMessageProvider().applyFormatting(out)),
                                            false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runItemIgnoreAdd(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        if (player.getMainHandItem().isEmpty()) {
            return emptyHand(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ItemStack work = player.getMainHandItem().copy();
                            FabricItemIgnoreTags.setIgnoreManaAbilityInteract(work, true);
                            player.setItemSlot(EquipmentSlot.MAINHAND, work);
                            User u = p.getUserManager().getUser(player.getUUID());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            String out =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    p.getMsg(
                                                            CommandMessage.ITEM_IGNORE_ADD_ADDED, lo));
                            ctx.getSource()
                                    .sendSuccess(() -> Component.literal(out), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runItemIgnoreRemove(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        if (player.getMainHandItem().isEmpty()) {
            return emptyHand(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ItemStack work = player.getMainHandItem().copy();
                            FabricItemIgnoreTags.setIgnoreManaAbilityInteract(work, false);
                            player.setItemSlot(EquipmentSlot.MAINHAND, work);
                            User u = p.getUserManager().getUser(player.getUUID());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            String out =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    p.getMsg(
                                                            CommandMessage
                                                                    .ITEM_IGNORE_REMOVE_REMOVED,
                                                            lo));
                            ctx.getSource()
                                    .sendSuccess(() -> Component.literal(out), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runItemRequirementAdd(CommandContext<CommandSourceStack> ctx, boolean lore) {
        return requirementAdd(ctx, lore, ModifierType.ITEM);
    }

    private static int runArmorRequirementAdd(CommandContext<CommandSourceStack> ctx, boolean lore) {
        return requirementAdd(ctx, lore, ModifierType.ARMOR);
    }

    private static int requirementAdd(
            CommandContext<CommandSourceStack> ctx, boolean lore, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        RequirementMsg msg = RequirementMsg.of(type);
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            String raw = StringArgumentType.getString(ctx, ARG_SKILL);
                            Skill skill = FabricCommandSkillParser.parse(p, raw);
                            if (skill == null) {
                                return unknownSkill(ctx, p);
                            }
                            int level = IntegerArgumentType.getInteger(ctx, ARG_LEVEL);
                            ItemStack work = player.getMainHandItem().copy();
                            Map<Skill, Integer> m =
                                    new HashMap<>(
                                            FabricSkillsItemReader.getRequirements(p, work, type));
                            if (m.containsKey(skill)) {
                                User u = p.getUserManager().getUser(player.getUUID());
                                Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                                String err =
                                        TextUtil.replace(
                                                p.getMsg(msg.addAlready, lo),
                                                "{skill}",
                                                skill.getDisplayName(lo, false));
                                ctx.getSource()
                                        .sendFailure(
                                                Component.literal(
                                                        p.getMessageProvider()
                                                                .applyFormatting(err)));
                                return 0;
                            }
                            m.put(skill, level);
                            FabricSkillsItemWriter.putRequirements(work, type, m);
                            if (lore) {
                                User u = p.getUserManager().getUser(player.getUUID());
                                Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                                FabricItemModifierLore.addRequirementLore(
                                        p, work, type, skill, level, lo);
                            }
                            player.setItemSlot(EquipmentSlot.MAINHAND, work);
                            User u = p.getUserManager().getUser(player.getUUID());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            String out =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    TextUtil.replace(
                                                            p.getMsg(msg.addAdded, lo),
                                                            "{skill}",
                                                            skill.getDisplayName(lo, false),
                                                            "{level}",
                                                            String.valueOf(level)));
                            ctx.getSource().sendSuccess(() -> Component.literal(out), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runItemRequirementRemove(CommandContext<CommandSourceStack> ctx, boolean lore) {
        return requirementRemove(ctx, lore, ModifierType.ITEM);
    }

    private static int runArmorRequirementRemove(CommandContext<CommandSourceStack> ctx, boolean lore) {
        return requirementRemove(ctx, lore, ModifierType.ARMOR);
    }

    private static int requirementRemove(
            CommandContext<CommandSourceStack> ctx, boolean lore, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        RequirementMsg msg = RequirementMsg.of(type);
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            String raw = StringArgumentType.getString(ctx, ARG_SKILL);
                            Skill skill = FabricCommandSkillParser.parse(p, raw);
                            if (skill == null) {
                                return unknownSkill(ctx, p);
                            }
                            ItemStack work = player.getMainHandItem().copy();
                            Map<Skill, Integer> m =
                                    new HashMap<>(
                                            FabricSkillsItemReader.getRequirements(p, work, type));
                            User u = p.getUserManager().getUser(player.getUUID());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            if (!m.containsKey(skill)) {
                                String err =
                                        TextUtil.replace(
                                                p.getMsg(msg.removeNot, lo),
                                                "{skill}",
                                                skill.getDisplayName(lo, false));
                                ctx.getSource()
                                        .sendFailure(
                                                Component.literal(
                                                        p.getMessageProvider()
                                                                .applyFormatting(err)));
                                return 0;
                            }
                            m.remove(skill);
                            FabricSkillsItemWriter.putRequirements(work, type, m);
                            if (lore) {
                                FabricItemModifierLore.removeRequirementLore(skill, work);
                            }
                            player.setItemSlot(EquipmentSlot.MAINHAND, work);
                            String ok =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    TextUtil.replace(
                                                            p.getMsg(msg.removeOk, lo),
                                                            "{skill}",
                                                            skill.getDisplayName(lo, false)));
                            ctx.getSource().sendSuccess(() -> Component.literal(ok), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int listItemRequirement(CommandContext<CommandSourceStack> ctx) {
        return listRequirement(ctx, ModifierType.ITEM);
    }

    private static int listArmorRequirement(CommandContext<CommandSourceStack> ctx) {
        return listRequirement(ctx, ModifierType.ARMOR);
    }

    private static int listRequirement(CommandContext<CommandSourceStack> ctx, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        RequirementMsg msg = RequirementMsg.of(type);
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            User u = p.getUserManager().getUser(player.getUUID());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            Map<Skill, Integer> map =
                                    FabricSkillsItemReader.getRequirements(
                                            p, player.getMainHandItem(), type);
                            StringBuilder sb = new StringBuilder(p.getMsg(msg.listHeader, lo));
                            for (Map.Entry<Skill, Integer> e : map.entrySet()) {
                                sb.append("\n")
                                        .append(
                                                TextUtil.replace(
                                                        p.getMsg(msg.listEntry, lo),
                                                        "{skill}",
                                                        e.getKey().getDisplayName(lo, false),
                                                        "{level}",
                                                        String.valueOf(e.getValue())));
                            }
                            String out = sb.toString();
                            ctx.getSource().sendSuccess(() -> Component.literal(out), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int removeAllItemRequirement(CommandContext<CommandSourceStack> ctx) {
        return removeAllRequirement(ctx, ModifierType.ITEM);
    }

    private static int removeAllArmorRequirement(CommandContext<CommandSourceStack> ctx) {
        return removeAllRequirement(ctx, ModifierType.ARMOR);
    }

    private static int removeAllRequirement(
            CommandContext<CommandSourceStack> ctx, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        RequirementMsg msg = RequirementMsg.of(type);
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            ItemStack work = player.getMainHandItem().copy();
                            FabricSkillsItemWriter.putRequirements(work, type, Map.of());
                            player.setItemSlot(EquipmentSlot.MAINHAND, work);
                            User u = p.getUserManager().getUser(player.getUUID());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            String out =
                                    p.getMessageProvider()
                                            .applyFormatting(p.getMsg(msg.removeAll, lo));
                            ctx.getSource().sendSuccess(() -> Component.literal(out), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runItemMultiplierAdd(CommandContext<CommandSourceStack> ctx, boolean lore) {
        return multiplierAdd(ctx, lore, ModifierType.ITEM);
    }

    private static int runArmorMultiplierAdd(CommandContext<CommandSourceStack> ctx, boolean lore) {
        return multiplierAdd(ctx, lore, ModifierType.ARMOR);
    }

    private static int multiplierAdd(
            CommandContext<CommandSourceStack> ctx, boolean lore, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        MultiplierMsg msg = MultiplierMsg.of(type);
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            String rawTarget = StringArgumentType.getString(ctx, ARG_TARGET);
                            double value = DoubleArgumentType.getDouble(ctx, ARG_VALUE);
                            boolean global = rawTarget.equalsIgnoreCase("global");
                            @Nullable Skill skill =
                                    global ? null : FabricCommandSkillParser.parse(p, rawTarget);
                            User u0 = p.getUserManager().getUser(player.getUUID());
                            Locale lo0 = u0 != null ? u0.getLocale() : p.getDefaultLanguage();
                            if (!global && skill == null) {
                                ctx.getSource()
                                        .sendFailure(
                                                Component.literal(
                                                        "Target must be valid skill name or global"));
                                return 0;
                            }
                            ItemStack work = player.getMainHandItem().copy();
                            List<Multiplier> list =
                                    new ArrayList<>(
                                            FabricSkillsItemReader.getMultipliers(
                                                    p, work, type, false));
                            for (Multiplier m : list) {
                                if (global && m.skill() == null) {
                                    String globalName = p.getMsg(CommandMessage.MULTIPLIER_GLOBAL, lo0);
                                    String err =
                                            TextUtil.replace(
                                                    p.getMsg(msg.addAlready, lo0),
                                                    "{target}",
                                                    globalName);
                                    ctx.getSource()
                                            .sendFailure(
                                                    Component.literal(
                                                            p.getMessageProvider()
                                                                    .applyFormatting(err)));
                                    return 0;
                                }
                                if (!global && m.skill() == skill) {
                                    String err =
                                            TextUtil.replace(
                                                    p.getMsg(msg.addAlready, lo0),
                                                    "{target}",
                                                    skill.getDisplayName(lo0, false));
                                    ctx.getSource()
                                            .sendFailure(
                                                    Component.literal(
                                                            p.getMessageProvider()
                                                                    .applyFormatting(err)));
                                    return 0;
                                }
                            }
                            list.add(new Multiplier("item_armor_cmd_mult", skill, value));
                            FabricSkillsItemWriter.putMultipliers(work, type, list);
                            if (lore) {
                                User u = p.getUserManager().getUser(player.getUUID());
                                Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                                FabricItemModifierLore.addMultiplierLore(
                                        p, work, type, skill, value, lo);
                            }
                            player.setItemSlot(EquipmentSlot.MAINHAND, work);
                            User u2 = p.getUserManager().getUser(player.getUUID());
                            Locale lo2 = u2 != null ? u2.getLocale() : p.getDefaultLanguage();
                            String targetLabel =
                                    global
                                            ? p.getMsg(CommandMessage.MULTIPLIER_GLOBAL, lo2)
                                            : skill.getDisplayName(lo2, false);
                            String out =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    TextUtil.replace(
                                                            p.getMsg(msg.addAdded, lo2),
                                                            "{target}",
                                                            targetLabel,
                                                            "{value}",
                                                            String.valueOf(value)));
                            ctx.getSource().sendSuccess(() -> Component.literal(out), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runItemMultiplierRemove(CommandContext<CommandSourceStack> ctx) {
        return multiplierRemove(ctx, ModifierType.ITEM);
    }

    private static int runArmorMultiplierRemove(CommandContext<CommandSourceStack> ctx) {
        return multiplierRemove(ctx, ModifierType.ARMOR);
    }

    private static int multiplierRemove(CommandContext<CommandSourceStack> ctx, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        MultiplierMsg msg = MultiplierMsg.of(type);
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            String rawTarget = StringArgumentType.getString(ctx, ARG_TARGET);
                            boolean global = rawTarget.equalsIgnoreCase("global");
                            @Nullable Skill skill =
                                    global ? null : FabricCommandSkillParser.parse(p, rawTarget);
                            User u0 = p.getUserManager().getUser(player.getUUID());
                            Locale lo0 = u0 != null ? u0.getLocale() : p.getDefaultLanguage();
                            if (!global && skill == null) {
                                ctx.getSource()
                                        .sendFailure(
                                                Component.literal(
                                                        "Target must be valid skill name or global"));
                                return 0;
                            }
                            ItemStack work = player.getMainHandItem().copy();
                            List<Multiplier> list =
                                    new ArrayList<>(
                                            FabricSkillsItemReader.getMultipliers(
                                                    p, work, type, false));
                            boolean removed = false;
                            for (int i = 0; i < list.size(); i++) {
                                Multiplier m = list.get(i);
                                if (global && m.skill() == null) {
                                    list.remove(i);
                                    removed = true;
                                    break;
                                }
                                if (!global && m.skill() == skill) {
                                    list.remove(i);
                                    removed = true;
                                    break;
                                }
                            }
                            String targetName =
                                    global
                                            ? p.getMsg(CommandMessage.MULTIPLIER_GLOBAL, lo0)
                                            : skill.getDisplayName(lo0, false);
                            if (!removed) {
                                String err =
                                        TextUtil.replace(
                                                p.getMsg(msg.removeNot, lo0), "{target}", targetName);
                                ctx.getSource()
                                        .sendFailure(
                                                Component.literal(
                                                        p.getMessageProvider()
                                                                .applyFormatting(err)));
                                return 0;
                            }
                            FabricSkillsItemWriter.putMultipliers(work, type, list);
                            player.setItemSlot(EquipmentSlot.MAINHAND, work);
                            String ok =
                                    TextUtil.replace(
                                            p.getMsg(msg.removeOk, lo0), "{target}", targetName);
                            ctx.getSource()
                                    .sendSuccess(
                                            () ->
                                                    Component.literal(
                                                            p.getMessageProvider()
                                                                    .applyFormatting(ok)),
                                            false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int listItemMultiplier(CommandContext<CommandSourceStack> ctx) {
        return listMultiplier(ctx, ModifierType.ITEM);
    }

    private static int listArmorMultiplier(CommandContext<CommandSourceStack> ctx) {
        return listMultiplier(ctx, ModifierType.ARMOR);
    }

    private static int listMultiplier(CommandContext<CommandSourceStack> ctx, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        MultiplierMsg msg = MultiplierMsg.of(type);
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            User u = p.getUserManager().getUser(player.getUUID());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            List<Multiplier> list =
                                    FabricSkillsItemReader.getMultipliers(
                                            p, player.getMainHandItem(), type, false);
                            StringBuilder sb = new StringBuilder(p.getMsg(msg.listHeader, lo));
                            for (Multiplier m : list) {
                                String targetName =
                                        m.skill() != null
                                                ? m.skill().getDisplayName(lo, false)
                                                : p.getMsg(CommandMessage.MULTIPLIER_GLOBAL, lo);
                                sb.append("\n")
                                        .append(
                                                TextUtil.replace(
                                                        p.getMsg(msg.listEntry, lo),
                                                        "{target}",
                                                        targetName,
                                                        "{value}",
                                                        String.valueOf(m.value())));
                            }
                            String out = sb.toString();
                            ctx.getSource().sendSuccess(() -> Component.literal(out), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int removeAllItemMultiplier(CommandContext<CommandSourceStack> ctx) {
        return removeAllMultiplier(ctx, ModifierType.ITEM);
    }

    private static int removeAllArmorMultiplier(CommandContext<CommandSourceStack> ctx) {
        return removeAllMultiplier(ctx, ModifierType.ARMOR);
    }

    private static int removeAllMultiplier(
            CommandContext<CommandSourceStack> ctx, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        MultiplierMsg msg = MultiplierMsg.of(type);
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            ItemStack work = player.getMainHandItem().copy();
                            FabricSkillsItemWriter.putMultipliers(work, type, List.of());
                            player.setItemSlot(EquipmentSlot.MAINHAND, work);
                            User u = p.getUserManager().getUser(player.getUUID());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            String out =
                                    p.getMessageProvider()
                                            .applyFormatting(p.getMsg(msg.removeAll, lo));
                            ctx.getSource().sendSuccess(() -> Component.literal(out), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int unknownSkill(CommandContext<CommandSourceStack> ctx, FabricGraniteSkills p) {
        User u = null;
        if (ctx.getSource().getEntity() instanceof ServerPlayer sp) {
            u = p.getUserManager().getUser(sp.getUUID());
        }
        Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
        ctx.getSource()
                .sendFailure(
                        Component.literal(
                                p.getMessageProvider()
                                        .applyFormatting(
                                                p.getMsg(CommandMessage.UNKNOWN_SKILL, lo))));
        return 0;
    }

    private static void refreshModifiers(FabricGraniteSkills p, ServerPlayer player) {
        User u = p.getUserManager().getUser(player.getUUID());
        if (u != null) {
            p.getModifierManager().applyModifiers(u, true);
        }
    }

    private static Locale localeForSource(FabricGraniteSkills p, CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer sp) {
            User u = p.getUserManager().getUser(sp.getUUID());
            if (u != null) {
                return u.getLocale();
            }
        }
        return p.getDefaultLanguage();
    }

    private static int runItemRegister(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String key = StringArgumentType.getString(ctx, ARG_KEY);
                            if (key.contains(" ")) {
                                User u = p.getUserManager().getUser(player.getUUID());
                                Locale lo =
                                        u != null ? u.getLocale() : p.getDefaultLanguage();
                                String out =
                                        p.getPrefix(lo)
                                                + p.getMessageProvider()
                                                        .applyFormatting(
                                                                p.getMsg(
                                                                        CommandMessage
                                                                                .ITEM_REGISTER_NO_SPACES,
                                                                        lo));
                                ctx.getSource().sendFailure(Component.literal(out));
                                return 0;
                            }
                            if (player.getMainHandItem().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            final NamespacedId id;
                            try {
                                id = NamespacedId.fromDefault(key);
                            } catch (IllegalArgumentException ex) {
                                ctx.getSource()
                                        .sendFailure(
                                                FabricGraniteText.key(
                                                        "graniteskills.granite.command.item"
                                                                + ".bad_key",
                                                        key));
                                return 0;
                            }
                            if (p.getFabricItemRegistry().getItem(id) != null) {
                                User u = p.getUserManager().getUser(player.getUUID());
                                Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                                String msg =
                                        TextUtil.replace(
                                                p.getMsg(
                                                        CommandMessage
                                                                .ITEM_REGISTER_ALREADY_REGISTERED,
                                                        lo),
                                                "{key}",
                                                key);
                                String full =
                                        p.getPrefix(lo)
                                                + p.getMessageProvider()
                                                        .applyFormatting(msg);
                                ctx.getSource()
                                        .sendSuccess(() -> Component.literal(full), true);
                                return 0;
                            }
                            p.getFabricItemRegistry()
                                    .register(id, player.getMainHandItem().copy());
                            p.getFabricItemRegistry().saveToDisk();
                            User u = p.getUserManager().getUser(player.getUUID());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            String msg =
                                    TextUtil.replace(
                                            p.getMsg(CommandMessage.ITEM_REGISTER_REGISTERED, lo),
                                            "{key}",
                                            key);
                            String full =
                                    p.getPrefix(lo)
                                            + p.getMessageProvider()
                                                    .applyFormatting(msg);
                            ctx.getSource()
                                    .sendSuccess(() -> Component.literal(full), true);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runItemUnregister(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String key = StringArgumentType.getString(ctx, ARG_KEY);
                            final NamespacedId id;
                            try {
                                id = NamespacedId.fromDefault(key);
                            } catch (IllegalArgumentException ex) {
                                ctx.getSource()
                                        .sendFailure(
                                                FabricGraniteText.key(
                                                        "graniteskills.granite.command.item"
                                                                + ".bad_key",
                                                        key));
                                return 0;
                            }
                            if (!p.getFabricItemRegistry().containsItem(id)) {
                                User u = p.getUserManager().getUser(player.getUUID());
                                Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                                String msg =
                                        TextUtil.replace(
                                                p.getMsg(
                                                        CommandMessage
                                                                .ITEM_UNREGISTER_NOT_REGISTERED,
                                                        lo),
                                                "{key}",
                                                key);
                                String full =
                                        p.getPrefix(lo)
                                                + p.getMessageProvider()
                                                        .applyFormatting(msg);
                                ctx.getSource()
                                        .sendSuccess(() -> Component.literal(full), true);
                                return 0;
                            }
                            p.getFabricItemRegistry().unregister(id);
                            p.getFabricItemRegistry().saveToDisk();
                            User u = p.getUserManager().getUser(player.getUUID());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            String msg =
                                    TextUtil.replace(
                                            p.getMsg(
                                                    CommandMessage.ITEM_UNREGISTER_UNREGISTERED, lo),
                                            "{key}",
                                            key);
                            String full =
                                    p.getPrefix(lo)
                                            + p.getMessageProvider()
                                                    .applyFormatting(msg);
                            ctx.getSource()
                                    .sendSuccess(() -> Component.literal(full), true);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runItemGive(CommandContext<CommandSourceStack> ctx, int amount) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String targetName = StringArgumentType.getString(ctx, ARG_GIVE_TARGET);
                            String key = StringArgumentType.getString(ctx, ARG_KEY);
                            ServerPlayer target =
                                    FabricCommandPlayerTarget.findOnline(
                                                    ctx.getSource().getServer(), targetName)
                                            .orElse(null);
                            if (target == null) {
                                ctx.getSource()
                                        .sendFailure(
                                                FabricGraniteText.key(
                                                        "graniteskills.granite.command.xp"
                                                                + ".player_not_found",
                                                        targetName));
                                return 0;
                            }
                            User targetUser = p.getUserManager().getUser(target.getUUID());
                            if (targetUser == null) {
                                ctx.getSource()
                                        .sendFailure(
                                                Component.literal(
                                                        p.getMessageProvider()
                                                                .applyFormatting(
                                                                        p.getMsg(
                                                                                CommandMessage
                                                                                        .NO_PROFILE,
                                                                                p.getDefaultLanguage()))));
                                return 0;
                            }
                            final NamespacedId id;
                            try {
                                id = NamespacedId.fromDefault(key);
                            } catch (IllegalArgumentException ex) {
                                ctx.getSource()
                                        .sendFailure(
                                                FabricGraniteText.key(
                                                        "graniteskills.granite.command.item"
                                                                + ".bad_key",
                                                        key));
                                return 0;
                            }
                            if (!p.getFabricItemRegistry().containsItem(id)) {
                                Locale lo = localeForSource(p, ctx.getSource());
                                String msg =
                                        TextUtil.replace(
                                                p.getMsg(
                                                        CommandMessage
                                                                .ITEM_UNREGISTER_NOT_REGISTERED,
                                                        lo),
                                                "{key}",
                                                key);
                                String full =
                                        p.getPrefix(lo)
                                                + p.getMessageProvider()
                                                        .applyFormatting(msg);
                                ctx.getSource().sendFailure(Component.literal(full));
                                return 0;
                            }
                            p.getScheduler()
                                    .executeSync(
                                            () -> {
                                                if (!p.getFabricItemRegistry()
                                                        .containsItem(id)) {
                                                    return;
                                                }
                                                int given =
                                                        p.getFabricItemRegistry()
                                                                .addRegisteredItemToInventory(
                                                                        targetUser,
                                                                        target,
                                                                        id,
                                                                        amount);
                                                if (given < 0) {
                                                    return;
                                                }
                                                Locale senderLo =
                                                        localeForSource(p, ctx.getSource());
                                                String senderMsg =
                                                        TextUtil.replace(
                                                                p.getMsg(
                                                                        CommandMessage
                                                                                .ITEM_GIVE_SENDER,
                                                                        senderLo),
                                                                "{amount}",
                                                                String.valueOf(given),
                                                                "{key}",
                                                                key,
                                                                "{player}",
                                                                target
                                                                        .getName()
                                                                        .getString());
                                                if (!senderMsg.isEmpty()) {
                                                    String out =
                                                            p.getMessageProvider()
                                                                    .applyFormatting(
                                                                            p.getPrefix(senderLo)
                                                                                    + senderMsg);
                                                    ctx.getSource()
                                                            .sendSuccess(
                                                                    () -> Component.literal(out), true);
                                                }
                                                if (ctx.getSource().getEntity() == null
                                                        || !ctx.getSource()
                                                                .getEntity()
                                                                .equals(target)) {
                                                    String recv =
                                                            TextUtil.replace(
                                                                    p.getMsg(
                                                                            CommandMessage
                                                                                    .ITEM_GIVE_RECEIVER,
                                                                            senderLo),
                                                                    "{amount}",
                                                                    String.valueOf(given),
                                                                    "{key}",
                                                                    key);
                                                    if (!recv.isEmpty()) {
                                                        String out =
                                                                p.getMessageProvider()
                                                                        .applyFormatting(
                                                                                p.getPrefix(
                                                                                                senderLo)
                                                                                        + recv);
                                                        targetUser.sendMessage(out);
                                                    }
                                                }
                                            });
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }
}
