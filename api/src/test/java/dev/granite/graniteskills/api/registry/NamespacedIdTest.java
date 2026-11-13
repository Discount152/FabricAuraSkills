package dev.granite.graniteskills.api.registry;

import org.junit.jupiter.api.Test;

public class NamespacedIdTest {

    @Test
    void testFromString() {
        assert NamespacedId.of("namespace", "key").toString().equals("namespace/key");
        assert NamespacedId.fromString("namespace/key").toString().equals("namespace/key");
        assert NamespacedId.fromDefault("namespace/key").toString().equals("namespace/key");
        assert NamespacedId.fromDefault("key").toString().equals(NamespacedId.GRANITESKILLS + "/key");
    }

}
