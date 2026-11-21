package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.api.item.ModifierType;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricUser;
import dev.granite.graniteskills.fabric.item.FabricSkillsItemReader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Blocks inventory-click armor equip when requirements fail (does not call {@code setItemSlot}).
 */
@Mixin(ArmorSlot.class)
public abstract class ArmorSlotRequirementMixin {

    @Shadow
    private LivingEntity owner;

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void graniteskills$blockArmorSlot(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        if (!(owner instanceof ServerPlayer serverPlayer)) {
            return;
        }
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            return;
        }
        if (plugin.getWorldManager().isDisabledWorld(FabricUser.worldNameCompat(serverPlayer))) {
            return;
        }
        if (!plugin.configBoolean(Option.REQUIREMENT_ENABLED)
                || !plugin.configBoolean(Option.REQUIREMENT_ARMOR_PREVENT_ARMOR_EQUIP)) {
            return;
        }
        if (!FabricSkillsItemReader.meetsRequirements(plugin, serverPlayer, stack, ModifierType.ARMOR)) {
            cir.setReturnValue(false);
        }
    }
}
