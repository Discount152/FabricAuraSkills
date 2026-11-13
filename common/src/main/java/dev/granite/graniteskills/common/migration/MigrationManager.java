package dev.granite.graniteskills.common.migration;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.storage.sql.SqlStorageProvider;
import dev.granite.graniteskills.common.util.file.FileUtil;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;

public class MigrationManager {

    private final GraniteSkillsPlugin plugin;

    public MigrationManager(GraniteSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    public void attemptConfigMigration() {
        try {
            File mainConfig = new File(plugin.getPluginFolder(), "config.yml");
            ConfigurationNode config = FileUtil.loadYamlFile(mainConfig);
            boolean migrateConfig = shouldMigrate("config_migration_complete", config);

            if (!migrateConfig) {
                return;
            }

            plugin.logger().warn("[Migrator] As part of the 2.0 update, the plugin has been renamed from GraniteSkills to GraniteSkills and config files have had format changes");
            plugin.logger().warn("[Migrator] Watch the following log messages for errors");
            plugin.logger().warn("[Migrator] Attempting to migrate config files from GraniteSkills to GraniteSkills");

            ConfigMigrator configMigrator = new ConfigMigrator(plugin);
            configMigrator.migrate();

            config = FileUtil.loadYamlFile(mainConfig); // Refresh main config to account for saves
            setMigrated("config_migration_complete", config);

            FileUtil.saveYamlFile(new File(plugin.getPluginFolder(), "config.yml"), config);
        } catch (Exception e) {
            plugin.logger().severe("[Migrator] Error while migrating, please report this to the plugin Discord!");
            e.printStackTrace();
        }
    }

    public void attemptUserMigration() {
        try {
            ConfigurationNode config = FileUtil.loadYamlFile(new File(plugin.getPluginFolder(), "config.yml"));
            boolean migrateFileUser = shouldMigrate("file_user_migration_complete", config);
            boolean migrateSqlUser = shouldMigrate("sql_user_migration_complete", config);

            if (!migrateFileUser && !migrateSqlUser) {
                return;
            }

            // Migrate playerdata folder to userdata
            if (migrateFileUser) {
                FileUserMigrator fileUserMigrator = new FileUserMigrator(plugin);
                fileUserMigrator.migrate();
                setMigrated("file_user_migration_complete", config);
            }
            if (migrateSqlUser) {
                if (plugin.getStorageProvider() instanceof SqlStorageProvider storageProvider) {
                    SqlUserMigrator sqlUserMigrator = new SqlUserMigrator(plugin, storageProvider);
                    sqlUserMigrator.migrate();
                }
                setMigrated("sql_user_migration_complete", config);
            }
            FileUtil.saveYamlFile(new File(plugin.getPluginFolder(), "config.yml"), config);
        } catch (Exception e) {
            plugin.logger().severe("[Migrator] Error while migrating, please report this to the plugin Discord!");
            e.printStackTrace();
        }
    }

    public boolean shouldMigrate(String key, ConfigurationNode config) {
        File aureliumDir = new File(plugin.getPluginFolder().getParentFile(), "GraniteSkills");
        boolean migrationCompleted = config.node("metadata", key).getBoolean(true);

        return aureliumDir.exists() && !migrationCompleted;
    }

    public void setMigrated(String key, ConfigurationNode config) throws SerializationException {
        config.node("metadata", key).set(true);
    }

}
