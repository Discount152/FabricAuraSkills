# Sprint 76C — startup blocker: `FabricWorldManager#loadWorlds` (immutable set)

## Original crash (authoritative)

From the dedicated server log after **Sprint 76B** ( **auraskills 2.3.105** ):

- **Minecraft** 1.21.11, **Fabric Loader** 0.18.4, **Fabric API** 0.141.3+1.21.11, **Java** 21.
- **granitelib** **0.1.0-alpha.1** in mod list while root **`gradle.properties`** has **`granitelibVersion=0.1.0-alpha.2`** — [GraniteLib note](#granitelib-version--deploy) below (not the crash).

**Fatal:**

```text
Failed to start Fabric AuraSkills
java.lang.UnsupportedOperationException
  at java.util.ImmutableCollections$AbstractImmutableCollection.add
  at dev.aurelium.auraskills.fabric.FabricWorldManager.loadWorlds(FabricWorldManager.java:43)
  at dev.aurelium.auraskills.fabric.FabricAuraSkills.enable(FabricAuraSkills.java:188)
```

**Context:** Mixin blockers (76A anvil, 76B tree) were cleared; **Brigadier** registration ran, then mod **enable** failed in **`loadWorlds`**.

## Root cause

- **`blockedCheckBlockReplaceWorlds`** was a **field** initialized with **`Set.of()`** (immutable).
- **`loadWorlds()`** reassigns **`blockedWorlds`** and **`disabledWorlds`** to **`Sets.newConcurrentHashSet()`** before iterating config lists, but **did not reassign** **`blockedCheckBlockReplaceWorlds`**.
- The third loop does **`blockedCheckBlockReplaceWorlds.add(w)`** → **immutable** set → **`UnsupportedOperationException`**.

**Collection ownership:** all three world-name registries are **populated from `config.yml`** and must be **mutable** (and, for the first two, **replaced** on each `loadWorlds` so reload does not grow stale data). The fix aligns **`blockedCheckBlockReplaceWorlds`** with the same **mutable** pattern.

## Fix (code)

- In **`loadWorlds()`**, after the other two assignments: **`blockedCheckBlockReplaceWorlds = Sets.newConcurrentHashSet();`**

**Files changed:** `FabricWorldManager.java` (one line + behavior restored).

**World / disabled / blocked / check_replace behavior:** **unchanged** vs **intended** design — the bug prevented **any** `check_block_replace.blocked_worlds` entries from loading; with the fix, **YAML** list entries populate the set as originally meant. Callers (e.g. **`FabricRegionManager`**) use **`isCheckReplaceDisabled`** as before.

## Local validation (in-repo)

- `.\gradlew -PfabricOnly=true :fabric:test`
- `.\gradlew -PfabricOnly=true :fabric:build`

**Unit test:** not added — **`loadWorlds`** depends on **`ConfigurateLoader`** and plugin data paths; regression is a **one-line** ownership fix. **Follow-up:** if we extract a small config→sets helper, add tests there.

## Runtime / operator validation

- Deploy **`FabricAuraSkills-2.3.106+`**.
- **Minimum:** server reaches **“Done”**; **no** `FabricWorldManager.loadWorlds` **immutable** crash.
- **Sanity (when possible):** **Overworld** / other dimensions; **`blocked_worlds`**, **`disabled_worlds`**, **`check_block_replace.blocked_worlds`** in **`config.yml`** still gate skills/region behavior as before; **reload** does not **append** duplicate entries (sets are **recreated** each `loadWorlds`).

## GraniteLib version / deploy

**In-repo:** **`fabric` JiJ-embeds** `granitelib-fabric` from the Gradle `granitelib-fabric` project; **slate-fabric** `processResources` expands **`fabric.mod.json` `version`** from **`granitelibVersion`**.

**If the mod list still shows 0.1.0-alpha.1**

1. **Remove** a **standalone** old **`granitelib-fabric-0.1.0-alpha.1.jar`** from **`mods/`** (duplicate mod / wrong version line).
2. **Rebuild** and deploy the **current** `FabricAuraSkills` JAR; **`jar tf`** the shadow JAR and confirm nested **`granitelib-fabric-*.jar`** matches **`granitelibVersion`**.

## Remaining warnings

- Classify from **`latest.log`** (other mods, LuckPerms during shutdown if AuraSkills fails first, etc.) — **Sprint 76C** only targets the **AuraSkills** `loadWorlds` **fatal** path.

## Next recommended step

1. Confirm dedicated **“Done”** on disposable test host.
2. If another **fatal** appears in **`FabricAuraSkills.enable`**, open **Sprint 76D** (or next) with a **new** `latest.log` excerpt.
3. Reconcile **GraniteLib** mod-list line with deploy policy (**JiJ-only** vs **external** mod).
