package dev.granite.graniteskills.bukkit.util;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.storage.sql.SqlStorageProvider;
import dev.granite.graniteskills.common.storage.sql.pool.MySqlConnectionPool;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

public class MetricsUtil {

    private final GraniteSkills plugin;

    public MetricsUtil(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    public void registerCustomCharts(Metrics metrics) {
        metrics.addCustomChart(new SimplePie("skills_enabled", () ->
                String.valueOf(plugin.getSkillManager().getEnabledSkills().size())));
        metrics.addCustomChart(new SimplePie("default_language", () ->
                plugin.configString(Option.DEFAULT_LANGUAGE)));
        metrics.addCustomChart(new SimplePie("storage_type", () -> {
            String type = "yaml";
            if (plugin.getStorageProvider() instanceof SqlStorageProvider sql) {
                if (sql.getPool() instanceof MySqlConnectionPool) {
                    type = "mysql";
                }
            }
            return type;
        }));
    }

}
