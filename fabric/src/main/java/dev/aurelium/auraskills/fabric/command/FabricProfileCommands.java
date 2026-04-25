package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.trait.TraitModifier;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.user.UserState;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.user.FabricUserDataRead;
import dev.aurelium.auraskills.fabric.user.FabricUserDataTransfer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bukkit {@link dev.aurelium.auraskills.bukkit.commands.ProfileCommand} — read-only
 * <strong>UserState</strong> inspection (same data model as {@code /transfer}, Sprint 58). **Sprint
 * 59.**
 */
public final class FabricProfileCommands {

    private static final String ARG_PLAYER = "player";

    private FabricProfileCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> profileLiteral() {
        return literal("profile")
                .then(
                        literal("skills")
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.PROFILE, true))
                                .then(
                                        CommandManager.argument(
                                                        ARG_PLAYER, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .onlinePlayers())
                                                .executes(FabricProfileCommands::executeSkills)))
                .then(
                        literal("stats")
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.PROFILE, true))
                                .then(
                                        CommandManager.argument(
                                                        ARG_PLAYER, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .onlinePlayers())
                                                .executes(FabricProfileCommands::executeStats)));
    }

    private static int executeSkills(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String nameArg = StringArgumentType.getString(ctx, ARG_PLAYER);
                            return withResolvedState(
                                    p,
                                    ctx,
                                    nameArg,
                                    (state, displayName, uuid, lo) -> {
                                        sendSkills(
                                                p,
                                                ctx.getSource(),
                                                state,
                                                displayName,
                                                uuid,
                                                lo);
                                        return 1;
                                    });
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeStats(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String nameArg = StringArgumentType.getString(ctx, ARG_PLAYER);
                            return withResolvedState(
                                    p,
                                    ctx,
                                    nameArg,
                                    (state, displayName, uuid, lo) -> {
                                        sendStats(
                                                p,
                                                ctx.getSource(),
                                                state,
                                                displayName,
                                                uuid,
                                                lo);
                                        return 1;
                                    });
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    @FunctionalInterface
    private interface StateConsumer {
        int accept(UserState state, String displayName, UUID uuid, Locale lo);
    }

    private static int withResolvedState(
            FabricAuraSkills p,
            CommandContext<ServerCommandSource> ctx,
            String nameArg,
            StateConsumer consumer) {
        ServerCommandSource source = ctx.getSource();
        MinecraftServer server = source.getServer();
        Locale lo = senderLocale(p, source);

        return FabricUserDataRead.resolveUuid(server, nameArg)
                .map(
                        uuid -> {
                            String displayName = FabricCommandLeaderboardText.resolvePlayerName(server, uuid);
                            if (displayName.equals("?")) {
                                displayName = nameArg;
                            }
                            if (FabricUserDataTransfer.isFileStorage(p)
                                    && !FabricUserDataTransfer.isPlayerOnline(server, uuid)
                                    && !FabricUserDataTransfer.hasYamlDataFile(p, uuid)) {
                                source.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.profile.no_userdata_file",
                                                uuid));
                                return 0;
                            }
                            if (FabricUserDataTransfer.isPlayerOnline(server, uuid)) {
                                UserState state =
                                        FabricUserDataTransfer.getStateForOnlineUser(p, uuid);
                                if (state == null) {
                                    source.sendError(
                                            Text.translatable(
                                                    "auraskills.granite.command.profile"
                                                            + ".source_not_loaded",
                                                    uuid.toString()));
                                    return 0;
                                }
                                return consumer.accept(state, displayName, uuid, lo);
                            }
                            UserState state = FabricUserDataRead.loadUserState(p, server, uuid);
                            if (state == null) {
                                source.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.profile.load_failed"));
                                return 0;
                            }
                            return consumer.accept(state, displayName, uuid, lo);
                        })
                .orElseGet(
                        () -> {
                            source.sendError(
                                    Text.translatable(
                                            "auraskills.granite.command.profile.player_not_found",
                                            nameArg));
                            return 0;
                        });
    }

    private static void sendSkills(
            FabricAuraSkills p,
            ServerCommandSource source,
            UserState state,
            String username,
            UUID uuid,
            Locale locale) {
        String message = p.getMsg(CommandMessage.PROFILE_SKILLS_HEADER, locale);
        message =
                TextUtil.replace(
                        message, "{name}", username, "{uuid}", uuid.toString());
        StringBuilder skillEntries = new StringBuilder();
        List<Skill> skillList = new ArrayList<>(List.copyOf(state.skillLevels().keySet()));
        skillList.sort(Comparator.comparing(Skill::name));
        for (Skill skill : skillList) {
            if (!p.getSkillManager().isLoaded(skill)) {
                continue;
            }
            skillEntries.append(
                    TextUtil.replace(
                            p.getMsg(CommandMessage.PROFILE_SKILLS_ENTRY, locale),
                            "{skill}",
                            skill.getDisplayName(locale, false),
                            "{level}",
                            String.valueOf(state.skillLevels().get(skill)),
                            "{xp}",
                            NumberUtil.format1(state.skillXp().get(skill))));
        }
        message = TextUtil.replace(message, "{skill_entries}", skillEntries.toString());
        String line =
                p.getPrefix(locale)
                        + p.getMessageProvider().applyFormatting(message);
        String out =
                line.replaceAll("(\\u005C\\u006E)|(\\n)", "\n")
                        + "\n"
                        + Text.translatable("auraskills.granite.command.profile.userstate_disclaimer")
                                .getString();
        source.sendFeedback(() -> Text.literal(out), false);
    }

    private static void sendStats(
            FabricAuraSkills p,
            ServerCommandSource source,
            UserState state,
            String username,
            UUID uuid,
            Locale locale) {
        Map<Skill, Integer> skillLevels = state.skillLevels();
        int start = p.config().getStartLevel();
        Map<Stat, Double> baseStats = new HashMap<>();
        for (Skill skill : p.getSkillManager().getEnabledSkills()) {
            Map<Stat, Double> skillRewardedStats =
                    p.getRewardManager()
                            .getRewardTable(skill)
                            .getStatLevels(
                                    skillLevels.getOrDefault(skill, start));
            for (Map.Entry<Stat, Double> entry : skillRewardedStats.entrySet()) {
                double existing = baseStats.getOrDefault(entry.getKey(), 0.0);
                baseStats.put(entry.getKey(), existing + entry.getValue());
            }
        }
        Map<Stat, Double> modifiedStats = new HashMap<>();
        for (StatModifier modifier : state.statModifiers().values()) {
            double existing = modifiedStats.getOrDefault(modifier.stat(), 0.0);
            modifiedStats.put(modifier.stat(), existing + modifier.value());
        }
        Map<Stat, Double> totalStats = new HashMap<>();
        for (Stat stat : p.getStatManager().getEnabledStats()) {
            double base = baseStats.getOrDefault(stat, 0.0);
            double modified = modifiedStats.getOrDefault(stat, 0.0);
            totalStats.put(stat, base + modified);
        }
        String message = p.getMsg(CommandMessage.PROFILE_STATS_HEADER, locale);
        message =
                TextUtil.replace(
                        message, "{name}", username, "{uuid}", uuid.toString());
        StringBuilder statEntries = new StringBuilder();
        List<Stat> statList = new ArrayList<>(List.copyOf(totalStats.keySet()));
        statList.sort(Comparator.comparing(Stat::name));
        for (Stat stat : statList) {
            if (!p.getStatManager().isLoaded(stat)) {
                continue;
            }
            statEntries.append(
                    TextUtil.replace(
                            p.getMsg(CommandMessage.PROFILE_STATS_ENTRY, locale),
                            "{stat}",
                            stat.getDisplayName(locale, false),
                            "{total_level}",
                            NumberUtil.format1(totalStats.getOrDefault(stat, 0.0)),
                            "{base_level}",
                            NumberUtil.format1(baseStats.getOrDefault(stat, 0.0)),
                            "{modified_level}",
                            NumberUtil.format1(modifiedStats.getOrDefault(stat, 0.0))));
        }
        message = TextUtil.replace(message, "{stat_entries}", statEntries.toString());
        StringBuilder userState = new StringBuilder();
        userState
                .append(
                        Text.translatable(
                                        "auraskills.granite.command.profile.mana_line",
                                        NumberUtil.format1(state.mana()))
                                .getString())
                .append("\n");
        userState
                .append(
                        formatModifierBlock(
                                p,
                                locale,
                                "auraskills.granite.command.profile.stat_mods_header",
                                state.statModifiers().values().stream()
                                        .sorted(Comparator.comparing(StatModifier::name))
                                        .toList(),
                                FabricProfileCommands::formatStatMod));
        userState
                .append(
                        formatModifierBlock(
                                p,
                                locale,
                                "auraskills.granite.command.profile.trait_mods_header",
                                state.traitModifiers().values().stream()
                                        .sorted(Comparator.comparing(TraitModifier::name))
                                        .toList(),
                                FabricProfileCommands::formatTraitMod));
        String line =
                p.getPrefix(locale)
                        + p.getMessageProvider().applyFormatting(message);
        String out =
                line.replaceAll("(\\u005C\\u006E)|(\\n)", "\n")
                        + "\n"
                        + userState
                        + "\n"
                        + Text.translatable("auraskills.granite.command.profile.userstate_disclaimer")
                                .getString();
        source.sendFeedback(() -> Text.literal(out), false);
    }

    private static <T> String formatModifierBlock(
            FabricAuraSkills p,
            Locale lo,
            String headerKey,
            List<T> items,
            java.util.function.Function<T, String> line) {
        String h =
                p.getMessageProvider()
                        .applyFormatting(Text.translatable(headerKey).getString());
        if (items.isEmpty()) {
            return h
                    + " "
                    + p.getMessageProvider()
                            .applyFormatting(
                                    Text.translatable("auraskills.granite.command.profile.modifier_none")
                                            .getString())
                    + "\n";
        }
        StringBuilder b = new StringBuilder(h).append("\n");
        for (T t : items) {
            b.append("  ").append(line.apply(t)).append("\n");
        }
        return b.toString();
    }

    private static String formatStatMod(StatModifier m) {
        return m.name()
                + " | "
                + m.stat().name()
                + " "
                + opSymbol(m)
                + " "
                + NumberUtil.format1(m.value());
    }

    private static String formatTraitMod(TraitModifier m) {
        return m.name()
                + " | "
                + m.trait().name()
                + " "
                + opSymbol(m)
                + " "
                + NumberUtil.format1(m.value());
    }

    private static String opSymbol(AuraSkillsModifier<?> m) {
        return switch (m.operation()) {
            case ADD -> "+";
            case MULTIPLY -> "×";
            case ADD_PERCENT -> "+%";
        };
    }

    private static Locale senderLocale(FabricAuraSkills p, ServerCommandSource source) {
        if (source.getEntity() instanceof ServerPlayerEntity sp) {
            User u = p.getUserManager().getUser(sp.getUuid());
            if (u != null) {
                return u.getLocale();
            }
        }
        return p.getDefaultLanguage();
    }
}
