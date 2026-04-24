package dev.aurelium.auraskills.fabric.menu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricMenuRegistryTest {

    @Test
    void registerAndLookup_byId() {
        FabricMenuRegistry r = new FabricMenuRegistry();
        r.register(new FabricMenuDefinition(
                "skills", "auraskills.granite.menu.skills_title", (p, pl, t, c) -> {}));
        assertTrue(r.get("skills").isPresent());
        assertTrue(r.get("nope").isEmpty());
    }

    @Test
    void menuIds_returnsRegisteredOrder() {
        FabricMenuRegistry r = new FabricMenuRegistry();
        r.register(new FabricMenuDefinition("a", "k.a", (p, pl, t, c) -> {}));
        r.register(new FabricMenuDefinition("b", "k.b", (p, pl, t, c) -> {}));
        assertEquals(2, r.menuIds().size());
        assertTrue(r.menuIds().contains("a"));
    }

    @Test
    void open_invokesOpenerWithTitle() {
        FabricMenuRegistry r = new FabricMenuRegistry();
        final boolean[] called = {false};
        r.register(new FabricMenuDefinition("x", "auraskills.granite.menu.skills_title", (p, pl, title, c) -> {
            called[0] = title != null;
        }));
        r.open("x", null, null);
        assertTrue(called[0]);
    }

    @Test
    void open_levelProgression_withNullPlugin_doesNotInvokeOpener() {
        FabricMenuRegistry r = new FabricMenuRegistry();
        final int[] n = {0};
        r.register(new FabricMenuDefinition(
                FabricMenuKeys.LEVEL_PROGRESSION,
                "k",
                (p, pl, t, c) -> n[0]++));
        r.open(FabricMenuKeys.LEVEL_PROGRESSION, null, null, FabricMenuContext.none());
        assertEquals(0, n[0]);
    }
}
