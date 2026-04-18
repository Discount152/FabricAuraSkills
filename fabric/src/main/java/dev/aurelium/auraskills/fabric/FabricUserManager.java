package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.ref.PlayerRef;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.user.UserManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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

    public @NotNull User getUser(ServerPlayerEntity player) {
        User existing = playerDataMap.get(player.getUuid());
        if (existing != null) {
            return existing;
        }
        return createNewUser(player.getUuid(), new FabricPlayerRef(player));
    }

    @Override
    public User instantiateUser(UUID uuid, PlayerRef ref) {
        ServerPlayerEntity p = null;
        if (ref instanceof FabricPlayerRef fpr) {
            p = fpr.player();
        } else if (ref != null && ref.get() instanceof ServerPlayerEntity sp) {
            p = sp;
        }
        return new FabricUser(uuid, p, plugin);
    }

    @Override
    public List<User> getOnlineUsers() {
        List<User> list = new ArrayList<>();
        MinecraftServer server = plugin.getServer();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            User u = playerDataMap.get(player.getUuid());
            if (u != null) {
                list.add(u);
            }
        }
        return list;
    }
}
