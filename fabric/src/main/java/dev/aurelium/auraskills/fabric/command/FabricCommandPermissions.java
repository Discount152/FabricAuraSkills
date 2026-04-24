package dev.aurelium.auraskills.fabric.command;

/**
 * {@code auraskills.command.*} node strings aligned with the Bukkit reference
 * (ACF {@code @CommandPermission}) and future Brigadier subcommands. **Sprint
 * 39:** shared constants so new command batches do not re-string permission
 * names; defaults match typical LuckPerms / fabric-permissions-api setup.
 * <p>
 * Final <strong>Granite</strong> public command names are still <strong>not
 * frozen</strong> — this file is about <strong>permission</strong> stability,
 * not user-facing command roots/aliases.
 */
public final class FabricCommandPermissions {

    public static final String VERSION = "auraskills.command.version";
    public static final String SKILLS = "auraskills.command.skills";
    public static final String STATS = "auraskills.command.stats";
    public static final String RELOAD = "auraskills.command.reload";
    public static final String SAVE = "auraskills.command.save";

    /** Bukkit {@code /skills help}; Fabric currently uses {@link #VERSION} for the {@code help} subnode (see audit). */
    public static final String HELP = "auraskills.command.help";

    /**
     * Self {@code /mana} (default). Subcommands on the same root use their own nodes (Sprint
     * 41).
     */
    public static final String MANA = "auraskills.command.mana";

    /**
     * View another's mana, console / player — Bukkit default **op**; see reference
     * {@code ManaCommand} and {@code mana.other} in {@code plugin.yml}.
     */
    public static final String MANA_OTHER = "auraskills.command.mana.other";

    public static final String MANA_ADD = "auraskills.command.mana.add";

    public static final String MANA_REMOVE = "auraskills.command.mana.remove";

    public static final String MANA_SET = "auraskills.command.mana.set";

    /** Read-only: {@code /xp view <player> <skill>} (Sprint 40). Default true so players can inspect online targets (lock down via LP if needed). */
    public static final String XP_VIEW = "auraskills.command.xp.view";

    /** Bukkit: {@code xp add} (see {@code XpCommand} in the reference module). */
    public static final String XP_ADD = "auraskills.command.xp.add";

    public static final String XP_SET = "auraskills.command.xp.set";

    public static final String XP_REMOVE = "auraskills.command.xp.remove";

    /**
     * Read-only: {@code /skilllevel view <player> <skill>} — not a separate Bukkit node; use a
     * dedicated Fabric name (default <strong>allow</strong> like {@link #XP_VIEW}).
     */
    public static final String SKILL_LEVEL_VIEW = "auraskills.command.skilllevel.view";

    /**
     * Bukkit: {@code skill setlevel} / {@code addlevel} / {@code setall} share {@code
     * auraskills.command.skill.setlevel} — used for add/set/remove/setall (**Sprints 42–43**).
     */
    public static final String SKILL_SETLEVEL = "auraskills.command.skill.setlevel";

    /** Bukkit: {@code skill reset} (single skill or all). **Sprint 43** */
    public static final String SKILL_RESET = "auraskills.command.skill.reset";

    /**
     * Bukkit: {@code openmenu} under {@code skills|sk|skill} (see
     * {@link dev.aurelium.auraskills.bukkit.commands.OpenMenuCommand}). **Sprint 44:** bounded
     * to built-in **skills** / **stats** on Fabric.
     */
    public static final String OPEN_MENU = "auraskills.command.openmenu";

    private FabricCommandPermissions() {}
}
