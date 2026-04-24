package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.level.LevelManager;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.ui.ActionBarType;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Player-facing **utility** / **settings** commands (**Sprint 48**): **multiplier** display, **action
 * bar** {@code toggle} / {@code abtoggle} (Bukkit **aliases**), and **lang**. Mirrors Bukkit
 * {@code SkillsRootCommand} behavior where possible; see {@code fabric/COMMAND_PARITY_AUDIT.md}.
 */
public final class FabricPlayerUtilityCommands {

    private static final String ARG_TARGET = "player";
    private static final String ARG_LANG = "language";

    private FabricPlayerUtilityCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> multiplierRoot() {
        return literal("multiplier")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.MULTIPLIER, true))
                .executes(FabricPlayerUtilityCommands::executeMultiplierSelf)
                .then(
                        CommandManager.argument(ARG_TARGET, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                                .executes(
                                        FabricPlayerUtilityCommands
                                                ::executeMultiplierOther));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> toggleRoot() {
        return literal("toggle")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.ABTOGGLE, true))
                .executes(FabricPlayerUtilityCommands::executeToggle);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> abToggleRoot() {
        return literal("abtoggle")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.ABTOGGLE, true))
                .executes(FabricPlayerUtilityCommands::executeAbToggle);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> langRoot() {
        return literal("lang")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.LANG, true))
                .then(
                        CommandManager.argument(ARG_LANG, StringArgumentType.word())
                                .suggests(
                                        FabricCommandSuggestionProviders
                                                .languageCodes())
                                .executes(FabricPlayerUtilityCommands::executeLang));
    }

    private static int executeAbToggle(CommandContext<ServerCommandSource> ctx) {
        return executeActionBarToggle(ctx);
    }

    private static int executeToggle(CommandContext<ServerCommandSource> ctx) {
        return executeActionBarToggle(ctx);
    }

    private static int executeMultiplierSelf(CommandContext<ServerCommandSource> ctx) {
        return run(
                ctx,
                p -> {
                    ServerCommandSource source = ctx.getSource();
                    if (!(source.getEntity() instanceof ServerPlayerEntity self)) {
                        sendMulPlayersOnly(source, p);
                        return 0;
                    }
                    return sendMultiplierView(source, p, self, self);
                });
    }

    private static int executeMultiplierOther(CommandContext<ServerCommandSource> ctx) {
        return run(
                ctx,
                p -> {
                    String name = StringArgumentType.getString(ctx, ARG_TARGET);
                    ServerCommandSource source = ctx.getSource();
                    ServerPlayerEntity target =
                            FabricCommandPlayerTarget.findOnline(source.getServer(), name)
                                    .orElse(null);
                    if (target == null) {
                        source.sendError(
                                Text.translatable(
                                        "auraskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    return sendMultiplierView(source, p, target, target);
                });
    }

    private static int sendMultiplierView(
            ServerCommandSource source,
            FabricAuraSkills p,
            ServerPlayerEntity userEntity,
            ServerPlayerEntity nameSource) {
        User user = p.getUserManager().getUser(userEntity.getUuid());
        if (user == null) {
            source.sendError(
                    Text.literal(
                            p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
            return 0;
        }
        LevelManager lm = p.getLevelManager();
        double global =
                lm.getPermissionMultiplier(user, null) + lm.getItemMultiplier(user, null);
        Locale lo = user.getLocale();
        String line =
                TextUtil.replace(
                        p.getMsg(CommandMessage.MULTIPLIER_LIST, lo),
                        "{player}",
                        nameSource.getName().getString(),
                        "{multiplier}",
                        NumberUtil.format2(1 + global),
                        "{percent}",
                        NumberUtil.format2(global * 100));
        source.sendFeedback(() -> Text.literal(line), false);
        for (Skill skill : p.getSkillManager().getEnabledSkills()) {
            double sm =
                    lm.getPermissionMultiplier(user, skill) + lm.getItemMultiplier(user, skill);
            if (sm != global) {
                String entry =
                        TextUtil.replace(
                                p.getMsg(CommandMessage.MULTIPLIER_SKILL_ENTRY, lo),
                                "{skill}",
                                skill.getDisplayName(lo),
                                "{multiplier}",
                                NumberUtil.format2(1 + sm),
                                "{percent}",
                                NumberUtil.format2(sm * 100));
                String out = entry;
                source.sendFeedback(() -> Text.literal(out), false);
            }
        }
        return 1;
    }

    private static void sendMulPlayersOnly(ServerCommandSource source, FabricAuraSkills p) {
        source.sendError(
                Text.literal(
                        p.getMsg(
                                CommandMessage.MULTIPLIER_PLAYERS_ONLY,
                                p.getDefaultLanguage())));
    }

    private static int executeActionBarToggle(CommandContext<ServerCommandSource> ctx) {
        return run(
                ctx,
                p -> {
                    ServerCommandSource source = ctx.getSource();
                    if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
                        source.sendError(
                                Text.translatable(
                                        "auraskills.granite.command.utility.players_only"));
                        return 0;
                    }
                    User user = p.getUserManager().getUser(player.getUuid());
                    if (user == null) {
                        source.sendError(
                                Text.literal(
                                        p.getMsg(
                                                CommandMessage.NO_PROFILE,
                                                p.getDefaultLanguage())));
                        return 0;
                    }
                    Locale lo = user.getLocale();
                    if (p.configBoolean(Option.ACTION_BAR_ENABLED)) {
                        if (!user.isActionBarEnabled(ActionBarType.IDLE)) {
                            user.setActionBarSetting(ActionBarType.IDLE, true);
                            user.setShouldSave(true);
                            source.sendFeedback(
                                    () -> Text.literal(
                                            p.getMsg(CommandMessage.TOGGLE_ENABLED, lo)),
                                    false);
                        } else {
                            user.setActionBarSetting(ActionBarType.IDLE, false);
                            user.setShouldSave(true);
                            source.sendFeedback(
                                    () -> Text.literal(
                                            p.getMsg(CommandMessage.TOGGLE_DISABLED, lo)),
                                    false);
                        }
                    } else {
                        source.sendFeedback(
                                () -> Text.literal(
                                        p.getMsg(CommandMessage.TOGGLE_NOT_ENABLED, lo)),
                                false);
                    }
                    return 1;
                });
    }

    private static int executeLang(CommandContext<ServerCommandSource> ctx) {
        return run(
                ctx,
                p -> {
                    ServerCommandSource source = ctx.getSource();
                    if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
                        source.sendError(
                                Text.translatable(
                                        "auraskills.granite.command.utility.players_only"));
                        return 0;
                    }
                    String raw = StringArgumentType.getString(ctx, ARG_LANG);
                    User user = p.getUserManager().getUser(player.getUuid());
                    if (user == null) {
                        source.sendError(
                                Text.literal(
                                        p.getMsg(
                                                CommandMessage.NO_PROFILE,
                                                p.getDefaultLanguage())));
                        return 0;
                    }
                    Locale current = user.getLocale();
                    Locale newLocale = FabricCommandLocaleArgs.parseLanguageTag(raw);
                    if (newLocale == null) {
                        source.sendFeedback(
                                () -> Text.literal(
                                        p.getMsg(CommandMessage.LANG_NOT_FOUND, current)),
                                false);
                        return 0;
                    }
                    if (p.getMessageProvider().hasLocale(newLocale)) {
                        user.setLocale(newLocale);
                        user.setShouldSave(true);
                        String code = p.getMessageProvider().getLanguageCode(newLocale);
                        String msg =
                                p.getMsg(CommandMessage.LANG_SET, newLocale)
                                        .replace("{lang}", code);
                        source.sendFeedback(() -> Text.literal(msg), false);
                    } else {
                        source.sendFeedback(
                                () -> Text.literal(
                                        p.getMsg(CommandMessage.LANG_NOT_FOUND, current)),
                                false);
                    }
                    return 1;
                });
    }

    private static int run(
            CommandContext<ServerCommandSource> ctx,
            Function<FabricAuraSkills, Integer> run) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(run)
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }
}
