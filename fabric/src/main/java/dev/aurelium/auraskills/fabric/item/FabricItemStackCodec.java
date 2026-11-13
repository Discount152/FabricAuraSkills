package dev.aurelium.auraskills.fabric.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

/**
 * Round-trips stacks through {@link ItemStack#CODEC} for {@code items.yml} persistence on Fabric.
 */
public final class FabricItemStackCodec {

    private FabricItemStackCodec() {
    }

    private static RegistryOps<Tag> ops(MinecraftServer server) {
        return RegistryOps.create(NbtOps.INSTANCE, server.registryAccess());
    }

    public static ItemStack decode(MinecraftServer server, String snbt) {
        if (snbt == null || snbt.isBlank()) {
            return ItemStack.EMPTY;
        }
        try {
            CompoundTag parsed = TagParser.parseCompoundFully(snbt.trim());
            DataResult<ItemStack> r = ItemStack.CODEC.parse(ops(server), parsed);
            return r.getOrThrow(msg -> new IllegalStateException(msg));
        } catch (CommandSyntaxException | IllegalStateException e) {
            return ItemStack.EMPTY;
        }
    }

    public static String encode(MinecraftServer server, ItemStack stack) {
        if (stack.isEmpty()) {
            return "{}";
        }
        try {
            DataResult<Tag> r = ItemStack.CODEC.encodeStart(ops(server), stack);
            Tag el = r.getOrThrow(msg -> new IllegalStateException(msg));
            return el.toString();
        } catch (Exception e) {
            return "{}";
        }
    }
}
