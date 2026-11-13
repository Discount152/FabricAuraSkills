package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.item.FabricUserEquipment;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

public final class FabricUser extends User {

    private final FabricGraniteSkills plugin;
    private final @Nullable ServerPlayer player;
    private final FabricUserEquipment fabricEquipment = FabricUserEquipment.empty();

    public FabricUser(UUID uuid, @Nullable ServerPlayer player, FabricGraniteSkills plugin) {
        super(uuid, plugin);
        this.player = player;
        this.plugin = plugin;
    }

    public FabricUserEquipment getFabricEquipment() {
        return fabricEquipment;
    }

    public static @Nullable ServerPlayer getPlayer(User user) {
        if (user instanceof FabricUser fu) {
            return fu.player;
        }
        return null;
    }

    /**
     * Maps dimension keys to Bukkit-style world names where possible for config compatibility.
     */
    public static String worldNameCompat(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel world)) {
            return "world";
        }
        if (world.dimension().equals(Level.OVERWORLD)) {
            return "world";
        }
        return world.dimension().identifier().getPath();
    }

    @Override
    public String getUsername() {
        if (player != null) {
            return player.getName().getString();
        }
        ServerPlayer online = plugin.getServer().getPlayerList().getPlayer(uuid);
        if (online != null) {
            return online.getName().getString();
        }
        return "?";
    }

    @Override
    public void sendMessage(String message) {
        if (player != null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
        }
    }

    @Override
    public void sendMessage(Component component) {
        if (player == null) {
            return;
        }
        if (plugin.getMessageProvider().componentToString(component).isEmpty()) {
            return;
        }
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(plugin.getMessageProvider().componentToString(component)));
    }

    @Override
    public double getPermissionMultiplier(@Nullable Skill skill) {
        return 0.0;
    }

    @Override
    public boolean hasSkillPermission(Skill skill) {
        ServerPlayer p = resolvePlayer();
        if (p == null) {
            return true;
        }
        // Default true when unset so servers without a permission provider still gain skill XP;
        // LuckPerms (or another provider) can deny specific graniteskills.skill.<name> nodes.
        return Permissions.check(p, "graniteskills.skill." + skill.name().toLowerCase(Locale.ROOT), true);
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
        ServerPlayer p = resolvePlayer();
        if (p == null) {
            return false;
        }
        return Permissions.check(p, permission, false);
    }

    @Override
    public boolean canSelectJob(@NotNull Skill skill) {
        ServerPlayer p = resolvePlayer();
        if (p == null) {
            return true;
        }
        String key = skill.getId().getKey();
        String id = skill.getId().toString();
        if (Permissions.check(p, "graniteskills.jobs.block." + key, false)) {
            return false;
        }
        if (Permissions.check(p, "graniteskills.jobs.block." + id, false)) {
            return false;
        }
        return true;
    }

    private @Nullable ServerPlayer resolvePlayer() {
        if (player != null) {
            return player;
        }
        return plugin.getServer().getPlayerList().getPlayer(uuid);
    }
}
