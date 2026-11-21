package dev.granite.graniteskills.fabric;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class FabricLuckPermsGrantsTest {

    @Test
    void denialAndUnsetAreOmittedFromGrantedList() {
        Map<Object, Object> map = new LinkedHashMap<>();
        map.put("graniteskills.multiplier.7", Boolean.TRUE);
        map.put("graniteskills.multiplier.25", Boolean.FALSE);
        map.put("graniteskills.multiplier.farming.15", Boolean.TRUE);
        map.put("graniteskills.multiplier.50", null);
        map.put(42, Boolean.TRUE);
        List<String> granted = FabricLuckPermsGrants.grantedFromPermissionMap(map);
        assertEquals(List.of("graniteskills.multiplier.7", "graniteskills.multiplier.farming.15"), granted);
        assertFalse(granted.contains("graniteskills.multiplier.25"));
    }
}
