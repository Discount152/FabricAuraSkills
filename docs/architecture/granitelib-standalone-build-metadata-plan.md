# GraniteLib standalone build / metadata implementation plan

| Field | Value |
|-------|--------|
| **Status** | **Proposed** (Sprint 89 — design only; **no** Gradle / file creates in this sprint) |
| **Date** | 2026-07-10 |
| **Prereq** | [granitelib-standalone-dependency-identity-adr.md](granitelib-standalone-dependency-identity-adr.md) (Sprint 88) · [granitelib-distribution-adr.md](granitelib-distribution-adr.md) (Milestone D + Sprint 88 supplement) |
| **Next** | **Sprint 90** — implement per this plan (metadata file, `granitelibVersion`, Gradle, validation); **this file** does not change the repo |

**Sprint 89 does not** create `fabric.mod.json`, edit `build.gradle.kts`, change `fabric`’s `fabric.mod.json`, or remove JiJ.

---

## 1. Purpose

Answer **concretely** how **`:granitelib-fabric`** becomes a **separately installable** Fabric mod (mod id **`granitelib`**, version **`0.1.x`**) while **FabricAuraSkills** continues to **build and run** with **JiJ** through the transition, so **Sprint 90** can implement **without** guessing at Loom, Loader, or duplicate-class behavior.

---

## 2. Current build state (audited)

### 2.1 Project wiring

- **`settings.gradle.kts`:** `include("granitelib-fabric")` with `projectDir = file("slate-fabric")`.
- **Root / `allprojects`:** `version` = `projectVersion` from **`gradle.properties`** (currently **2.3.22**) — **:granitelib-fabric** uses this today (`version = rootProject.version` in `slate-fabric/build.gradle.kts`).
- **`:fabric` → `implementation(project(":granitelib-fabric", configuration = "namedElements"))` + `include(...)`** — JiJ ships **`granitelib-fabric-*.jar`** in **`META-INF/jars/`** of the remapped **FabricAuraSkills** JAR.
- **`verifyFabricReleaseBundle`:** requires a nested **`granitelib-fabric-*.jar`**.

### 2.2 `:granitelib-fabric` (`slate-fabric/build.gradle.kts`)

- **Plugins:** `java`, **fabric-loom** `1.13.6`.
- **Loom** block is **minimal** (no `loom { mods { } }` registration like `:fabric` has for `auraskills`).
- **Dependencies (aligned with `:fabric` for MC/Loader/API):** Minecraft **1.21.11**, Yarn **1.21.11+build.4:v2**, `fabric-loader` **0.18.4**, `fabric-api` **0.141.3+1.21.11**.
- **JAR base name:** `archiveBaseName` = **`granitelib-fabric`** (all `Jar` tasks).
- **Tasks present (representative):** `jar`, **`remapJar`**, `remapSourcesJar`, `sourcesJar` — Loom’s **`remapJar`** remaps the output to **intermediary** (same as any Loom subproject).
- **Resources:** there is **no** `slate-fabric/src/main/resources/fabric.mod.json` **today** (and no `fabric.mod.json` in the **library** output).

### 2.3 Is the current `granitelib-fabric` JAR a “Fabric mod” for `mods/`?

| Question | Answer |
|----------|--------|
| Is **`remapJar` output a valid, remapped artifact? | **Yes** — Loom produces a remapped JAR under **`slate-fabric/build/libs/`** (exact name/classifier per Loom; see Sprint 90). |
| Will **Fabric Loader** treat it as a **loadable mod** if placed in **`mods/`** **alone**? | **Not yet** — there is **no** **`fabric.mod.json`** inside the JAR. Loader requires **mod metadata** (typically **`fabric.mod.json`** in the JAR) for a **separate** mod. |
| Is it valid as a **nested JiJ** library under **FabricAuraSkills**? | **Yes** for the **current** model: nested **library** JAR (used via **`namedElements`**), **not** a second top-level mod registration. |
| **Conclusion** | **Today:** **:granitelib-fabric** is a **Loom-linked, remapped library JAR** suitable for **embedding**, **not** a **standalone** Fabric mod deliverable. **Sprint 90** must add **metadata** (and any **Loom** `mods { }` / resource processing needed) to make a **second** shippable artifact line. |

