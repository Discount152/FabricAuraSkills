package dev.granite.graniteskills.bukkit.api;

import dev.granite.graniteskills.api.GraniteSkillsBukkit;
import dev.granite.graniteskills.api.GraniteSkillsBukkitProvider;

import java.lang.reflect.Method;

public class ApiBukkitRegistrationUtil {

    private static final Method REGISTER_METHOD;
    private static final Method UNREGISTER_METHOD;

    static {
        try {
            REGISTER_METHOD = GraniteSkillsBukkitProvider.class.getDeclaredMethod("register", GraniteSkillsBukkit.class);
            REGISTER_METHOD.setAccessible(true);

            UNREGISTER_METHOD = GraniteSkillsBukkitProvider.class.getDeclaredMethod("unregister");
            UNREGISTER_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void register(GraniteSkillsBukkit instance) {
        try {
            REGISTER_METHOD.invoke(null, instance);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public static void unregister() {
        try {
            UNREGISTER_METHOD.invoke(null);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

}
