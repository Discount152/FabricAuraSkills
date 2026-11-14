package dev.granitelib.fabric.template;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

/** Classpath helpers for read-only template YAML (Sprint 99). */
public final class GraniteTemplateResources {

    private GraniteTemplateResources() {
    }

    /**
     * @param classpathPath e.g. {@code templates/stat_info.yml}
     */
    public static @Nullable InputStream openClasspath(Class<?> anchor, String classpathPath) {
        ClassLoader cl = anchor.getClassLoader();
        if (cl == null) {
            return null;
        }
        return cl.getResourceAsStream(classpathPath);
    }
}
