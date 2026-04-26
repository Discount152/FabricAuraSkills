# Sprint 76A — startup blocker: `AnvilScreenHandlerAnvilDiscountMixin` (dedicated server)

## Original crash (authoritative)

From attached server log (`latest(20).log`):

- **Minecraft** 1.21.11, **Fabric Loader** 0.18.4, **Fabric API** 0.141.3+1.21.11, **Java** 21.
- **auraskills** **2.3.102** (operator should always record the **exact** JAR + mod list line when testing).
- **granitelib** **0.1.0-alpha.1** in log (and Loader warning: **`${version}`** not expanded — see below).

**Fatal:**

```text
Mixin apply for mod auraskills failed auraskills.mixins.json:AnvilScreenHandlerAnvilDiscountMixin
@Shadow field field_22482 was not located in the target class net.minecraft.class_1706. No refMap loaded.
```

Followed by `BootstrapMethodError` / `Mixin transformation of net.minecraft.class_1706 failed`.

**Non-fatal (same log):** other mods’ missing refmaps, `REFMAP JSON` NPE in Loader, owo/Geysers/etc. class-not-found **warnings** — not treated as the AuraSkills blocker.

## Root cause (repo analysis)

1. **Intermediary `field_22482`** in Yarn **1.21.11+build.4** is **`ForgingScreenHandler#player`** (`class_4861.field_22482`), **not** a field on **`AnvilScreenHandler`** (`class_1706`).
2. The mixin was **`@Mixin(AnvilScreenHandler.class)`** with **`@Shadow protected PlayerEntity player`**. The inherited field is declared on the **parent** class. With **no / broken refMap** on the server, the shadow was attached to the **wrong** class, so the runtime looked for **`class_1706.field_22482`** (which does not exist) and failed during mixin apply.
3. **`AnvilScreenHandler#levelCost`** is intermediary **`field_7770`** on `class_1706` (separate from the `player` bug; kept explicit for robustness).

So this was **not** “levelCost mapping randomly renamed,” it was **wrong class target for the forging `player` field** + **“No refMap loaded”** making the bad resolution visible.

## Fix (code)

- **Removed** `@Shadow` for `player` and `levelCost` from `AnvilScreenHandlerAnvilDiscountMixin`.
- **Use `VarHandle`** with:
  - **Development / Yarn names:** `ForgingScreenHandler#player`, `AnvilScreenHandler#levelCost`.
  - **Production dedicated server (intermediary):** `field_22482` (forging player), `field_7770` (anvil `levelCost`), toggled by `FabricLoader.getInstance().isDevelopmentEnvironment()`.
- If either `VarHandle` cannot be created, the **inject no-ops** (trait inert, **no** crash) — see parity note below.

**Files changed**

- `fabric/.../mixin/AnvilScreenHandlerAnvilDiscountMixin.java`
- `slate-fabric/build.gradle.kts` — `processResources` **expand** `version` in **`fabric.mod.json`** (GraniteLib metadata).

**RefMap packaging:** not changed in Gradle this sprint; the mixin no longer **depends** on a correct mod refmap for these two fields. Re-enabling a reliably packaged refmap is still a good follow-up for **other** mixins.

## GraniteLib `${version}` warning

**Before:** `granitelib` showed **`0.1.0-alpha.1`** and Loader warned that **`${version}`** is not a valid SemVer (resource not expanded).

**After:** `slate-fabric` `processResources` expands **`${version}`** to **`granitelibVersion`** from root **`gradle.properties`** (currently **0.1.0-alpha.2**), so the **embedded** JiJ `granitelib-fabric` `fabric.mod.json` is valid SemVer and **not** a literal **`${version}`** string.

**Note:** **`projectVersion`** (e.g. **2.3.104**) remains the **FabricAuraSkills** mod version; **GraniteLib** keeps its **own** release line via **`granitelibVersion`**.

## Build / JAR (local)

- `.\gradlew -PfabricOnly=true :fabric:test` — **PASS**
- `.\gradlew -PfabricOnly=true :fabric:build` — run before deploy
- `.\gradlew -PfabricOnly=true :granitelib-fabric:build` — after slate `processResources` change

**Deploy:** `fabric/build/libs/FabricAuraSkills-2.3.104.jar` (or current `projectVersion`).

## Runtime validation (operator)

- [ ] Replace **auraskills** JAR on the **disposable** test server; keep **same** MC / Loader / API as before.
- [ ] **Expected:** server reaches **“Done”**; **no** `AnvilScreenHandlerAnvilDiscountMixin` / `class_1706` mixin **ERROR**.
- [ ] **Optional:** in-game anvil with **anvil_discount** trait still reduces displayed cost vs vanilla (if `VarHandle` init succeeded).
- [ ] **Paste a short post-fix `latest.log` excerpt** (mod list + first errors) into this doc or the PR.

**I cannot run your dedicated host from the repo;** the row above is **unchecked** until you confirm.

## Parity / behavior note

- **Intended behavior:** same as pre-fix — `FabricAnvilDiscount.afterUpdateResult` after `updateResult` TAIL.
- If **both** `VarHandle`s init to **null** (unexpected module access), the inject returns early: **anvil discount trait** does **not** run, but the **server still boots** (per sprint safety rule). Log would not contain the old crash; **in-game** cost would match vanilla until diagnosed.

## Next steps

1. **Confirm** dedicated startup on your server with **2.3.104+** JAR. If the next fatal mixin is **`TreeFeatureGrowContextMixin`**, deploy **Sprint 76B** — **`SPRINT_76B_STARTUP_BLOCKER_TREE_FEATURE_MIXIN.md`** — typically **`projectVersion` 2.3.105+**.
2. Revisit **Sprint 75** Java acceptance checklist only **after** stable boot ( **76A** + **76B** as needed).
3. Optional follow-up: audit **other** `ForgingScreenHandler` / `@Shadow` of inherited fields; ensure refmap is present in remapped JAR for long-term.
