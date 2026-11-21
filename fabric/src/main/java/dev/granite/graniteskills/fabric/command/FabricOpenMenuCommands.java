package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.common.message.type.CommandMessage;
import dev.granite.graniteskills.common.message.type.MenuMessage;
import dev.granite.graniteskills.common.skill.LoadedSkill;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.item.FabricUnclaimedItemsMerge;
import dev.granite.graniteskills.fabric.menu.FabricGraniteSkillsMenus;
import dev.granite.graniteskills.fabric.menu.FabricMenuKeys;
import dev.granite.graniteskills.fabric.menu.FabricMenuUserReadiness;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static net.minecraft.commands.Commands.literal;

/**
 * Bounded **openmenu** for built-in registry menus (Sprint 44+), Bukkit
 * {@link dev.granite.graniteskills.bukkit.commands.OpenMenuCommand}-style. Reuse the same {@code
 * literal("openmenu")…} for {@code /openmenu}, {@code /graniteskills openmenu}, and {@code /sk
 * openmenu} / {@code /skill openmenu}. **Sprint 45:** {@code level_progression} and {@code
 * stat_info} with typed **skill** / **stat** args — routes through
 * {@link FabricGraniteSkillsMenus#openLevelProgression} / {@link FabricGraniteSkillsMenus#openStatInfo}
 * and {@link dev.granite.graniteskills.fabric.menu.FabricMenuContext}. **Sprint 70:** {@code sources}
 * with skill + optional sort → {@link FabricGraniteSkillsMenus#openSources} (not registry-loaded).
 * **Sprint 71:** {@code unclaimed_items} (self or online target) → {@link
 * FabricGraniteSkillsMenus#openUnclaimedItems}. **Sprint 72:** {@code leaderboard} →
 * {@link FabricOpenMenuLeaderboard} / {@link FabricGraniteSkillsMenus#openLeaderboard} (not
 * registry-loaded). **Sprint 73:** {@code abilities} → {@link FabricOpenMenuAbilities} /
 * {@link FabricGraniteSkillsMenus#openAbilities} (not registry-loaded).
 */
public final class FabricOpenMenuCommands {

    private static final String ARG_TARGET = "target";
    private static final String ARG_SKILL = "skill";
    private static final String ARG_STAT = "stat";
    private static final String ARG_SORT = "sort";

    private FabricOpenMenuCommands() {}

    /** Opening a menu on another player's screen requires the openmenu node or op level 2. */
    private static boolean lacksOpenOthers(CommandSourceStack source, ServerPlayer target) {
        if (source.getEntity() instanceof ServerPlayer self && self.getUUID().equals(target.getUUID())) {
            return false;
        }
        if (FabricCommandRequirements.permissionOrOpLevel(FabricCommandPermissions.OPEN_MENU, 2).test(source)) {
            return false;
        }
        source.sendFailure(FabricGraniteText.key("graniteskills.granite.command.permission_denied"));
        return true;
    }

