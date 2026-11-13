package dev.granite.graniteskills.bukkit.item;

import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import dev.granite.graniteskills.api.item.ModifierType;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.skill.Multiplier;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.util.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Multipliers {

    private final GraniteSkills plugin;

    public Multipliers(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    public List<Multiplier> getLegacyMultipliers(ModifierType type, ReadWriteNBT nbt) {
        ReadWriteNBT legacyCompound = ItemUtils.getLegacyMultipliersTypeCompound(nbt, type);
        List<Multiplier> multipliers = new ArrayList<>();

        for (String key : legacyCompound.getKeys()) {
            double value = legacyCompound.getDouble(key);
            if (key.equals("Global")) {
                multipliers.add(new Multiplier(key, null, value));
            } else {
                Skill skill = plugin.getSkillRegistry().getOrNull(NamespacedId.fromDefault(key.toLowerCase(Locale.ROOT)));
                if (skill != null) {
                    multipliers.add(new Multiplier(key, skill, value));
                }
            }
        }
        return multipliers;
    }

}
