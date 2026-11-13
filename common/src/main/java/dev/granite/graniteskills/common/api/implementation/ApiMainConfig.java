package dev.granite.graniteskills.common.api.implementation;

import dev.granite.graniteskills.api.config.MainConfig;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.hooks.EconomyHook;

public class ApiMainConfig implements MainConfig {

    private final GraniteSkillsPlugin plugin;

    public ApiMainConfig(GraniteSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isDisabledInCreative() {
        return plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE);
    }

    @Override
    public int getStartLevel() {
        return plugin.config().getStartLevel();
    }

    @Override
    public int getHighestMaxLevel() {
        return plugin.config().getHighestMaxLevel();
    }

    @Override
    public boolean isJobsEnabled() {
        return plugin.configBoolean(Option.JOBS_ENABLED) && plugin.getHookManager().isRegistered(EconomyHook.class);
    }

    @Override
    public boolean jobSelectionEnabled() {
        return plugin.config().jobSelectionEnabled();
    }

}
