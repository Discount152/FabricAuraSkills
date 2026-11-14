package dev.granite.graniteskills.fabric.command;

/**
 * {@code graniteskills.command.*} node strings aligned with the Bukkit reference
 * (ACF {@code @CommandPermission}) and future Brigadier subcommands. **Sprint
 * 39:** shared constants so new command batches do not re-string permission
 * names; defaults match typical LuckPerms / fabric-permissions-api setup.
 * <p>
 * Final <strong>Granite</strong> public command names are still <strong>not
 * frozen</strong> — this file is about <strong>permission</strong> stability,
 * not user-facing command roots/aliases.
 */
public final class FabricCommandPermissions {

    public static final String VERSION = "graniteskills.command.version";
    public static final String SKILLS = "graniteskills.command.skills";
    public static final String STATS = "graniteskills.command.stats";
    public static final String RELOAD = "graniteskills.command.reload";
    public static final String SAVE = "graniteskills.command.save";

    /**
     * Granite: {@code templates export} / {@code templates status} / overwrite flow (**Sprint 108–110**). Default
     * <strong>deny</strong>; console and RCON allowed without explicit grant via {@link
     * FabricCommandRequirements#permissionOrNonPlayerSource}. Overwrite <strong>confirm</strong> is dedicated
     * <strong>console/RCON</strong> only (**Sprint 109**).
     */
    public static final String TEMPLATES = "graniteskills.command.templates";

    /** Bukkit {@code /skills help}; Fabric currently uses {@link #VERSION} for the {@code help} subnode (see audit). */
    public static final String HELP = "graniteskills.command.help";

    /**
     * Self {@code /mana} (default). Subcommands on the same root use their own nodes (Sprint
     * 41).
     */
    public static final String MANA = "graniteskills.command.mana";

    /**
     * View another's mana, console / player — Bukkit default **op**; see reference
     * {@code ManaCommand} and {@code mana.other} in {@code plugin.yml}.
     */
    public static final String MANA_OTHER = "graniteskills.command.mana.other";

    public static final String MANA_ADD = "graniteskills.command.mana.add";

    public static final String MANA_REMOVE = "graniteskills.command.mana.remove";

    public static final String MANA_SET = "graniteskills.command.mana.set";

    /**
     * Bukkit {@code ManaAbilityCommand#onResetCooldown} — {@code manaability resetcooldown …}
     * (**Sprint 66**). Default <strong>deny</strong> (reference {@code plugin.yml} default **op**).
     */
    public static final String MANAABILITY_RESETCOOLDOWN =
            "graniteskills.command.manaability.resetcooldown";

    /**
     * Bukkit {@code AntiAfkCommand#onLogs} — {@code antiafk logs …} (**Sprint 67**). Default
     * <strong>deny</strong> (reference {@code plugin.yml} default <strong>op</strong>).
     */
    public static final String ANTIAFK_LOGS = "graniteskills.command.antiafk.logs";

    /** Read-only: {@code /xp view <player> <skill>} (Sprint 40). Default true so players can inspect online targets (lock down via LP if needed). */
    public static final String XP_VIEW = "graniteskills.command.xp.view";

    /** Bukkit: {@code xp add} (see {@code XpCommand} in the reference module). */
    public static final String XP_ADD = "graniteskills.command.xp.add";

    public static final String XP_SET = "graniteskills.command.xp.set";

    public static final String XP_REMOVE = "graniteskills.command.xp.remove";
    public static final String XP_DIAG = "graniteskills.command.xpdiag";

    /**
     * Read-only: {@code /skilllevel view <player> <skill>} — not a separate Bukkit node; use a
     * dedicated Fabric name (default <strong>allow</strong> like {@link #XP_VIEW}).
     */
    public static final String SKILL_LEVEL_VIEW = "graniteskills.command.skilllevel.view";

