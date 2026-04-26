# Sprint 93 — Two-JAR release policy + parity roadmap reset

| Field | Value |
|--------|--------|
| **Sprint** | **93** — Correct packaging architecture + honest parity framing |
| **Gameplay artifact** | **`FabricAuraSkills-2.3.123.jar`** (`projectVersion` **2.3.123**) |
| **GraniteLib artifact** | **`slate-fabric/build/libs/granitelib-fabric-2.3.123.jar`** (Gradle `:granitelib-fabric`; on-disk module folder **`slate-fabric/`**) |
| **Bedrock / Geyser** | **Not tested** |

## Why this sprint exists

Release-readiness work (**Sprint 91–92**) described a **JiJ-only** / single-jar operator path because the **then-current** remapped JAR nested **`granitelib-fabric-*.jar`**. That matched the **artifact**, but it **drifted** from the **product architecture**:

- **Granite Skills** = gameplay mod (**`auraskills`**).
- **GraniteLib** = reusable Fabric library (**`granitelib`**) — **its own** installable mod.

Similarly, **Java smoke success** with **built-in Fabric menus** must **not** be read as **full AuraSkills + Slate** menu parity.

## Part A — Packaging diagnosis (pre → post)

| Question | Finding |
|----------|---------|
| How is GraniteLib built? | Gradle **`:granitelib-fabric`** (projectDir **`slate-fabric/`**), Loom **1.21.11**. |
| How was it embedded? | **`include(implementation(project(":granitelib-fabric", configuration = "namedElements")))`** produced **`META-INF/jars/granitelib-fabric-*.jar`**. |
| Can gameplay run with **external only** GraniteLib? | **Yes** (canonical after Sprint 93): gameplay JAR **does not** nest GraniteLib by default. |
| Did gameplay declare **`depends: granitelib`**? | **No** before Sprint 93; **Yes** after (**`>=0.1.0-alpha.2`**). |
| **`verifyFabricReleaseBundle`** | Previously **required** nested GraniteLib. Now: **forbids** nested GraniteLib **unless** **`-PembedGraniteJij=true`**. |
| Duplicate mod risk | **Never** mix **external `granitelib`** + **JiJ `granitelib`** — Loader can load **two** GraniteLib instances. Pick **one** policy per deploy. |

## Part B — Canonical install policy

**Production / RC:**

1. **`FabricAuraSkills-<version>.jar`**
2. **`granitelib-fabric-<version>.jar`** from **`slate-fabric/build/libs/`** (same **`projectVersion`** line as root `gradle.properties` for the **filename**; mod **`version`** inside GraniteLib `fabric.mod.json` is **`granitelibVersion`**, currently **`0.1.0-alpha.2`**).

**Legacy / dev-only single bundle:**

```text
.\gradlew -PfabricOnly=true -PembedGraniteJij=true :fabric:build
```

Nested GraniteLib returns; **do not** also drop a **second** `granitelib-fabric-*.jar` into `mods/`.

## Part C — Gradle / metadata changes (summary)

- **`fabric/build.gradle.kts`:** default **no** JiJ for `:granitelib-fabric`; **`compileOnly`** + **`testImplementation`** for compile/test (avoids Loom requiring a prebuilt remapped GraniteLib jar during **`:fabric`** configuration). Opt-in **`embedGraniteJij`**. **`verifyFabricReleaseBundle`** branches on that flag.
- **`fabric/src/main/resources/fabric.mod.json`:** **`depends.granitelib`**, description no longer claims embedding.
- **`gradle.properties`:** **`projectVersion=2.3.123`**.

## Part D — RC1 status

- **`v2.3.122-fabric-rc1`** remains a **valid Java-smoke checkpoint** for the **2.3.122** line, but its **install docs assumed JiJ-only as normal** — **superseded for final-release packaging policy**. Prefer **`2.3.123+`** and **`v2.3.123-fabric-rc2`** for RCs aligned with **two-JAR** policy.

## Part E — Parity reset summary

See **`fabric/PARITY_COMPLETION_ROADMAP.md`**:

- Built-in menus = **partial** vs Slate.
- Full **AuraSkills + Slate** capability parity = **open program**.
- **JobsCommand** = **deferred** unless owner reopens.

## Part F — Build / test (maintainer)

```text
.\gradlew -PfabricOnly=true :granitelib-fabric:build
.\gradlew -PfabricOnly=true :fabric:test
.\gradlew -PfabricOnly=true :fabric:build
.\gradlew -PfabricOnly=true :fabric:verifyFabricReleaseBundle
```

**Sprint 93 cut:** **PASS** (15 nested JARs in gameplay mod — **no** `granitelib-fabric-*.jar` nested when `embedGraniteJij` is false).

## Remaining blockers

- **Bedrock/Geyser** evidence.
- **Slate / YAML / MenuFileManager** port (Path A).
- **Destructive** admin matrix rows on disposable worlds.

## Recommended next sprint

1. Publish **`v2.3.123-fabric-rc2`** for Java operators (two-JAR).
2. Optional **docs triage** for untracked `docs/architecture/*` / `docs/handoffs/*`.
3. **Sprint 87** when a Bedrock tester exists.
