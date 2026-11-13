package dev.granite.graniteskills.bukkit.config;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.config.ConfigProvider;
import dev.granite.graniteskills.common.config.ConfigProviderTest;
import dev.granite.graniteskills.common.util.TestSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.Map;

public class BukkitConfigProviderTest extends ConfigProviderTest {

    private static GraniteSkills plugin;

    @BeforeAll
    static void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(GraniteSkills.class, TestSession.create());
    }

    @AfterAll
    static void unload() {
        MockBukkit.unmock();
    }

    @Override
    protected ConfigProvider getConfigProvider() {
        return new BukkitConfigProvider(plugin, Map.of());
    }

}
