package dev.aurelium.auraskills.fabric.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.MinecraftServer;

/**
 * Round-trips stacks through {@link ItemStack#CODEC} for {@code items.yml} persistence on Fabric.
 */
public final class FabricItemStackCodec {

    private FabricItemStackCodec() {
    }

    private static RegistryOps<NbtElement> ops(MinecraftServer server) {
        return RegistryOps.of(NbtOps.INSTANCE, server.getRegistryManager());
    }

    public static ItemStack decode(MinecraftServer server, String snbt) {
        if (snbt == null || snbt.isBlank()) {
            return ItemStack.EMPTY;
        }
        try {
            NbtCompound parsed = StringNbtReader.readCompound(snbt.trim());
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
            DataResult<NbtElement> r = ItemStack.CODEC.encodeStart(ops(server), stack);
            NbtElement el = r.getOrThrow(msg -> new IllegalStateException(msg));
            return el.toString();
        } catch (Exception e) {
            return "{}";
        }
    }
}
