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
     * {@link dev.aurelium.auraskills.bukkit.commands.OpenMenuCommand}). **Sprint 44+:** bounded
     * built-ins on Fabric — **skills**, **stats**, **level_progression**, **stat_info** (see
     * {@link dev.aurelium.auraskills.fabric.command.FabricOpenMenuCommands}).
     */
    public static final String OPEN_MENU = "auraskills.command.openmenu";

    /**
     * Bukkit {@code SkillsRootCommand} {@code claimitems} (see {@code plugin.yml} — default
     * <strong>true</strong> under {@code auraskills.command.user}). **Sprint 56.**
     */
    public static final String CLAIMITEMS = "auraskills.command.claimitems";

    /**
     * Bukkit {@code SkillsRootCommand} {@code sources} (default <strong>true</strong> under
     * {@code auraskills.command.user} in {@code plugin.yml}). **Sprint 57.**
     */
    public static final String SOURCES = "auraskills.command.sources";

    /**
     * Bukkit {@code SkillsRootCommand} {@code resethealth} (console uninstall helper; default
     * <strong>op</strong> in typical setups — not under {@code command.user} in
     * {@code plugin.yml}). **Sprint 57.**
     */
    public static final String RESETHEALTH = "auraskills.command.resethealth";

    /**
     * Bukkit {@code SkillsRootCommand} {@code transfer} (see {@code plugin.yml} — default
     * <strong>op</strong>). **Sprint 58.**
     */
    public static final String TRANSFER = "auraskills.command.transfer";

    /**
     * Bukkit {@code profile skills|stats} (see {@code plugin.yml} — default <strong>true</strong>).
     * **Sprint 59** — read-only {@link dev.aurelium.auraskills.common.user.UserState} inspection.
     */
    public static final String PROFILE = "auraskills.command.profile";

    /**
     * Bukkit {@code /sk top} / {@code skilltop} (see {@code plugin.yml}). **Sprint 47:** also
     * {@code /top}, {@code /skilltop}, and under {@code /auraskills} / {@code /sk|skill}.
     */
    public static final String TOP = "auraskills.command.top";

    /**
     * Bukkit {@code /sk rank} / {@code skillrank} (see {@code plugin.yml}). **Sprint 47:** also
     * {@code /rank}, {@code /skillrank}, and alias mirrors.
     */
    public static final String RANK = "auraskills.command.rank";

    /** Bukkit {@code /sk multiplier} — view XP multipliers (self or optional online target). **Sprint 48** */
    public static final String MULTIPLIER = "auraskills.command.multiplier";

    /**
     * Bukkit: {@code /sk toggle} and {@code abtoggle} share this node. **Sprint 48** — action bar
     * idle line toggle.
     */
    public static final String ABTOGGLE = "auraskills.command.abtoggle";

    /** Bukkit {@code /sk lang <code>}. **Sprint 48** */
    public static final String LANG = "auraskills.command.lang";

    /**
     * Bukkit {@code /sk updateleaderboards [samples]} — default <strong>op</strong> in reference
     * {@code plugin.yml}. **Sprint 49**
     */
    public static final String UPDATE_LEADERBOARDS = "auraskills.command.updateleaderboards";

    /**
     * Bukkit **player** {@code modifier} and {@code trait} add/remove/list (see {@code ModifierCommand} /
     * {@code TraitCommand}) — also {@code addtemp} in reference; not split on Fabric. **Sprint
     * 50** — default <strong>op</strong> ({@code false} in {@link FabricCommandRequirements}).
     */
    public static final String MODIFIER = "auraskills.command.modifier";

    /**
     * Bukkit {@code ItemCommand} {@code item modifier …} / {@code item trait …} (see reference
     * {@code plugin.yml}). **Sprint 51** — held-item stat/trait modifier admin.
     */
    public static final String ITEM_MODIFIER = "auraskills.command.item.modifier";

    /**
     * Bukkit {@code ItemCommand} {@code item ignore add|remove} (mana-ability “ignore hold” on main-hand
     * item). **Sprint 52** — see {@code plugin.yml} {@code auraskills.command.item.ignore}.
     */
    public static final String ITEM_IGNORE = "auraskills.command.item.ignore";

    /**
     * Bukkit {@code ItemCommand} {@code item requirement …}. **Sprint 53** — see {@code
     * plugin.yml} {@code auraskills.command.item.requirement}.
     */
    public static final String ITEM_REQUIREMENT = "auraskills.command.item.requirement";

    /**
     * Bukkit {@code ItemCommand} {@code item multiplier …}. **Sprint 53** — see {@code plugin.yml}
     * {@code auraskills.command.item.multiplier}.
     */
    public static final String ITEM_MULTIPLIER = "auraskills.command.item.multiplier";

    /**
     * Bukkit {@code ItemCommand} {@code item register|unregister …} (see {@code
     * plugin.yml}). **Sprint 55.**
     */
    public static final String ITEM_REGISTER = "auraskills.command.item.register";

    /**
     * Bukkit {@code ItemCommand} {@code item give …} (see {@code plugin.yml}). **Sprint 55.**
     */
    public static final String ITEM_GIVE = "auraskills.command.item.give";

    /**
     * Bukkit {@code ArmorCommand} {@code armor modifier …} / {@code armor trait …}. **Sprint 51**
     */
    public static final String ARMOR_MODIFIER = "auraskills.command.armor.modifier";

    /**
     * Bukkit {@code ArmorCommand} {@code armor requirement …}. **Sprint 54** — see {@code
     * plugin.yml} {@code auraskills.command.armor.requirement}.
     */
    public static final String ARMOR_REQUIREMENT = "auraskills.command.armor.requirement";

    /**
     * Bukkit {@code ArmorCommand} {@code armor multiplier …}. **Sprint 54** — see {@code
     * plugin.yml} {@code auraskills.command.armor.multiplier}.
     */
    public static final String ARMOR_MULTIPLIER = "auraskills.command.armor.multiplier";

    private FabricCommandPermissions() {}
}
