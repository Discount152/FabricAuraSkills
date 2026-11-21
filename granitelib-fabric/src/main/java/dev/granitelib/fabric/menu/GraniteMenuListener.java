package dev.granitelib.fabric.menu;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;

/**
 * Callback owned by one load of a menu. {@link #onReplaced()} runs when a reload installs another
 * listener, and that listener must not stay registered.
 */
public interface GraniteMenuListener {

    void onOpen(GraniteMenuTemplate template);

    /**
     * Open-by-id supplies the typed context and page. The default keeps listeners that only
     * implement {@link #onOpen(GraniteMenuTemplate)}.
     */
    default void onOpen(GraniteMenuTemplate template, GraniteMenuContext<?> context) {
        onOpen(template);
    }

    void onReplaced();
}
