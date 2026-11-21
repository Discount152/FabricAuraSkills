package dev.granitelib.consumer;

import dev.granitelib.fabric.menu.GraniteClickActions;
import dev.granitelib.fabric.menu.GraniteLoadedMenuRegistry;
import dev.granitelib.fabric.menu.GraniteMenuBuilder;
import dev.granitelib.fabric.menu.GraniteMenuListener;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

/**
 * A menu user that links only to GraniteLib. It does not reference Granite Skills.
 */
public final class IndependentMenuConsumer {

    private final GraniteLoadedMenuRegistry registry = new GraniteLoadedMenuRegistry();
    private final GraniteClickActions.Actor actor =
            new GraniteClickActions.Actor(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), "Consumer");
    private final Listener listener = new Listener();
    private String menuId = "";
    private String title = "";
    private String action = "";
    private String actionActor = "";

    public void openMenu(String id, String menuTitle) {
        registry.load(GraniteMenuBuilder.menu(id).title(menuTitle).rows(1).build(), listener);
        registry.open(id);
    }

    public void runAction(String actionName) {
        boolean ran = GraniteClickActions.dispatch(actor, actionName, 0, (who, action) -> true, click -> {
            this.action = click.action();
            this.actionActor = click.actor().name();
        });
        if (!ran) {
            throw new IllegalStateException("action did not run");
        }
    }

    public void reload(Path file) throws IOException {
        registry.reload(menuId, file, listener);
        title = registry.template(menuId).title();
    }

    public String menuId() {
        return menuId;
    }

    public String title() {
        return title;
    }

    public String action() {
        return action;
    }

    public String actionActor() {
        return actionActor;
    }

    private final class Listener implements GraniteMenuListener {
        @Override
        public void onOpen(GraniteMenuTemplate template) {
            menuId = template.id();
            title = template.title();
        }

        @Override
        public void onReplaced() {
        }
    }
}
