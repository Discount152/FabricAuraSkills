package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.source.type.DamageXpSource.DamageCause;
import dev.granite.graniteskills.api.source.type.EntityXpSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BleedXpAwardTest {

    private static final EntityXpSource.EntityTriggers[] DAMAGE = {EntityXpSource.EntityTriggers.DAMAGE};
    private static final EntityXpSource.EntityTriggers[] DEATH = {EntityXpSource.EntityTriggers.DEATH};
    private static final EntityXpSource.EntityDamagers[] PLAYER = {EntityXpSource.EntityDamagers.PLAYER};
    private static final EntityXpSource.EntityDamagers[] PROJECTILE = {EntityXpSource.EntityDamagers.PROJECTILE};

    @Test
    void bleedAwardsWhenTheSourceMatchesAndAwardsNothingWhenItDoesNot() {
        boolean match = BleedXpAward.matches("zombie", "zombie", DAMAGE, EntityXpSource.EntityDamagers.PLAYER, PLAYER,
                DamageCause.CUSTOM, new DamageCause[]{DamageCause.CUSTOM}, null);
        assertTrue(match);
        assertEquals(6.0, BleedXpAward.xp(match, 2.0, 3.0));

        assertFalse(BleedXpAward.matches("skeleton", "zombie", DAMAGE, EntityXpSource.EntityDamagers.PLAYER, PLAYER,
                DamageCause.CUSTOM, null, null));
        assertFalse(BleedXpAward.matches("zombie", "zombie", DEATH, EntityXpSource.EntityDamagers.PLAYER, PLAYER,
                DamageCause.CUSTOM, null, null));
        assertFalse(BleedXpAward.matches("zombie", "zombie", DAMAGE, EntityXpSource.EntityDamagers.PLAYER, PROJECTILE,
                DamageCause.CUSTOM, null, null));
        assertFalse(BleedXpAward.matches("zombie", "zombie", DAMAGE, EntityXpSource.EntityDamagers.PLAYER, PLAYER,
                DamageCause.CUSTOM, new DamageCause[]{DamageCause.ENTITY_ATTACK}, null));
        assertEquals(0.0, BleedXpAward.xp(false, 2.0, 3.0));
        assertEquals(0.0, BleedXpAward.xp(true, 0.0, 3.0));
    }
}
