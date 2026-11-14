package dev.granite.graniteskills.fabric.text;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kyori Adventure components to vanilla {@link net.minecraft.network.chat.Component}. Adventure Gson
 * JSON is decoded with {@link ComponentSerialization} (the vanilla text component codec).
 */
public final class FabricTextAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger("graniteskills");
    private static final GsonComponentSerializer GSON = GsonComponentSerializer.gson();

    private FabricTextAdapter() {
    }

    public static net.minecraft.network.chat.Component toNativeText(
            net.kyori.adventure.text.Component adventure, @Nullable MinecraftServer server) {
        if (adventure == null || net.kyori.adventure.text.Component.empty().equals(adventure)) {
            return net.minecraft.network.chat.Component.empty();
        }
        if (server != null) {
            try {
                String json = GSON.serialize(adventure);
                var ops = RegistryOps.create(JsonOps.INSTANCE, server.registryAccess());
                return ComponentSerialization.CODEC.parse(ops, JsonParser.parseString(json))
                        .result()
                        .orElseGet(() -> net.minecraft.network.chat.Component.literal(FabricTextFormatter.toString(adventure)));
            } catch (Exception e) {
                LOGGER.warn("Could not map Adventure component to NMS Component; using legacy string fallback: {}", e.toString());
            }
        }
        return net.minecraft.network.chat.Component.literal(FabricTextFormatter.toString(adventure));
    }
}
