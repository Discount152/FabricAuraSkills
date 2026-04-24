package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.TraitModifier;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.item.FabricItemIgnoreTags;
import dev.aurelium.auraskills.fabric.item.FabricItemModifierLore;
import dev.aurelium.auraskills.fabric.item.FabricSkillsItemReader;
import dev.aurelium.auraskills.fabric.item.FabricSkillsItemWriter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.Locale;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * **Sprint 51+** — Bukkit-shaped {@code /item} and {@code /armor} stat/trait <strong>modifier</strong> subtrees
 * (main-hand held item; {@code item.modifier} / {@code armor.modifier}) plus {@code /item ignore …}
 * (Sprint 52, {@code item.ignore}).
 */
public final class FabricItemArmorModifierCommands {

    private static final String ARG_STAT = "stat";
    private static final String ARG_TRAIT = "trait";
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

    private static final SuggestionProvider<ServerCommandSource> OPERATIONS =
            (c, b) -> {
                b.suggest("ADD");
                b.suggest("ADD_PERCENT");
                b.suggest("MULTIPLY");
                return b.buildFuture();
            };

    private FabricItemArmorModifierCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> itemCommandRoot() {
        return literal("item")
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
                                                                ::runItemIgnoreRemove)));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> armorCommandRoot() {
        return literal("armor")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.ARMOR_MODIFIER, false))
                .then(
                        literal(LIT_MODIFIER)
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
                                                                        ModifierType.ARMOR))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> itemOrArmorStatAdd(ModifierType type) {
        return CommandManager.argument(ARG_STAT, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.enabledStatKeys())
                .then(
                        CommandManager.argument(ARG_VALUE, DoubleArgumentType.doubleArg())
                                .executes(
                                        ctx ->
                                                runStatAdd(
                                                        ctx,
                                                        type,
                                                        AuraSkillsModifier.Operation.ADD,
                                                        true))
                                .then(
                                        CommandManager.argument(
                                                        ARG_OP, StringArgumentType.word())
                                                .suggests(OPERATIONS)
                                                .executes(
                                                        ctx ->
                                                                runStatAdd(
                                                                        ctx,
                                                                        type,
                                                                        AuraSkillsModifier
                                                                                .Operation
                                                                                .parse(
                                                                                        StringArgumentType
                                                                                                .getString(
                                                                                                        ctx,
                                                                                                        ARG_OP)),
                                                                        true))
                                                .then(
                                                        CommandManager.argument(
                                                                        ARG_LORE,
                                                                        BoolArgumentType.bool())
                                                                .executes(
                                                                        ctx ->
                                                                                runStatAdd(
                                                                                        ctx,
                                                                                        type,
                                                                                        AuraSkillsModifier
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

    private static ArgumentBuilder<ServerCommandSource, ?> itemOrArmorStatRemove(ModifierType type) {
        return CommandManager.argument(ARG_STAT, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.enabledStatKeys())
                .executes(ctx -> runStatRemove(ctx, type, true))
                .then(
                        CommandManager.argument(ARG_LORE, BoolArgumentType.bool())
                                .executes(
                                        ctx ->
                                                runStatRemove(
                                                        ctx,
                                                        type,
                                                        BoolArgumentType.getBool(
                                                                ctx, ARG_LORE))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> itemOrArmorTraitAdd(ModifierType type) {
        return CommandManager.argument(ARG_TRAIT, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.enabledTraitKeys())
                .then(
                        CommandManager.argument(ARG_VALUE, DoubleArgumentType.doubleArg())
                                .executes(
                                        ctx ->
                                                runTraitAdd(
                                                        ctx,
                                                        type,
                                                        AuraSkillsModifier.Operation.ADD,
                                                        true))
                                .then(
                                        CommandManager.argument(
                                                        ARG_OP, StringArgumentType.word())
                                                .suggests(OPERATIONS)
                                                .executes(
                                                        ctx ->
                                                                runTraitAdd(
                                                                        ctx,
                                                                        type,
                                                                        AuraSkillsModifier
                                                                                .Operation
                                                                                .parse(
                                                                                        StringArgumentType
                                                                                                .getString(
                                                                                                        ctx,
                                                                                                        ARG_OP)),
                                                                        true))
                                                .then(
                                                        CommandManager.argument(
                                                                        ARG_LORE,
                                                                        BoolArgumentType.bool())
                                                                .executes(
                                                                        ctx ->
                                                                                runTraitAdd(
                                                                                        ctx,
                                                                                        type,
                                                                                        AuraSkillsModifier
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

    private static ArgumentBuilder<ServerCommandSource, ?> itemOrArmorTraitRemove(ModifierType type) {
        return CommandManager.argument(ARG_TRAIT, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.enabledTraitKeys())
                .executes(ctx -> runTraitRemove(ctx, type));
    }

    private static int notPlayer(ServerCommandSource s) {
        s.sendError(
                Text.translatable("auraskills.granite.command.item_armor_mod.players_only"));
        return 0;
    }

    private static int emptyHand(ServerCommandSource s) {
        s.sendError(
                Text.translatable("auraskills.granite.command.item_armor_mod.empty_hand"));
        return 0;
    }

    private static int notReady(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
        return 0;
    }

    private static int runStatAdd(
            CommandContext<ServerCommandSource> ctx,
            ModifierType type,
            AuraSkillsModifier.Operation op,
            boolean lore) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String raw = StringArgumentType.getString(ctx, ARG_STAT);
                            Stat stat = FabricCommandStatParser.parse(p, raw);
                            if (stat == null) {
                                ctx.getSource()
                                        .sendError(
                                                Text.literal(
                                                        p.getMsg(
                                                                CommandMessage.UNKNOWN_STAT,
                                                                p.getDefaultLanguage())));
                                return 0;
                            }
                            if (player.getMainHandStack().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            double value = DoubleArgumentType.getDouble(ctx, ARG_VALUE);
                            boolean off = false;
                            ItemStack work = player.getMainHandStack().copy();
                            if (!FabricSkillsItemWriter.addStatModifier(
                                    p, work, type, off, stat, value, op)) {
                                sendStatExists(ctx, p, type, stat);
                                return 0;
                            }
                            if (lore) {
                                User u = p.getUserManager().getUser(player.getUuid());
                                Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                                FabricItemModifierLore.addStatLore(
                                        p, work, type, stat, value, op, lo);
                            }
                            player.equipStack(EquipmentSlot.MAINHAND, work);
                            refreshModifiers(p, player);
                            String line = formatStatAdded(p, type, stat, value, op, player);
                            ctx.getSource()
                                    .sendFeedback(() -> Text.literal(line), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static void sendStatExists(
            CommandContext<ServerCommandSource> ctx,
            FabricAuraSkills p,
            ModifierType type,
            Stat stat) {
        User u = null;
        if (ctx.getSource().getEntity() instanceof ServerPlayerEntity sp) {
            u = p.getUserManager().getUser(sp.getUuid());
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
        ctx.getSource().sendError(Text.literal(out));
    }

    private static String formatStatAdded(
            FabricAuraSkills p,
            ModifierType type,
            Stat stat,
            double value,
            AuraSkillsModifier.Operation op,
            ServerPlayerEntity player) {
        User u = p.getUserManager().getUser(player.getUuid());
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
            CommandContext<ServerCommandSource> ctx, ModifierType type, boolean lore) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String raw = StringArgumentType.getString(ctx, ARG_STAT);
                            Stat stat = FabricCommandStatParser.parse(p, raw);
                            if (stat == null) {
                                ctx.getSource()
                                        .sendError(
                                                Text.literal(
                                                        p.getMsg(
                                                                CommandMessage.UNKNOWN_STAT,
                                                                p.getDefaultLanguage())));
                                return 0;
                            }
                            if (player.getMainHandStack().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            boolean off = false;
                            ItemStack work = player.getMainHandStack().copy();
                            if (!FabricSkillsItemWriter.removeStatModifier(
                                    p, work, type, off, stat)) {
                                String line = statRemoveLine(p, type, stat, false, player);
                                ctx.getSource()
                                        .sendError(
                                                Text.literal(
                                                        p.getMessageProvider()
                                                                .applyFormatting(line)));
                                return 0;
                            }
                            if (lore) {
                                User u = p.getUserManager().getUser(player.getUuid());
                                Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                                FabricItemModifierLore.removeStatLore(
                                        p, work, stat, lo);
                            }
                            player.equipStack(EquipmentSlot.MAINHAND, work);
                            refreshModifiers(p, player);
                            String ok = statRemoveLine(p, type, stat, true, player);
                            ctx.getSource()
                                    .sendFeedback(
                                            () -> Text.literal(
                                                    p.getMessageProvider()
                                                            .applyFormatting(ok)),
                                            false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static String statRemoveLine(
            FabricAuraSkills p, ModifierType type, Stat stat, boolean removed, ServerPlayerEntity player) {
        User u = p.getUserManager().getUser(player.getUuid());
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

    private static int listStat(CommandContext<ServerCommandSource> ctx, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandStack().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            User u = p.getUserManager().getUser(player.getUuid());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            CommandMessage headerKey =
                                    type == ModifierType.ITEM
                                            ? CommandMessage.ITEM_MODIFIER_LIST_HEADER
                                            : CommandMessage.ARMOR_MODIFIER_LIST_HEADER;
                            var mods =
                                    FabricSkillsItemReader.getStatModifiers(
                                            p, player.getMainHandStack(), type, false);
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
                                    .sendFeedback(() -> Text.literal(out), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int removeAllStat(CommandContext<ServerCommandSource> ctx, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandStack().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            ItemStack work = player.getMainHandStack().copy();
                            FabricSkillsItemWriter.removeAllStatModifiers(
                                    p, work, type, false);
                            player.equipStack(EquipmentSlot.MAINHAND, work);
                            refreshModifiers(p, player);
                            User u = p.getUserManager().getUser(player.getUuid());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            CommandMessage k =
                                    type == ModifierType.ITEM
                                            ? CommandMessage.ITEM_MODIFIER_REMOVEALL_REMOVED
                                            : CommandMessage.ARMOR_MODIFIER_REMOVEALL_REMOVED;
                            String out = p.getMsg(k, lo);
                            ctx.getSource()
                                    .sendFeedback(
                                            () -> Text.literal(
                                                    p.getMessageProvider().applyFormatting(out)),
                                            false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runTraitAdd(
            CommandContext<ServerCommandSource> ctx,
            ModifierType type,
            AuraSkillsModifier.Operation op,
            boolean lore) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandStack().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            String raw = StringArgumentType.getString(ctx, ARG_TRAIT);
                            Trait trait = FabricCommandTraitParser.parse(p, raw);
                            if (trait == null) {
                                ctx.getSource()
                                        .sendError(
                                                Text.literal(
                                                        p.getMsg(
                                                                CommandMessage.UNKNOWN_TRAIT,
                                                                p.getDefaultLanguage())));
                                return 0;
                            }
                            double value = DoubleArgumentType.getDouble(ctx, ARG_VALUE);
                            ItemStack work = player.getMainHandStack().copy();
                            if (!FabricSkillsItemWriter.addTraitModifier(
                                    p, work, type, false, trait, value, op)) {
                                sendTraitExists(p, ctx, type, trait);
                                return 0;
                            }
                            if (lore) {
                                User u = p.getUserManager().getUser(player.getUuid());
                                Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                                FabricItemModifierLore.addTraitLore(
                                        p, work, type, trait, value, op, lo);
                            }
                            player.equipStack(EquipmentSlot.MAINHAND, work);
                            refreshModifiers(p, player);
                            String line = formatTraitAdded(p, type, trait, value, op, player);
                            ctx.getSource()
                                    .sendFeedback(() -> Text.literal(line), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static void sendTraitExists(
            FabricAuraSkills p, CommandContext<ServerCommandSource> ctx, ModifierType type, Trait t) {
        User u = null;
        if (ctx.getSource().getEntity() instanceof ServerPlayerEntity sp) {
            u = p.getUserManager().getUser(sp.getUuid());
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
        ctx.getSource().sendError(Text.literal(out));
    }

    private static String formatTraitAdded(
            FabricAuraSkills p,
            ModifierType type,
            Trait trait,
            double value,
            AuraSkillsModifier.Operation op,
            ServerPlayerEntity player) {
        User u = p.getUserManager().getUser(player.getUuid());
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

    private static int runTraitRemove(CommandContext<ServerCommandSource> ctx, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandStack().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            String raw = StringArgumentType.getString(ctx, ARG_TRAIT);
                            Trait trait = FabricCommandTraitParser.parse(p, raw);
                            if (trait == null) {
                                ctx.getSource()
                                        .sendError(
                                                Text.literal(
                                                        p.getMsg(
                                                                CommandMessage.UNKNOWN_TRAIT,
                                                                p.getDefaultLanguage())));
                                return 0;
                            }
                            ItemStack work = player.getMainHandStack().copy();
                            boolean removed =
                                    FabricSkillsItemWriter.removeTraitModifier(
                                            p, work, type, false, trait);
                            player.equipStack(EquipmentSlot.MAINHAND, work);
                            refreshModifiers(p, player);
                            User u = p.getUserManager().getUser(player.getUuid());
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
                                        .sendFeedback(() -> Text.literal(out), false);
                            } else {
                                ctx.getSource().sendError(Text.literal(out));
                            }
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int listTrait(CommandContext<ServerCommandSource> ctx, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandStack().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            User u = p.getUserManager().getUser(player.getUuid());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            CommandMessage headerKey =
                                    type == ModifierType.ITEM
                                            ? CommandMessage.ITEM_MODIFIER_LIST_HEADER
                                            : CommandMessage.ARMOR_MODIFIER_LIST_HEADER;
                            var mods =
                                    FabricSkillsItemReader.getTraitModifiers(
                                            p, player.getMainHandStack(), type, false);
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
                                    .sendFeedback(
                                            () -> Text.literal(sb.toString()), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int removeAllTrait(CommandContext<ServerCommandSource> ctx, ModifierType type) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            return notPlayer(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (player.getMainHandStack().isEmpty()) {
                                return emptyHand(ctx.getSource());
                            }
                            ItemStack work = player.getMainHandStack().copy();
                            FabricSkillsItemWriter.removeAllTraitModifiers(
                                    p, work, type, false);
                            player.equipStack(EquipmentSlot.MAINHAND, work);
                            refreshModifiers(p, player);
                            User u = p.getUserManager().getUser(player.getUuid());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            CommandMessage k =
                                    type == ModifierType.ITEM
                                            ? CommandMessage.ITEM_MODIFIER_REMOVEALL_REMOVED
                                            : CommandMessage.ARMOR_MODIFIER_REMOVEALL_REMOVED;
                            String out = p.getMsg(k, lo);
                            ctx.getSource()
                                    .sendFeedback(
                                            () -> Text.literal(
                                                    p.getMessageProvider().applyFormatting(out)),
                                            false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runItemIgnoreAdd(CommandContext<ServerCommandSource> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            return notPlayer(ctx.getSource());
        }
        if (player.getMainHandStack().isEmpty()) {
            return emptyHand(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ItemStack work = player.getMainHandStack().copy();
                            FabricItemIgnoreTags.setIgnoreManaAbilityInteract(work, true);
                            player.equipStack(EquipmentSlot.MAINHAND, work);
                            User u = p.getUserManager().getUser(player.getUuid());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            String out =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    p.getMsg(
                                                            CommandMessage.ITEM_IGNORE_ADD_ADDED, lo));
                            ctx.getSource()
                                    .sendFeedback(() -> Text.literal(out), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runItemIgnoreRemove(CommandContext<ServerCommandSource> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            return notPlayer(ctx.getSource());
        }
        if (player.getMainHandStack().isEmpty()) {
            return emptyHand(ctx.getSource());
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ItemStack work = player.getMainHandStack().copy();
                            FabricItemIgnoreTags.setIgnoreManaAbilityInteract(work, false);
                            player.equipStack(EquipmentSlot.MAINHAND, work);
                            User u = p.getUserManager().getUser(player.getUuid());
                            Locale lo = u != null ? u.getLocale() : p.getDefaultLanguage();
                            String out =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    p.getMsg(
                                                            CommandMessage
                                                                    .ITEM_IGNORE_REMOVE_REMOVED,
                                                            lo));
                            ctx.getSource()
                                    .sendFeedback(() -> Text.literal(out), false);
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static void refreshModifiers(FabricAuraSkills p, ServerPlayerEntity player) {
        User u = p.getUserManager().getUser(player.getUuid());
        if (u != null) {
            p.getModifierManager().applyModifiers(u, true);
        }
    }
}
