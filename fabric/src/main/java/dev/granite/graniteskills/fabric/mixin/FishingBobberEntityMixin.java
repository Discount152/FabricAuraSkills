package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricFishingCatchHolder;
import dev.granite.graniteskills.fabric.FabricFishingXpHandler;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.ability.FabricFishingAbilities;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FishingHook.class)
public abstract class FishingBobberEntityMixin {

    @ModifyArgs(
            method = "retrieve",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V",
                    ordinal = 0
            )
    )
    private void graniteskills$captureFirstItemEntityStack5(Args args) {
        ItemStack stack = args.get(4);
        applyLuckyCatchToCaughtStack(stack);
        FabricFishingCatchHolder.set(stack);
    }

    private void applyLuckyCatchToCaughtStack(ItemStack stack) {
        FishingHook hook = (FishingHook) (Object) this;
        if (hook.getPlayerOwner() instanceof ServerPlayer player) {
            FabricFishingAbilities.applyLuckyCatch(FabricGraniteSkills.getInstance(), player, stack);
        }
    }

    @Redirect(
            method = "retrieve",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/resources/Identifier;I)V"
            )
    )
    private void graniteskills$afterFishStat(Player player, Identifier stat, int amount) {
        player.awardStat(stat, amount);
        if (amount <= 0 || !Stats.FISH_CAUGHT.equals(stat)) {
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        FabricFishingXpHandler.onFishCaughtStat(FabricGraniteSkills.getInstance(), (FishingHook) (Object) this, serverPlayer);
    }

    @Inject(method = "retrieve", at = @At("HEAD"))
    private void graniteskills$grappler(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        FishingHook hook = (FishingHook) (Object) this;
        Entity hooked = hook.getHookedIn();
        if (hooked == null || hooked instanceof ItemEntity) {
            return;
        }
        if (hook.getPlayerOwner() instanceof ServerPlayer player) {
            FabricFishingAbilities.onGrapple(FabricGraniteSkills.getInstance(), player, hooked);
        }
    }

    @Inject(method = "retrieve", at = @At("RETURN"))
    private void graniteskills$clearPendingCatch(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        FabricFishingCatchHolder.clear();
    }
}
