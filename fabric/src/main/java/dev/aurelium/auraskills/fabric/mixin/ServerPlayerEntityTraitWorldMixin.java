package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricTraitManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityTraitWorldMixin {

    @Inject(
            method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z",
            at = @At("HEAD")
    )
    private void auraskills$traitsBeforeTeleport(ServerLevel world, double x, double y, double z,
            Set<Relative> flags, float yaw, float pitch, boolean bl,
            CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        ServerLevel from = self.level() instanceof ServerLevel sw ? sw : null;
        if (from != null && from != world) {
            notifyTraits(self, from, world);
        }
    }

    @Inject(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At("HEAD")
    )
    private void auraskills$traitsBeforeTeleportTarget(TeleportTransition target, CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        ServerLevel from = self.level() instanceof ServerLevel sw ? sw : null;
        ServerLevel to = target.newLevel();
        if (from != null && from != to) {
            notifyTraits(self, from, to);
        }
    }

    private static void notifyTraits(ServerPlayer player, ServerLevel from, ServerLevel to) {
        FabricAuraSkills inst = FabricAuraSkills.getInstance();
        if (inst == null) {
            return;
        }
        FabricTraitManager tm = (FabricTraitManager) inst.getTraitManager();
        tm.onPlayerChangedServerWorld(player, from, to);
    }
}
