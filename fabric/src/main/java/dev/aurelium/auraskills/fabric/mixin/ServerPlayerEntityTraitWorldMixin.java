package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricTraitManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

import net.minecraft.network.packet.s2c.play.PositionFlag;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityTraitWorldMixin {

    @Inject(method = "teleport", at = @At("HEAD"))
    private void auraskills$traitsBeforeTeleport(ServerWorld world, double x, double y, double z,
            Set<PositionFlag> flags, float yaw, float pitch, boolean bl,
            CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        ServerWorld from = self.getEntityWorld() instanceof ServerWorld sw ? sw : null;
        if (from != null && from != world) {
            notifyTraits(self, from, world);
        }
    }

    @Inject(method = "teleportTo", at = @At("HEAD"))
    private void auraskills$traitsBeforeTeleportTarget(TeleportTarget target, CallbackInfoReturnable<net.minecraft.entity.Entity> cir) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        ServerWorld from = self.getEntityWorld() instanceof ServerWorld sw ? sw : null;
        ServerWorld to = target.world();
        if (from != null && from != to) {
            notifyTraits(self, from, to);
        }
    }

    private static void notifyTraits(ServerPlayerEntity player, ServerWorld from, ServerWorld to) {
        FabricAuraSkills inst = FabricAuraSkills.getInstance();
        if (inst == null) {
            return;
        }
        FabricTraitManager tm = (FabricTraitManager) inst.getTraitManager();
        tm.onPlayerChangedServerWorld(player, from, to);
    }
}
