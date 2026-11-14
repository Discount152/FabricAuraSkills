package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.skill.Skills;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.menu.FabricGraniteSkillsMenus;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static net.minecraft.commands.Commands.literal;

/**
 * Bukkit per-skill {@link dev.granite.graniteskills.bukkit.commands.SkillCommands} family — <strong>
 * Sprint 68</strong>. Each class is a top-level {@code @CommandAlias} (e.g. {@code /mining})
 * with a single default action: open the <strong>level progression</strong> (detail) screen for
 * that skill, matching {@code LevelProgressionOpener} on the reference. **Not** under {@code
 * /skills} — the reference registers separate roots.
 * <p>
 * Gated by {@link Option#ENABLE_SKILL_COMMANDS} and, per command, the same
 * {@link dev.granite.graniteskills.common.skill.SkillManager#loadConfigEnabledMap()} rule as
 * {@link dev.granite.graniteskills.bukkit.commands.CommandRegistrar#registerSkillCommand}. Per-skill
 * permissions follow reference {@code plugin.yml} {@code graniteskills.skill.<name>} (see {@link
 * dev.granite.graniteskills.fabric.FabricUser#hasSkillPermission(Skill)}).
 */
public final class FabricSkillShortcutCommands {

    private FabricSkillShortcutCommands() {}

    /**
     * One entry per Bukkit {@code SkillCommands.*} class; literals match {@code @CommandAlias}
     * in the reference.
     */
    public static final List<SkillRoot> ROOTS =
            List.of(
                    new SkillRoot("farming", Skills.FARMING),
                    new SkillRoot("foraging", Skills.FORAGING),
                    new SkillRoot("mining", Skills.MINING),
                    new SkillRoot("fishing", Skills.FISHING),
                    new SkillRoot("excavation", Skills.EXCAVATION),
                    new SkillRoot("archery", Skills.ARCHERY),
                    new SkillRoot("defense", Skills.DEFENSE),
                    new SkillRoot("fighting", Skills.FIGHTING),
                    new SkillRoot("endurance", Skills.ENDURANCE),
                    new SkillRoot("agility", Skills.AGILITY),
                    new SkillRoot("alchemy", Skills.ALCHEMY),
                    new SkillRoot("enchanting", Skills.ENCHANTING),
                    new SkillRoot("sorcery", Skills.SORCERY),
                    new SkillRoot("healing", Skills.HEALING),
                    new SkillRoot("forging", Skills.FORGING));

    public record SkillRoot(String literal, Skill skill) {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (SkillRoot root : ROOTS) {
            Skill sk = root.skill();
            dispatcher.register(
                    literal(root.literal())
                            .requires(s -> requireVisibleSkillRoot(s, sk))
                            .executes(ctx -> execute(ctx, sk)));
        }
    }

    /**
     * Hides a root (like the reference not registering) when the global or per-skill config gate
     * is closed. When the server is not ready, still allows the node so execute can return a
     * consistent “still starting” line.
     */
    static boolean requireVisibleSkillRoot(
            CommandSourceStack source, Skill skill) {
        FabricGraniteSkills p = FabricGraniteSkills.getInstance();
        if (p == null || !p.isRuntimeReady()) {
            return true;
        }
        if (!p.configBoolean(Option.ENABLE_SKILL_COMMANDS)) {
            return false;
        }
        Map<Skill, Boolean> map = p.getSkillManager().loadConfigEnabledMap();
        if (!map.getOrDefault(skill, false)) {
            return false;
        }
        if (source.getEntity() instanceof ServerPlayer pl) {
            return Permissions.check(
                    pl, skillPermissionKey(skill), true);
        }
        return true;
    }

    static String skillPermissionKey(Skill skill) {
        return "graniteskills.skill." + skill.name().toLowerCase(Locale.ROOT);
    }

    private static int execute(
            CommandContext<CommandSourceStack> ctx, Skill skill) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            CommandSourceStack s = ctx.getSource();
                            if (!p.configBoolean(Option.ENABLE_SKILL_COMMANDS)) {
                                s.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.skill_root"
                                                        + ".global_disabled"));
                                return 0;
                            }
                            if (!p.getSkillManager()
                                    .loadConfigEnabledMap()
                                    .getOrDefault(skill, false)) {
                                s.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.skill_root"
                                                        + ".not_enabled"));
                                return 0;
                            }
                            if (!(s.getEntity() instanceof ServerPlayer self)) {
                                s.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.utility"
                                                        + ".players_only"));
                                return 0;
                            }
                            if (!Permissions.check(
                                    self, skillPermissionKey(skill), true)) {
                                s.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command"
                                                        + ".permission_denied"));
                                return 0;
                            }
                            if (!skill.isEnabled()) {
                                s.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.skill_root"
                                                        + ".unavailable"));
                                return 0;
                            }
                            // Bukkit LevelProgressionOpener: menu only, no success chat
                            FabricGraniteSkillsMenus.openLevelProgression(skill, self, p);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }
}
