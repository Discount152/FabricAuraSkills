package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FabricMenuContextTest {

    @Test
    void none_isEmpty() {
        FabricMenuContext c = FabricMenuContext.none();
        assertNull(c.skill());
        assertNull(c.stat());
    }

    @Test
    void ofSkill_wraps() {
        Skill s = Skills.FARMING;
        assertSame(s, FabricMenuContext.ofSkill(s).skill());
        assertNull(FabricMenuContext.ofSkill(s).stat());
    }

    @Test
    void ofStat_wraps() {
        Stat t = Stats.HEALTH;
        assertNull(FabricMenuContext.ofStat(t).skill());
        assertSame(t, FabricMenuContext.ofStat(t).stat());
    }

    @Test
    void ofSkill_rejectsNull() {
        assertThrows(NullPointerException.class, () -> FabricMenuContext.ofSkill(null));
    }

    @Test
    void ofStat_rejectsNull() {
        assertThrows(NullPointerException.class, () -> FabricMenuContext.ofStat(null));
    }
}