### 2.4 Code / entrypoint audit

- **`GraniteFabricBootstrap`** (`dev.granitelib.fabric`) is a **static** helper (e.g. `status()`) with **no** `ModInitializer` / `DedicatedServerModInitializer` / `ClientModInitializer` usage.
- **No** `fabric.mod.json` **entrypoint** is required for **current** code paths. **Initial** standalone `fabric.mod.json` can use **empty** **`"entrypoints": {}`** (or omit key per schema — align with Loom / Fabric template in Sprint 90).

---

## 3. Target standalone state

| Aspect | Target |
|--------|--------|
| **Mod `id`** | `granitelib` (per identity ADR) |
| **Display `name`** | `GraniteLib` |
| **Version line** | **`0.1.0` or `0.1.0-alpha`** (SemVer for **library**; **independent** of gameplay `2.3.22` once split) |
| **Artifact** | **Preferred base name** `granitelib-fabric-<version>.jar` (matches current **`archiveBaseName`**, indicates Fabric stack; “GraniteLib” is **display** / docs only) |
| **Install story** | User can download **one** JAR and put it in **`mods/`**; **later** **FabricAuraSkills** lists **`depends: granitelib`**. |
| **Transition** | **JiJ remains** until a phase explicitly removes or narrows it; **avoid** two conflicting definitions of the same mod without testing (see §7). |

---

## 4. Metadata file location

| Option | Verdict |
|--------|---------|
| **A. `slate-fabric/src/main/resources/fabric.mod.json`** | **Recommended default.** Standard Fabric location; colocated with **`:granitelib-fabric`**; works with `projectDir = slate-fabric/`. |
| **B. `META-INF/mods.toml` / other** | **Not** the usual Fabric 1.21 path for this stack; **defer** unless Loom doc mandates an exception. |
| **C. Generated-only JSON** | Possible for advanced pipelines; **higher** complexity. **Default:** **static file** in **resources** for clarity and diffs. |

**Recommendation:** add **`slate-fabric/src/main/resources/fabric.mod.json`** in **Sprint 90** (not Sprint 89).

---

## 5. Draft `fabric.mod.json` (GraniteLib — do not add until Sprint 90)

**Inputs today:** [fabric/src/main/resources/fabric.mod.json](../../fabric/src/main/resources/fabric.mod.json) uses **`fabricloader` `>=0.18.4`**, **`minecraft` `~1.21.11`**, **`java` `>=21`**, **`fabric-api` `*`**, **`fabric-permissions-api-v0` `*`** (gameplay only). **:granitelib-fabric** build file pins **Loader 0.18.4** and **API 0.141.3+1.21.11** — the draft should **not** go **stricter** than the **actual** compile pins without reason.

**Draft (string templates — `version` expansion in Sprint 90):**

```json
{
  "schemaVersion": 1,
  "id": "granitelib",
  "version": "${version}",
  "name": "GraniteLib",
  "description": "A server-side menu definition and layout planning layer for Fabric mods. Used by FabricAuraSkills; not a full client GUI framework.",
  "authors": ["Discount152"],
  "contact": {
    "homepage": "https://github.com/Discount152/FabricAuraSkills",
    "sources": "https://github.com/Discount152/FabricAuraSkills",
    "issues": "https://github.com/Discount152/FabricAuraSkills/issues"
  },
  "license": "GPL-3.0",
  "environment": "*",
  "entrypoints": {},
  "depends": {
    "fabricloader": ">=0.18.4",
    "minecraft": "~1.21.11",
    "java": ">=21",
    "fabric-api": "*"
  }
}
```

**Notes for Sprint 90:**

- **`fabric-permissions-api-v0`:** **omit** for GraniteLib **unless** the library JAR **actually** requires it at runtime. Gameplay mod keeps it.  
- **Description:** keep **factual**; do not claim “every mod uses GraniteLib” (see [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md) for public listing).  
- **`${version}`:** must expand to **standalone** line (e.g. `0.1.0-alpha.1`) — see §6.

---

## 6. Versioning model

