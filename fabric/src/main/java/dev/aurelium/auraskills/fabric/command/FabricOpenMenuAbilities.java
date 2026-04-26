package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import dev.aurelium.auraskills.fabric.menu.FabricAuraSkillsMenus;
import dev.aurelium.auraskills.fabric.menu.FabricMenuKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Comparator;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * {@code /openmenu abilities …} (**Sprint 73**). Bukkit {@link
 * dev.aurelium.auraskills.bukkit.menus.AbilitiesMenu} is skill-scoped with a {@code skill} property;
 * Granite uses explicit skill args or a deterministic default (first enabled skill by id) when
 * omitted.
 */
public final class FabricOpenMenuAbilities {

    private static final String ARG_AB_TOKEN = "ab_token";
    private static final String ARG_AB_TARGET = "ab_target";
    private static final String ARG_AB_SKILL = "ab_skill";

    private FabricOpenMenuAbilities() {}

    public static LiteralArgumentBuilder<ServerCommandSource> node() {
        return literal(FabricMenuKeys.ABILITIES)
                .executes(FabricOpenMenuAbilities::executeNoArg)
                .then(
                        CommandManager.argument(ARG_AB_TOKEN, StringArgumentType.word())
                                .suggests(
                                        FabricCommandSuggestionProviders
                                                .onlinePlayersAndEnabledSkillKeys())
                                .executes(FabricOpenMenuAbilities::executeOneToken))
                .then(
                        CommandManager.argument(ARG_AB_TARGET, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                                .then(
                                        CommandManager.argument(
                                                        ARG_AB_SKILL, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .enabledSkillKeys())
                                                .executes(
                                                        FabricOpenMenuAbilities
                                                                ::executeTargetWithSkill)));
    }

    private static int executeNoArg(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource s = ctx.getSource();
                            if (!(s.getEntity() instanceof ServerPlayerEntity self)) {
                                s.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.openmenu"
                                                        + ".abilities_console"));
                                return 0;
                            }
                            Skill def = firstEnabledSkill(p);
                            if (def == null) {
                                s.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.openmenu"
                                                        + ".abilities_no_enabled_skill"));
                                return 0;
                            }
                            FabricAuraSkillsMenus.openAbilities(def, self, p, 0);
                            s.sendFeedback(
                                    () -> FabricGraniteText.key("auraskills.granite.menu.abilities_opened"),
                                    true);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeOneToken(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String token = StringArgumentType.getString(ctx, ARG_AB_TOKEN);
                            ServerCommandSource s = ctx.getSource();
                            Skill sk = FabricCommandSkillParser.parse(p, token);
                            if (sk != null) {
                                if (!(s.getEntity() instanceof ServerPlayerEntity self)) {
                                    s.sendError(
                                            FabricGraniteText.key(
                                                    "auraskills.granite.command.openmenu"
                                                            + ".abilities_console"));
                                    return 0;
                                }
                                if (!sk.isEnabled()) {
                                    s.sendError(
                                            FabricGraniteText.key(
                                                    "auraskills.granite.command"
                                                            + ".skill_root.not_enabled"));
                                    return 0;
                                }
                                FabricAuraSkillsMenus.openAbilities(sk, self, p, 0);
                                s.sendFeedback(
                                        () -> FabricGraniteText.key(
                                                "auraskills.granite.menu.abilities_opened"),
                                        true);
                                return 1;
                            }
                            var online =
                                    FabricCommandPlayerTarget.findOnline(
                                            s.getServer(), token);
                            if (online.isEmpty()) {
                                s.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.openmenu"
                                                        + ".abilities_bad_token",
                                                token));
                                return 0;
                            }
                            ServerPlayerEntity target = online.get();
                            Skill def = firstEnabledSkill(p);
                            if (def == null) {
                                s.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.openmenu"
                                                        + ".abilities_no_enabled_skill"));
                                return 0;
                            }
                            FabricAuraSkillsMenus.openAbilities(def, target, p, 0);
                            sendFeedback(s, target);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeTargetWithSkill(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String tname = StringArgumentType.getString(ctx, ARG_AB_TARGET);
                            String raw = StringArgumentType.getString(ctx, ARG_AB_SKILL);
                            ServerCommandSource s = ctx.getSource();
                            var target = FabricCommandPlayerTarget.findOnline(s.getServer(), tname);
                            if (target.isEmpty()) {
                                s.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.xp.player_not_found",
                                                tname));
                                return 0;
                            }
                            Skill sk = FabricCommandSkillParser.parse(p, raw);
                            if (sk == null) {
                                s.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.openmenu.unknown_skill",
                                                raw));
                                return 0;
                            }
                            if (!sk.isEnabled()) {
                                s.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.skill_root.not_enabled"));
                                return 0;
                            }
                            FabricAuraSkillsMenus.openAbilities(sk, target.get(), p, 0);
                            if (s.getEntity() instanceof ServerPlayerEntity ex
                                    && ex.getUuid().equals(target.get().getUuid())) {
                                s.sendFeedback(
                                        () -> FabricGraniteText.key(
                                                "auraskills.granite.menu.abilities_opened"),
                                        true);
                            } else {
                                s.sendFeedback(
                                        () -> FabricGraniteText.key(
                                                "auraskills.granite.command.openmenu.opened_for_context",
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
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static void sendFeedback(ServerCommandSource source, ServerPlayerEntity target) {
        if (source.getEntity() instanceof ServerPlayerEntity sp
                && sp.getUuid().equals(target.getUuid())) {
            source.sendFeedback(
                    () -> FabricGraniteText.key("auraskills.granite.menu.abilities_opened"), true);
        } else {
            source.sendFeedback(
                    () -> FabricGraniteText.key(
                            "auraskills.granite.command.openmenu.opened_for",
                            FabricMenuKeys.ABILITIES,
                            target.getName().getString()),
                    true);
        }
    }

    private static Skill firstEnabledSkill(FabricAuraSkills p) {
        return p.getSkillManager().getEnabledSkills().stream()
                .filter(Skill::isEnabled)
                .min(Comparator.comparing(s -> s.getId().getKey().toLowerCase()))
                .orElse(null);
    }
}
