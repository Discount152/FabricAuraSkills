package dev.granite.graniteskills.bukkit.api;

import dev.granite.graniteskills.api.GraniteSkillsBukkit;
import dev.granite.graniteskills.api.item.ItemManager;
import dev.granite.graniteskills.api.menu.MenuManager;
import dev.granite.graniteskills.api.region.LocationManager;
import dev.granite.graniteskills.api.region.Regions;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.api.implementation.ApiLocationManager;
import dev.granite.graniteskills.bukkit.api.implementation.ApiMenuManager;
import dev.granite.graniteskills.bukkit.api.implementation.ApiRegions;

public class ApiGraniteSkillsBukkit implements GraniteSkillsBukkit {

    private final ApiRegions blockTracker;
    private final ItemManager itemManager;
    private final LocationManager locationManager;
    private final MenuManager menuManager;

    public ApiGraniteSkillsBukkit(GraniteSkills plugin) {
        this.blockTracker = new ApiRegions(plugin);
        this.itemManager = plugin.getItemManager();
        this.locationManager = new ApiLocationManager(plugin);
        this.menuManager = new ApiMenuManager(plugin);
    }

    @Override
    public Regions getRegions() {
        return blockTracker;
    }

    @Override
    public ItemManager getItemManager() {
        return itemManager;
    }

    @Override
    public LocationManager getLocationManager() {
        return locationManager;
    }

    @Override
    public MenuManager getMenuManager() {
        return menuManager;
    }

}
