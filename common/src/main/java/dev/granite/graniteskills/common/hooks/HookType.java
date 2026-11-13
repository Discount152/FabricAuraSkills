package dev.granite.graniteskills.common.hooks;

public interface HookType {

    Class<? extends Hook> getHookClass();

    String getPluginName();

    boolean requiresEnabledFirst();

}
