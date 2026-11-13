package dev.granite.graniteskills.bukkit.antiafk;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.antiafk.AntiAfkManagerTest;
import dev.granite.graniteskills.common.antiafk.CheckType;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.util.TestSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.Map;

public class BukkitAntiAfkManagerTest extends AntiAfkManagerTest {

    private static GraniteSkills plugin;

    public BukkitAntiAfkManagerTest() {
        super(plugin);
    }

    @BeforeAll
    static void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(GraniteSkills.class, new TestSession(Map.of(Option.ANTI_AFK_ENABLED, true)));
    }

    @AfterAll
    static void unload() {
        MockBukkit.unmock();
    }

    @Override
    protected CheckType[] getCheckTypes() {
        return BukkitCheckType.values();
    }

}
