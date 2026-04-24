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
     * Self mana display and (future) admin mana subcommands — see Bukkit
     * {@code ManaCommand} / {@code common.commands.ManaCommand}.
     */
    public static final String MANA = "auraskills.command.mana";

    private FabricCommandPermissions() {}
}
