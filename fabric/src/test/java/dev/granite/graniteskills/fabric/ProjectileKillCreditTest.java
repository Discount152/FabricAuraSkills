package dev.granite.graniteskills.fabric;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProjectileKillCreditTest {

    @Test
    void projectileXpGoesToTheShooterNotAnotherPlayer() {
        UUID shooter = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        ProjectileKillCredit.Credit credit = ProjectileKillCredit.resolve(true, false, shooter, other, null);
        assertEquals(shooter, credit.playerId());
        assertNotEquals(other, credit.playerId());
        assertEquals(dev.granite.graniteskills.api.source.type.EntityXpSource.EntityDamagers.PROJECTILE, credit.damager());

        assertNull(ProjectileKillCredit.resolve(true, false, null, null, other));

        ProjectileKillCredit.Credit melee = ProjectileKillCredit.resolve(false, false, null, other, shooter);
        assertEquals(shooter, melee.playerId());
        assertNotEquals(other, melee.playerId());
    }
}
