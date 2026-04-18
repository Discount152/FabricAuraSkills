package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.mana.ManaAbilities;
import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.ability.AbilityData;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.mana.ManaAbilityData;
import dev.aurelium.auraskills.common.message.type.ManaAbilityMessage;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Fabric port of Bukkit {@link dev.aurelium.auraskills.bukkit.skills.archery.ChargedShot}: optional toggle (left-click
 * swing with bow), mana-scaled bow damage on the next shot, and {@link FabricManaAbilityUseXpHandler} on activation.
 * Projectile strength is applied via {@link dev.aurelium.auraskills.fabric.mixin.LivingEntityChargedShotDamageMixin}.
 */
public final class FabricChargedShot {

    private static final ManaAbility MANA = ManaAbilities.CHARGED_SHOT;

    /** Full damage multiplier stored per projectile until first damaging hit (piercing may hit multiple targets). */
    private static final Map<UUID, Double> PROJECTILE_DAMAGE_MULTIPLIER = new ConcurrentHashMap<>();

    private FabricChargedShot() {
    }

    public static void register(FabricAuraSkills plugin) {
        tickToggleCooldown(plugin);
    }

    public static float modifyDamageFromChargedProjectile(ServerWorld world, DamageSource source, float amount) {
        if (world.isClient()) {
            return amount;
        }
        Entity projEntity = source.getSource();
        if (!(projEntity instanceof ProjectileEntity)) {
            return amount;
        }
        UUID id = projEntity.getUuid();
        Double mult = PROJECTILE_DAMAGE_MULTIPLIER.get(id);
        if (mult == null) {
            return amount;
        }
        return amount * mult.floatValue();
    }

    public static void onBowStoppedUsingHead(net.minecraft.item.ItemStack stack, net.minecraft.world.World world,
            LivingEntity user, int remainingUseTicks) {
        if (world.isClient() || !(user instanceof ServerPlayerEntity sp)) {
            return;
        }
        if (!stack.isOf(Items.BOW) || FabricManaAbilityItemTags.shouldIgnoreInteract(stack)) {
            return;
        }
        net.minecraft.item.BowItem bow = (net.minecraft.item.BowItem) stack.getItem();
        int max = bow.getMaxUseTime(stack, user);
        int drawnTicks = max - remainingUseTicks;
        float pull = net.minecraft.item.BowItem.getPullProgress(drawnTicks);
        FabricChargedShotBowContext.set(sp, pull);
    }

    public static void onBowStoppedUsingReturn() {
        FabricChargedShotBowContext.clear();
    }

