# Sprint 57 — manual validation: `/sources`, `/resethealth`

**Scope:** Fabric **Granite Skills** only. **Java** and **Bedrock (Geyser/Floodgate)** where noted.

## `/sources`

### Java (client)

1. Join a dedicated Fabric server with this mod; ensure you have **`auraskills.command.sources`** (default **true** with a typical LuckPerms-style `auraskills.command.user` bundle, or grant the node explicitly).
2. Run **`/sources farming`** (or another **enabled** skill key from tab completion).
3. Expect a **multi-line chat** message: header (skill name, sort mode, entry count, page), then **up to 28** lines per message chunk, each line: **index**, **localized source display name**, **base XP** (from config), **source id**, **source type id**.
4. Run **`/sources farming descending`** (and **`alphabetical`**, **`reverse_alphabetical`**). Order should change in a sensible way (XP order vs name order).
5. Run **`/sources farming typo_sort`** — expect an **error** listing valid sort names.
6. **Permission:** revoke **`auraskills.command.sources`**; command should **deny** (fabric-permissions-api + LP).
7. **Console:** run **`/sources farming` from the server console** — should fail **players only** (same as Bukkit: command is **player**-targeted in reference).

### Bedrock (Geyser/Floodgate)

- Repeat the **player** steps above. **Chat** output should still appear; if translations differ, the **keys** are in **`assets/auraskills/lang/en_us.json`** (`auraskills.granite.command.sources.*`).

### Regression (related)

- **`/item give`**, **claim flow**, and **XP** from sources: unchanged; this sprint does not alter **`LevelManager`** or source YAML loading.

## `/resethealth`

**Warning:** This **rewrites** `playerdata/<uuid>.dat` on disk for **offline** players. Test on a **copy** of a world or a disposable server.

1. As **console** (not in-game), run **`/resethealth`**.
2. Expect a **summary** with: count of **offline** `.dat` files scanned, count **written** (only if at least one `auraskills:` attribute modifier was removed), and a **read/write error** count (usually **0**).
3. **In-game** (even as **op**), run **`/resethealth`**: expect **“Only the dedicated server console (or RCON) may run this command.”**
4. **RCON** (if enabled): send **`resethealth`** — should **succeed** like console, provided the source has the permission.
5. **Permission:** a console source typically passes **`auraskills.command.resethealth`**; if you lock it down, confirm deny behavior matches your policy.

### NBT note

- Implementation follows the Bukkit 1.21+ path: list **`attributes`** → per-entry **`modifiers`** with string **`id`** starting with **`auraskills:`**. **Legacy** pre-1.21 **`Attributes` / `skillsHealth` / …** (older **`Uninstaller`**) is **not** run on Fabric in this sprint.

### Path note

- Uses **`MinecraftServer#getSavePath(WorldSavePath.PLAYERDATA)`** (vanilla `playerdata`). If a future Paper-style layout (e.g. `players/data`) is required, treat as a **follow-up**; not part of **Sprint 57**.

## Build (CI / local)

```text
.\gradlew -PfabricOnly=true :fabric:test
```

Expected: **BUILD SUCCESSFUL** (includes **`FabricCommandPermissionsTest`**, updated for **`SOURCES`**, **`RESETHEALTH`**).
