package dev.granite.graniteskills.bukkit.hooks.mythicmobs;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.conditions.ICasterCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.core.utils.annotations.MythicCondition;

@MythicCondition(name = "hasMana")
public class HasManaCondition implements ICasterCondition {

    private final GraniteSkills plugin;
    private final double requiredMana;

    public HasManaCondition(GraniteSkills plugin, MythicConditionLoadEvent loader) {
        this.plugin = plugin;
        this.requiredMana = loader.getConfig().getDouble(new String[]{"mana", "m"}, 0);
    }

    @Override
    public boolean check(SkillCaster caster) {
        if (!caster.getEntity().isPlayer()) return false;
        return plugin.getUser(BukkitAdapter.adapt(caster.getEntity().asPlayer())).getMana() >= requiredMana;
    }

}
