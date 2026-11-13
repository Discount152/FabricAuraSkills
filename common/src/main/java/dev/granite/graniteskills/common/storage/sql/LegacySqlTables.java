package dev.granite.graniteskills.common.storage.sql;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.storage.sql.pool.ConnectionPool;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Renames SQL tables and stored ids saved under the auraskills and aureliumskills prefixes.
 */
public final class LegacySqlTables {

    private static final String NEW_PREFIX = SqlStorageProvider.TABLE_PREFIX;
    private static final String[] OLD_PREFIXES = {"auraskills_", "aureliumskills_"};
    private static final String[] TABLES = {
            "modifiers",
            "skill_levels",
            "key_values",
            "logs",
            "schema_migrations",
            "users"
    };

    private LegacySqlTables() {
    }

    public static void migrate(GraniteSkillsPlugin plugin, ConnectionPool pool) {
        try (Connection connection = pool.getConnection()) {
            renameTables(plugin, pool, connection);
            rewriteStoredIds(plugin, connection);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to migrate AuraSkills SQL tables. Please report this!", e);
        }
    }

    private static void renameTables(GraniteSkillsPlugin plugin, ConnectionPool pool, Connection connection) throws SQLException {
        for (String oldPrefix : OLD_PREFIXES) {
            boolean oldExists = false;
            boolean newHasRows = false;
            boolean oldHasRows = false;
            for (String table : TABLES) {
                String oldName = oldPrefix + table;
                String newName = NEW_PREFIX + table;
                if (tableExists(connection, pool, oldName)) {
                    oldExists = true;
                    if (countRows(connection, oldName) > 0) {
                        oldHasRows = true;
                    }
                }
                if (tableExists(connection, pool, newName) && countRows(connection, newName) > 0) {
                    newHasRows = true;
                }
            }
            if (!oldExists) {
                continue;
            }
            if (newHasRows && oldHasRows) {
                plugin.logger().warn("Both " + oldPrefix + "* and " + NEW_PREFIX + "* tables contain rows. Old tables were left in place.");
                continue;
            }
            if (!newHasRows) {
                dropEmptyNewTables(connection, pool);
            }
            for (String table : TABLES) {
                String oldName = oldPrefix + table;
                String newName = NEW_PREFIX + table;
                if (!tableExists(connection, pool, oldName) || tableExists(connection, pool, newName)) {
                    continue;
                }
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("RENAME TABLE `" + oldName + "` TO `" + newName + "`");
                }
                plugin.logger().info("Renamed SQL table " + oldName + " to " + newName);
            }
        }
    }

    private static void dropEmptyNewTables(Connection connection, ConnectionPool pool) throws SQLException {
        for (String table : TABLES) {
            String name = NEW_PREFIX + table;
            if (tableExists(connection, pool, name) && countRows(connection, name) == 0) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("DROP TABLE `" + name + "`");
                }
            }
        }
    }

    private static void rewriteStoredIds(GraniteSkillsPlugin plugin, Connection connection) throws SQLException {
        int skillRows = rewrite(connection, NEW_PREFIX + "skill_levels",
                "skill_name", "skill_name LIKE 'auraskills/%' OR skill_name LIKE 'aureliumskills/%'",
                "REPLACE(REPLACE(skill_name, 'aureliumskills/', 'graniteskills/'), 'auraskills/', 'graniteskills/')");
        int categoryRows = rewrite(connection, NEW_PREFIX + "key_values",
                "category_id", "category_id LIKE 'auraskills/%' OR category_id LIKE 'aureliumskills/%' OR category_id LIKE 'auraskills:%' OR category_id LIKE 'aureliumskills:%'",
                idExpression("category_id"));
        int keyRows = rewrite(connection, NEW_PREFIX + "key_values",
                "key_name", "key_name LIKE 'auraskills/%' OR key_name LIKE 'aureliumskills/%' OR key_name LIKE 'auraskills:%' OR key_name LIKE 'aureliumskills:%' OR key_name LIKE 'AuraSkills.%' OR key_name LIKE 'AureliumSkills.%'",
                nameExpression("key_name"));
        int valueRows = rewrite(connection, NEW_PREFIX + "key_values",
                "value", "value LIKE 'auraskills/%' OR value LIKE 'aureliumskills/%' OR value LIKE 'auraskills:%' OR value LIKE 'aureliumskills:%' OR value LIKE 'AuraSkills.%' OR value LIKE 'AureliumSkills.%'",
                nameExpression("value"));
        int typeRows = rewrite(connection, NEW_PREFIX + "modifiers",
                "type_id", "type_id LIKE 'auraskills/%' OR type_id LIKE 'aureliumskills/%' OR type_id LIKE 'auraskills:%' OR type_id LIKE 'aureliumskills:%'",
                idExpression("type_id"));
        int modifierRows = rewrite(connection, NEW_PREFIX + "modifiers",
                "modifier_name", "modifier_name LIKE 'AuraSkills.%' OR modifier_name LIKE 'AureliumSkills.%' OR modifier_name LIKE 'auraskills/%' OR modifier_name LIKE 'aureliumskills/%'",
                nameExpression("modifier_name"));
        int total = skillRows + categoryRows + keyRows + valueRows + typeRows + modifierRows;
        if (total > 0) {
            plugin.logger().info("Rewrote " + total + " stored AuraSkills SQL values to graniteskills.");
        }
    }

    private static String idExpression(String column) {
        return "REPLACE(REPLACE(REPLACE(REPLACE(" + column
                + ", 'aureliumskills/', 'graniteskills/'), 'auraskills/', 'graniteskills/'), 'aureliumskills:', 'graniteskills:'), 'auraskills:', 'graniteskills:')";
    }

    private static String nameExpression(String column) {
        return "REPLACE(REPLACE(" + idExpression(column) + ", 'AureliumSkills.', 'GraniteSkills.'), 'AuraSkills.', 'GraniteSkills.')";
    }

    private static int rewrite(Connection connection, String table, String column, String where, String expression) throws SQLException {
        if (!tableExists(connection, null, table)) {
            return 0;
        }
        String sql = "UPDATE `" + table + "` SET `" + column + "` = " + expression + " WHERE " + where;
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            if (isMissingTable(e)) {
                return 0;
            }
            throw e;
        }
    }

    private static boolean tableExists(Connection connection, ConnectionPool pool, String table) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        String catalog = pool == null ? connection.getCatalog() : pool.getDatabaseName();
        try (ResultSet tables = meta.getTables(catalog, null, table, new String[]{"TABLE"})) {
            if (tables.next()) {
                return true;
            }
        }
        try (ResultSet tables = meta.getTables(catalog, null, table.toUpperCase(), new String[]{"TABLE"})) {
            return tables.next();
        }
    }

    private static long countRows(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM `" + table + "`")) {
            return result.next() ? result.getLong(1) : 0L;
        }
    }

    private static boolean isMissingTable(SQLException e) {
        String state = e.getSQLState();
        return "42S02".equals(state) || (e.getMessage() != null && e.getMessage().contains("doesn't exist"));
    }

}