    /**
     * Bukkit: {@code skill setlevel} / {@code addlevel} / {@code setall} share {@code
     * graniteskills.command.skill.setlevel} — used for add/set/remove/setall (**Sprints 42–43**).
     */
    public static final String SKILL_SETLEVEL = "graniteskills.command.skill.setlevel";

    /** Bukkit: {@code skill reset} (single skill or all). **Sprint 43** */
    public static final String SKILL_RESET = "graniteskills.command.skill.reset";

    /**
     * Bukkit: {@code openmenu} under {@code skills|sk|skill} (see
     * {@link dev.granite.graniteskills.bukkit.commands.OpenMenuCommand}). **Sprint 44+:** bounded
     * built-ins on Fabric — **skills**, **stats**, **level_progression**, **stat_info** (see
     * {@link dev.granite.graniteskills.fabric.command.FabricOpenMenuCommands}).
     */
    public static final String OPEN_MENU = "graniteskills.command.openmenu";

    /**
     * Bukkit {@code SkillsRootCommand} {@code claimitems} (see {@code plugin.yml} — default
     * <strong>true</strong> under {@code graniteskills.command.user}). **Sprint 56.**
     */
    public static final String CLAIMITEMS = "graniteskills.command.claimitems";

    /**
     * Bukkit {@code SkillsRootCommand} {@code sources} (default <strong>true</strong> under
     * {@code graniteskills.command.user} in {@code plugin.yml}). **Sprint 57.**
     */
    public static final String SOURCES = "graniteskills.command.sources";

    /**
     * Bukkit {@code SkillsRootCommand} {@code resethealth} (console uninstall helper; default
     * <strong>op</strong> in typical setups — not under {@code command.user} in
     * {@code plugin.yml}). **Sprint 57.**
     */
    public static final String RESETHEALTH = "graniteskills.command.resethealth";

    /**
     * Bukkit {@code SkillsRootCommand} {@code transfer} (see {@code plugin.yml} — default
     * <strong>op</strong>). **Sprint 58.**
     */
    public static final String TRANSFER = "graniteskills.command.transfer";

    /**
     * Bukkit {@code profile skills|stats} (see {@code plugin.yml} — default <strong>true</strong>).
     * **Sprint 59** — read-only {@link dev.granite.graniteskills.common.user.UserState} inspection.
     */
    public static final String PROFILE = "graniteskills.command.profile";

    /**
     * Granite **bounded** backup admin (**create** / **list** / **info**). Bukkit:
     * {@code graniteskills.command.backup.save}. **Sprint 60** (default <strong>deny</strong> /
     * op-style).
     */
    public static final String BACKUP = "graniteskills.command.backup";

    /**
     * Bukkit {@code graniteskills.command.backup.load} — **restore** / **load** subcommands (**Sprint
     * 61**). Default <strong>deny</strong>.
     */
    public static final String BACKUP_LOAD = "graniteskills.command.backup.load";

    /**
     * Bukkit {@code PresetCommand} — {@code preset load} from {@code presets/*.zip} (see {@code
     * plugin.yml}). **Sprint 62** — list / info / guarded **load**; default <strong>deny</strong>.
     */
    public static final String PRESET = "graniteskills.command.preset";

    /**
     * Bukkit {@code StorageCommand} (see {@code plugin.yml}). **Sprint 63** — **info** / **save** /
     * **reload** explainer; **Sprint 64** — **mergeskills** / **removeallmodifiers** (guarded); default
     * <strong>deny</strong>.
     */
    public static final String STORAGE = "graniteskills.command.storage";

    /**
     * Bukkit {@code /sk top} / {@code skilltop} (see {@code plugin.yml}). **Sprint 47:** also
     * {@code /top}, {@code /skilltop}, and under {@code /graniteskills} / {@code /sk|skill}.
     */
    public static final String TOP = "graniteskills.command.top";