| Approach | When to use |
|----------|-------------|
| **Single `projectVersion` for all modules** | **Current**; **not** appropriate once GraniteLib is **`0.1.x`** and gameplay is **`2.3.x`**. |
| **Separate property `granitelibVersion` in `gradle.properties`** | **Recommended** for first standalone releases, e.g. `granitelibVersion=0.1.0-alpha.1`. |
| **`:granitelib-fabric` `version` override in `slate-fabric/build.gradle.kts`** | **Implementation detail:** set `version =` property for **that** subproject only (leave root `projectVersion` for **:fabric**). |

**How `${version}` resolves:** Loom/Gradle `processResources` (or Loom’s built-in) must expand **`fabric.mod.json`** in **:granitelib-fabric** to **`granitelibVersion`**, not **`rootProject.version`**, once split.

**Sprint 89 does not** add the property; **Sprint 90** does.

**Gameplay** (`auraskills`) **stays** on **`projectVersion` `2.3.22`** (or its normal bumps) **until** a separate gameplay release process says otherwise.

---

## 7. Gradle / Loom changes required (plan only)

**Sprint 90** should address (checklist, not this sprint):

1. **`granitelibVersion` (or equivalent)** in **`gradle.properties`**; **:granitelib-fabric** `version =` that property (or `rootProject` extra property).
2. **Resource processing** so **`${version}`** in **GraniteLib** `fabric.mod.json` expands to **granitelib** SemVer. (Follow Loom docs for the project’s Loom version; may mirror **:fabric**’s pattern.)
3. **Optional: `loom { mods { register("granitelib") { sourceSet("main") } }` }` in `slate-fabric`** — **verify** in Sprint 90 whether Loom **requires** a `mods { }` block for a **remapped standalone** mod JAR in a **multi-Loom** monorepo; if builds fail, add per Loom 1.13 / Fabric docs.
4. **Output artifact for distribution:** use **`remapJar`** (or the task chain Loom documents as the shippable mod JAR) for **`granitelib-fabric`**, **named** `granitelib-fabric-<granitelibVersion>[…].jar` — align **`archiveBaseName`** / **`archiveVersion`** in Sprint 90.
5. **CI / release job:** a **second** published artifact (GitHub **Release** asset or Maven) for **GraniteLib** only — out of scope for **Sprint 90** if product prefers a minimal first step: **local** `build/libs/` only.
6. **:fabric` JiJ** line: **keep** `include(project(":granitelib-fabric", configuration = "namedElements"))` **until** a phase **drops** it by design. **Dropping** must update **`verifyFabricReleaseBundle`** in lockstep (see [granitelib-verification-report.md](granitelib-verification-report.md)).

---

## 8. JiJ transition behavior

