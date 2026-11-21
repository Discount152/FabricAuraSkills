package dev.granite.graniteskills.common.storage.backup;

import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.stat.StatModifier;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.TraitModifier;
import dev.granite.graniteskills.api.util.GraniteSkillsModifier.Operation;
import dev.granite.graniteskills.common.user.UserState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * The user section of a version-2 backup. Save and restore use this so a disposable file can be
 * checked without a running server.
 */
public final class BackupUserCodec {

    private BackupUserCodec() {
    }

    public static void writeUser(ConfigurationNode userNode, UserState state, int startLevel) throws Exception {
        for (Skill skill : state.skillLevels().keySet()) {
            int level = state.skillLevels().getOrDefault(skill, startLevel);
            double xp = state.skillXp().getOrDefault(skill, 0.0);
            ConfigurationNode skillNode = userNode.node("skills", skill.getId().toString());
            skillNode.node("level").set(level);
            skillNode.node("xp").set(xp);
        }
        userNode.node("mana").set(state.mana());
        for (StatModifier modifier : state.statModifiers().values()) {
            ConfigurationNode modifierNode = userNode.node("stat_modifiers").appendListNode();
            modifierNode.node("name").set(modifier.name());
            modifierNode.node("stat").set(modifier.stat().getId().toString());
            modifierNode.node("operation").set(modifier.operation().toString());
            modifierNode.node("value").set(modifier.value());
        }
        for (TraitModifier modifier : state.traitModifiers().values()) {
            ConfigurationNode modifierNode = userNode.node("trait_modifiers").appendListNode();
            modifierNode.node("name").set(modifier.name());
            modifierNode.node("trait").set(modifier.trait().getId().toString());
            modifierNode.node("operation").set(modifier.operation().toString());
            modifierNode.node("value").set(modifier.value());
        }
    }

    public static @Nullable UserState readUser(ConfigurationNode userNode, Function<NamespacedId, Skill> skills,
            Function<NamespacedId, Stat> stats, Function<NamespacedId, Trait> traits) {
        UUID uuid = uuidFromKey(userNode);
        if (uuid == null) {
            return null;
        }
        Map<Skill, Integer> skillLevels = new ConcurrentHashMap<>();
        Map<Skill, Double> skillXp = new ConcurrentHashMap<>();
        for (ConfigurationNode skillNode : userNode.node("skills").childrenMap().values()) {
            String skillName = (String) skillNode.key();
            if (skillName == null) {
                continue;
            }
            Skill skill = skills.apply(NamespacedId.fromDefault(skillName.toLowerCase(Locale.ROOT)));
            if (skill == null) {
                continue;
            }
            skillLevels.put(skill, skillNode.node("level").getInt());
            skillXp.put(skill, skillNode.node("xp").getDouble());
        }
        Map<String, StatModifier> statModifiers = new ConcurrentHashMap<>();
        for (ConfigurationNode modifierNode : userNode.node("stat_modifiers").childrenList()) {
            String name = modifierNode.node("name").getString();
            String statName = modifierNode.node("stat").getString();
            if (statName == null) {
                continue;
            }
            Stat stat = stats.apply(NamespacedId.fromString(statName));
            if (stat == null || name == null) {
                continue;
            }
            String operationName = modifierNode.node("operation").getString(Operation.ADD.toString());
            double value = modifierNode.node("value").getDouble();
            statModifiers.put(name, new StatModifier(name, stat, value, Operation.parse(operationName)));
        }
        Map<String, TraitModifier> traitModifiers = new ConcurrentHashMap<>();
        for (ConfigurationNode modifierNode : userNode.node("trait_modifiers").childrenList()) {
            String name = modifierNode.node("name").getString();
            String traitName = modifierNode.node("trait").getString();
            if (traitName == null) {
                continue;
            }
            Trait trait = traits.apply(NamespacedId.fromString(traitName));
            if (trait == null || name == null) {
                continue;
            }
            String operationName = modifierNode.node("operation").getString(Operation.ADD.toString());
            double value = modifierNode.node("value").getDouble();
            traitModifiers.put(name, new TraitModifier(name, trait, value, Operation.parse(operationName)));
        }
        return new UserState(uuid, skillLevels, skillXp, statModifiers, traitModifiers, userNode.node("mana").getDouble());
    }

    private static @Nullable UUID uuidFromKey(ConfigurationNode node) {
        String key = (String) node.key();
        if (key == null) {
            return null;
        }
        return UUID.fromString(key);
    }
}