    public static void onBowShotProjectile(LivingEntity shooter, ProjectileEntity projectile) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        FabricChargedShotBowContext.Context ctx = FabricChargedShotBowContext.takeIfShooterMatches(shooter);
        if (ctx == null) {
            return;
        }
        tryActivateOnProjectile(plugin, ctx.player(), ctx.pullProgress(), projectile);
    }

    public static void onSwingMainHandBow(FabricAuraSkills plugin, ServerPlayerEntity player) {
        if (isDisabled()) {
            return;
        }
        if (MANA.optionBoolean("always_enabled", false)) {
            return;
        }
        if (!player.getMainHandStack().isOf(Items.BOW)) {
            return;
        }
        if (FabricManaAbilityItemTags.shouldIgnoreInteract(player.getMainHandStack())) {
            return;
        }
        if (failsChecks(plugin, player)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        AbilityData abilityData = user.getAbilityData(MANA);
        if (abilityData.getInt("toggle_cooldown") != 0) {
            return;
        }
        Locale locale = user.getLocale();
        if (!abilityData.getBoolean("enabled")) {
            abilityData.setData("enabled", true);
            plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.CHARGED_SHOT_ENABLE, locale));
        } else {
            abilityData.setData("enabled", false);
            plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.CHARGED_SHOT_DISABLE, locale));
        }
        abilityData.setData("toggle_cooldown", 8);
    }

    private static void tryActivateOnProjectile(FabricAuraSkills plugin, ServerPlayerEntity player, float pull,
            ProjectileEntity projectile) {
        if (isDisabled()) {
            return;
        }
        if (failsChecks(plugin, player)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        AbilityData ad = user.getAbilityData(MANA);
        if (!ad.getBoolean("enabled") && !MANA.optionBoolean("always_enabled", false)) {
            return;
        }
        ManaAbilityData data = user.getManaAbilityData(MANA);
        if (data.getCooldown() != 0) {
            if (data.getErrorTimer() == 0) {
                String msg = TextUtil.replace(plugin.getMsg(ManaAbilityMessage.NOT_READY, user.getLocale()), "{cooldown}",
                        NumberUtil.format1((double) data.getCooldown() / 20.0));
                user.sendMessage(msg);
                data.setErrorTimer(2);
            }
            return;
        }
        if (data.isActivated()) {
            return;
        }

        double manaCost = Math.min(MANA.getManaCost(user.getManaAbilityLevel(MANA)) * pull, user.getMana());
        if (insufficientMana(plugin, user, manaCost)) {
            return;
        }

        data.setActivated(true);
        double manaConsumed = plugin.configBoolean(Option.MANA_ENABLED) ? manaCost : 0.0;
        double damagePercent = manaConsumed * MANA.getValue(user.getManaAbilityLevel(MANA));
        double multiplier = 1.0 + damagePercent / 100.0;
        PROJECTILE_DAMAGE_MULTIPLIER.put(projectile.getUuid(), multiplier);

        if (MANA.optionBoolean("enable_sound", true)) {
            if (player.getEntityWorld() instanceof ServerWorld sw) {
                sw.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 0.5f, 1.0f);
            }
        }

        if (manaConsumed > 0 && MANA.optionBoolean("enable_message", true)) {
            String manaStr = manaConsumed < 1 ? NumberUtil.format1(manaConsumed) : NumberUtil.format0(manaConsumed);
            String pctStr = damagePercent < 1 ? NumberUtil.format1(damagePercent) : NumberUtil.format0(damagePercent);
            user.sendMessage(TextUtil.replace(plugin.getMsg(ManaAbilityMessage.CHARGED_SHOT_SHOOT, user.getLocale()),
                    "{mana}", manaStr,
                    "{percent}", pctStr));
        } else if (manaConsumed <= 0 && MANA.optionBoolean("enable_message", true)) {
            user.sendMessage(TextUtil.replace(plugin.getMsg(ManaAbilityMessage.CHARGED_SHOT_SHOOT, user.getLocale()),
                    "{mana}", NumberUtil.format0(0),
                    "{percent}", NumberUtil.format0(0)));
        }

        if (plugin.configBoolean(Option.MANA_ENABLED) && manaConsumed > 0) {
            user.setMana(user.getMana() - manaConsumed);
        }
        FabricManaAbilityUseXpHandler.onManaAbilityActivated(player, MANA, manaConsumed);

        finishImmediateActivation(user, data);
    }

    private static void finishImmediateActivation(User user, ManaAbilityData data) {
        data.setActivated(false);
        data.setReady(false);
        data.setCooldown((int) MANA.getCooldown(user.getManaAbilityLevel(MANA)) * 20);
    }

    private static void tickToggleCooldown(FabricAuraSkills plugin) {
        plugin.getScheduler().timerSync(new TaskRunnable() {
            @Override
            public void run() {
                for (User user : plugin.getUserManager().getOnlineUsers()) {
                    AbilityData abilityData = user.getAbilityData(MANA);
                    int cd = abilityData.getInt("toggle_cooldown");
                    if (cd != 0) {
                        abilityData.setData("toggle_cooldown", cd - 1);
                    }
                }
            }
        }, 3 * 50L, 5 * 50L, TimeUnit.MILLISECONDS);
    }

    private static boolean failsChecks(FabricAuraSkills plugin, ServerPlayerEntity player) {
        if (!plugin.getUserManager().hasUser(player.getUuid())) {
            return true;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (user.getManaAbilityLevel(MANA) <= 0) {
            return true;
        }
        String w = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isDisabledWorld(w)) {
            return true;
        }
        if (!user.hasSkillPermission(MANA.getSkill())) {
            return true;
        }
        return plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode;
    }

    private static boolean insufficientMana(FabricAuraSkills plugin, User user, double manaCost) {
        if (!plugin.configBoolean(Option.MANA_ENABLED) || user.getMana() >= manaCost) {
            return false;
        }
        plugin.getManaAbilityManager().sendNotEnoughManaMessage(user, manaCost);
        return true;
    }

    private static boolean isDisabled() {
        return !MANA.isEnabled() || !MANA.getSkill().isEnabled();
    }

}
