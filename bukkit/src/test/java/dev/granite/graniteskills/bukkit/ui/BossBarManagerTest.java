package dev.granite.graniteskills.bukkit.ui;

import dev.granite.graniteskills.api.skill.Skills;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.util.TestSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BossBarManagerTest {

    private static ServerMock server;
    private static GraniteSkills plugin;

    @BeforeAll
    static void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(GraniteSkills.class, TestSession.create());
    }

    @AfterAll
    static void unload() {
        MockBukkit.unmock();
    }

    @Test
    void testIncrementAction() {
        BossBarManager bossBarManager = new BossBarManager(plugin);

        bossBarManager.loadOptions();

        PlayerMock player = server.addPlayer();

        assertEquals(-1, bossBarManager.getCurrentAction(player, Skills.FARMING));

        bossBarManager.incrementAction(player, Skills.FARMING);

        assertEquals(0, bossBarManager.getCurrentAction(player, Skills.FARMING));

        bossBarManager.incrementAction(player, Skills.FARMING);
        bossBarManager.incrementAction(player, Skills.FARMING);

        assertEquals(2, bossBarManager.getCurrentAction(player, Skills.FARMING));
    }

    @Test
    void testIncrementActionMultiMode() {
        GraniteSkills plugin = MockBukkit.load(GraniteSkills.class, new TestSession(Map.of(Option.BOSS_BAR_MODE, "multi")));

        BossBarManager bossBarManager = new BossBarManager(plugin);

        bossBarManager.loadOptions();

        PlayerMock player = server.addPlayer();

        assertEquals(-1, bossBarManager.getCurrentAction(player, Skills.FARMING));

        bossBarManager.incrementAction(player, Skills.FARMING);

        assertEquals(0, bossBarManager.getCurrentAction(player, Skills.FARMING));

        bossBarManager.incrementAction(player, Skills.FARMING);
        bossBarManager.incrementAction(player, Skills.FARMING);

        assertEquals(2, bossBarManager.getCurrentAction(player, Skills.FARMING));
    }

}
