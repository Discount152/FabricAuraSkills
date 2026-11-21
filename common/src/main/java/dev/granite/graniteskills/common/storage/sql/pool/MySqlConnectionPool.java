package dev.granite.graniteskills.common.storage.sql.pool;

import com.zaxxer.hikari.HikariConfig;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.storage.sql.DatabaseCredentials;

public class MySqlConnectionPool extends ConnectionPool {

    public MySqlConnectionPool(GraniteSkillsPlugin plugin, DatabaseCredentials credentials) {
        super(plugin, credentials);
    }

    @Override
    public void configure(HikariConfig config, DatabaseCredentials credentials) {
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        // allowPublicKeyRetrieval is required for caching_sha2_password over non-SSL local links.
        config.setJdbcUrl("jdbc:mysql://" + credentials.host() + ":" + credentials.port() + "/" + credentials.database()
                + "?useSSL=" + credentials.ssl()
                + "&allowPublicKeyRetrieval=" + !credentials.ssl());
        config.setUsername(credentials.username());
        config.setPassword(credentials.password());
    }

}
