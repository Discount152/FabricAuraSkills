package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricFishingBobberUseContext;
import dev.aurelium.auraskills.fabric.FabricFishingCatchHolder;
import dev.aurelium.auraskills.fabric.FabricFishingXpHandler;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin {

    @Inject(method = "use", at = @At("HEAD"))
    private void auraskills$useHead(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        FabricFishingBobberUseContext.enter((FishingBobberEntity) (Object) this);
        FabricFishingXpHandler.onBobberUseHead((FishingBobberEntity) (Object) this);
    }

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

    @Inject(method = "use", at = @At("RETURN"))
    private void auraskills$bobberUseReturn(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        FabricFishingXpHandler.onBobberUseReturn((FishingBobberEntity) (Object) this);
        FabricFishingBobberUseContext.leave();
        FabricFishingCatchHolder.clear();
    }
}
