package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.source.type.DamageXpSource.DamageCause;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Maps Minecraft {@link DamageSource} types to GraniteSkills {@link DamageCause} (Bukkit-compatible names).
 */
public final class FabricDamageCauseMapper {

    private FabricDamageCauseMapper() {
    }

    public static DamageCause toApiCause(DamageSource source) {
        return source.typeHolder().unwrapKey()
                .map(ResourceKey::identifier)
                .map(id -> mapPath(id.getPath()))
                .orElse(DamageCause.CUSTOM);
    }

    private static DamageCause mapPath(String path) {
        return switch (path) {
            case "arrow", "trident", "fireball", "unattributed_fireball", "wind_charge",
                    "wither_skull", "spit", "snowball", "thrown", "fireworks", "mob_projectile" -> DamageCause.PROJECTILE;
            case "player_attack", "mob_attack", "mob_attack_no_aggro", "sting" -> DamageCause.ENTITY_ATTACK;
            case "player_explosion", "explosion" -> DamageCause.ENTITY_EXPLOSION;
            case "bad_respawn_point" -> DamageCause.CUSTOM;
            case "cactus", "sweet_berry_bush" -> DamageCause.CONTACT;
            case "campfire" -> DamageCause.CAMPFIRE;
            case "cramming" -> DamageCause.CRAMMING;
            case "dragon_breath" -> DamageCause.DRAGON_BREATH;
            case "drowning" -> DamageCause.DROWNING;
            case "dry_out" -> DamageCause.DRYOUT;
            case "ender_pearl" -> DamageCause.FALL;
            case "fall", "falling_anvil", "falling_block", "falling_stalactite", "stalagmite" -> DamageCause.FALL;
            case "fly_into_wall" -> DamageCause.FLY_INTO_WALL;
            case "freeze" -> DamageCause.FREEZE;
            case "generic", "outside_border" -> DamageCause.CUSTOM;
            case "generic_kill" -> DamageCause.KILL;
            case "hot_floor" -> DamageCause.HOT_FLOOR;
            case "in_fire" -> DamageCause.FIRE;
            case "in_wall" -> DamageCause.SUFFOCATION;
            case "indirect_magic", "magic" -> DamageCause.MAGIC;
            case "lava" -> DamageCause.LAVA;
            case "lightning_bolt" -> DamageCause.LIGHTNING;
            case "on_fire" -> DamageCause.FIRE_TICK;
            case "out_of_world" -> DamageCause.VOID;
            case "sonic_boom" -> DamageCause.SONIC_BOOM;
            case "starve" -> DamageCause.STARVATION;
            case "thorns" -> DamageCause.THORNS;
            case "wither" -> DamageCause.WITHER;
            default -> DamageCause.CUSTOM;
        };
    }
}
