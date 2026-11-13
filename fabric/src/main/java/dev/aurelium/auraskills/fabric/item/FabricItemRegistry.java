package dev.aurelium.auraskills.fabric.item;

import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.common.item.ItemRegistry;
import dev.aurelium.auraskills.common.message.type.LevelerMessage;
import dev.aurelium.auraskills.common.source.SourceMenuItems;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.data.KeyIntPair;
import dev.aurelium.auraskills.common.util.file.FileUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricSourceMenuItems;
import dev.aurelium.auraskills.fabric.FabricUser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class FabricItemRegistry implements ItemRegistry {

    private final FabricAuraSkills plugin;
    private final Map<NamespacedId, ItemStack> items = new ConcurrentHashMap<>();
    private final FabricSourceMenuItems sourceMenuItems;

    public FabricItemRegistry(FabricAuraSkills plugin) {
        this.plugin = plugin;
        this.sourceMenuItems = new FabricSourceMenuItems(plugin, this);
    }

    public void register(NamespacedId key, ItemStack item) {
        items.put(key, item.copy());
    }

    public void unregister(NamespacedId key) {
        items.remove(key);
    }

    public Set<NamespacedId> getIds() {
        return items.keySet();
    }

    public Map<NamespacedId, ItemStack> getItems() {
        return items;
    }

    public @Nullable ItemStack getItem(NamespacedId key) {
        return getItem(key, null);
    }

    @SuppressWarnings("unused")
    public @Nullable ItemStack getItem(NamespacedId key, @Nullable Consumer<ItemStack> unresolvedConsumer) {
        ItemStack item = items.get(key);
        if (item != null) {
            return item.copy();
        }
        return null;
    }

    public void loadFromDisk() {
        items.clear();
        File file = new File(plugin.getPluginFolder(), "items.yml");
        if (!file.exists()) {
            return;
        }
        try {
            ConfigurationNode root = FileUtil.loadYamlFile(file);
            ConfigurationNode itemsSection = root.node("items");
            if (itemsSection.virtual() || !itemsSection.isMap()) {
                return;
            }
            int registered = 0;
            for (Map.Entry<Object, ? extends ConfigurationNode> e : itemsSection.childrenMap().entrySet()) {
                String keyStr = String.valueOf(e.getKey());
                ConfigurationNode val = e.getValue();
                ItemStack stack = deserializeEntry(val);
                if (stack == null || stack.isEmpty()) {
                    plugin.logger().warn("Failed to load item at path items." + keyStr + " in items.yml");
                    continue;
                }
                NamespacedId id = NamespacedId.fromDefault(keyStr);
                register(id, stack);
                registered++;
            }
            if (registered > 0) {
                plugin.logger().info("Registered " + registered + " item" + (registered > 1 ? "s" : "") + " from items.yml");
            }
        } catch (IOException ex) {
            plugin.logger().warn("Could not load items.yml: " + ex.getMessage());
        }
    }

    private @Nullable ItemStack deserializeEntry(ConfigurationNode val) {
        Object raw = val.raw();
        if (raw instanceof String s) {
            ItemStack fromSnbt = FabricItemStackCodec.decode(plugin.getServer(), s);
            return fromSnbt.isEmpty() ? null : fromSnbt;
        }
        if (val.isMap()) {
            if (!val.node("snbt").virtual()) {
                String snbt = val.node("snbt").getString();
                ItemStack fromSnbt = FabricItemStackCodec.decode(plugin.getServer(), snbt == null ? "" : snbt);
                return fromSnbt.isEmpty() ? null : fromSnbt;
            }
            if (!val.node("material").virtual()) {
                try {
                    ItemStack stack = FabricConfigurateItemParser.parseMenuItemStack(plugin, this, val);
                    return stack.isEmpty() ? null : stack;
                } catch (Exception ignored) {
                    return null;
                }
            }
            String eq = val.node("==").getString("");
            if (eq.contains("ItemStack") || !val.node("type").virtual()) {
                ItemStack legacy = FabricConfigurateItemParser.parseLegacyBukkitItemMap(val);
                return legacy.isEmpty() ? null : legacy;
            }
        }
        return null;
    }

    public void saveToDisk() {
        File file = new File(plugin.getPluginFolder(), "items.yml");
        try {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            ConfigurationNode root = file.exists() ? FileUtil.loadYamlFile(file) : BasicConfigurationNode.root();
            root.node("items").set(null);
            for (Map.Entry<NamespacedId, ItemStack> entry : items.entrySet()) {
                String encoded = FabricItemStackCodec.encode(plugin.getServer(), entry.getValue());
                root.node("items").node(entry.getKey().toString()).set(encoded);
            }
            FileUtil.saveYamlFile(file, root);
        } catch (IOException e) {
            plugin.logger().warn("Failed to save items.yml: " + e.getMessage());
        }
    }

    @Override
    public boolean containsItem(NamespacedId key) {
        return items.containsKey(key);
    }

    @Override
    public void giveItem(User user, NamespacedId key, int amount) {
        ServerPlayer player = FabricUser.getPlayer(user);
        if (player == null) {
            return;
        }
        ItemStack template = getItem(key);
        if (template == null || template.isEmpty()) {
            return;
        }
        plugin.getScheduler().executeSync(() -> {
            ItemStack item = template.copy();
            int count = amount == -1 ? template.getCount() : Math.max(1, amount);
            item.setCount(count);
            ItemStack leftover = FabricItemInventoryUtil.addItemToInventory(player.getInventory(), item);
            if (leftover != null && !leftover.isEmpty()) {
                user.getUnclaimedItems().add(new KeyIntPair(key.toString(), leftover.getCount()));
                String msg = plugin.getPrefix(user.getLocale()) + plugin.getMsg(LevelerMessage.UNCLAIMED_ITEM, user.getLocale());
                player.sendSystemMessage(Component.literal(msg));
            }
        });
    }

    @Override
    public int getItemAmount(NamespacedId key) {
        ItemStack item = items.get(key);
        if (item == null) {
            return 1;
        }
        return item.getCount();
    }

    @Override
    public @Nullable String getEffectiveItemName(NamespacedId key) {
        ItemStack item = items.get(key);
        if (item == null || item.isEmpty()) {
            return null;
        }
        if (item.has(DataComponents.ITEM_NAME)) {
            Component name = item.get(DataComponents.ITEM_NAME);
            return name == null ? null : name.getString();
        }
        return BuiltInRegistries.ITEM.getKey(item.getItem()).getPath().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    @Override
    public SourceMenuItems<?> getSourceMenuItems() {
        return sourceMenuItems;
    }
}
