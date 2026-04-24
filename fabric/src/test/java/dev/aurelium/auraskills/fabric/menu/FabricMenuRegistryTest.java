package dev.aurelium.auraskills.fabric.menu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricMenuRegistryTest {

    @Test
    void registerAndLookup_byId() {
        FabricMenuRegistry r = new FabricMenuRegistry();
        r.register(new FabricMenuDefinition("skills", "auraskills.granite.menu.skills_title", (p, pl, t) -> {}));
        assertTrue(r.get("skills").isPresent());
        assertTrue(r.get("nope").isEmpty());
    }

    @Test
    void menuIds_returnsRegisteredOrder() {
        FabricMenuRegistry r = new FabricMenuRegistry();
        r.register(new FabricMenuDefinition("a", "k.a", (p, pl, t) -> {}));
        r.register(new FabricMenuDefinition("b", "k.b", (p, pl, t) -> {}));
        assertEquals(2, r.menuIds().size());
        assertTrue(r.menuIds().contains("a"));
    }

    @Test
    void open_invokesOpenerWithTitle() {
        FabricMenuRegistry r = new FabricMenuRegistry();
        final boolean[] called = {false};
        r.register(new FabricMenuDefinition("x", "auraskills.granite.menu.skills_title", (p, pl, title) -> {
            called[0] = title != null;
        }));
        r.open("x", null, null);
        assertTrue(called[0]);
    }
}
