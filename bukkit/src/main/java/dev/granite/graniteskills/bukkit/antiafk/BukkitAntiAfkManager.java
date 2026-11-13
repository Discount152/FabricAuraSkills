package dev.granite.graniteskills.bukkit.antiafk;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.antiafk.AntiAfkManager;
import dev.granite.graniteskills.common.antiafk.Check;
import dev.granite.graniteskills.common.antiafk.CheckType;
import dev.granite.graniteskills.common.antiafk.LogLocation;
import dev.granite.graniteskills.common.ref.PlayerRef;
import dev.granite.graniteskills.common.region.BlockPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;

import static dev.granite.graniteskills.bukkit.ref.BukkitPlayerRef.unwrap;

public class BukkitAntiAfkManager extends AntiAfkManager {

    private final GraniteSkills plugin;

    public BukkitAntiAfkManager(GraniteSkills plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void registerCheckEvents(Check check) {
        if (check instanceof Listener listener) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    @Override
    public void unregisterCheckEvents(Check check) {
        if (check instanceof Listener listener) {
            HandlerList.unregisterAll(listener);
        }
    }

    @Override
    public LogLocation getLogLocation(PlayerRef ref) {
        Player player = unwrap(ref);

        Location loc = player.getLocation();
        BlockPosition coords = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        @Nullable World world = loc.getWorld();
        String worldName = world != null ? world.getName() : "";

        return new LogLocation(coords, worldName);
    }

    @Override
    protected Constructor<?> getCheckConstructor(Class<? extends Check> checkClass) throws NoSuchMethodException {
        return checkClass.getDeclaredConstructor(BukkitCheckType.class, BukkitAntiAfkManager.class);
    }

    @Override
    public CheckType[] getCheckTypes() {
        return BukkitCheckType.values();
    }

}
