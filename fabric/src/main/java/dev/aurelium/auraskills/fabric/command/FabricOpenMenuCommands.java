package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.message.type.MenuMessage;
import dev.aurelium.auraskills.common.skill.LoadedSkill;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.item.FabricUnclaimedItemsMerge;
import dev.aurelium.auraskills.fabric.menu.FabricAuraSkillsMenus;
import dev.aurelium.auraskills.fabric.menu.FabricMenuKeys;
import dev.aurelium.auraskills.fabric.menu.FabricMenuUserReadiness;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bounded **openmenu** for built-in registry menus (Sprint 44+), Bukkit
 * {@link dev.aurelium.auraskills.bukkit.commands.OpenMenuCommand}-style. Reuse the same {@code
 * literal("openmenu")…} for {@code /openmenu}, {@code /auraskills openmenu}, and {@code /sk
 * openmenu} / {@code /skill openmenu}. **Sprint 45:** {@code level_progression} and {@code
 * stat_info} with typed **skill** / **stat** args — routes through
 * {@link FabricAuraSkillsMenus#openLevelProgression} / {@link FabricAuraSkillsMenus#openStatInfo}
 * and {@link dev.aurelium.auraskills.fabric.menu.FabricMenuContext}. **Sprint 70:** {@code sources}
 * with skill + optional sort → {@link FabricAuraSkillsMenus#openSources} (not registry-loaded).
 * **Sprint 71:** {@code unclaimed_items} (self or online target) → {@link
 * FabricAuraSkillsMenus#openUnclaimedItems}. **Sprint 72:** {@code leaderboard} →
 * {@link FabricOpenMenuLeaderboard} / {@link FabricAuraSkillsMenus#openLeaderboard} (not
 * registry-loaded). **Sprint 73:** {@code abilities} → {@link FabricOpenMenuAbilities} /
 * {@link FabricAuraSkillsMenus#openAbilities} (not registry-loaded).
 */
public final class FabricOpenMenuCommands {

    private static final String ARG_TARGET = "target";
    private static final String ARG_SKILL = "skill";
    private static final String ARG_STAT = "stat";
    private static final String ARG_SORT = "sort";

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
                                                                                ::executeStatInfoForTarget))))
                .then(
                        literal(FabricMenuKeys.SOURCES)
                                .then(
                                        CommandManager.argument(
                                                        ARG_SKILL, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .enabledSkillKeys())
                                                .executes(
                                                        FabricOpenMenuCommands::executeSourcesSelf)
                                                .then(
                                                        CommandManager.argument(
                                                                        ARG_SORT,
                                                                        StringArgumentType.word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .sourceSortTypes())
                                                                .executes(
                                                                        FabricOpenMenuCommands
                                                                                ::executeSourcesSelfWithSortArg)))
                                .then(
                                        CommandManager.argument(
                                                        ARG_TARGET, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .onlinePlayers())
                                                .then(
                                                        CommandManager.argument(
                                                                        ARG_SKILL,
                                                                        StringArgumentType.word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .enabledSkillKeys())
                                                                .executes(
                                                                        FabricOpenMenuCommands
                                                                                ::executeSourcesForTarget)
                                                                .then(
                                                                        CommandManager.argument(
                                                                                        ARG_SORT,
                                                                                        StringArgumentType
                                                                                                .word())
                                                                                .suggests(
                                                                                        FabricCommandSuggestionProviders
                                                                                                .sourceSortTypes())
                                                                                .executes(
                                                                                        FabricOpenMenuCommands
                                                                                                ::executeSourcesForTargetWithSortArg)))))
                .then(
                        literal(FabricMenuKeys.UNCLAIMED_ITEMS)
                                .executes(FabricOpenMenuCommands::executeUnclaimedSelf)
                                .then(
                                        CommandManager.argument(
                                                        ARG_TARGET, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .onlinePlayers())
                                                .executes(
                                                        FabricOpenMenuCommands
                                                                ::executeUnclaimedForTarget)))
                .then(FabricOpenMenuLeaderboard.node())
                .then(FabricOpenMenuAbilities.node());
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
                                FabricGraniteText.key(
                                        "auraskills.granite.command.openmenu.console_specify"));
                        return 0;
                    }
                    if (FabricMenuUserReadiness.loadedUserOrWarn(p, self) == null) {
                        return 1;
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
                                FabricGraniteText.key(
                                        "auraskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    if (FabricMenuUserReadiness.loadedUserOrWarn(p, target) == null) {
                        return 1;
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
                                FabricGraniteText.key(
                                        "auraskills.granite.command.openmenu.console_specify"));
                        return 0;
                    }
                    if (FabricMenuUserReadiness.loadedUserOrWarn(p, self) == null) {
                        return 1;
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
                                FabricGraniteText.key(
                                        "auraskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    if (FabricMenuUserReadiness.loadedUserOrWarn(p, target) == null) {
                        return 1;
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
                                FabricGraniteText.key(
                                        "auraskills.granite.command.openmenu.context.console_specify"));
                        return 0;
                    }
                    String raw = StringArgumentType.getString(ctx, ARG_SKILL);
                    Skill skill = FabricCommandSkillParser.parse(p, raw);
                    if (skill == null) {
                        s.sendError(
                                FabricGraniteText.key(
                                        "auraskills.granite.command.openmenu.unknown_skill", raw));
                        return 0;
                    }
                    FabricAuraSkillsMenus.openLevelProgression(skill, self, p);
                    s.sendFeedback(
                            () -> FabricGraniteText.key("auraskills.granite.menu.level_progression_opened"),
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
                                FabricGraniteText.key(
                                        "auraskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    Skill skill = FabricCommandSkillParser.parse(p, raw);
                    if (skill == null) {
                        s.sendError(
                                FabricGraniteText.key(
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
                                FabricGraniteText.key(
                                        "auraskills.granite.command.openmenu.context.console_specify"));
                        return 0;
                    }
                    String raw = StringArgumentType.getString(ctx, ARG_STAT);
                    Stat stat = FabricCommandStatParser.parse(p, raw);
                    if (stat == null) {
                        s.sendError(
                                FabricGraniteText.key(
                                        "auraskills.granite.command.openmenu.unknown_stat", raw));
                        return 0;
                    }
                    FabricAuraSkillsMenus.openStatInfo(stat, self, p);
                    s.sendFeedback(
                            () -> FabricGraniteText.key("auraskills.granite.menu.stat_info_opened"), true);
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
                                FabricGraniteText.key(
                                        "auraskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    Stat stat = FabricCommandStatParser.parse(p, raw);
                    if (stat == null) {
                        s.sendError(
                                FabricGraniteText.key(
                                        "auraskills.granite.command.openmenu.unknown_stat", raw));
                        return 0;
                    }
                    FabricAuraSkillsMenus.openStatInfo(stat, target, p);
                    sendContextMenuForTarget(s, target, FabricMenuKeys.STAT_INFO, raw);
                    return 1;
                });
    }

    private static int executeSourcesSelf(CommandContext<ServerCommandSource> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    ServerCommandSource s = ctx.getSource();
                    if (!(s.getEntity() instanceof ServerPlayerEntity self)) {
                        s.sendError(
                                FabricGraniteText.key(
                                        "auraskills.granite.command.openmenu.context.console_specify"));
                        return 0;
                    }
                    return runOpenmenuSourcesInGame(
                            p,
                            s,
                            self,
                            StringArgumentType.getString(ctx, ARG_SKILL),
                            FabricSourceSortType.ASCENDING,
                            0,
                            true,
                            true);
                });
    }

    private static int executeSourcesSelfWithSortArg(CommandContext<ServerCommandSource> ctx) {
        return FabricSourceSortType.fromTokenStrict(
                        StringArgumentType.getString(ctx, ARG_SORT))
                .map(
                        srt -> runWithPlugin(
                                ctx,
                                p -> {
                                    ServerCommandSource s = ctx.getSource();
                                    if (!(s.getEntity() instanceof ServerPlayerEntity self)) {
                                        s.sendError(
                                                FabricGraniteText.key(
                                                        "auraskills.granite.command.openmenu.context.console_specify"));
                                        return 0;
                                    }
                                    return runOpenmenuSourcesInGame(
                                            p,
                                            s,
                                            self,
                                            StringArgumentType.getString(ctx, ARG_SKILL),
                                            srt,
                                            0,
                                            true,
                                            true);
                                }))
                .orElseGet(
                        () -> {
                            String bad = StringArgumentType.getString(ctx, ARG_SORT);
                            ctx.getSource()
                                    .sendError(
                                            FabricGraniteText.key(
                                                    "auraskills.granite.command.sources.bad_sort",
                                                    bad));
                            return 0;
                        });
    }

    private static int executeSourcesForTarget(CommandContext<ServerCommandSource> ctx) {
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
                                FabricGraniteText.key(
                                        "auraskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    return runOpenmenuSourcesInGame(
                            p, s, target, raw, FabricSourceSortType.ASCENDING, 0, false, true);
                });
    }

    private static int executeSourcesForTargetWithSortArg(CommandContext<ServerCommandSource> ctx) {
        return FabricSourceSortType.fromTokenStrict(
                        StringArgumentType.getString(ctx, ARG_SORT))
                .map(
                        srt -> runWithPlugin(
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
                                                FabricGraniteText.key(
                                                        "auraskills.granite.command.xp.player_not_found",
                                                        name));
                                        return 0;
                                    }
                                    return runOpenmenuSourcesInGame(
                                            p, s, target, raw, srt, 0, false, true);
                                }))
                .orElseGet(
                        () -> {
                            String bad = StringArgumentType.getString(ctx, ARG_SORT);
                            ctx.getSource()
                                    .sendError(
                                            FabricGraniteText.key(
                                                    "auraskills.granite.command.sources.bad_sort",
                                                    bad));
                            return 0;
                        });
    }

    /**
     * @param sendSelfLineOnOpen when true (self path), send {@code menu.sources_opened} to the
     *     executor after a successful open
     */
    private static int executeUnclaimedSelf(CommandContext<ServerCommandSource> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    ServerCommandSource s = ctx.getSource();
                    if (!(s.getEntity() instanceof ServerPlayerEntity self)) {
                        s.sendError(
                                FabricGraniteText.key(
                                        "auraskills.granite.command.openmenu.unclaimed_console"));
                        return 0;
                    }
                    return runOpenmenuUnclaimed(p, s, self);
                });
    }

    private static int executeUnclaimedForTarget(CommandContext<ServerCommandSource> ctx) {
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
                                FabricGraniteText.key(
                                        "auraskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    return runOpenmenuUnclaimed(p, s, target);
                });
    }

    /**
     * Open the unclaimed menu for an online player; on GUI failure, run the same merge pass as
     * {@code /claimitems} (**Sprint 71**).
     */
    private static int runOpenmenuUnclaimed(
            FabricAuraSkills p, ServerCommandSource source, ServerPlayerEntity menuPlayer) {
        User user = p.getUserManager().getUser(menuPlayer.getUuid());
        if (user == null) {
            source.sendError(
                    Text.literal(
                            p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
            return 0;
        }
        user.clearInvalidItems();
        if (user.getUnclaimedItems().isEmpty()) {
            String line =
                    p.getPrefix(user.getLocale())
                            + p.getMessageProvider()
                                    .applyFormatting(
                                            p.getMsg(
                                                    CommandMessage.CLAIMITEMS_NO_ITEMS,
                                                    user.getLocale()));
            source.sendFeedback(() -> Text.literal(line), false);
            return 1;
        }
        try {
            FabricAuraSkillsMenus.openUnclaimedItems(menuPlayer, p, 0);
        } catch (Throwable t) {
            p.logger().warn("Failed to open unclaimed items menu", t);
            int touch = FabricUnclaimedItemsMerge.mergeAllInOrder(p, user, menuPlayer);
            return sendUnclaimedMergeFallbackFeedback(p, user, source, touch);
        }
        sendListMenuFeedback(
                source,
                menuPlayer,
                "auraskills.granite.menu.unclaimed_opened",
                FabricMenuKeys.UNCLAIMED_ITEMS);
        return 1;
    }

    private static int sendUnclaimedMergeFallbackFeedback(
            FabricAuraSkills p, User user, ServerCommandSource source, int touch) {
        int sizeAfter = user.getUnclaimedItems().size();
        if (sizeAfter == 0) {
            source.sendFeedback(
                    () -> FabricGraniteText.key("auraskills.granite.command.claimitems.all_claimed"),
                    false);
            return 1;
        }
        if (touch > 0) {
            source.sendFeedback(
                    () -> FabricGraniteText.key(
                            "auraskills.granite.command.claimitems.partial", sizeAfter, touch),
                    false);
            return 1;
        }
        source.sendFeedback(
                () -> Text.literal(
                        p.getMessageProvider()
                                .applyFormatting(
                                        p.getMsg(
                                                MenuMessage.INVENTORY_FULL, user.getLocale()))),
                false);
        return 0;
    }

    private static int runOpenmenuSourcesInGame(
            FabricAuraSkills p,
            ServerCommandSource feedbackSource,
            ServerPlayerEntity menuRecipient,
            String skillRaw,
            FabricSourceSortType sort,
            int returnToSkillsListPage,
            boolean isSelfPath,
            boolean sendSelfLineOnOpen) {
        Skill skill = FabricCommandSkillParser.parse(p, skillRaw);
        if (skill == null) {
            feedbackSource.sendError(
                    FabricGraniteText.key(
                            "auraskills.granite.command.openmenu.unknown_skill", skillRaw));
            return 0;
        }
        User menuUser = p.getUserManager().getUser(menuRecipient.getUuid());
        Locale lo = menuUser != null ? menuUser.getLocale() : p.getDefaultLanguage();
        LoadedSkill loaded = p.getSkillManager().getSkill(skill);
        List<XpSource> list = FabricSourcesListSupport.buildSorted(p, loaded, sort, lo);
        if (list.isEmpty()) {
            feedbackSource.sendFeedback(
                    () -> FabricGraniteText.key(
                            "auraskills.granite.command.sources.empty",
                            skill.getDisplayName(lo, false)),
                    false);
            return 1;
        }
        boolean opened =
                FabricSourcesOpenSupport.tryOpenPlayerGuiWithChatFallback(
                        p, feedbackSource, menuRecipient, lo, skill, list, sort, returnToSkillsListPage);
        if (isSelfPath && sendSelfLineOnOpen && opened) {
            feedbackSource.sendFeedback(
                    () -> FabricGraniteText.key("auraskills.granite.menu.sources_opened"), true);
        } else if (!isSelfPath && opened) {
            sendContextMenuForTarget(feedbackSource, menuRecipient, FabricMenuKeys.SOURCES, skillRaw);
        }
        return 1;
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
            source.sendFeedback(() -> FabricGraniteText.key(selfKey), true);
        } else {
            source.sendFeedback(
                    () -> FabricGraniteText.key(
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
                        () -> FabricGraniteText.key("auraskills.granite.menu.level_progression_opened"),
                        true);
            } else if (FabricMenuKeys.STAT_INFO.equals(menuId)) {
                source.sendFeedback(
                        () -> FabricGraniteText.key("auraskills.granite.menu.stat_info_opened"), true);
            } else if (FabricMenuKeys.SOURCES.equals(menuId)) {
                source.sendFeedback(
                        () -> FabricGraniteText.key("auraskills.granite.menu.sources_opened"), true);
            }
        } else {
            source.sendFeedback(
                    () -> FabricGraniteText.key(
                            "auraskills.granite.command.openmenu.opened_for_context",
                            menuId,
                            contextKeyRaw,
                            target.getName().getString()),
                    true);
        }
    }
}
