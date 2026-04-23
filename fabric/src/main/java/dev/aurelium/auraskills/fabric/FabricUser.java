package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.item.FabricUserEquipment;
import dev.aurelium.auraskills.fabric.text.FabricTextAdapter;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

public final class FabricUser extends User {

    private final FabricAuraSkills plugin;
    private final @Nullable ServerPlayerEntity player;
    private final FabricUserEquipment fabricEquipment = FabricUserEquipment.empty();

    public FabricUser(UUID uuid, @Nullable ServerPlayerEntity player, FabricAuraSkills plugin) {
        super(uuid, plugin);
        this.player = player;
        this.plugin = plugin;
    }

    public FabricUserEquipment getFabricEquipment() {
        return fabricEquipment;
    }

    public static @Nullable ServerPlayerEntity getPlayer(User user) {
        if (user instanceof FabricUser fu) {
            return fu.player;
        }
        return null;
    }

    /**
     * Maps dimension keys to Bukkit-style world names where possible for config compatibility.
     */
    public static String worldNameCompat(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld world)) {
            return "world";
        }
        return worldNameCompat(world);
    }

    /**
     * Bukkit world folder / {@code config.yml} world name alignment for a dimension (overworld → {@code "world"}).
     */
    public static String worldNameCompat(ServerWorld world) {
        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            return "world";
        }
        return world.getRegistryKey().getValue().getPath();
    }

    @Override
    public String getUsername() {
        if (player != null) {
            return player.getName().getString();
        }
        ServerPlayerEntity online = plugin.getServer().getPlayerManager().getPlayer(uuid);
        if (online != null) {
            return online.getName().getString();
        }
        return "?";
    }

    @Override
    public void sendMessage(String message) {
        if (player != null) {
            player.sendMessage(
                    FabricTextAdapter.toNativeText(plugin.getMessageProvider().stringToComponent(message), plugin.getServer()),
                    false);
        }
    }

    @Override
    public void sendMessage(Component component) {
        if (player == null) {
            return;
        }
        if (Component.empty().equals(component)) {
            return;
        }
        player.sendMessage(FabricTextAdapter.toNativeText(component, plugin.getServer()), false);
    }

    @Override
    public double getPermissionMultiplier(@Nullable Skill skill) {
        return 0.0;
    }

    @Override
    public boolean hasSkillPermission(Skill skill) {
        ServerPlayerEntity p = resolvePlayer();
        if (p == null) {
            return true;
        }
        // Default true when unset so servers without a permission provider still gain skill XP;
        // LuckPerms (or another provider) can deny specific auraskills.skill.<name> nodes.
        return Permissions.check(p, "auraskills.skill." + skill.name().toLowerCase(Locale.ROOT), true);
    }

    @Override
    public void setCommandLocale(Locale locale) {
    }

    @Override
    public int getPermissionJobLimit() {
        return 0;
    }

    @Override
    public String getWorld() {
        if (player == null) {
            return "world";
        }
        return worldNameCompat(player);
    }

    @Override
    public boolean hasPermission(String permission) {
        ServerPlayerEntity p = resolvePlayer();
        if (p == null) {
            return false;
        }
        return Permissions.check(p, permission, false);
    }

    @Override
    public boolean canSelectJob(@NotNull Skill skill) {
        ServerPlayerEntity p = resolvePlayer();
        if (p == null) {
            return true;
        }
        String key = skill.getId().getKey();
        String id = skill.getId().toString();
        if (Permissions.check(p, "auraskills.jobs.block." + key, false)) {
            return false;
        }
        if (Permissions.check(p, "auraskills.jobs.block." + id, false)) {
            return false;
        }
        return true;
    }

    private @Nullable ServerPlayerEntity resolvePlayer() {
        if (player != null) {
            return player;
        }
        return plugin.getServer().getPlayerManager().getPlayer(uuid);
    }
}