| Phase | Behavior |
|-------|----------|
| **1** | **Add** `fabric.mod.json` + version split; **still** build **nested** `granitelib-fabric-*.jar` in **FabricAuraSkills**; **also** build **standalone** JAR for download. **verifyFabricReleaseBundle** unchanged. |
| **2** | **Manual / CI smoke:** run server with **only** remapped **FabricAuraSkills** (as today) — **A**. Then add **standalone** GraniteLib JAR to `mods/` **in addition** — **B**; observe duplicate-class / duplicate-mod **behavior**. |
| **3** | **Add** `depends: { "granitelib": "&gt;=0.1.0" }` on **:fabric** `fabric.mod.json` when **standalone** JAR is **proven**; optionally **one** release with **both** JiJ and `depends` to flush Loader behavior (per product). |
| **4** | **Remove** `include(… granitelib-fabric …)` from **:fabric** when **safe**; **update** `verifyFabricReleaseBundle` to **not** require nested **`granitelib-fabric-*.jar`**; **or** keep JiJ only for **dev**/**CI** (document clearly). |

**Critical risk — duplicate library vs duplicate mod:**  
If the **nested** JAR **includes** a **`fabric.mod.json` with `id: granitelib`**, the Loader / JiJ may treat **nested** and **external** as **the same mod** or may **load classes twice** — **behavior is not assumed**; **Sprint 90** / early **Sprint 90** validation must **reproduce** on a test server. Mitigations to evaluate later: **strip** `fabric.mod.json` from **JiJ**-only build variant (two task graphs), or **remove** JiJ first and rely on **`depends`** only. **Sprint 89** only records the **need to test**; **Sprint 90+** pick the **mitigation**.

---

## 9. FabricAuraSkills `depends` timing

- **Do not** add `"granitelib": "..."` in **Sprint 89.**
- **Add in Sprint 10x** only when **(a)** a **remapped** standalone **`granitelib` mod JAR** exists that Loader accepts, and **(b)** version range is agreed (e.g. `">=0.1.0"` / upper bound TBD).
- **Until then:** `fabric.mod.json` for **`auraskills`** **unchanged** for **`depends`**.

---

## 10. Validation matrix (for Sprint 90+)

| Step | Command / action | Pass criterion |
|------|------------------|----------------|
| Lib compile | `.\gradlew.bat :granitelib-fabric:compileJava` | Success |
| Lib jar + remap | `.\gradlew.bat :granitelib-fabric:remapJar` (or Loom’s equivalent lifecycle) | **`granitelib-fabric-*.jar`** in **`slate-fabric/build/libs/`** (paths per Gradle) |
| Lib test | `.\gradlew.bat :granitelib-fabric:test` | Success (or **NO-SOURCE** if no tests) |
| Gameplay | `.\gradlew.bat :fabric:test` : `.\gradlew.bat :fabric:build -PfabricOnly=true` | **BUILD SUCCESSFUL**; **`verifyFabricReleaseBundle: OK`**; **`FabricAuraSkills-&lt;projectVersion&gt;.jar`**. |
| **Runtime A** | Server: **only** FabricAuraSkills (current) | Loads as today |
| **Runtime B** | FabricAuraSkills **+** external GraniteLib in `mods/` | No duplicate mod / class errors (**define** in Sprint 90 after trying) |
| **Runtime C** | **`depends: granitelib`**, **JiJ removed** (future phase) | Server starts; GraniteLib **once** on classpath |

If **`remapJar`** is **not** the final shippable name, follow Loom output docs and add a row to this matrix in the **implementation** PR.

---

## 11. Release artifact naming

| Pattern | Use |
|---------|-----|
| **`granitelib-fabric-0.1.0-alpha.1.jar`** (example) | **Recommended** — matches **`archiveBaseName`**, version from **`granitelibVersion`**. |
| **`GraniteLib-0.1.0.jar`** | **Avoid** for **file** name — **breaks** current **`verifyFabricReleaseBundle`** / nested **prefix** **story** until **renamed** everywhere. **Display** name in **`fabric.mod.json` `name`** remains **GraniteLib**. |

---

## 12. Loader / runtime smoke (definition only)

- **A:** `mods/` = FabricAuraSkills only (baseline).  
- **B:** + standalone **`granitelib` JAR` (test duplicate with JiJ still embedded in FabricAuraSkills).  
- **C:** `depends` on **`granitelib`**, no JiJ (or dev-only JiJ) — end state.

**Sprint 90** may not run **B/C** fully; the **plan** requires them to be **scheduled** before **production** **depends** and **Modrinth**.

---

## 13. Risks / blockers

- **Loader duplicate** mod or **classes** if JiJ and external **both** present with **identical** `id` / packages — **must** be **resolved** in implementation (§8).  
- **Loom** multi-project: **`loom { mods { } }`**, **`namedElements`**, and **standalone** JAR may need a **one-time** Loom / Fabric **doc** read in Sprint 90.  
- **Legal** before **public** Modrinth: [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md), [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md). **Internal** / **owner-hosted** JAR can precede public listing.  
- **`GPL-3.0`** in draft matches **monorepo**; **do not** change **license** story in the **branding** sprint.

---

## 14. Non-goals (Sprint 89)

- **No** file creates, **no** Gradle edits, **no** Java, **no** `fabric.mod.json` in repo, **no** `depends` on **:fabric**, **no** JiJ removal, **no** **publish**, **no** **NOTICE**.

---

## 15. Recommended Sprint 90

- **Sprint 90 — implement** **GraniteLib** **`fabric.mod.json`**, **`granitelibVersion`**, **resource expansion**, **optional** Loom `mods { }` for **:granitelib-fabric**, **document** or **add** a **publish** / **CI** step for the **standalone** JAR, and **run** validation matrix rows **1–4** minimum.  
- **Schedule** **Runtime B** before merging **`depends: granitelib`**.

---

*Sprint 89: this plan + doc cross-links + handoff; **no** build changes.*
