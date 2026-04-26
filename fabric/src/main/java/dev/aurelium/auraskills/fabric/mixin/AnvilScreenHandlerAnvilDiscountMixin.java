package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAnvilDiscount;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * Bukkit {@code PrepareAnvilEvent} parity: apply the {@code anvil_discount} trait after
 * {@link AnvilScreenHandler#updateResult()} has set the vanilla level cost, so the shown and
 * charged level cost (including Forging XP in {@code FabricAnvilTakeContext} / {@code getLevelCost})
 * stay in sync.
 *
 * <p><strong>Mapping note (Sprint 76A):</strong> {@code player} lives on {@link ForgingScreenHandler}
 * (Yarn), not on {@link AnvilScreenHandler}. A {@code @Shadow} for {@code player} on an
 * {@code AnvilScreenHandler} mixin can be remapped to {@code class_1706.field_22482}, but
 * intermediary {@code field_22482} belongs to {@code class_4861} (the forging handler’s
 * {@code player} field). That shadow then fails at runtime, especially when the mod refmap is not
 * loaded. We use {@link VarHandle} with explicit Yarn vs intermediary names instead of
 * {@code @Shadow}.
 */
@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerAnvilDiscountMixin {

    /** Intermediary {@code ForgingScreenHandler#player} — see Yarn 1.21.11+build.4. */
    private static final String FORGING_PLAYER_INTERMEDIARY = "field_22482";

    /** Intermediary {@code AnvilScreenHandler#levelCost} — see Yarn 1.21.11+build.4. */
    private static final String ANVIL_LEVEL_COST_INTERMEDIARY = "field_7770";

    private static final VarHandle AURASKILLS$FORGING_PLAYER;
    private static final VarHandle AURASKILLS$LEVEL_COST;

    static {
        boolean dev = isDevLike();
        AURASKILLS$FORGING_PLAYER = varHandleForgingPlayer(dev);
        AURASKILLS$LEVEL_COST = varHandleLevelCost(dev);
    }

    private static boolean isDevLike() {
        try {
            return FabricLoader.getInstance().isDevelopmentEnvironment();
        } catch (Throwable ignored) {
            // Unit tests / non-Fabric classpath: Yarn field names.
            return true;
        }
    }

    private static @Nullable VarHandle varHandleForgingPlayer(boolean dev) {
        final MethodHandles.Lookup lookup;
        try {
            lookup = MethodHandles.privateLookupIn(ForgingScreenHandler.class, MethodHandles.lookup());
        } catch (IllegalAccessException e) {
            return null;
        }
        String a = dev ? "player" : FORGING_PLAYER_INTERMEDIARY;
        String b = dev ? FORGING_PLAYER_INTERMEDIARY : "player";
        try {
            return lookup.findVarHandle(ForgingScreenHandler.class, a, PlayerEntity.class);
        } catch (ReflectiveOperationException e1) {
            try {
                return lookup.findVarHandle(ForgingScreenHandler.class, b, PlayerEntity.class);
            } catch (ReflectiveOperationException e2) {
                e1.addSuppressed(e2);
                return null;
            }
        }
    }

    private static @Nullable VarHandle varHandleLevelCost(boolean dev) {
        final MethodHandles.Lookup lookup;
        try {
            lookup = MethodHandles.privateLookupIn(AnvilScreenHandler.class, MethodHandles.lookup());
        } catch (IllegalAccessException e) {
            return null;
        }
        String a = dev ? "levelCost" : ANVIL_LEVEL_COST_INTERMEDIARY;
        String b = dev ? ANVIL_LEVEL_COST_INTERMEDIARY : "levelCost";
        try {
            return lookup.findVarHandle(AnvilScreenHandler.class, a, Property.class);
        } catch (ReflectiveOperationException e1) {
            try {
                return lookup.findVarHandle(AnvilScreenHandler.class, b, Property.class);
            } catch (ReflectiveOperationException e2) {
                e1.addSuppressed(e2);
                return null;
            }
        }
    }

    @Inject(method = "updateResult", at = @At("TAIL"))
    private void auraskills$applyAnvilTraitDiscount(CallbackInfo ci) {
        if (AURASKILLS$FORGING_PLAYER == null || AURASKILLS$LEVEL_COST == null) {
            return;
        }
        AnvilScreenHandler self = (AnvilScreenHandler) (Object) this;
        PlayerEntity player =
                (PlayerEntity) AURASKILLS$FORGING_PLAYER.get((ForgingScreenHandler) self);
        Property levelCost = (Property) AURASKILLS$LEVEL_COST.get(self);
        if (player.getEntityWorld().isClient()) {
            return;
        }
        if (!(player instanceof ServerPlayerEntity sp)) {
            return;
        }
        FabricAnvilDiscount.afterUpdateResult(levelCost, sp);
    }
}