    /**
     * Bukkit {@code /sk rank} / {@code skillrank} (see {@code plugin.yml}). **Sprint 47:** also
     * {@code /rank}, {@code /skillrank}, and alias mirrors.
     */
    public static final String RANK = "graniteskills.command.rank";

    /** Bukkit {@code /sk multiplier} — view XP multipliers (self or optional online target). **Sprint 48** */
    public static final String MULTIPLIER = "graniteskills.command.multiplier";

    /**
     * Bukkit: {@code /sk toggle} and {@code abtoggle} share this node. **Sprint 48** — action bar
     * idle line toggle.
     */
    public static final String ABTOGGLE = "graniteskills.command.abtoggle";

    /** Bukkit {@code /sk lang <code>}. **Sprint 48** */
    public static final String LANG = "graniteskills.command.lang";

    /**
     * Bukkit {@code /sk updateleaderboards [samples]} — default <strong>op</strong> in reference
     * {@code plugin.yml}. **Sprint 49**
     */
    public static final String UPDATE_LEADERBOARDS = "graniteskills.command.updateleaderboards";

    /**
     * Bukkit **player** {@code modifier} and {@code trait} add/remove/list (see {@code ModifierCommand} /
     * {@code TraitCommand}) — also {@code addtemp} in reference; not split on Fabric. **Sprint
     * 50** — default <strong>op</strong> ({@code false} in {@link FabricCommandRequirements}).
     */
    public static final String MODIFIER = "graniteskills.command.modifier";

    /**
     * Bukkit {@code ItemCommand} {@code item modifier …} / {@code item trait …} (see reference
     * {@code plugin.yml}). **Sprint 51** — held-item stat/trait modifier admin.
     */
    public static final String ITEM_MODIFIER = "graniteskills.command.item.modifier";

    /**
     * Bukkit {@code ItemCommand} {@code item ignore add|remove} (mana-ability “ignore hold” on main-hand
     * item). **Sprint 52** — see {@code plugin.yml} {@code graniteskills.command.item.ignore}.
     */
    public static final String ITEM_IGNORE = "graniteskills.command.item.ignore";

    /**
     * Bukkit {@code ItemCommand} {@code item requirement …}. **Sprint 53** — see {@code
     * plugin.yml} {@code graniteskills.command.item.requirement}.
     */
    public static final String ITEM_REQUIREMENT = "graniteskills.command.item.requirement";

    /**
     * Bukkit {@code ItemCommand} {@code item multiplier …}. **Sprint 53** — see {@code plugin.yml}
     * {@code graniteskills.command.item.multiplier}.
     */
    public static final String ITEM_MULTIPLIER = "graniteskills.command.item.multiplier";

    /**
     * Bukkit {@code ItemCommand} {@code item register|unregister …} (see {@code
     * plugin.yml}). **Sprint 55.**
     */
    public static final String ITEM_REGISTER = "graniteskills.command.item.register";

    /**
     * Bukkit {@code ItemCommand} {@code item give …} (see {@code plugin.yml}). **Sprint 55.**
     */
    public static final String ITEM_GIVE = "graniteskills.command.item.give";

    /**
     * Bukkit {@code ArmorCommand} {@code armor modifier …} / {@code armor trait …}. **Sprint 51**
     */
    public static final String ARMOR_MODIFIER = "graniteskills.command.armor.modifier";

    /**
     * Bukkit {@code ArmorCommand} {@code armor requirement …}. **Sprint 54** — see {@code
     * plugin.yml} {@code graniteskills.command.armor.requirement}.
     */
    public static final String ARMOR_REQUIREMENT = "graniteskills.command.armor.requirement";

    /**
     * Bukkit {@code ArmorCommand} {@code armor multiplier …}. **Sprint 54** — see {@code
     * plugin.yml} {@code graniteskills.command.armor.multiplier}.
     */
    public static final String ARMOR_MULTIPLIER = "graniteskills.command.armor.multiplier";

    private FabricCommandPermissions() {}
}
