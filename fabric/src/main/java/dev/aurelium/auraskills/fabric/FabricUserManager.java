package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.ref.PlayerRef;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.user.UserManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class FabricUserManager extends UserManager {

    private final FabricAuraSkills plugin;

    public FabricUserManager(FabricAuraSkills plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public @NotNull User getUser(ServerPlayer player) {
        User existing = playerDataMap.get(player.getUUID());
        if (existing != null) {
            return existing;
        }
        return createNewUser(player.getUUID(), new FabricPlayerRef(player));
    }

    @Override
    public User instantiateUser(UUID uuid, PlayerRef ref) {
        ServerPlayer p = null;
        if (ref instanceof FabricPlayerRef fpr) {
            p = fpr.player();
        } else if (ref != null && ref.get() instanceof ServerPlayer sp) {
            p = sp;
        }
        return new FabricUser(uuid, p, plugin);
    }

    @Override
    public List<User> getOnlineUsers() {
        List<User> list = new ArrayList<>();
        MinecraftServer server = plugin.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            User u = playerDataMap.get(player.getUUID());
            if (u != null) {
                list.add(u);
            }
        }
        return list;
    }
}
