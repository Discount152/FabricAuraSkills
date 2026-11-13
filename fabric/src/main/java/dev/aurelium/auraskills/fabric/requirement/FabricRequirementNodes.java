package dev.aurelium.auraskills.fabric.requirement;

import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricUser;
import java.util.Locale;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

final class FabricSkillNode extends FabricRequirementNode {

    private final Skill skill;
    private final int level;

    FabricSkillNode(FabricAuraSkills plugin, Skill skill, int level, String message) {
        super(plugin, message);
        this.skill = skill;
        this.level = level;
    }

    @Override
    public boolean check(ServerPlayer player) {
        if (skill == null) {
            return true;
        }
        return plugin.getUserManager().getUser(player.getUUID()).getSkillLevel(skill) >= level;
    }
}

final class FabricPermissionNode extends FabricRequirementNode {

    private final String permission;

    FabricPermissionNode(FabricAuraSkills plugin, String permission, String message) {
        super(plugin, message);
        this.permission = permission;
    }

    @Override
    public boolean check(ServerPlayer player) {
        if (permission == null) {
            return true;
        }
        if (plugin.getUserManager().getUser(player.getUUID()) instanceof FabricUser user) {
            return user.hasPermission(permission);
        }
        return false;
    }
}

final class FabricExcludedWorldNode extends FabricRequirementNode {

    private final String[] worlds;

    FabricExcludedWorldNode(FabricAuraSkills plugin, String[] worlds, String message) {
        super(plugin, message);
        this.worlds = worlds != null ? worlds : new String[0];
    }

    @Override
    public boolean check(ServerPlayer player) {
        String w = FabricUser.worldNameCompat(player);
        for (String world : worlds) {
            if (world != null && w.equalsIgnoreCase(world)) {
                return false;
            }
        }
        return true;
    }
}

final class FabricStatNode extends FabricRequirementNode {

    private final Stat stat;
    private final int value;

    FabricStatNode(FabricAuraSkills plugin, Stat stat, int value, String message) {
        super(plugin, message);
        this.stat = stat;
        this.value = value;
    }

    @Override
    public boolean check(ServerPlayer player) {
        if (stat == null) {
            return true;
        }
        return plugin.getUserManager().getUser(player.getUUID()).getStatLevel(stat) >= value;
    }
}

final class FabricWorldNode extends FabricRequirementNode {

    private final String world;

    FabricWorldNode(FabricAuraSkills plugin, String world, String message) {
        super(plugin, message);
        this.world = world;
    }

    @Override
    public boolean check(ServerPlayer player) {
        return world != null && FabricUser.worldNameCompat(player).equalsIgnoreCase(world);
    }
}

final class FabricBiomeNode extends FabricRequirementNode {

    private final String biome;

    FabricBiomeNode(FabricAuraSkills plugin, String biome, String message) {
        super(plugin, message);
        this.biome = biome;
    }

    @Override
    public boolean check(ServerPlayer player) {
        if (biome == null || !(player.level() instanceof ServerLevel sw)) {
            return false;
        }
        Identifier id = sw.registryAccess().lookupOrThrow(Registries.BIOME).getKey(sw.getBiome(player.blockPosition()).value());
        if (id == null) {
            return false;
        }
        String full = id.toString();
        String path = id.getPath();
        return biome.equalsIgnoreCase(full) || biome.equalsIgnoreCase(path) || biome.equalsIgnoreCase(id.getNamespace() + ":" + path);
    }
}

/**
 * WorldGuard-backed on Bukkit; without that hook on Fabric this node always passes (Bukkit returns true when WG absent).
 */
final class FabricRegionNode extends FabricRequirementNode {

    FabricRegionNode(FabricAuraSkills plugin, @SuppressWarnings("unused") String region, String message) {
        super(plugin, message);
    }

    @Override
    public boolean check(ServerPlayer player) {
        return true;
    }
}

final class FabricItemNode extends FabricRequirementNode {

    private final Identifier expectedItemId;

    FabricItemNode(FabricAuraSkills plugin, String item, String message) {
        super(plugin, message);
        String upper = item != null ? item.toUpperCase(Locale.ROOT) : "";
        Identifier id = Identifier.withDefaultNamespace(upper.toLowerCase(Locale.ROOT));
        if (!BuiltInRegistries.ITEM.containsKey(id)) {
            this.expectedItemId = Identifier.tryParse(item != null ? item.toLowerCase(Locale.ROOT) : "");
        } else {
            this.expectedItemId = id;
        }
    }

    @Override
    public boolean check(ServerPlayer player) {
        if (expectedItemId == null || !BuiltInRegistries.ITEM.containsKey(expectedItemId)) {
            return true;
        }
        ItemStack held = player.getMainHandItem();
        Identifier heldId = BuiltInRegistries.ITEM.getKey(held.getItem());
        return expectedItemId.equals(heldId);
    }
}

final class FabricEnchantmentNode extends FabricRequirementNode {

    private final Holder<Enchantment> enchantment;
    private final int levelMin;
    private final int levelMax;

    FabricEnchantmentNode(FabricAuraSkills plugin, Identifier enchantmentId, int levelMin, int levelMax, String message) {
        super(plugin, message);
        Holder<Enchantment> resolved = null;
        if (enchantmentId != null) {
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, enchantmentId);
            resolved = plugin.getServer().registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .get(key)
                    .orElse(null);
        }
        this.enchantment = resolved;
        this.levelMin = levelMin;
        this.levelMax = levelMax;
    }

    @Override
    public boolean check(ServerPlayer player) {
        if (enchantment == null) {
            return true;
        }
        ItemStack held = player.getMainHandItem();
        ItemEnchantments c = held.get(DataComponents.ENCHANTMENTS);
        if (c == null || c.isEmpty()) {
            return false;
        }
        int enchantmentLevel = c.getLevel(enchantment);
        if (levelMin >= 0) {
            if (levelMin == levelMax && enchantmentLevel != levelMin) {
                return false;
            }
            if (enchantmentLevel < levelMin) {
                return false;
            }
            if (levelMax >= 0 && enchantmentLevel > levelMax) {
                return false;
            }
        }
        return true;
    }
}
