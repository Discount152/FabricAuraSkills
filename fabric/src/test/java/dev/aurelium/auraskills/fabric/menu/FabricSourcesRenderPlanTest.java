package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.source.SourceIncome;
import dev.aurelium.auraskills.api.source.SourceType;
import dev.aurelium.auraskills.api.source.SourceValues;
import dev.aurelium.auraskills.api.source.XpSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricSourcesRenderPlanTest {

    @Test
    void defenseTwoVisibleSourcesRenderToTwoDistinctSlots() {
        List<XpSource> visible = List.of(
                new FakeSource("mob_damage", "Damage from Mobs", 1.0),
                new FakeSource("player_damage", "Damage from Players", 2.0));

        List<FabricSourcesMenuGui.RenderedSourceRow> rows = FabricSourcesMenuGui.renderPlanForPage(visible, 0);
        assertEquals(2, rows.size(), "two visible sources should render two rows");
        Set<Integer> slots = rows.stream().map(FabricSourcesMenuGui.RenderedSourceRow::slot).collect(Collectors.toSet());
        assertEquals(2, slots.size(), "source rows must not overwrite into one slot");
    }

    private record FakeSource(String id, String name, double xp) implements XpSource {
        @Override public NamespacedId getId() { return NamespacedId.of(NamespacedId.AURASKILLS, id); }
        @Override public SourceType getType() { return null; }
        @Override public String getDisplayName(Locale locale) { return name; }
        @Override public String getUnitName(Locale locale) { return null; }
        @Override public String name() { return id.toUpperCase(Locale.ROOT); }
        @Override public double getXp() { return xp; }
        @Override public SourceIncome getIncome() { return null; }
        @Override public SourceValues getValues() { return null; }
    }
}