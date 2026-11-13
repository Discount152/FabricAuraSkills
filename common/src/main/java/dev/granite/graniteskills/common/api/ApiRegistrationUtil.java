package dev.granite.graniteskills.common.api;

import dev.granite.graniteskills.api.GraniteSkillsApi;
import dev.granite.graniteskills.api.GraniteSkillsProvider;

import java.lang.reflect.Method;

public class ApiRegistrationUtil {

    private static final Method REGISTER_METHOD;
    private static final Method UNREGISTER_METHOD;

    static {
        try {
            REGISTER_METHOD = GraniteSkillsProvider.class.getDeclaredMethod("register", GraniteSkillsApi.class);
            REGISTER_METHOD.setAccessible(true);

            UNREGISTER_METHOD = GraniteSkillsProvider.class.getDeclaredMethod("unregister");
            UNREGISTER_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void register(GraniteSkillsApi instance) {
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
