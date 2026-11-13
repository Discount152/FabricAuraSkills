package dev.granite.graniteskills.bukkit.hooks;

import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.hooks.Hook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.ConfigurationNode;

public class TownyHook extends Hook {

    public TownyHook(GraniteSkillsPlugin plugin, ConfigurationNode config) {
        super(plugin, config);
    }

    public boolean canBreak(Player player, Block block) {
        try {
            return PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.DESTROY);
        } catch (Exception ignored) {
        }
        return true;
    }

    @Override
    public Class<? extends Hook> getTypeClass() {
        return TownyHook.class;
    }

}
