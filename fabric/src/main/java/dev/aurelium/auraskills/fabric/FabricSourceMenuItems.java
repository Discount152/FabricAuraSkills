package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.source.type.EntityXpSource;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.source.SourceMenuItems;
import dev.aurelium.auraskills.fabric.item.FabricConfigurateItemParser;
import dev.aurelium.auraskills.fabric.item.FabricItemRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Objects;

public final class FabricSourceMenuItems extends SourceMenuItems<ItemStack> {

    private final FabricAuraSkills plugin;
    private final FabricItemRegistry itemRegistry;

    public FabricSourceMenuItems(AuraSkillsPlugin plugin, FabricItemRegistry itemRegistry) {
        super(plugin);
        this.plugin = (FabricAuraSkills) plugin;
        this.itemRegistry = itemRegistry;
    }

    @Override
    public void parseAndRegisterMenuItem(XpSource source, ConfigurationNode config) {
        if (config.isMap() && config.node("key").virtual()) {
            try {
                ItemStack itemStack;
                try {
                    itemStack = FabricConfigurateItemParser.parseMenuItemStack(plugin, itemRegistry, config);
                    if (itemStack.isEmpty()) {
                        itemStack = fallbackStack(source);
                    }
                } catch (IllegalArgumentException e) {
                    itemStack = fallbackStack(source);
                }
                registerMenuItem(source, itemStack);
            } catch (Exception e) {
                plugin.logger().warn("Error parsing source menu item for source " + source.getId());
                e.printStackTrace();
            }
        } else {
            String itemId = config.isMap() ? config.node("key").getString() : config.getString();
            if (itemId == null) {
                registerMenuItem(source, fallbackStack(source));
                return;
            }
            NamespacedId namespacedId = NamespacedId.fromDefaultWithColon(itemId);
            ItemStack item = itemRegistry.getItem(namespacedId, reloaded -> registerMenuItem(source, reloaded));
            registerMenuItem(source, Objects.requireNonNullElseGet(item, () -> fallbackStack(source)));
        }
    }

    private static ItemStack fallbackStack(XpSource source) {
        if (source instanceof EntityXpSource entity) {
            return switch (entity.getEntity()) {
                case "wither" -> new ItemStack(Items.WITHER_SKELETON_SKULL);
                case "ender_dragon" -> new ItemStack(Items.DRAGON_HEAD);
                case "iron_golem" -> new ItemStack(Items.IRON_BLOCK);
                case "snow_golem" -> new ItemStack(Items.SNOW_BLOCK);
                default -> new ItemStack(Items.STONE);
            };
        }
        return new ItemStack(Items.STONE);
    }
}
