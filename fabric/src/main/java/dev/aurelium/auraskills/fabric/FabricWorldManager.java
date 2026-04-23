package dev.aurelium.auraskills.fabric;

import com.google.common.collect.Sets;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.config.ConfigurateLoader;
import dev.aurelium.auraskills.fabric.FabricUser;
import dev.aurelium.auraskills.common.region.WorldManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public final class FabricWorldManager implements WorldManager {

    private final AuraSkillsPlugin plugin;
    private Set<String> blockedWorlds = Set.of();
    private Set<String> disabledWorlds = Set.of();
    private Set<String> blockedCheckBlockReplaceWorlds = Set.of();

    public FabricWorldManager(AuraSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadWorlds() {
        ConfigurateLoader loader = new ConfigurateLoader(plugin, TypeSerializerCollection.builder().build());
        try {
            ConfigurationNode config = loader.loadUserFile("config.yml");
            blockedWorlds = Sets.newConcurrentHashSet();
            disabledWorlds = Sets.newConcurrentHashSet();
            int count = 0;
            for (String w : config.node("blocked_worlds").getList(String.class, new ArrayList<>())) {
                blockedWorlds.add(w);
                count++;
            }
            for (String w : config.node("disabled_worlds").getList(String.class, new ArrayList<>())) {
                disabledWorlds.add(w);
                count++;
            }
            for (String w : config.node("check_block_replace", "blocked_worlds").getList(String.class, new ArrayList<>())) {
                blockedCheckBlockReplaceWorlds.add(w);
                count++;
            }
            plugin.logger().info("Loaded " + count + " blocked/disabled world entries (Fabric maps overworld to \"world\" and other dimensions by registry path).");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isBlockedWorld(String worldName) {
        return blockedWorlds.contains(worldName);
    }

    @Override
    public boolean isDisabledWorld(String worldName) {
        return disabledWorlds.contains(worldName);
    }

    public boolean isCheckReplaceDisabled(String worldName) {
        return blockedCheckBlockReplaceWorlds.contains(worldName);
    }

    public boolean isCheckReplaceDisabled(ServerWorld world) {
        return isCheckReplaceDisabled(FabricUser.worldNameCompat(world));
    }
}
