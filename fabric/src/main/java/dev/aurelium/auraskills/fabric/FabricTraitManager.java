package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.TraitHandler;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.trait.TraitManager;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.trait.FabricAnvilDiscountTrait;
import dev.aurelium.auraskills.fabric.trait.FabricAttackDamageTrait;
import dev.aurelium.auraskills.fabric.trait.FabricCritChanceTrait;
import dev.aurelium.auraskills.fabric.trait.FabricCritDamageTrait;
import dev.aurelium.auraskills.fabric.trait.FabricDamageReductionTrait;
import dev.aurelium.auraskills.fabric.trait.FabricExperienceBonusTrait;
import dev.aurelium.auraskills.fabric.trait.FabricGatheringLuckTraits;
import dev.aurelium.auraskills.fabric.trait.FabricHealthRegenTraits;
import dev.aurelium.auraskills.fabric.trait.FabricHpTrait;
import dev.aurelium.auraskills.fabric.trait.FabricLuckTrait;
import dev.aurelium.auraskills.fabric.trait.FabricManaRegenTrait;
import dev.aurelium.auraskills.fabric.trait.FabricMaxManaTrait;
import dev.aurelium.auraskills.fabric.trait.FabricMovementSpeedTrait;
import dev.aurelium.auraskills.fabric.trait.FabricTraitHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FabricTraitManager extends TraitManager {

    private final FabricAuraSkills plugin;
    private final Map<Class<?>, FabricTraitHandler> traitImpls = new ConcurrentHashMap<>();

    public FabricTraitManager(AuraSkillsPlugin plugin) {
        super(plugin);
        this.plugin = (FabricAuraSkills) plugin;
    }

    public void registerTraitImplementations() {
        registerTraitImpl(new FabricLuckTrait(plugin));
        registerTraitImpl(new FabricHealthRegenTraits(plugin));
        registerTraitImpl(new FabricHpTrait(plugin));
        registerTraitImpl(new FabricMovementSpeedTrait(plugin));
        registerTraitImpl(new FabricMaxManaTrait(plugin));
        registerTraitImpl(new FabricManaRegenTrait(plugin));
        registerTraitImpl(new FabricCritChanceTrait(plugin));
        registerTraitImpl(new FabricCritDamageTrait(plugin));
        registerTraitImpl(new FabricAttackDamageTrait(plugin));
        registerTraitImpl(new FabricDamageReductionTrait(plugin));
        registerTraitImpl(new FabricAnvilDiscountTrait(plugin));
        registerTraitImpl(new FabricExperienceBonusTrait(plugin));
        registerTraitImpl(new FabricGatheringLuckTraits(plugin));
    }

    public void registerTraitImpl(FabricTraitHandler fabricTrait) {
        traitImpls.put(fabricTrait.getClass(), fabricTrait);
    }

    public <T extends FabricTraitHandler> T getTraitImpl(Class<T> clazz) {
        FabricTraitHandler h = traitImpls.get(clazz);
        if (h != null) {
            return clazz.cast(h);
        }
        throw new IllegalArgumentException("Trait implementation of type " + clazz.getSimpleName() + " not found!");
    }

    public @Nullable FabricTraitHandler getTraitImpl(Trait trait) {
        for (FabricTraitHandler traitImpl : traitImpls.values()) {
            for (Trait tr : traitImpl.getTraits()) {
                if (trait.getId().equals(tr.getId())) {
                    return traitImpl;
                }
            }
        }
        return null;
    }

    public Collection<FabricTraitHandler> getAllTraitImpls() {
        return traitImpls.values();
    }

    /**
     * Invoked when a player moves to another dimension so attribute traits can refresh.
     */
    public void onPlayerChangedServerWorld(ServerPlayerEntity player, ServerWorld from, ServerWorld to) {
        for (FabricTraitHandler impl : traitImpls.values()) {
            impl.onPlayerChangedServerWorld(player, from, to);
        }
    }

    @Override
    public double getBaseLevel(User user, Trait trait) {
        ServerPlayerEntity player = FabricUser.getPlayer(user);
        if (player == null) {
            return 0.0;
        }
        FabricTraitHandler traitImpl = getTraitImpl(trait);
        if (traitImpl != null) {
            return traitImpl.getBaseLevel(player, trait);
        }
        return 0.0;
    }

    @Override
    public void registerTraitHandler(TraitHandler traitHandler) {
        if (traitHandler instanceof FabricTraitHandler fabricTraitHandler) {
            registerTraitImpl(fabricTraitHandler);
        }
    }

    @Override
    public String getMenuDisplay(Trait trait, double value, Locale locale, @Nullable NumberFormat format) {
        FabricTraitHandler impl = getTraitImpl(trait);
        if (impl != null) {
            if (trait.getId().getKey().equals("hp")) {
                return hpMenuDisplay(value, trait, format);
            }
            return impl.getMenuDisplay(value, trait, locale);
        }
        if (format != null) {
            return format.format(value);
        }
        return NumberUtil.format1(value);
    }

    private static String hpMenuDisplay(double value, Trait trait, @Nullable NumberFormat format) {
        double scaling = trait.optionDouble("action_bar_scaling", 1);
        double display = scaling * value;
        return format != null ? format.format(display) : NumberUtil.format1(display);
    }
}
