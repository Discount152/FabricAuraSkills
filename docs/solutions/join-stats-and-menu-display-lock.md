# Join stats refresh and menu display lock (2.3.246 / alpha.23)

## Character levels (max HP / stats) revert on leave and rejoin

Problem: skill levels and XP are stored on the `User` and reload correctly. Max health and other
traits are attribute modifiers on the `ServerPlayer`. Fabric only rebound the user and reloaded
traits on respawn, not on join. After reconnect the new player entity had vanilla hearts again.
Bukkit’s `HpTrait` listens to `PlayerJoinEvent`; Fabric had no equivalent.

A second race: disconnect saves and removes the user asynchronously. A fast reconnect can find
`hasUser == true`, skip `loadUserAsync`, and keep a `FabricUser` pointed at the old player. The
async remove can then drop the profile while the player is online.

Fix: on JOIN, if a `User` is already loaded, `refreshOnlineUser` rebinds, recalculates stats,
reapplies item modifiers, and reloads all traits. Disconnect only `removeUser` when that UUID is
still offline after the save; if they reconnected, refresh again instead.

Verify: raise max HP via Healing/HP rewards, note hearts, disconnect, rejoin — hearts must match
`/stats`. Check `FabricPlayerLifecycle.refreshOnlineUser`.

## Inventory Management emptying skills menus

Problem: click guards block vanilla shift-click and pick-all, but mods that walk the `Container`
API (`getItem` / `removeItem` / take-all) could still move display stacks.

Fix (GraniteLib alpha.23): after populate, `GraniteClickable9x6Handler` copies stacks onto
`DisplaySlot`s, clears the backing `GraniteDisplayContainer`, and locks it. Container helpers then
see an empty chest; the client still shows the display slots. Clicks still fire `topClick`.

Verify: open `/skills`, use Inventory Management “Take all” / “Sort” — player inventory must not
gain menu items; menu icons must stay.
