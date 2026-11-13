package dev.granite.graniteskills.api.registry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NamespacedIdTest {

    @Test
    void testFromString() {
        assert NamespacedId.of("namespace", "key").toString().equals("namespace/key");
        assert NamespacedId.fromString("namespace/key").toString().equals("namespace/key");
        assert NamespacedId.fromDefault("namespace/key").toString().equals("namespace/key");
        assert NamespacedId.fromDefault("key").toString().equals(NamespacedId.GRANITESKILLS + "/key");
    }

    @Test
    void oldPluginNamespacesResolveToGraniteSkills() {
        NamespacedId mining = NamespacedId.fromDefault("mining");
        assertEquals(mining, NamespacedId.fromString("auraskills/mining"));
        assertEquals(mining, NamespacedId.fromString("AureliumSkills/Mining"));
        assertEquals(mining, NamespacedId.fromDefault("auraskills/mining"));
        assertEquals("graniteskills", NamespacedId.of("auraskills", "fishing").getNamespace());
        assertEquals("other", NamespacedId.fromString("other/farming").getNamespace());
    }

}
