package dev.granite.graniteskills.bukkit.requirement;

import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import dev.granite.graniteskills.api.item.ModifierType;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.util.ItemUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Requirements {

    private final GraniteSkills plugin;

    public Requirements(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    public Map<Skill, Integer> getLegacyRequirements(ModifierType type, ReadWriteNBT nbtItem) {
        ReadWriteNBT legacyCompound = ItemUtils.getLegacyRequirementsTypeCompound(nbtItem, type);
        Map<Skill, Integer> requirements = new HashMap<>();

        for (String key : legacyCompound.getKeys()) {
            Skill skill = plugin.getSkillRegistry().getOrNull(NamespacedId.fromDefault(key.toLowerCase(Locale.ROOT)));
            if (skill != null) {
                int value = legacyCompound.getInteger(key);
                requirements.put(skill, value);
            }
        }

        return Map.copyOf(requirements);
    }

}
