package dev.granite.graniteskills.common.storage.backup;

import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.skill.Skills;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.stat.StatModifier;
import dev.granite.graniteskills.api.stat.Stats;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.util.GraniteSkillsModifier.Operation;
import dev.granite.graniteskills.common.user.UserState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class BackupUserCodecTest {

    @TempDir
    Path temp;

    @Test
    void restoredUserMatchesTheDisposableBackup() throws Exception {
        UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
        Map<Skill, Integer> levels = new ConcurrentHashMap<>();
        Map<Skill, Double> xp = new ConcurrentHashMap<>();
        levels.put(Skills.FARMING, 9);
        xp.put(Skills.FARMING, 120.25);
        Map<String, StatModifier> modifiers = new ConcurrentHashMap<>();
        modifiers.put("gem", new StatModifier("gem", Stats.HEALTH, 4.5, Operation.ADD));
        UserState original = new UserState(uuid, levels, xp, modifiers, Map.of(), 33.5);

        Path file = temp.resolve("backup.yml");
        ConfigurationNode root = BasicConfigurationNode.root();
        root.node("backup_version").set(2);
        BackupUserCodec.writeUser(root.node("users", uuid.toString()), original, 1);
        YamlConfigurationLoader.builder().path(file).build().save(root);

        levels.put(Skills.FARMING, 1);
        xp.put(Skills.FARMING, 0.0);

        ConfigurationNode loaded = YamlConfigurationLoader.builder().path(file).build().load();
        UserState restored = BackupUserCodec.readUser(loaded.node("users", uuid.toString()),
                this::skill, this::stat, id -> null);

        assertEquals(9, restored.skillLevels().get(Skills.FARMING));
        assertEquals(120.25, restored.skillXp().get(Skills.FARMING));
        assertEquals(4.5, restored.statModifiers().get("gem").value());
        assertEquals(Stats.HEALTH, restored.statModifiers().get("gem").stat());
        assertEquals(33.5, restored.mana());
        assertEquals(uuid, restored.uuid());
        assertNotEquals(1, restored.skillLevels().get(Skills.FARMING));
    }

    private Skill skill(NamespacedId id) {
        return id.equals(Skills.FARMING.getId()) ? Skills.FARMING : null;
    }

    private Stat stat(NamespacedId id) {
        return id.equals(Stats.HEALTH.getId()) ? Stats.HEALTH : null;
    }
}
