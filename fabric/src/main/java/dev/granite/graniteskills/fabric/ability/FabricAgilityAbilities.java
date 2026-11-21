package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.api.trait.TraitModifier;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.api.util.GraniteSkillsModifier.Operation;
import dev.granite.graniteskills.common.message.type.AbilityMessage;
import dev.granite.graniteskills.common.scheduler.TaskRunnable;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.AABB;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class FabricAgilityAbilities extends FabricAbilityImpl {

    private static final String FLEETING_ID = "graniteskills/fleeting";

    public FabricAgilityAbilities(FabricGraniteSkills plugin) {
        super(plugin, Abilities.LIGHT_FALL, Abilities.JUMPER, Abilities.SUGAR_RUSH, Abilities.FLEETING, Abilities.THUNDER_FALL);
        ServerPlayerEvents.LEAVE.register(this::onQuit);
        plugin.getScheduler().timerSync(new TaskRunnable() {
            @Override
            public void run() {
                if (plugin.getServer() == null) {
                    return;
                }
                for (ServerPlayer player : plugin.getServer().getPlayerList().getPlayers()) {
                    removeFleetingIfRecovered(player);
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public static void onConsume(FabricGraniteSkills plugin, ServerPlayer player, ItemStack stack) {
        FabricAgilityAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricAgilityAbilities.class);
        self.sugarRush(player, stack);
    }

    public static float adjustFall(FabricGraniteSkills plugin, ServerPlayer player, DamageSource source, float amount) {
        if (amount <= 0f || source == null) {
            return amount;
        }
        String path = source.typeHolder().unwrapKey().map(key -> key.identifier().getPath()).orElse("");
        if (!"fall".equals(path)) {
            FabricAgilityAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricAgilityAbilities.class);
            self.fleeting(player, amount);
            return amount;
        }
        FabricAgilityAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricAgilityAbilities.class);
        self.thunderFall(player, amount);
        self.fleeting(player, amount);
        return self.lightFall(player, amount);
    }

    public void onQuit(ServerPlayer player) {
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user == null) {
            return;
        }
        user.removeTraitModifier(FLEETING_ID);
    }

    private float lightFall(ServerPlayer player, float amount) {
        var ability = Abilities.LIGHT_FALL;
        if (isDisabled(ability) || failsChecks(player, ability) || amount <= 0f) {
            return amount;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        double reduction = getValue(ability, user) / 100.0;
        return (float) (amount * (1.0 - reduction));
    }

    private void sugarRush(ServerPlayer player, ItemStack stack) {
        var ability = Abilities.SUGAR_RUSH;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        float multiplier = (float) (1.0 + getValue(ability, user) / 100.0);
        for (MobEffectInstance effect : contents.getAllEffects()) {
            if (!effect.getEffect().equals(MobEffects.SPEED) && !effect.getEffect().equals(MobEffects.JUMP_BOOST)) {
                continue;
            }
            player.addEffect(new MobEffectInstance(effect.getEffect(), (int) (effect.getDuration() * multiplier), effect.getAmplifier()));
        }
    }

    private void fleeting(ServerPlayer player, float finalDamage) {
        var ability = Abilities.FLEETING;
        if (isDisabled(ability) || failsChecks(player, ability) || finalDamage <= 0f) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        double maxHealth = player.getMaxHealth();
        if (maxHealth <= 0) {
            return;
        }
        if (player.getHealth() - finalDamage >= healthRequired() * maxHealth) {
            return;
        }
        if (user.getTraitModifier(FLEETING_ID) != null) {
            return;
        }
        double percent = getValue(ability, user);
        user.addTraitModifier(new TraitModifier(FLEETING_ID, Traits.MOVEMENT_SPEED, percent, Operation.ADD));
        Locale locale = user.getLocale();
        plugin.getAbilityManager().sendAbilityMessage(user, TextUtil.replace(
                plugin.getMsg(AbilityMessage.FLEETING_START, locale), "{value}", String.valueOf((int) percent)));
    }

    private void removeFleetingIfRecovered(ServerPlayer player) {
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user.getTraitModifier(FLEETING_ID) == null) {
            return;
        }
        double maxHealth = player.getMaxHealth();
        if (maxHealth <= 0 || player.getHealth() < healthRequired() * maxHealth) {
            return;
        }
        if (user.removeTraitModifier(FLEETING_ID)) {
            plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(AbilityMessage.FLEETING_END, user.getLocale()));
        }
    }

    private void thunderFall(ServerPlayer player, float amount) {
        applyThunderFall(player, amount, Double.NaN);
    }

    /**
     * Bukkit Thunder Fall: while sneaking, chance to splash secondary% of fall damage to nearby entities.
     *
     * @param forcedRoll {@link Double#NaN} uses live RNG
     * @return number of entities damaged
     */
    public static int applyThunderFall(ServerPlayer player, float amount, double forcedRoll) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || player == null || amount <= 0f || !player.isShiftKeyDown()) {
            return 0;
        }
        FabricAgilityAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricAgilityAbilities.class);
        var ability = Abilities.THUNDER_FALL;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return 0;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        double roll = Double.isNaN(forcedRoll) ? self.rand.nextDouble() : forcedRoll;
        if (roll >= self.getValue(ability, user) / 100.0) {
            return 0;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return 0;
        }
        double damage = (self.getSecondaryValue(ability, user) / 100.0) * amount;
        DamageSource source = level.damageSources().playerAttack(player);
        AABB box = player.getBoundingBox().inflate(3.0, 1.0, 3.0);
        int hit = 0;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, living -> living != player)) {
            if (entity.hurtServer(level, source, (float) damage)) {
                hit++;
            }
        }
        return hit;
    }

    private static double healthRequired() {
        return Abilities.FLEETING.optionDouble("health_percent_required", 20) / 100.0;
    }

}
