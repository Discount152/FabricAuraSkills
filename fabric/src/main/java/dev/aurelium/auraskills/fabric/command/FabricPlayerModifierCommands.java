package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.TraitModifier;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * **Sprint 50** — Bukkit-shaped **player** {@code /modifier} and {@code /trait} (no item/armor
 * subtrees, no addtemp in this batch).
 */
public final class FabricPlayerModifierCommands {

    private static final String ARG_PLAYER = "player";
    private static final String ARG_STAT = "stat";
    private static final String ARG_TRAIT = "trait";
    private static final String ARG_NAME = "name";
    private static final String ARG_VALUE = "value";
    private static final String ARG_OP = "operation";
    private static final String ARG_STACK = "stack";
    private static final String LIT_ADD = "add";
    private static final String LIT_REMOVE = "remove";
    private static final String LIT_LIST = "list";
    private static final String LIT_REMOVEALL = "removeall";

    private static final SuggestionProvider<ServerCommandSource> OPERATIONS =
            (context, builder) -> {
                builder.suggest("ADD");
                builder.suggest("ADD_PERCENT");
                builder.suggest("MULTIPLY");
                return builder.buildFuture();
            };

    private FabricPlayerModifierCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> modifierCommandRoot() {
        return literal("modifier")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.MODIFIER, false))
                .then(literal(LIT_ADD).then(modifierAddArguments()))
                .then(literal(LIT_REMOVE).then(modifierRemoveArguments()))
                .then(
                        literal(LIT_LIST)
                                .executes(FabricPlayerModifierCommands::runStatListSelfOrError)
                                .then(modifierListWithOptionalPlayerAndStat()))
                .then(
                        literal(LIT_REMOVEALL)
                                .executes(
                                        FabricPlayerModifierCommands
                                                ::runStatRemoveAllSelfOrError)
                                .then(modifierRemoveAllWithOptionalPlayerAndStat()));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> traitCommandRoot() {
        return literal("trait")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.MODIFIER, false))
                .then(literal(LIT_ADD).then(traitAddArguments()))
                .then(literal(LIT_REMOVE).then(traitRemoveArguments()))
                .then(
                        literal(LIT_LIST)
                                .executes(FabricPlayerModifierCommands::runTraitListSelfOrError)
                                .then(traitListWithOptionalPlayerAndTrait()))
                .then(
                        literal(LIT_REMOVEALL)
                                .executes(
                                        FabricPlayerModifierCommands
                                                ::runTraitRemoveAllSelfOrError)
                                .then(traitRemoveAllWithOptionalPlayerAndTrait()));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> modifierListWithOptionalPlayerAndStat() {
        return CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                .executes(
                        ctx ->
                                runListStat(
                                        ctx,
                                        StringArgumentType.getString(ctx, ARG_PLAYER),
                                        null))
                .then(
                        CommandManager.argument(ARG_STAT, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledStatKeys())
                                .executes(
                                        ctx ->
                                                runListStat(
                                                        ctx,
                                                        StringArgumentType.getString(
                                                                ctx, ARG_PLAYER),
                                                        StringArgumentType.getString(
                                                                ctx, ARG_STAT))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?>
            modifierRemoveAllWithOptionalPlayerAndStat() {
        return CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                .executes(
                        ctx ->
                                runStatRemoveAll(
                                        ctx,
                                        StringArgumentType.getString(ctx, ARG_PLAYER),
                                        null))
                .then(
                        CommandManager.argument(ARG_STAT, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledStatKeys())
                                .executes(
                                        ctx ->
                                                runStatRemoveAll(
                                                        ctx,
                                                        StringArgumentType.getString(
                                                                ctx, ARG_PLAYER),
                                                        StringArgumentType.getString(
                                                                ctx, ARG_STAT))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> traitListWithOptionalPlayerAndTrait() {
        return CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                .executes(
                        ctx ->
                                runListTrait(
                                        ctx,
                                        StringArgumentType.getString(ctx, ARG_PLAYER),
                                        null))
                .then(
                        CommandManager.argument(ARG_TRAIT, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledTraitKeys())
                                .executes(
                                        ctx ->
                                                runListTrait(
                                                        ctx,
                                                        StringArgumentType.getString(
                                                                ctx, ARG_PLAYER),
                                                        StringArgumentType.getString(
                                                                ctx, ARG_TRAIT))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?>
            traitRemoveAllWithOptionalPlayerAndTrait() {
        return CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                .executes(
                        ctx ->
                                runTraitRemoveAll(
                                        ctx,
                                        StringArgumentType.getString(ctx, ARG_PLAYER),
                                        null))
                .then(
                        CommandManager.argument(ARG_TRAIT, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledTraitKeys())
                                .executes(
                                        ctx ->
                                                runTraitRemoveAll(
                                                        ctx,
                                                        StringArgumentType.getString(
                                                                ctx, ARG_PLAYER),
                                                        StringArgumentType.getString(
                                                                ctx, ARG_TRAIT))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> modifierAddArguments() {
        return CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                .then(
                        CommandManager.argument(ARG_STAT, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledStatKeys())
                                .then(
                                        CommandManager.argument(ARG_NAME, StringArgumentType.word())
                                                .then(
                                                        withStatModifierValueTail(
                                                                DoubleArgumentType.doubleArg()))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> withStatModifierValueTail(
            com.mojang.brigadier.arguments.ArgumentType<Double> valueType) {
        return CommandManager.argument(ARG_VALUE, valueType)
                .executes(
                        ctx -> runStatAdd(ctx, AuraSkillsModifier.Operation.ADD, false))
                .then(
                        CommandManager.argument(ARG_OP, StringArgumentType.word())
                                .suggests(OPERATIONS)
                                .executes(
                                        ctx ->
                                                runStatAdd(
                                                        ctx,
                                                        AuraSkillsModifier.Operation.parse(
                                                                StringArgumentType.getString(
                                                                        ctx, ARG_OP)),
                                                        false))
                                .then(
                                        CommandManager.argument(ARG_STACK, BoolArgumentType.bool())
                                                .executes(
                                                        ctx ->
                                                                runStatAdd(
                                                                        ctx,
                                                                        AuraSkillsModifier
                                                                                .Operation
                                                                                .parse(
                                                                                        StringArgumentType
                                                                                                .getString(
                                                                                                        ctx,
                                                                                                        ARG_OP)),
                                                                        BoolArgumentType.getBool(
                                                                                ctx, ARG_STACK)))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> traitAddArguments() {
        return CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                .then(
                        CommandManager.argument(ARG_TRAIT, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledTraitKeys())
                                .then(
                                        CommandManager.argument(ARG_NAME, StringArgumentType.word())
                                                .then(
                                                        withTraitModifierValueTail(
                                                                DoubleArgumentType.doubleArg()))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> withTraitModifierValueTail(
            com.mojang.brigadier.arguments.ArgumentType<Double> valueType) {
        return CommandManager.argument(ARG_VALUE, valueType)
                .executes(
                        ctx -> runTraitAdd(ctx, AuraSkillsModifier.Operation.ADD, false))
                .then(
                        CommandManager.argument(ARG_OP, StringArgumentType.word())
                                .suggests(OPERATIONS)
                                .executes(
                                        ctx ->
                                                runTraitAdd(
                                                        ctx,
                                                        AuraSkillsModifier.Operation.parse(
                                                                StringArgumentType.getString(
                                                                        ctx, ARG_OP)),
                                                        false))
                                .then(
                                        CommandManager.argument(ARG_STACK, BoolArgumentType.bool())
                                                .executes(
                                                        ctx ->
                                                                runTraitAdd(
                                                                        ctx,
                                                                        AuraSkillsModifier
                                                                                .Operation
                                                                                .parse(
                                                                                        StringArgumentType
                                                                                                .getString(
                                                                                                        ctx,
                                                                                                        ARG_OP)),
                                                                        BoolArgumentType.getBool(
                                                                                ctx, ARG_STACK)))));
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<ServerCommandSource, ?>
            modifierRemoveArguments() {
        return CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                .then(
                        CommandManager.argument(ARG_NAME, StringArgumentType.word())
                                .executes(FabricPlayerModifierCommands::runStatRemove));
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<ServerCommandSource, ?>
            traitRemoveArguments() {
        return CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                .then(
                        CommandManager.argument(ARG_NAME, StringArgumentType.word())
                                .executes(FabricPlayerModifierCommands::runTraitRemove));
    }

    private static int runStatListSelfOrError(CommandContext<ServerCommandSource> ctx) {
        return runListStat(ctx, null, null);
    }

    private static Locale localeForSource(ServerCommandSource source, FabricAuraSkills p) {
        if (source.getEntity() instanceof ServerPlayerEntity sp) {
            User u = p.getUserManager().getUser(sp.getUuid());
            if (u != null) {
                return u.getLocale();
            }
        }
        return p.getDefaultLanguage();
    }

    private static int runListStat(
            CommandContext<ServerCommandSource> ctx, String nameOrNull, String rawStatOrNull) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            Locale lo = localeForSource(source, p);
                            if (nameOrNull == null) {
                                if (!(source.getEntity() instanceof ServerPlayerEntity self)) {
                                    source.sendError(
                                            Text.literal(
                                                    p.getMsg(
                                                            CommandMessage
                                                                    .MODIFIER_LIST_PLAYERS_ONLY,
                                                            lo)));
                                    return 0;
                                }
                                return sendStatList(
                                        p, source, self, self, parseStatOrNull(p, rawStatOrNull), lo);
                            }
                            ServerPlayerEntity target =
                                    FabricCommandPlayerTarget.findOnline(
                                                    source.getServer(), nameOrNull)
                                            .orElse(null);
                            if (target == null) {
                                source.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.xp.player_not_found",
                                                nameOrNull));
                                return 0;
                            }
                            return sendStatList(
                                    p, source, target, target, parseStatOrNull(p, rawStatOrNull), lo);
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static Stat parseStatOrNull(FabricAuraSkills p, String raw) {
        if (raw == null) {
            return null;
        }
        return FabricCommandStatParser.parse(p, raw);
    }

    private static int sendStatList(
            FabricAuraSkills p,
            ServerCommandSource source,
            ServerPlayerEntity targetEntity,
            ServerPlayerEntity nameSource,
            Stat statOrNull,
            Locale senderLocale) {
        User user = p.getUserManager().getUser(targetEntity.getUuid());
        if (user == null) {
            source.sendError(
                    Text.literal(
                            p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
            return 0;
        }
        Locale lineLocale = user.getLocale();
        if (statOrNull == null) {
            String header =
                    FabricCommandModifierText.playerOnly(
                            p.getMsg(CommandMessage.MODIFIER_LIST_ALL_STATS_HEADER, senderLocale),
                            nameSource);
            source.sendFeedback(
                    () -> Text.literal(p.getMessageProvider().applyFormatting(header)), false);
            for (String key : user.getStatModifiers().keySet()) {
                StatModifier mod = user.getStatModifiers().get(key);
                String line =
                        FabricCommandModifierText.applyStatModifier(
                                p,
                                p.getMsg(CommandMessage.MODIFIER_LIST_ALL_STATS_ENTRY, lineLocale),
                                mod,
                                nameSource,
                                lineLocale);
                String out = line;
                source.sendFeedback(() -> Text.literal(out), false);
            }
        } else {
            String h =
                    FabricCommandModifierText.applyStatHeader(
                            p,
                            p.getMsg(CommandMessage.MODIFIER_LIST_ONE_STAT_HEADER, senderLocale),
                            statOrNull,
                            nameSource,
                            lineLocale);
            source.sendFeedback(() -> Text.literal(p.getMessageProvider().applyFormatting(h)), false);
            for (String key : user.getStatModifiers().keySet()) {
                StatModifier mod = user.getStatModifiers().get(key);
                if (mod.stat() == statOrNull) {
                    String line =
                            FabricCommandModifierText.applyStatModifier(
                                    p,
                                    p.getMsg(
                                            CommandMessage.MODIFIER_LIST_ONE_STAT_ENTRY, lineLocale),
                                    mod,
                                    nameSource,
                                    lineLocale);
                    String out = line;
                    source.sendFeedback(() -> Text.literal(out), false);
                }
            }
        }
        return 1;
    }

    private static int runTraitListSelfOrError(CommandContext<ServerCommandSource> ctx) {
        return runListTrait(ctx, null, null);
    }

    private static int runListTrait(
            CommandContext<ServerCommandSource> ctx, String nameOrNull, String rawTraitOrNull) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            Locale lo = localeForSource(source, p);
                            if (nameOrNull == null) {
                                if (!(source.getEntity() instanceof ServerPlayerEntity self)) {
                                    source.sendError(
                                            Text.literal(
                                                    p.getMsg(
                                                            CommandMessage
                                                                    .MODIFIER_LIST_PLAYERS_ONLY,
                                                            lo)));
                                    return 0;
                                }
                                return sendTraitList(
                                        p, source, self, self, parseTraitOrNull(p, rawTraitOrNull), lo);
                            }
                            ServerPlayerEntity target =
                                    FabricCommandPlayerTarget.findOnline(
                                                    source.getServer(), nameOrNull)
                                            .orElse(null);
                            if (target == null) {
                                source.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.xp.player_not_found",
                                                nameOrNull));
                                return 0;
                            }
                            return sendTraitList(
                                    p, source, target, target, parseTraitOrNull(p, rawTraitOrNull), lo);
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static Trait parseTraitOrNull(FabricAuraSkills p, String raw) {
        if (raw == null) {
            return null;
        }
        return FabricCommandTraitParser.parse(p, raw);
    }

    private static int sendTraitList(
            FabricAuraSkills p,
            ServerCommandSource source,
            ServerPlayerEntity targetEntity,
            ServerPlayerEntity nameSource,
            Trait traitOrNull,
            Locale senderLocale) {
        User user = p.getUserManager().getUser(targetEntity.getUuid());
        if (user == null) {
            source.sendError(
                    Text.literal(
                            p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
            return 0;
        }
        Locale lineLocale = user.getLocale();
        if (traitOrNull == null) {
            String header =
                    FabricCommandModifierText.playerOnly(
                            p.getMsg(CommandMessage.TRAIT_LIST_ALL_TRAITS_HEADER, senderLocale),
                            nameSource);
            source.sendFeedback(
                    () -> Text.literal(p.getMessageProvider().applyFormatting(header)), false);
            for (String key : user.getTraitModifiers().keySet()) {
                TraitModifier mod = user.getTraitModifiers().get(key);
                String line =
                        FabricCommandModifierText.applyTraitModifier(
                                p,
                                p.getMsg(CommandMessage.MODIFIER_LIST_ALL_STATS_ENTRY, lineLocale),
                                mod,
                                nameSource,
                                lineLocale);
                String out = line;
                source.sendFeedback(() -> Text.literal(out), false);
            }
        } else {
            String h =
                    FabricCommandModifierText.applyTraitHeader(
                            p,
                            p.getMsg(CommandMessage.TRAIT_LIST_ONE_TRAIT_HEADER, senderLocale),
                            traitOrNull,
                            nameSource,
                            lineLocale);
            source.sendFeedback(() -> Text.literal(p.getMessageProvider().applyFormatting(h)), false);
            for (String key : user.getTraitModifiers().keySet()) {
                TraitModifier mod = user.getTraitModifiers().get(key);
                if (mod.trait().equals(traitOrNull)) {
                    String line =
                            FabricCommandModifierText.applyTraitModifier(
                                    p,
                                    p.getMsg(
                                            CommandMessage.MODIFIER_LIST_ONE_STAT_ENTRY, lineLocale),
                                    mod,
                                    nameSource,
                                    lineLocale);
                    String out = line;
                    source.sendFeedback(() -> Text.literal(out), false);
                }
            }
        }
        return 1;
    }

    private static int runStatAdd(
            CommandContext<ServerCommandSource> ctx,
            AuraSkillsModifier.Operation operation,
            boolean stack) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            String pName = StringArgumentType.getString(ctx, ARG_PLAYER);
                            String rawStat = StringArgumentType.getString(ctx, ARG_STAT);
                            String mName = StringArgumentType.getString(ctx, ARG_NAME);
                            double value = DoubleArgumentType.getDouble(ctx, ARG_VALUE);
                            ServerPlayerEntity target =
                                    FabricCommandPlayerTarget.findOnline(source.getServer(), pName)
                                            .orElse(null);
                            if (target == null) {
                                source.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.xp.player_not_found",
                                                pName));
                                return 0;
                            }
                            Stat stat = FabricCommandStatParser.parse(p, rawStat);
                            if (stat == null) {
                                source.sendError(
                                        Text.literal(
                                                p.getMsg(
                                                        CommandMessage.UNKNOWN_STAT,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            User user = p.getUserManager().getUser(target.getUuid());
                            if (user == null) {
                                source.sendError(
                                        Text.literal(
                                                p.getMsg(
                                                        CommandMessage.NO_PROFILE,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            Locale lo = user.getLocale();
                            StatModifier mod = new StatModifier(mName, stat, value, operation);
                            if (!user.getStatModifiers().containsKey(mName)) {
                                user.addStatModifier(mod);
                                user.setShouldSave(true);
                                p.getModifierManager().applyModifiers(user, true);
                                String line =
                                        FabricCommandModifierText.applyStatModifier(
                                                p,
                                                p.getMsg(CommandMessage.MODIFIER_ADD_ADDED, lo),
                                                mod,
                                                target,
                                                lo);
                                String out = line;
                                source.sendFeedback(() -> Text.literal(out), false);
                            } else if (stack) {
                                String stacked =
                                        FabricCommandModifierStackName.getStackedName(
                                                user.getStatModifiers().keySet(), mName);
                                StatModifier real = new StatModifier(stacked, stat, value, operation);
                                user.addStatModifier(real);
                                user.setShouldSave(true);
                                p.getModifierManager().applyModifiers(user, true);
                                String line =
                                        FabricCommandModifierText.applyStatModifier(
                                                p,
                                                p.getMsg(CommandMessage.MODIFIER_ADD_ADDED, lo),
                                                real,
                                                target,
                                                lo);
                                String out = line;
                                source.sendFeedback(() -> Text.literal(out), false);
                            } else {
                                String err =
                                        FabricCommandModifierText.applyStatModifier(
                                                p,
                                                p.getMsg(
                                                        CommandMessage.MODIFIER_ADD_ALREADY_EXISTS,
                                                        lo),
                                                mod,
                                                target,
                                                lo);
                                source.sendError(Text.literal(err));
                            }
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runTraitAdd(
            CommandContext<ServerCommandSource> ctx,
            AuraSkillsModifier.Operation operation,
            boolean stack) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            String pName = StringArgumentType.getString(ctx, ARG_PLAYER);
                            String rawTrait = StringArgumentType.getString(ctx, ARG_TRAIT);
                            String mName = StringArgumentType.getString(ctx, ARG_NAME);
                            double value = DoubleArgumentType.getDouble(ctx, ARG_VALUE);
                            ServerPlayerEntity target =
                                    FabricCommandPlayerTarget.findOnline(source.getServer(), pName)
                                            .orElse(null);
                            if (target == null) {
                                source.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.xp.player_not_found",
                                                pName));
                                return 0;
                            }
                            Trait trait = FabricCommandTraitParser.parse(p, rawTrait);
                            if (trait == null) {
                                source.sendError(
                                        Text.literal(
                                                p.getMsg(
                                                        CommandMessage.UNKNOWN_TRAIT,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            User user = p.getUserManager().getUser(target.getUuid());
                            if (user == null) {
                                source.sendError(
                                        Text.literal(
                                                p.getMsg(
                                                        CommandMessage.NO_PROFILE,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            Locale lo = user.getLocale();
                            TraitModifier mod = new TraitModifier(mName, trait, value, operation);
                            if (!user.getTraitModifiers().containsKey(mName)) {
                                user.addTraitModifier(mod);
                                user.setShouldSave(true);
                                p.getModifierManager().applyModifiers(user, true);
                                String line =
                                        FabricCommandModifierText.applyTraitModifier(
                                                p,
                                                p.getMsg(CommandMessage.MODIFIER_ADD_ADDED, lo),
                                                mod,
                                                target,
                                                lo);
                                String out = line;
                                source.sendFeedback(() -> Text.literal(out), false);
                            } else if (stack) {
                                String stacked =
                                        FabricCommandModifierStackName.getStackedName(
                                                user.getTraitModifiers().keySet(), mName);
                                TraitModifier real =
                                        new TraitModifier(stacked, trait, value, operation);
                                user.addTraitModifier(real);
                                user.setShouldSave(true);
                                p.getModifierManager().applyModifiers(user, true);
                                String line =
                                        FabricCommandModifierText.applyTraitModifier(
                                                p,
                                                p.getMsg(CommandMessage.MODIFIER_ADD_ADDED, lo),
                                                real,
                                                target,
                                                lo);
                                String out = line;
                                source.sendFeedback(() -> Text.literal(out), false);
                            } else {
                                String err =
                                        p.getMessageProvider()
                                                .applyFormatting(
                                                        p.getMsg(
                                                                        CommandMessage
                                                                                .TRAIT_ADD_ALREADY_EXISTS,
                                                                        lo)
                                                                .replace("{name}", mName)
                                                                .replace(
                                                                        "{player}",
                                                                        target.getName()
                                                                                .getString()));
                                source.sendError(Text.literal(err));
                            }
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runStatRemove(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            String pName = StringArgumentType.getString(ctx, ARG_PLAYER);
                            String mName = StringArgumentType.getString(ctx, ARG_NAME);
                            ServerPlayerEntity target =
                                    FabricCommandPlayerTarget.findOnline(source.getServer(), pName)
                                            .orElse(null);
                            if (target == null) {
                                source.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.xp.player_not_found",
                                                pName));
                                return 0;
                            }
                            User user = p.getUserManager().getUser(target.getUuid());
                            if (user == null) {
                                source.sendError(
                                        Text.literal(
                                                p.getMsg(
                                                        CommandMessage.NO_PROFILE,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            Locale lo = user.getLocale();
                            if (user.removeStatModifier(mName)) {
                                user.setShouldSave(true);
                                p.getModifierManager().applyModifiers(user, true);
                                String line =
                                        FabricCommandModifierText.nameAndPlayer(
                                                p.getMsg(
                                                        CommandMessage.MODIFIER_REMOVE_REMOVED, lo),
                                                mName,
                                                target);
                                String out = p.getMessageProvider().applyFormatting(line);
                                source.sendFeedback(() -> Text.literal(out), false);
                            } else {
                                String line =
                                        FabricCommandModifierText.nameAndPlayer(
                                                p.getMsg(
                                                        CommandMessage.MODIFIER_REMOVE_NOT_FOUND,
                                                        lo),
                                                mName,
                                                target);
                                String out = p.getMessageProvider().applyFormatting(line);
                                source.sendError(Text.literal(out));
                            }
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runTraitRemove(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            String pName = StringArgumentType.getString(ctx, ARG_PLAYER);
                            String mName = StringArgumentType.getString(ctx, ARG_NAME);
                            ServerPlayerEntity target =
                                    FabricCommandPlayerTarget.findOnline(source.getServer(), pName)
                                            .orElse(null);
                            if (target == null) {
                                source.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.xp.player_not_found",
                                                pName));
                                return 0;
                            }
                            User user = p.getUserManager().getUser(target.getUuid());
                            if (user == null) {
                                source.sendError(
                                        Text.literal(
                                                p.getMsg(
                                                        CommandMessage.NO_PROFILE,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            Locale lo = user.getLocale();
                            if (user.removeTraitModifier(mName)) {
                                user.setShouldSave(true);
                                p.getModifierManager().applyModifiers(user, true);
                                String line =
                                        FabricCommandModifierText.nameAndPlayer(
                                                p.getMsg(
                                                        CommandMessage.MODIFIER_REMOVE_REMOVED, lo),
                                                mName,
                                                target);
                                String out = p.getMessageProvider().applyFormatting(line);
                                source.sendFeedback(() -> Text.literal(out), false);
                            } else {
                                String line =
                                        FabricCommandModifierText.nameAndPlayer(
                                                p.getMsg(
                                                        CommandMessage.MODIFIER_REMOVE_NOT_FOUND,
                                                        lo),
                                                mName,
                                                target);
                                String out = p.getMessageProvider().applyFormatting(line);
                                source.sendError(Text.literal(out));
                            }
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runStatRemoveAllSelfOrError(CommandContext<ServerCommandSource> ctx) {
        return runStatRemoveAll(ctx, null, null);
    }

    private static int runStatRemoveAll(
            CommandContext<ServerCommandSource> ctx, String nameOrNull, String rawStatOrNull) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            Locale lo = localeForSource(source, p);
                            ServerPlayerEntity target;
                            if (nameOrNull == null) {
                                if (!(source.getEntity() instanceof ServerPlayerEntity self)) {
                                    String err = p.getMsg(CommandMessage.MODIFIER_REMOVEALL_PLAYERS_ONLY, lo);
                                    source.sendError(
                                            Text.literal(
                                                    p.getMessageProvider().applyFormatting(err)));
                                    return 0;
                                }
                                target = self;
                            } else {
                                target =
                                        FabricCommandPlayerTarget.findOnline(
                                                        source.getServer(), nameOrNull)
                                                .orElse(null);
                                if (target == null) {
                                    source.sendError(
                                            FabricGraniteText.key(
                                                    "auraskills.granite.command.xp.player_not_found",
                                                    nameOrNull));
                                    return 0;
                                }
                            }
                            Stat statFilter =
                                    rawStatOrNull == null
                                            ? null
                                            : FabricCommandStatParser.parse(p, rawStatOrNull);
                            User user = p.getUserManager().getUser(target.getUuid());
                            if (user == null) {
                                source.sendError(
                                        Text.literal(
                                                p.getMsg(
                                                        CommandMessage.NO_PROFILE,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            int removed = 0;
                            List<String> toRemove = new ArrayList<>();
                            for (String key : user.getStatModifiers().keySet()) {
                                StatModifier mod = user.getStatModifiers().get(key);
                                if (statFilter == null) {
                                    toRemove.add(key);
                                    removed++;
                                } else if (mod.stat() == statFilter) {
                                    toRemove.add(key);
                                    removed++;
                                }
                            }
                            for (String key : toRemove) {
                                user.removeStatModifier(key);
                            }
                            if (!toRemove.isEmpty()) {
                                user.setShouldSave(true);
                                p.getModifierManager().applyModifiers(user, true);
                            }
                            if (statFilter == null) {
                                String line =
                                        FabricCommandModifierText.playerOnly(
                                                p.getMsg(
                                                        CommandMessage
                                                                .MODIFIER_REMOVEALL_REMOVED_ALL_STATS,
                                                        lo),
                                                target);
                                line = line.replace("{num}", String.valueOf(removed));
                                String out = p.getMessageProvider().applyFormatting(line);
                                source.sendFeedback(() -> Text.literal(out), false);
                            } else {
                                String line =
                                        FabricCommandModifierText.applyStatHeader(
                                                p,
                                                p.getMsg(
                                                        CommandMessage
                                                                .MODIFIER_REMOVEALL_REMOVED_ONE_STAT,
                                                        lo),
                                                statFilter,
                                                target,
                                                user.getLocale());
                                line = line.replace("{num}", String.valueOf(removed));
                                String out = p.getMessageProvider().applyFormatting(line);
                                source.sendFeedback(() -> Text.literal(out), false);
                            }
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int runTraitRemoveAllSelfOrError(CommandContext<ServerCommandSource> ctx) {
        return runTraitRemoveAll(ctx, null, null);
    }

    private static int runTraitRemoveAll(
            CommandContext<ServerCommandSource> ctx, String nameOrNull, String rawTraitOrNull) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            Locale lo = localeForSource(source, p);
                            ServerPlayerEntity target;
                            if (nameOrNull == null) {
                                if (!(source.getEntity() instanceof ServerPlayerEntity self)) {
                                    String err = p.getMsg(CommandMessage.MODIFIER_REMOVEALL_PLAYERS_ONLY, lo);
                                    source.sendError(
                                            Text.literal(
                                                    p.getMessageProvider().applyFormatting(err)));
                                    return 0;
                                }
                                target = self;
                            } else {
                                target =
                                        FabricCommandPlayerTarget.findOnline(
                                                        source.getServer(), nameOrNull)
                                                .orElse(null);
                                if (target == null) {
                                    source.sendError(
                                            FabricGraniteText.key(
                                                    "auraskills.granite.command.xp.player_not_found",
                                                    nameOrNull));
                                    return 0;
                                }
                            }
                            Trait traitFilter =
                                    rawTraitOrNull == null
                                            ? null
                                            : FabricCommandTraitParser.parse(p, rawTraitOrNull);
                            User user = p.getUserManager().getUser(target.getUuid());
                            if (user == null) {
                                source.sendError(
                                        Text.literal(
                                                p.getMsg(
                                                        CommandMessage.NO_PROFILE,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            int removed = 0;
                            List<String> toRemove = new ArrayList<>();
                            for (String key : user.getTraitModifiers().keySet()) {
                                TraitModifier mod = user.getTraitModifiers().get(key);
                                if (traitFilter == null) {
                                    toRemove.add(key);
                                    removed++;
                                } else if (mod.trait().equals(traitFilter)) {
                                    toRemove.add(key);
                                    removed++;
                                }
                            }
                            for (String key : toRemove) {
                                user.removeTraitModifier(key);
                            }
                            if (!toRemove.isEmpty()) {
                                user.setShouldSave(true);
                                p.getModifierManager().applyModifiers(user, true);
                            }
                            if (traitFilter == null) {
                                String line =
                                        FabricCommandModifierText.playerOnly(
                                                p.getMsg(
                                                        CommandMessage
                                                                .TRAIT_REMOVEALL_REMOVED_ALL_TRAITS,
                                                        lo),
                                                target);
                                line = line.replace("{num}", String.valueOf(removed));
                                String out = p.getMessageProvider().applyFormatting(line);
                                source.sendFeedback(() -> Text.literal(out), false);
                            } else {
                                String line =
                                        FabricCommandModifierText.applyTraitHeader(
                                                p,
                                                p.getMsg(
                                                        CommandMessage
                                                                .MODIFIER_REMOVEALL_REMOVED_ONE_STAT,
                                                        lo),
                                                traitFilter,
                                                target,
                                                user.getLocale());
                                line = line.replace("{num}", String.valueOf(removed));
                                String out = p.getMessageProvider().applyFormatting(line);
                                source.sendFeedback(() -> Text.literal(out), false);
                            }
                            return 1;
                        })
                .orElseGet(() -> notReady(ctx));
    }

    private static int notReady(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
        return 0;
    }
}
