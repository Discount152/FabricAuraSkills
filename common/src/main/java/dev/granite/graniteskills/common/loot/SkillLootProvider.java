package dev.granite.graniteskills.common.loot;

import dev.granite.graniteskills.api.loot.LootDropCause;
import dev.granite.graniteskills.api.loot.LootPool;
import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.user.User;

public abstract class SkillLootProvider {

    protected final GraniteSkillsPlugin plugin;
    protected final AbstractLootHandler handler;

    public SkillLootProvider(GraniteSkillsPlugin plugin, AbstractLootHandler handler) {
        this.plugin = plugin;
        this.handler = handler;
    }

    public abstract LootDropCause getCause(LootPool pool);

    public double getChance(LootPool pool, User user) {
        return handler.getCommonChance(pool, user);
    }

    public boolean isApplicable(LootPool pool, XpSource source) {
        return true;
    }

}
