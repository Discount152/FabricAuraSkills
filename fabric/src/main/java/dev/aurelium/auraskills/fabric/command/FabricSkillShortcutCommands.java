package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.menu.FabricAuraSkillsMenus;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bukkit per-skill {@link dev.aurelium.auraskills.bukkit.commands.SkillCommands} family — <strong>
 * Sprint 68</strong>. Each class is a top-level {@code @CommandAlias} (e.g. {@code /mining})
 * with a single default action: open the <strong>level progression</strong> (detail) screen for
 * that skill, matching {@code LevelProgressionOpener} on the reference. **Not** under {@code
 * /skills} — the reference registers separate roots.
 * <p>
 * Gated by {@link Option#ENABLE_SKILL_COMMANDS} and, per command, the same
 * {@link dev.aurelium.auraskills.common.skill.SkillManager#loadConfigEnabledMap()} rule as
 * {@link dev.aurelium.auraskills.bukkit.commands.CommandRegistrar#registerSkillCommand}. Per-skill
 * permissions follow reference {@code plugin.yml} {@code auraskills.skill.<name>} (see {@link
 * dev.aurelium.auraskills.fabric.FabricUser#hasSkillPermission(Skill)}).
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

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
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
            ServerCommandSource source, Skill skill) {
        FabricAuraSkills p = FabricAuraSkills.getInstance();
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
        if (source.getEntity() instanceof ServerPlayerEntity pl) {
            return Permissions.check(
                    pl, skillPermissionKey(skill), true);
        }
        return true;
    }

    static String skillPermissionKey(Skill skill) {
        return "auraskills.skill." + skill.name().toLowerCase(Locale.ROOT);
    }

    private static int execute(
            CommandContext<ServerCommandSource> ctx, Skill skill) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource s = ctx.getSource();
                            if (!p.configBoolean(Option.ENABLE_SKILL_COMMANDS)) {
                                s.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.skill_root"
                                                        + ".global_disabled"));
                                return 0;
                            }
                            if (!p.getSkillManager()
                                    .loadConfigEnabledMap()
                                    .getOrDefault(skill, false)) {
                                s.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.skill_root"
                                                        + ".not_enabled"));
                                return 0;
                            }
                            if (!(s.getEntity() instanceof ServerPlayerEntity self)) {
                                s.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.utility"
                                                        + ".players_only"));
                                return 0;
                            }
                            if (!Permissions.check(
                                    self, skillPermissionKey(skill), true)) {
                                s.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command"
                                                        + ".permission_denied"));
                                return 0;
                            }
                            if (!skill.isEnabled()) {
                                s.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.skill_root"
                                                        + ".unavailable"));
                                return 0;
                            }
                            // Bukkit LevelProgressionOpener: menu only, no success chat
                            FabricAuraSkillsMenus.openLevelProgression(skill, self, p);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }
}
