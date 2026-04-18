package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricFishingCatchHolder;
import dev.aurelium.auraskills.fabric.FabricFishingXpHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin {

    @ModifyArgs(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ItemEntity;<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V",
                    ordinal = 0
            )
    )
    private void auraskills$captureFirstItemEntityStack5(Args args) {
        FabricFishingCatchHolder.set(args.get(4));
    }

    @ModifyArgs(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ItemEntity;<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;DDD)V",
                    ordinal = 0
            )
    )
    private void auraskills$captureFirstItemEntityStack8(Args args) {
        FabricFishingCatchHolder.set(args.get(4));
    }

    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;increaseStat(Lnet/minecraft/stat/Stat;I)V"
            )
    )
    private void auraskills$afterFishStat(PlayerEntity player, Stat<?> stat, int amount) {
        player.increaseStat(stat, amount);
        if (amount <= 0 || !stat.equals(Stats.CUSTOM.getOrCreateStat(Stats.FISH_CAUGHT))) {
            return;
        }
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }
        FabricFishingXpHandler.onFishCaughtStat(FabricAuraSkills.getInstance(), (FishingBobberEntity) (Object) this, serverPlayer);
    }

    @Inject(method = "use", at = @At("RETURN"))
    private void auraskills$clearPendingCatch(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        FabricFishingCatchHolder.clear();
    }
}
