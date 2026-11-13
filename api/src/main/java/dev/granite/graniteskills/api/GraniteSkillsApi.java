package dev.granite.graniteskills.api;

import dev.granite.graniteskills.api.config.MainConfig;
import dev.granite.graniteskills.api.loot.LootManager;
import dev.granite.graniteskills.api.message.MessageManager;
import dev.granite.graniteskills.api.registry.GlobalRegistry;
import dev.granite.graniteskills.api.registry.Handlers;
import dev.granite.graniteskills.api.registry.NamespacedRegistry;
import dev.granite.graniteskills.api.skill.XpRequirements;
import dev.granite.graniteskills.api.source.SourceManager;
import dev.granite.graniteskills.api.user.SkillsUser;
import dev.granite.graniteskills.api.user.UserManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.UUID;

/**
 * The main interface where most API interfaces are accessed from.
 */
public interface GraniteSkillsApi {

    /**
     * Gets an online player's user data from the player's UUID.
     * Same as {@link UserManager#getUser(UUID)}, see for more details.
     *
     * @return the {@link SkillsUser} object
     */
    SkillsUser getUser(UUID playerId);

    /**
     * Gets the {@link UserManager}, which is used to access player-related data in the plugin,
     * including player skill and stat levels.
     *
     * @return the user manager
     */
    UserManager getUserManager();

    /**
     * Gets the {@link MessageManager}, which contains common user-configured messages,
     * such as skill names and descriptions.
     *
     * @return the message manager
     */
    MessageManager getMessageManager();

    /**
     * Gets information about the XP required to level up skills
     *
     * @return the XP requirements
     */
    XpRequirements getXpRequirements();

    /**
     * Gets the global registry for getting any skill, stat, ability, etc. by its id.
     *
     * @return the global registry
     */
    GlobalRegistry getGlobalRegistry();

    /**
     * Creates and returns the {@link NamespacedRegistry} for the given namespace and content directory,
     * which is used to register custom skills, stats, abilities, etc.
     *
     * @param namespace The name of the plugin this is being called by to uniquely identify custom content.
     * The namespace will be forced to lowercase. Referencing custom content in config files is done
     * by appending "namespace/" to the name of the content.
     * @param contentDirectory The directory where configuration files for custom content will be loaded from.
     * For Bukkit, pass in JavaPlugin#getDataFolder to use the plugin's config folder.
     * @return the {@link NamespacedRegistry} to register custom content
     * @throws IllegalArgumentException if the namespace is "graniteskills", which is not allowed
     */
    NamespacedRegistry useRegistry(String namespace, File contentDirectory);

    /**
     * Gets the {@link NamespacedRegistry} linked to a given namespace. This namespace must have been
     * already created with {@link #useRegistry(String, File)} before calling this method.
     *
     * @param namespace the namespace to get
     * @return the {@link NamespacedRegistry}, or null if not registered
     */
    @Nullable
    NamespacedRegistry getNamespacedRegistry(String namespace);

    /**
     * Gets the {@link Handlers} used to register platform-specific handlers for custom content.
     *
     * @return the handlers
     */
    Handlers getHandlers();

    /**
     * Gets an interface for getting common values from the main config.yml file.
     *
     * @return the main config provider
     */
    MainConfig getMainConfig();

    /**
     * Gets the {@link SourceManager}, which can be used to get sources of a type.
     *
     * @return the source manager
     */
    SourceManager getSourceManager();

    /**
     * Gets the loot manager for registering custom loot types and getting information
     * on loaded loot tables.
     *
     * @return the loot manager
     */
    LootManager getLootManager();

    /**
     * Gets the instance of the {@link GraniteSkillsApi},
     * throwing {@link IllegalStateException} if the API is not loaded yet.
     *
     * @return the API instance
     * @throws IllegalStateException if the API is not loaded
     */
    static GraniteSkillsApi get() {
        return GraniteSkillsProvider.getInstance();
    }

}
