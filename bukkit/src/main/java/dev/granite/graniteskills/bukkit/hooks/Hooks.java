package dev.granite.graniteskills.bukkit.hooks;

import dev.granite.graniteskills.bukkit.hooks.mythicmobs.MythicMobsHook;
import dev.granite.graniteskills.common.hooks.Hook;
import dev.granite.graniteskills.common.hooks.HookType;

public enum Hooks implements HookType {

    DECENT_HOLOGRAMS(DecentHologramsHook.class, "DecentHolograms"),
    FANCY_HOLOGRAMS(FancyHologramsHook.class, "FancyHolograms"),
    HOLOGRAPHIC_DISPLAYS(HolographicDisplaysHook.class, "HolographicDisplays"),
    LUCK_PERMS(BukkitLuckPermsHook.class, "LuckPerms"),
    PLACEHOLDER_API(PlaceholderApiHook.class, "PlaceholderAPI", false),
    PROTOCOL_LIB(ProtocolLibHook.class, "ProtocolLib"),
    SLIMEFUN(SlimefunHook.class, "Slimefun"),
    TOWNY(TownyHook.class, "Towny"),
    VAULT(VaultHook.class, "Vault"),
    WORLD_GUARD(WorldGuardHook.class, "WorldGuard"),
    MYTHIC_MOBS(MythicMobsHook.class, "MythicMobs"),
    NEXO(NexoHook.class, "Nexo", false);

    private final Class<? extends Hook> hookClass;
    private final String pluginName;
    private final boolean requiresEnabledFirst;

    Hooks(Class<? extends Hook> hookClass, String pluginName) {
        this(hookClass, pluginName, true);
    }

    Hooks(Class<? extends Hook> hookClass, String pluginName, boolean requiresEnabledFirst) {
        this.hookClass = hookClass;
        this.pluginName = pluginName;
        this.requiresEnabledFirst = requiresEnabledFirst;
    }

    @Override
    public Class<? extends Hook> getHookClass() {
        return hookClass;
    }

    @Override
    public String getPluginName() {
        return pluginName;
    }

    @Override
    public boolean requiresEnabledFirst() {
        return requiresEnabledFirst;
    }

}
