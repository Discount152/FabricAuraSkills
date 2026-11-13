package dev.granite.graniteskills.fabric;

/**
 * Identity of the entity involved in the XP gain currently being applied.
 * Anti-AFK checks that compare the same attacker or victim read this.
 */
public final class FabricXpContext {

    private static final ThreadLocal<Object> IDENTITY = new ThreadLocal<>();

    private FabricXpContext() {
    }

    public static void run(Object identity, Runnable action) {
        Object previous = IDENTITY.get();
        IDENTITY.set(identity);
        try {
            action.run();
        } finally {
            if (previous == null) {
                IDENTITY.remove();
            } else {
                IDENTITY.set(previous);
            }
        }
    }

    public static Object identity() {
        return IDENTITY.get();
    }
}
