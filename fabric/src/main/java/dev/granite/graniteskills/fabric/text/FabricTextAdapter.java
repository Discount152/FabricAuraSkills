package dev.granite.graniteskills.fabric.text;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.NBTComponent;
import net.kyori.adventure.text.ScoreComponent;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Kyori Adventure components to vanilla {@link net.minecraft.network.chat.Component}.
 * Text, translatable, keybind and score content and every style field are mapped directly.
 * Selector, NBT and object content, custom and dialog clicks, and item hovers carrying data
 * go through the 26.3 component codec. A content node the codec rejects keeps its readable
 * text, style and children; an event the codec rejects is dropped and logged once.
 */
public final class FabricTextAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FabricTextAdapter.class);
    private static final GsonComponentSerializer GSON = GsonComponentSerializer.builder()
            .options(JSONOptions.byDataVersion().at(Integer.MAX_VALUE))
            .build();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private final DynamicOps<JsonElement> ops;

    private FabricTextAdapter(DynamicOps<JsonElement> ops) {
        this.ops = ops;
    }

    public static net.minecraft.network.chat.Component toNativeText(
            net.kyori.adventure.text.Component adventure, @Nullable MinecraftServer server) {
        if (adventure == null || net.kyori.adventure.text.Component.empty().equals(adventure)) {
            return net.minecraft.network.chat.Component.empty();
        }
        DynamicOps<JsonElement> ops = server == null
                ? JsonOps.INSTANCE
                : RegistryOps.create(JsonOps.INSTANCE, server.registryAccess());
        return new FabricTextAdapter(ops).walk(adventure);
    }

    private MutableComponent walk(net.kyori.adventure.text.Component adventure) {
        MutableComponent nativeComponent = content(adventure);
        nativeComponent.setStyle(style(adventure.style()));
        for (net.kyori.adventure.text.Component child : adventure.children()) {
            nativeComponent.append(walk(child));
        }
        return nativeComponent;
    }

    private MutableComponent content(net.kyori.adventure.text.Component adventure) {
        if (adventure instanceof TextComponent text) {
            return net.minecraft.network.chat.Component.literal(text.content());
        }
        if (adventure instanceof TranslatableComponent translatable) {
            Object[] args = translationArgs(translatable.arguments());
            return translatable.fallback() == null
                    ? net.minecraft.network.chat.Component.translatable(translatable.key(), args)
                    : net.minecraft.network.chat.Component.translatableWithFallback(
                            translatable.key(), translatable.fallback(), args);
        }
        if (adventure instanceof KeybindComponent keybind) {
            return net.minecraft.network.chat.Component.keybind(keybind.keybind());
        }
        if (adventure instanceof ScoreComponent score) {
            return net.minecraft.network.chat.Component.score(score.name(), score.objective());
        }
        net.kyori.adventure.text.Component bare = adventure
                .children(List.of())
                .style(net.kyori.adventure.text.format.Style.empty());
        net.minecraft.network.chat.Component decoded = decode(bare);
        if (decoded != null) {
            return decoded.plainCopy();
        }
        warnOnce("content:" + adventure.getClass().getSimpleName(),
                "Could not convert {} content to a native component; sending its readable text instead",
                adventure.getClass().getSimpleName());
        return net.minecraft.network.chat.Component.literal(readableText(adventure));
    }

    private Object[] translationArgs(List<TranslationArgument> arguments) {
        List<Object> nativeArgs = new ArrayList<>(arguments.size());
        for (TranslationArgument argument : arguments) {
            Object value = argument.value();
            if (value instanceof net.kyori.adventure.text.Component component) {
                nativeArgs.add(walk(component));
            } else {
                nativeArgs.add(value);
            }
        }
        return nativeArgs.toArray();
    }

    private Style style(net.kyori.adventure.text.format.Style style) {
        Style vanilla = Style.EMPTY;
        if (style.color() != null) {
            TextColor named = style.color() instanceof NamedTextColor namedColor
                    ? TextColor.parseColor(NamedTextColor.NAMES.key(namedColor)).result().orElse(null)
                    : null;
            vanilla = vanilla.withColor(named != null ? named : TextColor.fromRgb(style.color().value()));
        }
        ShadowColor shadow = style.shadowColor();
        if (shadow != null) {
            vanilla = vanilla.withShadowColor(shadow.value());
        }
        vanilla = decoration(vanilla, style, TextDecoration.BOLD, Style::withBold);
        vanilla = decoration(vanilla, style, TextDecoration.ITALIC, Style::withItalic);
        vanilla = decoration(vanilla, style, TextDecoration.UNDERLINED, Style::withUnderlined);
        vanilla = decoration(vanilla, style, TextDecoration.STRIKETHROUGH, Style::withStrikethrough);
        vanilla = decoration(vanilla, style, TextDecoration.OBFUSCATED, Style::withObfuscated);
        if (style.insertion() != null) {
            vanilla = vanilla.withInsertion(style.insertion());
        }
        if (style.font() != null) {
            Identifier font = identifier(style.font());
            if (font != null) {
                vanilla = vanilla.withFont(new FontDescription.Resource(font));
            }
        }
        if (style.clickEvent() != null) {
            ClickEvent click = click(style.clickEvent());
            if (click != null) {
                vanilla = vanilla.withClickEvent(click);
            }
        }
        if (style.hoverEvent() != null) {
            HoverEvent hover = hover(style.hoverEvent());
            if (hover != null) {
                vanilla = vanilla.withHoverEvent(hover);
            }
        }
        return vanilla;
    }

    private static Style decoration(
            Style vanilla,
            net.kyori.adventure.text.format.Style style,
            TextDecoration decoration,
            BiFunction<Style, Boolean, Style> apply) {
        TextDecoration.State state = style.decoration(decoration);
        if (state == TextDecoration.State.NOT_SET) {
            return vanilla;
        }
        return apply.apply(vanilla, state == TextDecoration.State.TRUE);
    }

    private @Nullable ClickEvent click(net.kyori.adventure.text.event.ClickEvent event) {
        net.kyori.adventure.text.event.ClickEvent.Action action = event.action();
        net.kyori.adventure.text.event.ClickEvent.Payload payload = event.payload();
        try {
            if (payload instanceof net.kyori.adventure.text.event.ClickEvent.Payload.Text text) {
                String value = text.value();
                if (action == net.kyori.adventure.text.event.ClickEvent.Action.OPEN_URL) {
                    return new ClickEvent.OpenUrl(URI.create(value));
                }
                if (action == net.kyori.adventure.text.event.ClickEvent.Action.OPEN_FILE) {
                    return new ClickEvent.OpenFile(value);
                }
                if (action == net.kyori.adventure.text.event.ClickEvent.Action.RUN_COMMAND) {
                    return new ClickEvent.RunCommand(value);
                }
                if (action == net.kyori.adventure.text.event.ClickEvent.Action.SUGGEST_COMMAND) {
                    return new ClickEvent.SuggestCommand(value);
                }
                if (action == net.kyori.adventure.text.event.ClickEvent.Action.COPY_TO_CLIPBOARD) {
                    return new ClickEvent.CopyToClipboard(value);
                }
            }
            if (payload instanceof net.kyori.adventure.text.event.ClickEvent.Payload.Int page
                    && action == net.kyori.adventure.text.event.ClickEvent.Action.CHANGE_PAGE) {
                return new ClickEvent.ChangePage(page.integer());
            }
        } catch (IllegalArgumentException e) {
            warnOnce("click:" + action, "Dropping {} click event: {}", action, e.getMessage());
            return null;
        }
        net.minecraft.network.chat.Component carrier = decode(net.kyori.adventure.text.Component.text("")
                .clickEvent(event));
        ClickEvent decoded = carrier == null ? null : carrier.getStyle().getClickEvent();
        if (decoded == null) {
            warnOnce("click:" + action, "Dropping {} click event the 26.3 codec rejected", action);
        }
        return decoded;
    }

    private @Nullable HoverEvent hover(net.kyori.adventure.text.event.HoverEvent<?> event) {
        Object value = event.value();
        if (value instanceof net.kyori.adventure.text.Component text) {
            return new HoverEvent.ShowText(walk(text));
        }
        if (value instanceof net.kyori.adventure.text.event.HoverEvent.ShowItem item
                && item.dataComponents().isEmpty()
                && item.nbt() == null) {
            Identifier id = identifier(item.item());
            Optional<Item> nativeItem = id == null ? Optional.empty() : BuiltInRegistries.ITEM.getOptional(id);
            if (nativeItem.isPresent()) {
                return new HoverEvent.ShowItem(new ItemStackTemplate(nativeItem.get(), item.count()));
            }
        }
        if (value instanceof net.kyori.adventure.text.event.HoverEvent.ShowEntity entity) {
            Identifier id = identifier(entity.type());
            Optional<EntityType<?>> type = id == null
                    ? Optional.empty()
                    : BuiltInRegistries.ENTITY_TYPE.getOptional(id);
            if (type.isPresent()) {
                Optional<net.minecraft.network.chat.Component> name = entity.name() == null
                        ? Optional.empty()
                        : Optional.of(walk(entity.name()));
                return new HoverEvent.ShowEntity(new HoverEvent.EntityTooltipInfo(type.get(), entity.id(), name));
            }
        }
        net.minecraft.network.chat.Component carrier = decode(net.kyori.adventure.text.Component.text("")
                .hoverEvent(event));
        HoverEvent decoded = carrier == null ? null : carrier.getStyle().getHoverEvent();
        if (decoded == null) {
            warnOnce("hover:" + event.action(), "Dropping {} hover event the 26.3 codec rejected", event.action());
        }
        return decoded;
    }

    private @Nullable net.minecraft.network.chat.Component decode(net.kyori.adventure.text.Component adventure) {
        try {
            JsonElement json = GSON.serializeToTree(adventure);
            return ComponentSerialization.CODEC.parse(ops, json).result().orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static @Nullable Identifier identifier(Key key) {
        try {
            return Identifier.fromNamespaceAndPath(key.namespace(), key.value());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String readableText(net.kyori.adventure.text.Component adventure) {
        if (adventure instanceof SelectorComponent selector) {
            return selector.pattern();
        }
        if (adventure instanceof NBTComponent<?, ?> nbt) {
            return nbt.nbtPath();
        }
        return "";
    }

    private static void warnOnce(String key, String message, Object... args) {
        if (WARNED.add(key)) {
            LOGGER.warn(message, args);
        }
    }
}