    /**
     * Subcommand {@code openmenu …}: attach with {@code .then(…)} under a parent
     * root.
     */
    public static LiteralArgumentBuilder<CommandSourceStack> openMenuSubcommand() {
        return literal("openmenu")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.OPEN_MENU, true))
                .then(
                        literal(FabricMenuKeys.SKILLS)
                                .executes(FabricOpenMenuCommands::executeSkillsSelf)
                                .then(
                                        Commands.argument(
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
                                        Commands.argument(
                                                        ARG_TARGET, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .onlinePlayers())
                                                .executes(
                                                        FabricOpenMenuCommands::executeStatsTarget)))
                .then(
                        literal(FabricMenuKeys.LEVEL_PROGRESSION)
                                .then(
                                        Commands.argument(
                                                        ARG_SKILL, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .enabledSkillKeys())
                                                .executes(
                                                        FabricOpenMenuCommands
                                                                ::executeLevelProgressionSelf))
                                .then(
                                        Commands.argument(
                                                        ARG_TARGET, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .onlinePlayers())
                                                .then(
                                                        Commands.argument(
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
                                        Commands.argument(
                                                        ARG_STAT, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .enabledStatKeys())
                                                .executes(
                                                        FabricOpenMenuCommands::executeStatInfoSelf))
                                .then(
                                        Commands.argument(
                                                        ARG_TARGET, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .onlinePlayers())
                                                .then(
                                                        Commands.argument(
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
                                        Commands.argument(
                                                        ARG_SKILL, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .enabledSkillKeys())
                                                .executes(
                                                        FabricOpenMenuCommands::executeSourcesSelf)
                                                .then(
                                                        Commands.argument(
                                                                        ARG_SORT,
                                                                        StringArgumentType.word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .sourceSortTypes())
                                                                .executes(
                                                                        FabricOpenMenuCommands
                                                                                ::executeSourcesSelfWithSortArg)))
                                .then(
                                        Commands.argument(
                                                        ARG_TARGET, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .onlinePlayers())
                                                .then(
                                                        Commands.argument(
                                                                        ARG_SKILL,
                                                                        StringArgumentType.word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .enabledSkillKeys())
                                                                .executes(
                                                                        FabricOpenMenuCommands
                                                                                ::executeSourcesForTarget)
                                                                .then(
                                                                        Commands.argument(
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
                                        Commands.argument(
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
    public static LiteralArgumentBuilder<CommandSourceStack> openMenuRoot() {
        return openMenuSubcommand();
    }

    private static int executeSkillsSelf(CommandContext<CommandSourceStack> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    CommandSourceStack s = ctx.getSource();
                    if (!(s.getEntity() instanceof ServerPlayer self)) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.openmenu.console_specify"));
                        return 0;
                    }
                    if (FabricMenuUserReadiness.loadedUserOrWarn(p, self) == null) {
                        return 1;
                    }
                    FabricGraniteSkillsMenus.open(FabricMenuKeys.SKILLS, self, p);
                    sendListMenuFeedback(
                            s, self, "graniteskills.granite.menu.skills_opened", FabricMenuKeys.SKILLS);
                    return 1;
                });
    }

    private static int executeSkillsTarget(CommandContext<CommandSourceStack> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    String name = StringArgumentType.getString(ctx, ARG_TARGET);
                    CommandSourceStack s = ctx.getSource();
                    ServerPlayer target =
                            FabricCommandPlayerTarget.findOnline(s.getServer(), name)
                                    .orElse(null);
                    if (target == null) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    if (lacksOpenOthers(s, target)) {
                        return 0;
                    }
                    if (FabricMenuUserReadiness.loadedUserOrWarn(p, target) == null) {
                        return 1;
                    }
                    FabricGraniteSkillsMenus.open(FabricMenuKeys.SKILLS, target, p);
                    sendListMenuFeedback(
                            s, target, "graniteskills.granite.menu.skills_opened", FabricMenuKeys.SKILLS);
                    return 1;
                });
    }

    private static int executeStatsSelf(CommandContext<CommandSourceStack> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    CommandSourceStack s = ctx.getSource();
                    if (!(s.getEntity() instanceof ServerPlayer self)) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.openmenu.console_specify"));
                        return 0;
                    }
                    if (FabricMenuUserReadiness.loadedUserOrWarn(p, self) == null) {
                        return 1;
                    }
                    FabricGraniteSkillsMenus.open(FabricMenuKeys.STATS, self, p);
                    sendListMenuFeedback(
                            s, self, "graniteskills.granite.menu.stats_opened", FabricMenuKeys.STATS);
                    return 1;
                });
    }

    private static int executeStatsTarget(CommandContext<CommandSourceStack> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    String name = StringArgumentType.getString(ctx, ARG_TARGET);
                    CommandSourceStack s = ctx.getSource();
                    ServerPlayer target =
                            FabricCommandPlayerTarget.findOnline(s.getServer(), name)
                                    .orElse(null);
                    if (target == null) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    if (lacksOpenOthers(s, target)) {
                        return 0;
                    }
                    if (FabricMenuUserReadiness.loadedUserOrWarn(p, target) == null) {
                        return 1;
                    }
                    FabricGraniteSkillsMenus.open(FabricMenuKeys.STATS, target, p);
                    sendListMenuFeedback(
                            s, target, "graniteskills.granite.menu.stats_opened", FabricMenuKeys.STATS);
                    return 1;
                });
    }

    private static int executeLevelProgressionSelf(CommandContext<CommandSourceStack> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    CommandSourceStack s = ctx.getSource();
                    if (!(s.getEntity() instanceof ServerPlayer self)) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.openmenu.context.console_specify"));
                        return 0;
                    }
                    String raw = StringArgumentType.getString(ctx, ARG_SKILL);
                    Skill skill = FabricCommandSkillParser.parse(p, raw);
                    if (skill == null) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.openmenu.unknown_skill", raw));
                        return 0;
                    }
                    FabricGraniteSkillsMenus.openLevelProgression(skill, self, p);
                    s.sendSuccess(
                            () -> FabricGraniteText.key("graniteskills.granite.menu.level_progression_opened"),
                            true);
                    return 1;
                });
    }

    private static int executeLevelProgressionForTarget(CommandContext<CommandSourceStack> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    String name = StringArgumentType.getString(ctx, ARG_TARGET);
                    String raw = StringArgumentType.getString(ctx, ARG_SKILL);
                    CommandSourceStack s = ctx.getSource();
                    ServerPlayer target =
                            FabricCommandPlayerTarget.findOnline(s.getServer(), name)
                                    .orElse(null);
                    if (target == null) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    if (lacksOpenOthers(s, target)) {
                        return 0;
                    }
                    Skill skill = FabricCommandSkillParser.parse(p, raw);
                    if (skill == null) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.openmenu.unknown_skill", raw));
                        return 0;
                    }
                    FabricGraniteSkillsMenus.openLevelProgression(skill, target, p);
                    sendContextMenuForTarget(s, target, FabricMenuKeys.LEVEL_PROGRESSION, raw);
                    return 1;
                });
    }

    private static int executeStatInfoSelf(CommandContext<CommandSourceStack> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    CommandSourceStack s = ctx.getSource();
                    if (!(s.getEntity() instanceof ServerPlayer self)) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.openmenu.context.console_specify"));
                        return 0;
                    }
                    String raw = StringArgumentType.getString(ctx, ARG_STAT);
                    Stat stat = FabricCommandStatParser.parse(p, raw);
                    if (stat == null) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.openmenu.unknown_stat", raw));
                        return 0;
                    }
                    FabricGraniteSkillsMenus.openStatInfo(stat, self, p);
                    s.sendSuccess(
                            () -> FabricGraniteText.key("graniteskills.granite.menu.stat_info_opened"), true);
                    return 1;
                });
    }

    private static int executeStatInfoForTarget(CommandContext<CommandSourceStack> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    String name = StringArgumentType.getString(ctx, ARG_TARGET);
                    String raw = StringArgumentType.getString(ctx, ARG_STAT);
                    CommandSourceStack s = ctx.getSource();
                    ServerPlayer target =
                            FabricCommandPlayerTarget.findOnline(s.getServer(), name)
                                    .orElse(null);
                    if (target == null) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    if (lacksOpenOthers(s, target)) {
                        return 0;
                    }
                    Stat stat = FabricCommandStatParser.parse(p, raw);
                    if (stat == null) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.openmenu.unknown_stat", raw));
                        return 0;
                    }
                    FabricGraniteSkillsMenus.openStatInfo(stat, target, p);
                    sendContextMenuForTarget(s, target, FabricMenuKeys.STAT_INFO, raw);
                    return 1;
                });
    }

    private static int executeSourcesSelf(CommandContext<CommandSourceStack> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    CommandSourceStack s = ctx.getSource();
                    if (!(s.getEntity() instanceof ServerPlayer self)) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.openmenu.context.console_specify"));
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

    private static int executeSourcesSelfWithSortArg(CommandContext<CommandSourceStack> ctx) {
        return FabricSourceSortType.fromTokenStrict(
                        StringArgumentType.getString(ctx, ARG_SORT))
                .map(
                        srt -> runWithPlugin(
                                ctx,
                                p -> {
                                    CommandSourceStack s = ctx.getSource();
                                    if (!(s.getEntity() instanceof ServerPlayer self)) {
                                        s.sendFailure(
                                                FabricGraniteText.key(
                                                        "graniteskills.granite.command.openmenu.context.console_specify"));
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
                                    .sendFailure(
                                            FabricGraniteText.key(
                                                    "graniteskills.granite.command.sources.bad_sort",
                                                    bad));
                            return 0;
                        });
    }

    private static int executeSourcesForTarget(CommandContext<CommandSourceStack> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    String name = StringArgumentType.getString(ctx, ARG_TARGET);
                    String raw = StringArgumentType.getString(ctx, ARG_SKILL);
                    CommandSourceStack s = ctx.getSource();
                    ServerPlayer target =
                            FabricCommandPlayerTarget.findOnline(s.getServer(), name)
                                    .orElse(null);
                    if (target == null) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    if (lacksOpenOthers(s, target)) {
                        return 0;
                    }
                    return runOpenmenuSourcesInGame(
                            p, s, target, raw, FabricSourceSortType.ASCENDING, 0, false, true);
                });
    }

    private static int executeSourcesForTargetWithSortArg(CommandContext<CommandSourceStack> ctx) {
        return FabricSourceSortType.fromTokenStrict(
                        StringArgumentType.getString(ctx, ARG_SORT))
                .map(
                        srt -> runWithPlugin(
                                ctx,
                                p -> {
                                    String name = StringArgumentType.getString(ctx, ARG_TARGET);
                                    String raw = StringArgumentType.getString(ctx, ARG_SKILL);
                                    CommandSourceStack s = ctx.getSource();
                                    ServerPlayer target =
                                            FabricCommandPlayerTarget.findOnline(s.getServer(), name)
                                                    .orElse(null);
                                    if (target == null) {
                                        s.sendFailure(
                                                FabricGraniteText.key(
                                                        "graniteskills.granite.command.xp.player_not_found",
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
                                    .sendFailure(
                                            FabricGraniteText.key(
                                                    "graniteskills.granite.command.sources.bad_sort",
                                                    bad));
                            return 0;
                        });
    }

    /**
     * @param sendSelfLineOnOpen when true (self path), send {@code menu.sources_opened} to the
     *     executor after a successful open
     */
    private static int executeUnclaimedSelf(CommandContext<CommandSourceStack> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    CommandSourceStack s = ctx.getSource();
                    if (!(s.getEntity() instanceof ServerPlayer self)) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.openmenu.unclaimed_console"));
                        return 0;
                    }
                    return runOpenmenuUnclaimed(p, s, self);
                });
    }

    private static int executeUnclaimedForTarget(CommandContext<CommandSourceStack> ctx) {
        return runWithPlugin(
                ctx,
                p -> {
                    String name = StringArgumentType.getString(ctx, ARG_TARGET);
                    CommandSourceStack s = ctx.getSource();
                    ServerPlayer target =
                            FabricCommandPlayerTarget.findOnline(s.getServer(), name)
                                    .orElse(null);
                    if (target == null) {
                        s.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    if (lacksOpenOthers(s, target)) {
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
            FabricGraniteSkills p, CommandSourceStack source, ServerPlayer menuPlayer) {
        User user = p.getUserManager().getUser(menuPlayer.getUUID());
        if (user == null) {
            source.sendFailure(
                    Component.literal(
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
            source.sendSuccess(() -> Component.literal(line), false);
            return 1;
        }
        try {
            FabricGraniteSkillsMenus.openUnclaimedItems(menuPlayer, p, 0);
        } catch (Throwable t) {
            p.logger().warn("Failed to open unclaimed items menu", t);
            int touch = FabricUnclaimedItemsMerge.mergeAllInOrder(p, user, menuPlayer);
            return sendUnclaimedMergeFallbackFeedback(p, user, source, touch);
        }
        sendListMenuFeedback(
                source,
                menuPlayer,
                "graniteskills.granite.menu.unclaimed_opened",
                FabricMenuKeys.UNCLAIMED_ITEMS);
        return 1;
    }

    private static int sendUnclaimedMergeFallbackFeedback(
            FabricGraniteSkills p, User user, CommandSourceStack source, int touch) {
        int sizeAfter = user.getUnclaimedItems().size();
        if (sizeAfter == 0) {
            source.sendSuccess(
                    () -> FabricGraniteText.key("graniteskills.granite.command.claimitems.all_claimed"),
                    false);
            return 1;
        }
        if (touch > 0) {
            source.sendSuccess(
                    () -> FabricGraniteText.key(
                            "graniteskills.granite.command.claimitems.partial", sizeAfter, touch),
                    false);
            return 1;
        }
        source.sendSuccess(
                () -> Component.literal(
                        p.getMessageProvider()
                                .applyFormatting(
                                        p.getMsg(
                                                MenuMessage.INVENTORY_FULL, user.getLocale()))),
                false);
        return 0;
    }

    private static int runOpenmenuSourcesInGame(
            FabricGraniteSkills p,
            CommandSourceStack feedbackSource,
            ServerPlayer menuRecipient,
            String skillRaw,
            FabricSourceSortType sort,
            int returnToSkillsListPage,
            boolean isSelfPath,
            boolean sendSelfLineOnOpen) {
        Skill skill = FabricCommandSkillParser.parse(p, skillRaw);
        if (skill == null) {
            feedbackSource.sendFailure(
                    FabricGraniteText.key(
                            "graniteskills.granite.command.openmenu.unknown_skill", skillRaw));
            return 0;
        }
        User menuUser = p.getUserManager().getUser(menuRecipient.getUUID());
        Locale lo = menuUser != null ? menuUser.getLocale() : p.getDefaultLanguage();
        LoadedSkill loaded = p.getSkillManager().getSkill(skill);
        List<XpSource> list = FabricSourcesListSupport.buildSorted(p, loaded, sort, lo);
        if (list.isEmpty()) {
            feedbackSource.sendSuccess(
                    () -> FabricGraniteText.key(
                            "graniteskills.granite.command.sources.empty",
                            skill.getDisplayName(lo, false)),
                    false);
            return 1;
        }
        boolean opened =
                FabricSourcesOpenSupport.tryOpenPlayerGuiWithChatFallback(
                        p, feedbackSource, menuRecipient, lo, skill, list, sort, returnToSkillsListPage);
        if (isSelfPath && sendSelfLineOnOpen && opened) {
            feedbackSource.sendSuccess(
                    () -> FabricGraniteText.key("graniteskills.granite.menu.sources_opened"), true);
        } else if (!isSelfPath && opened) {
            sendContextMenuForTarget(feedbackSource, menuRecipient, FabricMenuKeys.SOURCES, skillRaw);
        }
        return 1;
    }

    private static int runWithPlugin(
            CommandContext<CommandSourceStack> ctx, Function<FabricGraniteSkills, Integer> run) {
        return FabricCommandPluginState.pluginOrEmpty()
                .map(run)
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static void sendListMenuFeedback(
            CommandSourceStack source,
            ServerPlayer menuRecipient,
            String selfKey,
            String menuId) {
        if (source.getEntity() instanceof ServerPlayer srcPlayer
                && srcPlayer.getUUID().equals(menuRecipient.getUUID())) {
            source.sendSuccess(() -> FabricGraniteText.key(selfKey), true);
        } else {
            source.sendSuccess(
                    () -> FabricGraniteText.key(
                            "graniteskills.granite.command.openmenu.opened_for",
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
            CommandSourceStack source,
            ServerPlayer target,
            String menuId,
            String contextKeyRaw) {
        if (source.getEntity() instanceof ServerPlayer srcPlayer
                && srcPlayer.getUUID().equals(target.getUUID())) {
            if (FabricMenuKeys.LEVEL_PROGRESSION.equals(menuId)) {
                source.sendSuccess(
                        () -> FabricGraniteText.key("graniteskills.granite.menu.level_progression_opened"),
                        true);
            } else if (FabricMenuKeys.STAT_INFO.equals(menuId)) {
                source.sendSuccess(
                        () -> FabricGraniteText.key("graniteskills.granite.menu.stat_info_opened"), true);
            } else if (FabricMenuKeys.SOURCES.equals(menuId)) {
                source.sendSuccess(
                        () -> FabricGraniteText.key("graniteskills.granite.menu.sources_opened"), true);
            }
        } else {
            source.sendSuccess(
                    () -> FabricGraniteText.key(
                            "graniteskills.granite.command.openmenu.opened_for_context",
                            menuId,
                            contextKeyRaw,
                            target.getName().getString()),
                    true);
        }
    }
}
