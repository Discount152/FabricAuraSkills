package dev.granitelib.fabric.menu;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraniteClickActionsTest {

    @Test
    void allowedActorRunsTheActionAndDeniedActorDoesNot() {
        UUID allowedId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        UUID deniedId = UUID.fromString("ffffffff-eeee-dddd-cccc-bbbbbbbbbbbb");
        GraniteClickActions.Actor allowed = new GraniteClickActions.Actor(allowedId, "Op");
        GraniteClickActions.Actor denied = new GraniteClickActions.Actor(deniedId, "Guest");
        List<GraniteClickActions.Click> ran = new ArrayList<>();
        GraniteClickActions.Permission permission = (actor, action) -> actor.name().equals("Op");

        boolean allowedRan = GraniteClickActions.dispatch(allowed, "close", 49, permission, ran::add);
        boolean deniedRan = GraniteClickActions.dispatch(denied, "close", 49, permission, ran::add);

        assertTrue(allowedRan);
        assertFalse(deniedRan);
        assertEquals(1, ran.size());
        assertEquals("close", ran.getFirst().action());
        assertEquals(allowedId, ran.getFirst().actor().id());
        assertEquals("Op", ran.getFirst().actor().name());
        assertEquals(49, ran.getFirst().slot());
    }
}
