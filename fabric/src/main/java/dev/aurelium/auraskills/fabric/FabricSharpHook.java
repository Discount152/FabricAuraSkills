package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.mana.ManaAbilities;
import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.mana.ManaAbilityData;
import dev.aurelium.auraskills.common.message.type.ManaAbilityMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.Locale;

/**
 * Fabric port of Bukkit {@link dev.aurelium.auraskills.bukkit.skills.fishing.SharpHook}: left-click with a fishing rod
 * while a bobber is hooked into a mob to deal configured damage and consume mana/cooldown.
 */
public final class FabricSharpHook {

    private static final ManaAbility MANA = ManaAbilities.SHARP_HOOK;

    private FabricSharpHook() {
    }

    /**
     * Called from {@link dev.aurelium.auraskills.fabric.mixin.ServerPlayerEntitySharpHookMixin} when the player swings
     * the main hand (including left-click air/block with rod).
     */
    public static void onSwingMainHandRod(FabricAuraSkills plugin, ServerPlayerEntity player) {
        if (player.getEntityWorld().isClient()) {
            return;
        }
        if (isDisabled()) {
            return;
        }
        if (!player.getMainHandStack().isOf(Items.FISHING_ROD)) {
            return;
        }
        if (FabricManaAbilityItemTags.shouldIgnoreInteract(player.getMainHandStack())) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (user.getManaAbilityLevel(MANA) <= 0) {
            return;
        }
        if (failsChecks(plugin, player, user)) {
            return;
        }

        ServerWorld world = (ServerWorld) player.getEntityWorld();
        Box castProximity = player.getBoundingBox().expand(0.1);
        for (Entity nearby : world.getOtherEntities(player, castProximity)) {
            if (nearby instanceof FishingBobberEntity bobber && bobber.getPlayerOwner() == player) {
                return;
            }
        }

        Box range = player.getBoundingBox().expand(33.0);
        for (Entity entity : world.getOtherEntities(player, range)) {
            if (!(entity instanceof FishingBobberEntity bobber)) {
                continue;
            }
            if (bobber.getPlayerOwner() != player || !bobber.isAlive() || bobber.isRemoved()) {
                continue;
            }
            Entity hooked = bobber.getHookedEntity();
            if (!(hooked instanceof LivingEntity living) || !living.isAlive() || living.isRemoved()) {
                continue;
            }
            ManaAbilityData data = user.getManaAbilityData(MANA);
            if (data.getCooldown() > 0) {
                if (data.getErrorTimer() == 0) {
                    Locale locale = user.getLocale();
                    String msg = TextUtil.replace(plugin.getMsg(ManaAbilityMessage.NOT_READY, locale), "{cooldown}",
                            NumberUtil.format1(data.getCooldown() / 20.0));
                    user.sendMessage(msg);
                    data.setErrorTimer(2);
                }
                break;
            }
            if (areValidLocations(player, living)) {
                activateSharpHook(plugin, player, user, living, world);
            }
            break;
        }
    }

    private static void activateSharpHook(FabricAuraSkills plugin, ServerPlayerEntity player, User user,
            LivingEntity caught, ServerWorld world) {
        double manaCost = MANA.getManaCost(user.getManaAbilityLevel(MANA));
        if (insufficientMana(plugin, user, manaCost)) {
            return;
        }
        double damage = MANA.getValue(user.getManaAbilityLevel(MANA));
        float healthBefore = caught.getHealth();
        DamageSource source = player.getDamageSources().playerAttack(player);
        caught.damage(world, source, (float) damage);
        float healthAfter = caught.getHealth();
        if (!MANA.optionBoolean("disable_health_check", false) && healthBefore == healthAfter) {
            return;
        }
        completeActivation(plugin, player, user, world, manaCost);
    }

    private static void completeActivation(FabricAuraSkills plugin, ServerPlayerEntity player, User user,
            ServerWorld world, double manaCost) {
        if (insufficientMana(plugin, user, manaCost)) {
            return;
        }
        ManaAbilityData data = user.getManaAbilityData(MANA);
        data.setActivated(true);
        if (MANA.optionBoolean("enable_sound", true)) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.PLAYERS, 1.0f, 1.5f);
        }
        double manaUsed = consumeMana(plugin, user, manaCost);
        FabricManaAbilityUseXpHandler.onManaAbilityActivated(player, MANA, manaUsed);
        stop(plugin, player, user);
    }

    private static void stop(FabricAuraSkills plugin, ServerPlayerEntity player, User user) {
        ManaAbilityData data = user.getManaAbilityData(MANA);
        if (!data.isActivated()) {
            return;
        }
        data.setActivated(false);
        data.setReady(false);
        data.setCooldown((int) MANA.getCooldown(user.getManaAbilityLevel(MANA)) * 20);
    }

    private static double consumeMana(FabricAuraSkills plugin, User user, double manaCost) {
        double manaConsumed;
        if (plugin.configBoolean(Option.MANA_ENABLED)) {
            manaConsumed = manaCost;
            user.setMana(user.getMana() - manaConsumed);
        } else {
            manaConsumed = 0.0;
        }
        String msg = TextUtil.replace(plugin.getMsg(ManaAbilityMessage.SHARP_HOOK_USE, user.getLocale()),
                "{mana}", NumberUtil.format0(manaConsumed));
        user.sendMessage(msg);
        return manaConsumed;
    }

    private static boolean insufficientMana(FabricAuraSkills plugin, User user, double manaCost) {
        if (!plugin.configBoolean(Option.MANA_ENABLED) || user.getMana() >= manaCost) {
            return false;
        }
        plugin.getManaAbilityManager().sendNotEnoughManaMessage(user, manaCost);
        return true;
    }

    private static boolean failsChecks(FabricAuraSkills plugin, ServerPlayerEntity player, User user) {
        String w = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isDisabledWorld(w)) {
            return true;
        }
        if (!user.hasSkillPermission(MANA.getSkill())) {
            return true;
        }
        return plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode;
    }

    private static boolean areValidLocations(ServerPlayerEntity damager, LivingEntity hooked) {
        World dw = damager.getEntityWorld();
        World hw = hooked.getEntityWorld();
        if (dw != hw) {
            return false;
        }
        return damager.squaredDistanceTo(hooked) <= 1089.0;
    }

    private static boolean isDisabled() {
        return !MANA.isEnabled() || !MANA.getSkill().isEnabled();
    }
}
