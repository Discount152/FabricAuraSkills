package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.api.source.type.BlockXpSource;
import dev.granite.graniteskills.api.source.type.DamageXpSource;
import dev.granite.graniteskills.api.source.type.EntityXpSource;
import dev.granite.graniteskills.api.source.type.FishingXpSource;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.antiafk.AntiAfkManager;
import dev.granite.graniteskills.common.antiafk.Check;
import dev.granite.graniteskills.common.antiafk.CheckType;
import dev.granite.graniteskills.common.antiafk.FacingHandler;
import dev.granite.graniteskills.common.antiafk.IdentityHandler;
import dev.granite.graniteskills.common.antiafk.LogLocation;
import dev.granite.graniteskills.common.antiafk.PositionHandler;
import dev.granite.graniteskills.common.ref.LocationRef;
import dev.granite.graniteskills.common.ref.PlayerRef;
import dev.granite.graniteskills.common.region.BlockPosition;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.antiafk.FabricCheckType;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Constructor;

public final class FabricAntiAfkManager extends AntiAfkManager {

    public FabricAntiAfkManager(GraniteSkillsPlugin plugin) {
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
