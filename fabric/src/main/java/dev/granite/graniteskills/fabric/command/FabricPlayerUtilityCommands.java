package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.level.LevelManager;
import dev.granite.graniteskills.common.message.type.CommandMessage;
import dev.granite.graniteskills.common.ui.ActionBarIdleMode;
import dev.granite.graniteskills.common.ui.ActionBarToggleSubcommand;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

import static net.minecraft.commands.Commands.literal;

/**
 * Player-facing **utility** / **settings** commands (**Sprint 48**): **multiplier** display, **action
 * bar** {@code toggle} / {@code abtoggle} (Bukkit **aliases**), and **lang**. Mirrors Bukkit
 * {@code SkillsRootCommand} behavior where possible; see {@code fabric/COMMAND_PARITY_AUDIT.md}.
 */
public final class FabricPlayerUtilityCommands {

    private static final String ARG_TARGET = "player";
    private static final String ARG_LANG = "language";
    private static final String ARG_ABTOGGLE_SUB = "sub";

    private FabricPlayerUtilityCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> multiplierRoot() {
        return literal("multiplier")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.MULTIPLIER, true))
                .executes(FabricPlayerUtilityCommands::executeMultiplierSelf)
                .then(
                        Commands.argument(ARG_TARGET, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                                .executes(
                                        FabricPlayerUtilityCommands
                                                ::executeMultiplierOther));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> toggleRoot() {
        return buildActionBarToggleLiteral("toggle");
    }

    public static LiteralArgumentBuilder<CommandSourceStack> abToggleRoot() {
        return buildActionBarToggleLiteral("abtoggle");
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildActionBarToggleLiteral(
            String name) {
        return literal(name)
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.ABTOGGLE, true))
                .executes(ctx -> executeActionBarToggle(ctx, null))
                .then(
                        Commands.argument(ARG_ABTOGGLE_SUB, StringArgumentType.word())
                                .executes(
                                        ctx ->
                                                executeActionBarToggle(
                                                        ctx,
                                                        StringArgumentType.getString(
                                                                ctx, ARG_ABTOGGLE_SUB))));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> langRoot() {
        return literal("lang")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.LANG, true))
                .then(
                        Commands.argument(ARG_LANG, StringArgumentType.word())
                                .suggests(
                                        FabricCommandSuggestionProviders
                                                .languageCodes())
                                .executes(FabricPlayerUtilityCommands::executeLang));
    }

    private static int executeMultiplierSelf(CommandContext<CommandSourceStack> ctx) {
        return run(
                ctx,
                p -> {
                    CommandSourceStack source = ctx.getSource();
                    if (!(source.getEntity() instanceof ServerPlayer self)) {
                        sendMulPlayersOnly(source, p);
                        return 0;
                    }
                    return sendMultiplierView(source, p, self, self);
                });
    }

    private static int executeMultiplierOther(CommandContext<CommandSourceStack> ctx) {
        return run(
                ctx,
                p -> {
                    String name = StringArgumentType.getString(ctx, ARG_TARGET);
                    CommandSourceStack source = ctx.getSource();
                    ServerPlayer target =
                            FabricCommandPlayerTarget.findOnline(source.getServer(), name)
                                    .orElse(null);
                    if (target == null) {
                        source.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    return sendMultiplierView(source, p, target, target);
                });
    }

    private static int sendMultiplierView(
            CommandSourceStack source,
            FabricGraniteSkills p,
            ServerPlayer userEntity,
            ServerPlayer nameSource) {
        User user = p.getUserManager().getUser(userEntity.getUUID());
        if (user == null) {
            source.sendFailure(
                    Component.literal(
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
        source.sendSuccess(() -> Component.literal(line), false);
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
                source.sendSuccess(() -> Component.literal(out), false);
            }
        }
        return 1;
    }

    private static void sendMulPlayersOnly(CommandSourceStack source, FabricGraniteSkills p) {
        source.sendFailure(
                Component.literal(
                        p.getMsg(
                                CommandMessage.MULTIPLIER_PLAYERS_ONLY,
                                p.getDefaultLanguage())));
    }

    private static int executeActionBarToggle(
            CommandContext<CommandSourceStack> ctx, @Nullable String rawSub) {
        return run(
                ctx,
                p -> {
                    CommandSourceStack source = ctx.getSource();
                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                        source.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.utility.players_only"));
                        return 0;
                    }
                    User user = p.getUserManager().getUser(player.getUUID());
                    if (user == null) {
                        source.sendFailure(
                                Component.literal(
                                        p.getMsg(
                                                CommandMessage.NO_PROFILE,
                                                p.getDefaultLanguage())));
                        return 0;
                    }
                    Locale lo = user.getLocale();
                    if (!p.configBoolean(Option.ACTION_BAR_ENABLED)) {
                        source.sendSuccess(
                                () -> Component.literal(
                                        p.getMsg(CommandMessage.TOGGLE_NOT_ENABLED, lo)),
                                false);
                        return 1;
                    }
                    ActionBarToggleSubcommand.Result sub =
                            ActionBarToggleSubcommand.parse(rawSub);
                    return switch (sub.kind()) {
                        case INVALID -> {
                            source.sendFailure(
                                    Component.literal(
                                            p.getMsg(
                                                    CommandMessage.TOGGLE_INVALID_SUBCOMMAND, lo)));
                            yield 0;
                        }
                        case STATUS -> {
                            String m =
                                    p.getMsg(CommandMessage.TOGGLE_MODE_STATUS, lo)
                                            .replace(
                                                    "{mode}",
                                                    user.getActionBarIdleMode().displayName());
                            source.sendSuccess(() -> Component.literal(m), false);
                            yield 1;
                        }
                        case TOGGLE -> {
                            ActionBarIdleMode next = user.getActionBarIdleMode().toggled();
                            user.setActionBarIdleMode(next);
                            user.setShouldSave(true);
                            String msg = p.getMsg(
                                    next == ActionBarIdleMode.OFF
                                            ? CommandMessage.TOGGLE_DISABLED
                                            : CommandMessage.TOGGLE_ENABLED,
                                    lo);
                            source.sendSuccess(() -> Component.literal(msg), false);
                            yield 1;
                        }
                        case SET_MODE -> {
                            ActionBarIdleMode m = Objects.requireNonNull(sub.mode());
                            user.setActionBarIdleMode(m);
                            user.setShouldSave(true);
                            String msg = p.getMsg(messageKeyForSet(m), lo);
                            source.sendSuccess(() -> Component.literal(msg), false);
                            yield 1;
                        }
                    };
                });
    }

    private static CommandMessage messageKeyForSet(ActionBarIdleMode mode) {
        return switch (mode) {
            case SMART -> CommandMessage.TOGGLE_MODE_SET_SMART;
            case ALWAYS -> CommandMessage.TOGGLE_MODE_SET_ALWAYS;
            case OFF -> CommandMessage.TOGGLE_MODE_SET_OFF;
        };
    }

    private static int executeLang(CommandContext<CommandSourceStack> ctx) {
        return run(
                ctx,
                p -> {
                    CommandSourceStack source = ctx.getSource();
                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                        source.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.utility.players_only"));
                        return 0;
                    }
                    String raw = StringArgumentType.getString(ctx, ARG_LANG);
                    User user = p.getUserManager().getUser(player.getUUID());
                    if (user == null) {
                        source.sendFailure(
                                Component.literal(
                                        p.getMsg(
                                                CommandMessage.NO_PROFILE,
                                                p.getDefaultLanguage())));
                        return 0;
                    }
                    Locale current = user.getLocale();
                    Locale newLocale = FabricCommandLocaleArgs.parseLanguageTag(raw);
                    if (newLocale == null) {
                        source.sendSuccess(
                                () -> Component.literal(
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
                        source.sendSuccess(() -> Component.literal(msg), false);
                    } else {
                        source.sendSuccess(
                                () -> Component.literal(
                                        p.getMsg(CommandMessage.LANG_NOT_FOUND, current)),
                                false);
                    }
                    return 1;
                });
    }

    private static int run(
            CommandContext<CommandSourceStack> ctx,
            Function<FabricGraniteSkills, Integer> run) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(run)
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }
}
