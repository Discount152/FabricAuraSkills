package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.menu.FabricGraniteSkillsMenus;
import dev.granite.graniteskills.fabric.menu.FabricMenuKeys;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;

import static net.minecraft.commands.Commands.literal;

/**
 * {@code /openmenu abilities …} (**Sprint 73**). Bukkit {@link
 * dev.granite.graniteskills.bukkit.menus.AbilitiesMenu} is skill-scoped with a {@code skill} property;
 * Granite uses explicit skill args or a deterministic default (first enabled skill by id) when
 * omitted.
 */
public final class FabricOpenMenuAbilities {

    private static final String ARG_AB_TOKEN = "ab_token";
    private static final String ARG_AB_TARGET = "ab_target";
    private static final String ARG_AB_SKILL = "ab_skill";

    private FabricOpenMenuAbilities() {}

    public static LiteralArgumentBuilder<CommandSourceStack> node() {
        return literal(FabricMenuKeys.ABILITIES)
                .executes(FabricOpenMenuAbilities::executeNoArg)
                .then(
                        Commands.argument(ARG_AB_TOKEN, StringArgumentType.word())
                                .suggests(
                                        FabricCommandSuggestionProviders
                                                .onlinePlayersAndEnabledSkillKeys())
                                .executes(FabricOpenMenuAbilities::executeOneToken))
                .then(
                        Commands.argument(ARG_AB_TARGET, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                                .then(
                                        Commands.argument(
                                                        ARG_AB_SKILL, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .enabledSkillKeys())
                                                .executes(
                                                        FabricOpenMenuAbilities
                                                                ::executeTargetWithSkill)));
    }

    private static int executeNoArg(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            CommandSourceStack s = ctx.getSource();
                            if (!(s.getEntity() instanceof ServerPlayer self)) {
                                s.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.openmenu"
                                                        + ".abilities_console"));
                                return 0;
                            }
                            Skill def = firstEnabledSkill(p);
                            if (def == null) {
                                s.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.openmenu"
                                                        + ".abilities_no_enabled_skill"));
                                return 0;
                            }
                            FabricGraniteSkillsMenus.openAbilities(def, self, p, 0);
                            s.sendSuccess(
                                    () -> FabricGraniteText.key("graniteskills.granite.menu.abilities_opened"),
                                    true);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeOneToken(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String token = StringArgumentType.getString(ctx, ARG_AB_TOKEN);
                            CommandSourceStack s = ctx.getSource();
                            Skill sk = FabricCommandSkillParser.parse(p, token);
                            if (sk != null) {
                                if (!(s.getEntity() instanceof ServerPlayer self)) {
                                    s.sendFailure(
                                            FabricGraniteText.key(
                                                    "graniteskills.granite.command.openmenu"
                                                            + ".abilities_console"));
                                    return 0;
                                }
                                if (!sk.isEnabled()) {
                                    s.sendFailure(
                                            FabricGraniteText.key(
                                                    "graniteskills.granite.command"
                                                            + ".skill_root.not_enabled"));
                                    return 0;
                                }
                                FabricGraniteSkillsMenus.openAbilities(sk, self, p, 0);
                                s.sendSuccess(
                                        () -> FabricGraniteText.key(
                                                "graniteskills.granite.menu.abilities_opened"),
                                        true);
                                return 1;
                            }
                            var online =
                                    FabricCommandPlayerTarget.findOnline(
                                            s.getServer(), token);
                            if (online.isEmpty()) {
                                s.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.openmenu"
                                                        + ".abilities_bad_token",
                                                token));
                                return 0;
                            }
                            ServerPlayer target = online.get();
                            Skill def = firstEnabledSkill(p);
                            if (def == null) {
                                s.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.openmenu"
                                                        + ".abilities_no_enabled_skill"));
                                return 0;
                            }
                            FabricGraniteSkillsMenus.openAbilities(def, target, p, 0);
                            sendFeedback(s, target);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeTargetWithSkill(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String tname = StringArgumentType.getString(ctx, ARG_AB_TARGET);
                            String raw = StringArgumentType.getString(ctx, ARG_AB_SKILL);
                            CommandSourceStack s = ctx.getSource();
                            var target = FabricCommandPlayerTarget.findOnline(s.getServer(), tname);
                            if (target.isEmpty()) {
                                s.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.xp.player_not_found",
                                                tname));
                                return 0;
                            }
                            Skill sk = FabricCommandSkillParser.parse(p, raw);
                            if (sk == null) {
                                s.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.openmenu.unknown_skill",
                                                raw));
                                return 0;
                            }
                            if (!sk.isEnabled()) {
                                s.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.skill_root.not_enabled"));
                                return 0;
                            }
                            FabricGraniteSkillsMenus.openAbilities(sk, target.get(), p, 0);
                            if (s.getEntity() instanceof ServerPlayer ex
                                    && ex.getUUID().equals(target.get().getUUID())) {
                                s.sendSuccess(
                                        () -> FabricGraniteText.key(
                                                "graniteskills.granite.menu.abilities_opened"),
                                        true);
                            } else {
                                s.sendSuccess(
                                        () -> FabricGraniteText.key(
                                                "graniteskills.granite.command.openmenu.opened_for_context",
                                                FabricMenuKeys.ABILITIES,
                                                sk.getId().getKey(),
                                                target.get().getName().getString()),
                                        true);
                            }
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static void sendFeedback(CommandSourceStack source, ServerPlayer target) {
        if (source.getEntity() instanceof ServerPlayer sp
                && sp.getUUID().equals(target.getUUID())) {
            source.sendSuccess(
                    () -> FabricGraniteText.key("graniteskills.granite.menu.abilities_opened"), true);
        } else {
            source.sendSuccess(
                    () -> FabricGraniteText.key(
                            "graniteskills.granite.command.openmenu.opened_for",
                            FabricMenuKeys.ABILITIES,
                            target.getName().getString()),
                    true);
        }
    }

    private static Skill firstEnabledSkill(FabricGraniteSkills p) {
        return p.getSkillManager().getEnabledSkills().stream()
                .filter(Skill::isEnabled)
                .min(Comparator.comparing(s -> s.getId().getKey().toLowerCase()))
                .orElse(null);
    }
}
