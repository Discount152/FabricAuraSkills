package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricUser;
import dev.aurelium.auraskills.fabric.item.FabricSkillsItemReader;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Blocks equipping armor when {@code requirement.armor.prevent_armor_equip} is on and the piece fails
 * {@link FabricSkillsItemReader#meetsRequirements} (mirrors Bukkit {@code RequirementListener#onEquip}).
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityArmorRequirementMixin {

    @Inject(method = "equipStack", at = @At("HEAD"), cancellable = true)
    private void auraskills$cancelArmorIfRequirementsFail(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        if (stack.isEmpty()) {
            return;
        }
        if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayerEntity player)) {
            return;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            return;
        }
        if (plugin.getWorldManager().isDisabledWorld(FabricUser.worldNameCompat(player))) {
            return;
        }
        if (!plugin.configBoolean(Option.REQUIREMENT_ENABLED)) {
            return;
        }
        if (!plugin.configBoolean(Option.REQUIREMENT_ARMOR_PREVENT_ARMOR_EQUIP)) {
            return;
        }
        if (!FabricSkillsItemReader.meetsRequirements(plugin, player, stack, ModifierType.ARMOR)) {
            ci.cancel();
        }
    }
}
