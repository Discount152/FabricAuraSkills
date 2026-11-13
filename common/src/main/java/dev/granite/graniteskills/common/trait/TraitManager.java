package dev.granite.graniteskills.common.trait;

import com.google.common.collect.Sets;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.TraitHandler;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TraitManager {

    private final GraniteSkillsPlugin plugin;
    private final Map<Trait, LoadedTrait> traitMap;
    private final TraitSupplier supplier;

    public TraitManager(GraniteSkillsPlugin plugin) {
        this.plugin = plugin;
        this.traitMap = new ConcurrentHashMap<>();
        this.supplier = new TraitSupplier(this, plugin.getMessageProvider());
    }

    public TraitSupplier getSupplier() {
        return supplier;
    }

    public abstract double getBaseLevel(User user, Trait trait);

    @NotNull
    public LoadedTrait getTrait(Trait trait) {
        LoadedTrait loadedTrait = traitMap.get(trait);
        if (loadedTrait == null) {
            throw new IllegalArgumentException("Trait " + trait + " is not loaded!");
        }
        return loadedTrait;
    }

    public void register(Trait trait, LoadedTrait loadedTrait) {
        traitMap.put(trait, loadedTrait);
    }

    public void unregisterAll() {
        traitMap.clear();
    }

    public Set<Trait> getEnabledTraits() {
        Set<Trait> skills = Sets.newConcurrentHashSet();
        for (LoadedTrait loaded : traitMap.values()) {
            if (loaded.trait().isEnabled()) {
                skills.add(loaded.trait());
            }
        }
        return skills;
    }

    public Set<Stat> getLinkedStats(Trait trait) {
        Set<Stat> set = Sets.newConcurrentHashSet();
        for (Stat stat : plugin.getStatManager().getEnabledStats()) {
            if (stat.getTraits().contains(trait)) {
                set.add(stat);
            }
        }
        return set;
    }

    public boolean isLoaded(Trait trait) {
        return traitMap.containsKey(trait);
    }

    public abstract void registerTraitHandler(TraitHandler traitHandler);

    public abstract String getMenuDisplay(Trait trait, double value, Locale locale, @Nullable NumberFormat format);

}
