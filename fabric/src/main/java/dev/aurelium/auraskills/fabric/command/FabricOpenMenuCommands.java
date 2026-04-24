package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.menu.FabricAuraSkillsMenus;
import dev.aurelium.auraskills.fabric.menu.FabricMenuKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bounded **openmenu** for built-in registry menus (Sprint 44+), Bukkit
 * {@link dev.aurelium.auraskills.bukkit.commands.OpenMenuCommand}-style. Reuse the same {@code
 * literal("openmenu")…} for {@code /openmenu}, {@code /auraskills openmenu}, and {@code /sk
 * openmenu} / {@code /skill openmenu}. **Sprint 45:** {@code level_progression} and {@code
 * stat_info} with typed **skill** / **stat** args — routes through
 * {@link FabricAuraSkillsMenus#openLevelProgression} / {@link FabricAuraSkillsMenus#openStatInfo}
 * and {@link dev.aurelium.auraskills.fabric.menu.FabricMenuContext}.
 */
public final class FabricOpenMenuCommands {

    private static final String ARG_TARGET = "target";
    private static final String ARG_SKILL = "skill";
    private static final String ARG_STAT = "stat";

    private FabricOpenMenuCommands() {}

    /**
     * Subcommand {@code openmenu …}: attach with {@code .then(…)} under a parent
     * root.
     */
    public static LiteralArgumentBuilder<ServerCommandSource> openMenuSubcommand() {
        return literal("openmenu")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.OPEN_MENU, true))
                .then(
                        literal(FabricMenuKeys.SKILLS)
                                .executes(FabricOpenMenuCommands::executeSkillsSelf)
                                .then(
                                        CommandManager.argument(
                                                        ARG_TARGET, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .onlinePlayers())
                                                .executes(
                                                        FabricOpenMenuCommands::executeSkillsTarget)))
                .then(
                        literal(FabricMenuKeys.STATS)
                                .executes(FabricOpenMenuCommands::executeStatsSelf)
                                .then(
                                        CommandManager.argument(
                                                        ARG_TARGET, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .onlinePlayers())
                                                .executes(
                                                        FabricOpenMenuCommands::executeStatsTarget)))
                .then(
                        literal(FabricMenuKeys.LEVEL_PROGRESSION)
                                .then(
                                        CommandManager.argument(
                                                        ARG_SKILL, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .enabledSkillKeys())
                                                .executes(
                                                        FabricOpenMenuCommands
                                                                ::executeLevelProgressionSelf))
                                .then(
                                        CommandManager.argument(
                                                        ARG_TARGET, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .onlinePlayers())
                                                .then(
                                                        CommandManager.argument(
                                                                        ARG_SKILL,
                                                                        StringArgumentType
                                                                                .word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .enabledSkillKeys())
                                                                .executes(
                                                                        FabricOpenMenuCommands
                                                                                ::executeLevelProgressionForTarget))))
                .then(
                        literal(FabricMenuKeys.STAT_INFO)
                                .then(
                                        CommandManager.argument(
                                                        ARG_STAT, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .enabledStatKeys())
                                                .executes(
                                                        FabricOpenMenuCommands::executeStatInfoSelf))
                                .then(
                                        CommandManager.argument(
                                                        ARG_TARGET, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .onlinePlayers())
                                                .then(
                                                        CommandManager.argument(
                                                                        ARG_STAT,
                                                                        StringArgumentType
                                                                                .word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .enabledStatKeys())
                                                                .executes(
                                                                        FabricOpenMenuCommands
                                                                                ::executeStatInfoForTarget))));
    }

    /** Top-level {@code /openmenu …} (same tree; Fabric-only convenience). */
    public static LiteralArgumentBuilder<ServerCommandSource> openMenuRoot() {
        return openMenuSubcommand();
    }

    private static int executeSkillsSelf(CommandContext<ServerCommandSource> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    ServerCommandSource s = ctx.getSource();
                    if (!(s.getEntity() instanceof ServerPlayerEntity self)) {
                        s.sendError(
                                Text.translatable(
                                        "auraskills.granite.command.openmenu.console_specify"));
                        return 0;
                    }
                    FabricAuraSkillsMenus.open(FabricMenuKeys.SKILLS, self, p);
                    sendListMenuFeedback(
                            s, self, "auraskills.granite.menu.skills_opened", FabricMenuKeys.SKILLS);
                    return 1;
                });
    }

    private static int executeSkillsTarget(CommandContext<ServerCommandSource> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    String name = StringArgumentType.getString(ctx, ARG_TARGET);
                    ServerCommandSource s = ctx.getSource();
                    ServerPlayerEntity target =
                            FabricCommandPlayerTarget.findOnline(s.getServer(), name)
                                    .orElse(null);
                    if (target == null) {
                        s.sendError(
                                Text.translatable(
                                        "auraskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    FabricAuraSkillsMenus.open(FabricMenuKeys.SKILLS, target, p);
                    sendListMenuFeedback(
                            s, target, "auraskills.granite.menu.skills_opened", FabricMenuKeys.SKILLS);
                    return 1;
                });
    }

    private static int executeStatsSelf(CommandContext<ServerCommandSource> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    ServerCommandSource s = ctx.getSource();
                    if (!(s.getEntity() instanceof ServerPlayerEntity self)) {
                        s.sendError(
                                Text.translatable(
                                        "auraskills.granite.command.openmenu.console_specify"));
                        return 0;
                    }
                    FabricAuraSkillsMenus.open(FabricMenuKeys.STATS, self, p);
                    sendListMenuFeedback(
                            s, self, "auraskills.granite.menu.stats_opened", FabricMenuKeys.STATS);
                    return 1;
                });
    }

    private static int executeStatsTarget(CommandContext<ServerCommandSource> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    String name = StringArgumentType.getString(ctx, ARG_TARGET);
                    ServerCommandSource s = ctx.getSource();
                    ServerPlayerEntity target =
                            FabricCommandPlayerTarget.findOnline(s.getServer(), name)
                                    .orElse(null);
                    if (target == null) {
                        s.sendError(
                                Text.translatable(
                                        "auraskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    FabricAuraSkillsMenus.open(FabricMenuKeys.STATS, target, p);
                    sendListMenuFeedback(
                            s, target, "auraskills.granite.menu.stats_opened", FabricMenuKeys.STATS);
                    return 1;
                });
    }

    private static int executeLevelProgressionSelf(CommandContext<ServerCommandSource> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    ServerCommandSource s = ctx.getSource();
                    if (!(s.getEntity() instanceof ServerPlayerEntity self)) {
                        s.sendError(
                                Text.translatable(
                                        "auraskills.granite.command.openmenu.context.console_specify"));
                        return 0;
                    }
                    String raw = StringArgumentType.getString(ctx, ARG_SKILL);
                    Skill skill = FabricCommandSkillParser.parse(p, raw);
                    if (skill == null) {
                        s.sendError(
                                Text.translatable(
                                        "auraskills.granite.command.openmenu.unknown_skill", raw));
                        return 0;
                    }
                    FabricAuraSkillsMenus.openLevelProgression(skill, self, p);
                    s.sendFeedback(
                            () -> Text.translatable("auraskills.granite.menu.level_progression_opened"),
                            true);
                    return 1;
                });
    }

    private static int executeLevelProgressionForTarget(CommandContext<ServerCommandSource> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    String name = StringArgumentType.getString(ctx, ARG_TARGET);
                    String raw = StringArgumentType.getString(ctx, ARG_SKILL);
                    ServerCommandSource s = ctx.getSource();
                    ServerPlayerEntity target =
                            FabricCommandPlayerTarget.findOnline(s.getServer(), name)
                                    .orElse(null);
                    if (target == null) {
                        s.sendError(
                                Text.translatable(
                                        "auraskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    Skill skill = FabricCommandSkillParser.parse(p, raw);
                    if (skill == null) {
                        s.sendError(
                                Text.translatable(
                                        "auraskills.granite.command.openmenu.unknown_skill", raw));
                        return 0;
                    }
                    FabricAuraSkillsMenus.openLevelProgression(skill, target, p);
                    sendContextMenuForTarget(s, target, FabricMenuKeys.LEVEL_PROGRESSION, raw);
                    return 1;
                });
    }

    private static int executeStatInfoSelf(CommandContext<ServerCommandSource> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    ServerCommandSource s = ctx.getSource();
                    if (!(s.getEntity() instanceof ServerPlayerEntity self)) {
                        s.sendError(
                                Text.translatable(
                                        "auraskills.granite.command.openmenu.context.console_specify"));
                        return 0;
                    }
                    String raw = StringArgumentType.getString(ctx, ARG_STAT);
                    Stat stat = FabricCommandStatParser.parse(p, raw);
                    if (stat == null) {
                        s.sendError(
                                Text.translatable(
                                        "auraskills.granite.command.openmenu.unknown_stat", raw));
                        return 0;
                    }
                    FabricAuraSkillsMenus.openStatInfo(stat, self, p);
                    s.sendFeedback(
                            () -> Text.translatable("auraskills.granite.menu.stat_info_opened"), true);
                    return 1;
                });
    }

    private static int executeStatInfoForTarget(CommandContext<ServerCommandSource> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    String name = StringArgumentType.getString(ctx, ARG_TARGET);
                    String raw = StringArgumentType.getString(ctx, ARG_STAT);
                    ServerCommandSource s = ctx.getSource();
                    ServerPlayerEntity target =
                            FabricCommandPlayerTarget.findOnline(s.getServer(), name)
                                    .orElse(null);
                    if (target == null) {
                        s.sendError(
                                Text.translatable(
                                        "auraskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    Stat stat = FabricCommandStatParser.parse(p, raw);
                    if (stat == null) {
                        s.sendError(
                                Text.translatable(
                                        "auraskills.granite.command.openmenu.unknown_stat", raw));
                        return 0;
                    }
                    FabricAuraSkillsMenus.openStatInfo(stat, target, p);
                    sendContextMenuForTarget(s, target, FabricMenuKeys.STAT_INFO, raw);
                    return 1;
                });
    }

    private static int runWithPlugin(
            CommandContext<ServerCommandSource> ctx, Function<FabricAuraSkills, Integer> run) {
        return FabricCommandPluginState.pluginOrEmpty()
                .map(run)
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static void sendListMenuFeedback(
            ServerCommandSource source,
            ServerPlayerEntity menuRecipient,
            String selfKey,
            String menuId) {
        if (source.getEntity() instanceof ServerPlayerEntity srcPlayer
                && srcPlayer.getUuid().equals(menuRecipient.getUuid())) {
            source.sendFeedback(() -> Text.translatable(selfKey), true);
        } else {
            source.sendFeedback(
                    () -> Text.translatable(
                            "auraskills.granite.command.openmenu.opened_for",
                            menuId,
                            menuRecipient.getName().getString()),
                    true);
        }
    }

    /**
     * When opening a context menu for another (or for self with {@code
     * openmenu <menu> <player> <context>}, where executor == target), send self-style
     * feedback or "opened for" with id + key.
     */
    private static void sendContextMenuForTarget(
            ServerCommandSource source,
            ServerPlayerEntity target,
            String menuId,
            String contextKeyRaw) {
        if (source.getEntity() instanceof ServerPlayerEntity srcPlayer
                && srcPlayer.getUuid().equals(target.getUuid())) {
            if (FabricMenuKeys.LEVEL_PROGRESSION.equals(menuId)) {
                source.sendFeedback(
                        () -> Text.translatable(
                                "auraskills.granite.menu.level_progression_opened"),
                        true);
            } else {
                source.sendFeedback(
                        () -> Text.translatable("auraskills.granite.menu.stat_info_opened"), true);
            }
        } else {
            source.sendFeedback(
                    () -> Text.translatable(
                            "auraskills.granite.command.openmenu.opened_for_context",
                            menuId,
                            contextKeyRaw,
                            target.getName().getString()),
                    true);
        }
    }
}
