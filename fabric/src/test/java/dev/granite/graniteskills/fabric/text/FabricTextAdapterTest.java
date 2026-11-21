package dev.granite.graniteskills.fabric.text;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricTextAdapterTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static net.minecraft.network.chat.Component convert(Component adventure) {
        return FabricTextAdapter.toNativeText(adventure, null);
    }

    @Test
    void literalKeepsTextColorDecorationsAndChildren() {
        var nativeText = convert(Component.text("Skill ", NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD)
                .append(Component.text("Up", NamedTextColor.GOLD)));

        assertEquals("Skill ", ((PlainTextContents.LiteralContents) nativeText.getContents()).text());
        assertEquals(NamedTextColor.AQUA.value(), nativeText.getStyle().getColor().getValue());
        assertTrue(nativeText.getStyle().isBold());
        var child = nativeText.getSiblings().getFirst();
        assertEquals(NamedTextColor.GOLD.value(), child.getStyle().getColor().getValue());
        assertEquals("Skill Up", nativeText.getString());
    }

    @Test
    void namedColorsStayNamedAndHexColorsStayHex() {
        var nativeText = convert(Component.text("a", NamedTextColor.GOLD)
                .append(Component.text("b", TextColor.color(0x123456))));

        assertEquals(net.minecraft.network.chat.TextColor.fromLegacyFormat(ChatFormatting.GOLD),
                nativeText.getStyle().getColor());
        assertEquals("gold", nativeText.getStyle().getColor().toString());
        assertEquals("#123456", nativeText.getSiblings().getFirst().getStyle().getColor().toString());
    }

    @Test
    void explicitFalseDecorationIsKeptAsAReset() {
        var nativeText = convert(Component.text("a").decorate(TextDecoration.BOLD)
                .append(Component.text("b").decoration(TextDecoration.BOLD, false)
                        .decoration(TextDecoration.ITALIC, false)));

        Style childStyle = nativeText.getSiblings().getFirst().getStyle();
        assertEquals(Style.EMPTY.withBold(false).withItalic(false), childStyle);
    }

    @Test
    void shadowInsertionAndFontAreCopied() {
        var nativeText = convert(Component.text("x")
                .shadowColor(ShadowColor.shadowColor(0x80112233))
                .insertion("/skills")
                .font(Key.key("minecraft", "uniform")));

        Style style = nativeText.getStyle();
        assertEquals(0x80112233, style.getShadowColor());
        assertEquals("/skills", style.getInsertion());
        assertEquals(new FontDescription.Resource(Identifier.fromNamespaceAndPath("minecraft", "uniform")),
                style.getFont());
    }

    @Test
    void translatableKeepsKeyFallbackAndArguments() {
        var nativeText = convert(Component.translatable("graniteskills.test.key", "Fallback %s %s",
                Component.text("Farming", NamedTextColor.GREEN), Component.text(5)));

        var contents = assertInstanceOf(TranslatableContents.class, nativeText.getContents());
        assertEquals("graniteskills.test.key", contents.getKey());
        assertEquals("Fallback %s %s", contents.getFallback());
        assertEquals(2, contents.getArgs().length);
        var first = assertInstanceOf(net.minecraft.network.chat.Component.class, contents.getArgs()[0]);
        assertEquals("Farming", first.getString());
        assertEquals(NamedTextColor.GREEN.value(), first.getStyle().getColor().getValue());
        assertEquals("Fallback Farming 5", nativeText.getString());
    }

    @Test
    void translatableWithoutFallbackResolvesVanillaKey() {
        var nativeText = convert(Component.translatable("item.minecraft.diamond"));

        var contents = assertInstanceOf(TranslatableContents.class, nativeText.getContents());
        assertNull(contents.getFallback());
        assertEquals("item.minecraft.diamond", contents.getKey());
    }

    @Test
    void keybindAndScoreContentAreMapped() {
        var keybind = convert(Component.keybind("key.jump"));
        assertEquals("key.jump", assertInstanceOf(KeybindContents.class, keybind.getContents()).getName());

        var score = convert(Component.score("Steve", "kills"));
        var contents = assertInstanceOf(ScoreContents.class, score.getContents());
        assertEquals("kills", contents.objective());
        assertEquals("Steve", contents.name().right().orElseThrow());
    }

    @Test
    void selectorContentGoesThroughTheCodec() {
        var nativeText = convert(Component.selector("@p").color(NamedTextColor.RED));

        var contents = assertInstanceOf(SelectorContents.class, nativeText.getContents());
        assertEquals("@p", contents.selector().source());
        assertEquals(NamedTextColor.RED.value(), nativeText.getStyle().getColor().getValue());
    }

    @Test
    void clickEventsMapToNativeRecords() {
        assertEquals(new net.minecraft.network.chat.ClickEvent.RunCommand("/skills"),
                convert(Component.text("x").clickEvent(ClickEvent.runCommand("/skills"))).getStyle().getClickEvent());
        assertEquals(new net.minecraft.network.chat.ClickEvent.SuggestCommand("/sk "),
                convert(Component.text("x").clickEvent(ClickEvent.suggestCommand("/sk "))).getStyle().getClickEvent());
        assertEquals(new net.minecraft.network.chat.ClickEvent.CopyToClipboard("copied"),
                convert(Component.text("x").clickEvent(ClickEvent.copyToClipboard("copied"))).getStyle().getClickEvent());
        assertEquals(new net.minecraft.network.chat.ClickEvent.OpenUrl(URI.create("https://example.com/a")),
                convert(Component.text("x").clickEvent(ClickEvent.openUrl("https://example.com/a"))).getStyle().getClickEvent());
        assertEquals(new net.minecraft.network.chat.ClickEvent.ChangePage(3),
                convert(Component.text("x").clickEvent(ClickEvent.changePage(3))).getStyle().getClickEvent());
    }

    @Test
    void hoverEventsMapTextItemAndEntity() {
        var text = convert(Component.text("x").hoverEvent(HoverEvent.showText(
                Component.text("Tip", NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC))));
        var showText = assertInstanceOf(net.minecraft.network.chat.HoverEvent.ShowText.class,
                text.getStyle().getHoverEvent());
        assertEquals("Tip", showText.value().getString());
        assertEquals(NamedTextColor.YELLOW.value(), showText.value().getStyle().getColor().getValue());
        assertTrue(showText.value().getStyle().isItalic());

        var item = convert(Component.text("x").hoverEvent(HoverEvent.showItem(Key.key("minecraft", "diamond"), 4)));
        var showItem = assertInstanceOf(net.minecraft.network.chat.HoverEvent.ShowItem.class,
                item.getStyle().getHoverEvent());
        assertEquals(Items.DIAMOND, showItem.item().item().value());
        assertEquals(4, showItem.item().count());

        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000042");
        var entity = convert(Component.text("x").hoverEvent(HoverEvent.showEntity(
                Key.key("minecraft", "zombie"), id, Component.text("Bob"))));
        var showEntity = assertInstanceOf(net.minecraft.network.chat.HoverEvent.ShowEntity.class,
                entity.getStyle().getHoverEvent());
        assertEquals(Identifier.fromNamespaceAndPath("minecraft", "zombie"),
                BuiltInRegistries.ENTITY_TYPE.getKey(showEntity.entity().type));
        assertEquals(id, showEntity.entity().uuid);
        assertEquals("Bob", showEntity.entity().name.orElseThrow().getString());
    }

    @Test
    void miniMessageLevelUpLineKeepsStylesAndEventsWithoutRawTags() {
        Component adventure = FabricTextFormatter.toComponent(
                "<dark_aqua><strikethrough>----</strikethrough> <aqua><bold>Farming</bold> "
                        + "<hover:show_text:'<red>+1❤ Health'><click:run_command:'/skills'>1➜2</click></hover>");
        var nativeText = convert(adventure);
        String plain = nativeText.getString();

        assertEquals("---- Farming 1➜2", plain);
        assertFalse(plain.contains("<") || plain.contains("§") || plain.contains("&"), plain);
        var styled = new java.util.ArrayList<Style>();
        nativeText.visit((style, part) -> {
            if (!part.isEmpty()) {
                styled.add(style);
            }
            return java.util.Optional.empty();
        }, Style.EMPTY);
        assertTrue(styled.stream().anyMatch(Style::isStrikethrough));
        assertTrue(styled.stream().anyMatch(Style::isBold));
        Style numbers = styled.getLast();
        assertEquals(new net.minecraft.network.chat.ClickEvent.RunCommand("/skills"), numbers.getClickEvent());
        var hover = assertInstanceOf(net.minecraft.network.chat.HoverEvent.ShowText.class, numbers.getHoverEvent());
        assertEquals("+1❤ Health", hover.value().getString());
    }
}
