# Adventure-to-native text conversion and level progression paging (2.3.243–2.3.244)

## Text conversion lost content and events

Problem: `FabricTextAdapter` walked only `TextComponent` content plus color and five decorations.
Translatable keys and arguments, keybinds, scores, selectors, click/hover events, fonts, insertion
and shadow color were dropped. The earlier codec-only version fell back to a legacy string when the
26.3 codec rejected Adventure's JSON, which is what made level-up chat plain.

Fix: map text, translatable (key, fallback, arguments recursively), keybind and score content and
every style field directly. Selector, NBT, object content, custom/dialog clicks, and item hovers with
data components are decoded through `ComponentSerialization.CODEC` one node or event at a time. A
rejected node keeps its readable text, style and children; a rejected event is dropped with a
one-time warning.

Gotcha: `TextColor.fromRgb` makes an unnamed color, so `<gold>` serialized as `#FFAA00`. Map
`NamedTextColor` through `TextColor.parseColor(NamedTextColor.NAMES.key(color))` so named colors
stay named (Geyser maps named colors to exact Bedrock codes).

Verify: `FabricTextAdapterTest`; probes `action-bar`, `chat`, `title`, `levelup-chat`.

## Level progression Next Page and Back returned page 1

Problem: after the GraniteLib routing change (2.3.241), the bridge rebuilt the level progression
context from skill and list page only, dropping `progressionPage`. History also stored the skills
list page under `level_progression`, and Back passed it as the return page.

Fix (2.3.244): pass the whole `FabricMenuContext` to `openGameplayLevelProgression`. History keeps
the skills list page in `Place.page` (the "back to skills" button needs it) and stores the progression
page in `Place.sort`, which `level_progression` does not otherwise use. Back reopens with
`withListPage(place.page()).withProgressionPage(parseInt(place.sort()))`.

Gotcha: 2.3.243 stored only the progression page in `Place.page`. That fixed Back but dropped the
return list page; `menu-level-probe` (history page must equal the list page passed in) caught it.
`GraniteMenuHistory.Place` has one page field, so a menu with two pages must use the spare field.

Why it was missed: the menu probe clicked Next Page without checking what opened. Probes must assert
the post-click title and `GraniteMenuHistory.currentPlace` after every click.

Verify: `menu-migration-probe` (`nextPageClick ... Page 2 of`, `backToProgression ... page=1, sort=1`)
and `menu-level-probe` (`page=1`).
