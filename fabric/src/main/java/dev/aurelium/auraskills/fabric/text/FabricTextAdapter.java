package dev.aurelium.auraskills.fabric.text;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kyori {@link Component} → {@link Text} for chat / action bar / title. Adventure Gson JSON is
 * decoded with {@link TextCodecs} (same data model as the vanilla text component format).
 */
public final class FabricTextAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger("auraskills");
    private static final GsonComponentSerializer GSON = GsonComponentSerializer.gson();

    private FabricTextAdapter() {
    }

    public static Text toNativeText(Component adventure, @Nullable MinecraftServer server) {
        if (adventure == null || Component.empty().equals(adventure)) {
            return Text.empty();
        }
        if (server != null) {
            try {
                String json = GSON.serialize(adventure);
                var ops = RegistryOps.of(JsonOps.INSTANCE, server.getRegistryManager());
                return TextCodecs.CODEC.parse(ops, JsonParser.parseString(json))
                        .result()
                        .orElseGet(() -> Text.literal(FabricTextFormatter.toString(adventure)));
            } catch (Exception e) {
                LOGGER.warn("Could not map Adventure component to NMS Text; using legacy string fallback: {}", e.toString());
            }
        }
        return Text.literal(FabricTextFormatter.toString(adventure));
    }
}
