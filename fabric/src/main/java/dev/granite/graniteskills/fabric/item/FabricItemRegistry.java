package dev.granite.graniteskills.fabric.item;

import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.common.item.ItemGrantExpiry;
import dev.granite.graniteskills.common.item.ItemRegistry;
import dev.granite.graniteskills.common.message.type.LevelerMessage;
import dev.granite.graniteskills.common.source.SourceMenuItems;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.data.KeyIntPair;
import dev.granite.graniteskills.common.util.file.FileUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricSourceMenuItems;
import dev.granite.graniteskills.fabric.FabricUser;
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

    private final FabricGraniteSkills plugin;
    private final Map<NamespacedId, ItemStack> items = new ConcurrentHashMap<>();
    private final FabricSourceMenuItems sourceMenuItems;

    public FabricItemRegistry(FabricGraniteSkills plugin) {
        this.plugin = plugin;
        this.sourceMenuItems = new FabricSourceMenuItems(plugin, this);
    }

    public void register(NamespacedId key, ItemStack item) {
        ItemStack existing = items.putIfAbsent(key, item.copy());
        if (existing != null) {
            plugin.logger().warn("Duplicate item id " + key + " was ignored; the first registration stays");
        }
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
                applyYamlExpiration(stack, val);
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

    private static void applyYamlExpiration(ItemStack stack, ConfigurationNode node) {
        if (stack == null || stack.isEmpty() || node == null || !node.isMap()) {
            return;
        }
        long expiration = node.node(FabricItemExpiry.EXPIRATION_TIME).getLong(0L);
        if (expiration > 0L) {
            FabricItemExpiry.writeExpiration(stack, expiration);
        }
    }

    public boolean isGrantable(NamespacedId key, long nowEpochMillis) {
        ItemStack template = items.get(key);
        if (template == null || template.isEmpty()) {
            return false;
        }
        return ItemGrantExpiry.shouldGive(FabricItemExpiry.readExpiration(template), nowEpochMillis);
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

    /**
     * Server thread: try to add {@code count} of a registered item into the main inventory and hotbar.
     *
     * @return how many items did not fit
     */
    public int tryMergeRegisteredItemCount(ServerPlayer player, NamespacedId id, int count) {
        if (count <= 0) {
            return 0;
        }
        ItemStack template = getItem(id);
        if (template == null || template.isEmpty()) {
            return count;
        }
        if (FabricItemExpiry.amountToGive(template, count, System.currentTimeMillis()) == 0) {
            return count;
        }
        ItemStack toGive = template.copy();
        toGive.setCount(count);
        ItemStack leftover = FabricItemInventoryUtil.addItemToInventory(player.getInventory(), toGive);
        if (leftover == null || leftover.isEmpty()) {
            return 0;
        }
        return leftover.getCount();
    }

    /**
     * Server thread: give a registered item. When {@code amount} is {@code -1}, uses the template stack size.
     *
     * @return the requested count, or {@code -1} if the key is not registered
     */
    public int addRegisteredItemToInventory(User user, ServerPlayer player, NamespacedId key, int amount) {
        ItemStack template = getItem(key);
        if (template == null || template.isEmpty()) {
            return -1;
        }
        int count = amount == -1 ? template.getCount() : Math.max(1, amount);
        if (FabricItemExpiry.amountToGive(template, count, System.currentTimeMillis()) == 0) {
            return 0;
        }
        int notFit = tryMergeRegisteredItemCount(player, key, count);
        if (notFit > 0) {
            user.getUnclaimedItems().add(new KeyIntPair(key.toString(), notFit));
            user.setShouldSave(true);
            String msg = plugin.getPrefix(user.getLocale()) + plugin.getMsg(LevelerMessage.UNCLAIMED_ITEM, user.getLocale());
            user.sendMessage(msg);
        }
        return count;
    }

    @Override
    public void giveItem(User user, NamespacedId key, int amount) {
        ServerPlayer player = FabricUser.getPlayer(user);
        if (player == null || getItem(key) == null) {
            return;
        }
        plugin.getScheduler().executeSync(() -> {
            ItemStack template = getItem(key);
            if (template == null) {
                return;
            }
            int count = amount == -1 ? template.getCount() : Math.max(1, amount);
            if (FabricItemExpiry.amountToGive(template, count, System.currentTimeMillis()) == 0) {
                return;
            }
            addRegisteredItemToInventory(user, player, key, amount);
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
