package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.api.source.type.DamageXpSource;
import dev.aurelium.auraskills.api.source.type.EntityXpSource;
import dev.aurelium.auraskills.api.source.type.FishingXpSource;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.antiafk.AntiAfkManager;
import dev.aurelium.auraskills.common.antiafk.Check;
import dev.aurelium.auraskills.common.antiafk.CheckType;
import dev.aurelium.auraskills.common.antiafk.FacingHandler;
import dev.aurelium.auraskills.common.antiafk.IdentityHandler;
import dev.aurelium.auraskills.common.antiafk.LogLocation;
import dev.aurelium.auraskills.common.antiafk.PositionHandler;
import dev.aurelium.auraskills.common.ref.LocationRef;
import dev.aurelium.auraskills.common.ref.PlayerRef;
import dev.aurelium.auraskills.common.region.BlockPosition;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.antiafk.FabricCheckType;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Constructor;

public final class FabricAntiAfkManager extends AntiAfkManager {

    public FabricAntiAfkManager(AuraSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void registerCheckEvents(Check check) {
    }

    @Override
    public void unregisterCheckEvents(Check check) {
    }

    @Override
    public LogLocation getLogLocation(PlayerRef ref) {
        if (ref instanceof FabricPlayerRef fabric) {
            ServerPlayer player = fabric.player();
            return new LogLocation(
                    new BlockPosition(player.getBlockX(), player.getBlockY(), player.getBlockZ()),
                    FabricUser.worldNameCompat(player));
        }
        return new LogLocation(new BlockPosition(0, 0, 0), "");
    }

    @Override
    protected Constructor<?> getCheckConstructor(Class<? extends Check> checkClass) throws NoSuchMethodException {
        return checkClass.getConstructor(CheckType.class, AntiAfkManager.class);
    }

    @Override
    public CheckType[] getCheckTypes() {
        return FabricCheckType.values();
    }

    public boolean shouldCancel(User user, XpSource source) {
        if (source == null) {
            return false;
        }
        ServerPlayer player = FabricUser.getPlayer(user);
        if (player == null) {
            return false;
        }
        FabricPlayerRef ref = new FabricPlayerRef(player);
        for (CheckType type : getCheckTypes()) {
            Check check = getCheck(type).orElse(null);
            if (check instanceof FabricAfkCheck fabricCheck && fabricCheck.fails(user, source, ref)) {
                return true;
            }
        }
        return false;
    }

    public interface FabricAfkCheck {
        boolean fails(User user, XpSource source, FabricPlayerRef ref);
    }

    public static final class BlockA extends Check implements FabricAfkCheck {
        private final FacingHandler handler;

        public BlockA(CheckType type, AntiAfkManager manager) {
            super(type, manager);
            this.handler = new FacingHandler(optionInt("min_count"), FabricAntiAfkManager::yaw, FabricAntiAfkManager::pitch);
        }

        @Override
        public boolean fails(User user, XpSource source, FabricPlayerRef ref) {
            if (isDisabled() || !(source instanceof BlockXpSource)) {
                return false;
            }
            if (handler.failsCheck(getCheckData(ref), ref)) {
                logFail(ref);
                return true;
            }
            return false;
        }
    }

    public static final class DamageA extends Check implements FabricAfkCheck {
        private final PositionHandler handler;

        public DamageA(CheckType type, AntiAfkManager manager) {
            super(type, manager);
            this.handler = new PositionHandler(optionDouble("max_distance"), optionInt("min_count"), FabricAntiAfkManager::distanceSquared);
        }

        @Override
        public boolean fails(User user, XpSource source, FabricPlayerRef ref) {
            if (isDisabled() || !(source instanceof DamageXpSource)) {
                return false;
            }
            if (handler.failsCheck(getCheckData(ref), ref)) {
                logFail(ref);
                return true;
            }
            return false;
        }
    }

    public static final class DamageB extends Check implements FabricAfkCheck {
        private final FacingHandler handler;

        public DamageB(CheckType type, AntiAfkManager manager) {
            super(type, manager);
            this.handler = new FacingHandler(optionInt("min_count"), FabricAntiAfkManager::yaw, FabricAntiAfkManager::pitch);
        }

        @Override
        public boolean fails(User user, XpSource source, FabricPlayerRef ref) {
            if (isDisabled() || !(source instanceof DamageXpSource)) {
                return false;
            }
            if (handler.failsCheck(getCheckData(ref), ref)) {
                logFail(ref);
                return true;
            }
            return false;
        }
    }

    public static final class DamageC extends Check implements FabricAfkCheck {
        private final IdentityHandler handler;

        public DamageC(CheckType type, AntiAfkManager manager) {
            super(type, manager);
            this.handler = new IdentityHandler(optionInt("min_count"), "previous_entity");
        }

        @Override
        public boolean fails(User user, XpSource source, FabricPlayerRef ref) {
            if (isDisabled() || !(source instanceof DamageXpSource) || FabricXpContext.identity() == null) {
                return false;
            }
            if (handler.failsCheck(getCheckData(ref), FabricXpContext.identity())) {
                logFail(ref);
                return true;
            }
            return false;
        }
    }

    public static final class EntityA extends Check implements FabricAfkCheck {
        private final PositionHandler handler;

        public EntityA(CheckType type, AntiAfkManager manager) {
            super(type, manager);
            this.handler = new PositionHandler(optionDouble("max_distance"), optionInt("min_count"), FabricAntiAfkManager::distanceSquared);
        }

        @Override
        public boolean fails(User user, XpSource source, FabricPlayerRef ref) {
            if (isDisabled() || !(source instanceof EntityXpSource)) {
                return false;
            }
            if (handler.failsCheck(getCheckData(ref), ref)) {
                logFail(ref);
                return true;
            }
            return false;
        }
    }

    public static final class EntityB extends Check implements FabricAfkCheck {
        private final FacingHandler handler;

        public EntityB(CheckType type, AntiAfkManager manager) {
            super(type, manager);
            this.handler = new FacingHandler(optionInt("min_count"), FabricAntiAfkManager::yaw, FabricAntiAfkManager::pitch);
        }

        @Override
        public boolean fails(User user, XpSource source, FabricPlayerRef ref) {
            if (isDisabled() || !(source instanceof EntityXpSource)) {
                return false;
            }
            if (handler.failsCheck(getCheckData(ref), ref)) {
                logFail(ref);
                return true;
            }
            return false;
        }
    }

    public static final class EntityC extends Check implements FabricAfkCheck {
        private final IdentityHandler handler;

        public EntityC(CheckType type, AntiAfkManager manager) {
            super(type, manager);
            this.handler = new IdentityHandler(optionInt("min_count"), "previous_entity");
        }

        @Override
        public boolean fails(User user, XpSource source, FabricPlayerRef ref) {
            if (isDisabled() || !(source instanceof EntityXpSource) || FabricXpContext.identity() == null) {
                return false;
            }
            if (handler.failsCheck(getCheckData(ref), FabricXpContext.identity())) {
                logFail(ref);
                return true;
            }
            return false;
        }
    }

    public static final class FishingA extends Check implements FabricAfkCheck {
        private final FacingHandler handler;

        public FishingA(CheckType type, AntiAfkManager manager) {
            super(type, manager);
            this.handler = new FacingHandler(optionInt("min_count"), FabricAntiAfkManager::yaw, FabricAntiAfkManager::pitch);
        }

        @Override
        public boolean fails(User user, XpSource source, FabricPlayerRef ref) {
            if (isDisabled() || !(source instanceof FishingXpSource)) {
                return false;
            }
            if (handler.failsCheck(getCheckData(ref), ref)) {
                logFail(ref);
                return true;
            }
            return false;
        }
    }

    private static double yaw(PlayerRef ref) {
        if (ref instanceof FabricPlayerRef fabric) {
            return fabric.player().getYRot();
        }
        return 0;
    }

    private static double pitch(PlayerRef ref) {
        if (ref instanceof FabricPlayerRef fabric) {
            return fabric.player().getXRot();
        }
        return 0;
    }

    private static double distanceSquared(LocationRef first, LocationRef second) {
        if (first instanceof FabricLocationRef a && second instanceof FabricLocationRef b) {
            return a.distanceSquared(b);
        }
        return Double.MAX_VALUE;
    }
}
