package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.mana.ManaAbilities;
import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.mana.ManaAbilityData;
import dev.aurelium.auraskills.common.message.type.ManaAbilityMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

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
    public static void onSwingMainHandRod(FabricAuraSkills plugin, ServerPlayer player) {
        if (player.level().isClientSide()) {
            return;
        }
        if (isDisabled()) {
            return;
        }
        if (!player.getMainHandItem().is(Items.FISHING_ROD)) {
            return;
        }
        if (FabricManaAbilityItemTags.shouldIgnoreInteract(player.getMainHandItem())) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user.getManaAbilityLevel(MANA) <= 0) {
            return;
        }
        if (failsChecks(plugin, player, user)) {
            return;
        }

        ServerLevel world = (ServerLevel) player.level();
        AABB castProximity = player.getBoundingBox().inflate(0.1);
        for (Entity nearby : world.getEntities(player, castProximity)) {
            if (nearby instanceof FishingHook bobber && bobber.getPlayerOwner() == player) {
                return;
            }
        }

        AABB range = player.getBoundingBox().inflate(33.0);
        for (Entity entity : world.getEntities(player, range)) {
            if (!(entity instanceof FishingHook bobber)) {
                continue;
            }
            if (bobber.getPlayerOwner() != player || !bobber.isAlive() || bobber.isRemoved()) {
                continue;
            }
            Entity hooked = bobber.getHookedIn();
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

    private static void activateSharpHook(FabricAuraSkills plugin, ServerPlayer player, User user,
            LivingEntity caught, ServerLevel world) {
        double manaCost = MANA.getManaCost(user.getManaAbilityLevel(MANA));
        if (insufficientMana(plugin, user, manaCost)) {
            return;
        }
        double damage = MANA.getValue(user.getManaAbilityLevel(MANA));
        float healthBefore = caught.getHealth();
        DamageSource source = player.damageSources().playerAttack(player);
        caught.hurtServer(world, source, (float) damage);
        float healthAfter = caught.getHealth();
        if (!MANA.optionBoolean("disable_health_check", false) && healthBefore == healthAfter) {
            return;
        }
        completeActivation(plugin, player, user, world, manaCost);
    }

    private static void completeActivation(FabricAuraSkills plugin, ServerPlayer player, User user,
            ServerLevel world, double manaCost) {
        if (insufficientMana(plugin, user, manaCost)) {
            return;
        }
        ManaAbilityData data = user.getManaAbilityData(MANA);
        data.setActivated(true);
        if (MANA.optionBoolean("enable_sound", true)) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 1.0f, 1.5f);
        }
        double manaUsed = consumeMana(plugin, user, manaCost);
        FabricManaAbilityUseXpHandler.onManaAbilityActivated(player, MANA, manaUsed);
        stop(plugin, player, user);
    }

    private static void stop(FabricAuraSkills plugin, ServerPlayer player, User user) {
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

    private static boolean failsChecks(FabricAuraSkills plugin, ServerPlayer player, User user) {
        String w = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isDisabledWorld(w)) {
            return true;
        }
        if (!user.hasSkillPermission(MANA.getSkill())) {
            return true;
        }
        return plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().instabuild;
    }

    private static boolean areValidLocations(ServerPlayer damager, LivingEntity hooked) {
        Level dw = damager.level();
        Level hw = hooked.level();
        if (dw != hw) {
            return false;
        }
        return damager.distanceToSqr(hooked) <= 1089.0;
    }

    private static boolean isDisabled() {
        return !MANA.isEnabled() || !MANA.getSkill().isEnabled();
    }
}
