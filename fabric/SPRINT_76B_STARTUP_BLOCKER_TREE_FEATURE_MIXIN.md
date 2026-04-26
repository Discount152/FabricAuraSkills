# Sprint 76B — startup blocker: `TreeFeatureGrowContextMixin` (dedicated server)

## Original crash (authoritative)

From operator’s **post–76A** dedicated-server log:

- **Minecraft** 1.21.11, **Fabric Loader** 0.18.4, **Fabric API** 0.141.3+1.21.11, **Java** 21.
- **auraskills** **2.3.104** (matches intended **76A** gameplay JAR).
- **granitelib** **0.1.0-alpha.1** in the mod list while root **`gradle.properties`** has **`granitelibVersion=0.1.0-alpha.2`** — see [GraniteLib version situation](#granitelib-version-situation) below.

**Fatal:**

```text
MixinApplyError: Mixin [auraskills.mixins.json:TreeFeatureGrowContextMixin from mod auraskills] FAILED during APPLY
InvalidMixinException: Unexpected NullPointerException preparing @Inject annotation on auraskills$treeFeatureGenHead
target: method_13151(Lnet/minecraft/class_5821;)Z
NullPointerException: Cannot read field "index" because "insnNode" is null
```

**`class_5821`** = **`FeatureContext`** (Yarn `net.minecraft.world.gen.feature.util.FeatureContext`).

## Root cause

1. **`method_13151`** is the shared JVM name for **`Feature#generate(FeatureContext)Z`** in the hierarchy.
2. The mixin was **`@Mixin(Feature.class)`** with **`@Inject(method = "generate", at = @At("HEAD"))`** (and **RETURN**).
3. On **`Feature`**, **`generate` is abstract** — there is **no real bytecode body** in **`Feature.class`**, so the injector cannot find a **HEAD** instruction node → **NPE** during **@Inject** preparation (`insnNode == null`).
4. **Fix:** Target **`TreeFeature.class`** instead, which **implements** **`generate(FeatureContext)`** with a concrete method body. Disambiguate from **`TreeFeature`’s other `generate` overload** using the full JVM descriptor:
   **`generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z`**

**Mapping note:** The **symptom** pointed at **`method_13151` + `FeatureContext`**; the **bug** was **abstract base class injection**, not a wrong **`FeatureContext`** mapping for 1.21.11.

## Fix (code)

- **`@Mixin(TreeFeature.class)`** (was **`Feature.class`**).
- **`@Inject`** on **`generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z`** at **HEAD** and **RETURN** (same behavior: **`FabricTreeGrowContext.begin` / `end`** around configured tree generation).
- Removed redundant **`instanceof TreeFeature`** guards in the **HEAD** inject (mixin class is already **`TreeFeature`**).
- **RETURN** handler includes **`FeatureContext`** as the first parameter (Mixin requires the **target method’s** parameters before **`CallbackInfoReturnable`**, not the callback alone).

**Files changed**

- `fabric/.../mixin/TreeFeatureGrowContextMixin.java`
- `fabric/.../region/FabricTreeGrowContext.java` (javadoc: **`TreeFeature#generate`**)

**RefMap packaging:** unchanged this sprint; injection is now on a **concrete** method, so **HEAD**/**RETURN** resolve without relying on instruction surgery in an abstract stub.

**Feature behavior:** Intended parity unchanged — **Bukkit `StructureGrowEvent`-style grow Y** for **`check_replace`** / region cleanup still wraps the same **`FeatureContext`** tree path. If runtime proof shows a gap, track as **NRP**.

## GraniteLib version situation

**In-repo evidence**

- **`FabricAuraSkills`** **Jar-in-Jar** embeds **`granitelib-fabric`** from **`fabric/build.gradle.kts`** (`include(implementation(project(":granitelib-fabric", …)))`).
- **`slate-fabric`** **`processResources`** expands **`fabric.mod.json`** **`${version}`** to **`granitelibVersion`** (e.g. **0.1.0-alpha.2**).

**Why the server log can still show `0.1.0-alpha.1`**

1. **Standalone `granitelib-fabric-*.jar` in `mods/`** — Fabric Loader loads it as a **separate** mod; if it is **older** than the JiJ copy **inside** `FabricAuraSkills-*.jar`, the **mod list** can still show the **external** version (or duplicate-mod behavior depending on Loader resolution).
2. **Stale deploy** — operator copied an **older** `FabricAuraSkills` JAR or an **old** external GraniteLib.
3. **Verification:** After build, `jar tf` on the AuraSkills JAR and filter for `granitelib` should list an embedded `granitelib-fabric-*.jar` built with the current **`granitelibVersion`**. Prefer **one** GraniteLib source: either **JiJ-only** (remove external duplicate) or **external-only** (no duplicate) — document the pack layout you use.

**Follow-up:** If the team standard is **JiJ-only**, remove extra **`granitelib-fabric`** from server **`mods/`** and redeploy a **fresh** AuraSkills JAR built with current **`granitelibVersion`**.

## Local validation (in-repo)

Commands run ( maintainer machine ):

- `.\gradlew -PfabricOnly=true :fabric:test`
- `.\gradlew -PfabricOnly=true :fabric:build`

**Runtime (operator):** Deploy **`FabricAuraSkills-2.3.105+`** (or current **`projectVersion`**) to the disposable dedicated server; confirm **“Done”** and **no** fatal **`TreeFeatureGrowContextMixin`** / **`AnvilScreenHandlerAnvilDiscountMixin`** errors. Attach **`latest.log`** for the next blocker if any.

## Sprint 75 / Java dedicated acceptance

**Sprint 75** checklist remains **process-blocked** until a **booting** JAR is confirmed; **76A** fixed the anvil mixin; **76B** targets the **tree** mixin. Update **`SPRINT_75_MANUAL_JAVA_DEDICATED_ACCEPTANCE.md`** prerequisite line when both are deployed and startup succeeds.

## Remaining warnings (non-goal for 76B)

Classify in **`latest.log`** as usual: **other mods’** refMap warnings, missing optional classes, etc. — **not** this sprint unless AuraSkills **fatal** mixins reappear.

## Next recommended step

1. Operator: deploy **76B** JAR, confirm **startup** or capture **next** fatal mixin.
2. Reconcile **GraniteLib** mod-list line with **expected** **`granitelibVersion`** (duplicate external JAR vs JiJ).
3. When the server **reaches “Done”**, resume **Sprint 75** Java dedicated acceptance (commands/menus), not Bedrock this phase.
