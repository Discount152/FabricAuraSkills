# Sprint 92 — Fabric 2.3.122 release candidate prep

| Field | Value |
|--------|--------|
| **Sprint** | **92** — RC documentation, build verification, optional Git tag |
| **Target artifact** | **`FabricAuraSkills-2.3.122.jar`** (`projectVersion` **2.3.122**) |
| **Bedrock / Geyser** | **Not tested** — unchanged |

## Part A — Working tree (recorded at prep time)

| Item | Value |
|------|--------|
| **Branch** | `feature/granitelib-module-ids` |
| **HEAD (Sprint 92 docs commit)** | Commit **docs: Sprint 92 release candidate prep for FabricAuraSkills 2.3.122** — confirm with `git log -1 --oneline`. |
| **Baseline (pre–Sprint 92 docs)** | `4f55cdd2134115e238beda4df9170bd27160ddb4` — *Sprint 91 release readiness* |
| **Modified tracked files** | **None** at start of Sprint 92 (only deferred untracked paths). |
| **Untracked (deferred — do not batch-commit)** | `docs/architecture/granitelib-*`, `docs/handoffs/*`, `fabric/MOD_COMMANDS.txt`, `fabric/SPRINT_SUMMARY_SPEC.md` |
| **Clean enough to tag?** | **Yes** — tracked tree clean; untracked list is intentional and documented. |

## Part B — Build verification

**Commands:**

```text
.\gradlew -PfabricOnly=true :fabric:test :fabric:build :fabric:verifyFabricReleaseBundle
```

| Check | Result |
|--------|--------|
| **:fabric:test** | **PASS** |
| **:fabric:build** | **PASS** |
| **:fabric:verifyFabricReleaseBundle** | **PASS** (16 nested jars in `FabricAuraSkills-2.3.122.jar`) |
| **Non-fatal warnings** | Gradle “deprecated features” notice; Loom semver warnings for some nested coords; optional `onTake` remap message for `ForgingScreenHandler` — **non-blocking** on verified build. |

## Part C — Artifact metadata (inspected)

| Check | Finding |
|--------|---------|
| **Remapped JAR path** | `fabric/build/libs/FabricAuraSkills-2.3.122.jar` |
| **Main `fabric.mod.json`** | `id`: **`auraskills`**, `version`: **`2.3.122`** |
| **JiJ GraniteLib** | Entry **`META-INF/jars/granitelib-fabric-2.3.122-dev.jar`** present (`verifyFabricReleaseBundle` requires `granitelib-fabric-*.jar`) |
| **Nested GraniteLib `fabric.mod.json`** | `id`: **`granitelib`**, `version`: **`0.1.0-alpha.2`** — **not** `${version}` |
| **External GraniteLib JAR** | **Not required** for normal install (JiJ only). |

## Part D — Release note

- **`fabric/RELEASE_NOTES_2.3.122.md`**

## Part E — Tag recommendation

- **Recommended:** annotated tag **`v2.3.122-fabric-rc1`** on the commit that includes this Sprint 92 documentation (and Changelog/parity updates).
- **Push:** **not** automatic — operator runs `git push origin v2.3.122-fabric-rc1` when ready.

## Part F — Tag status

- **Created locally:** **`v2.3.122-fabric-rc1`** with message `FabricAuraSkills 2.3.122 Fabric release candidate 1` — **if** present after Sprint 92 commit (`git tag -l 'v2.3.122-fabric-rc1'`).
- **Deferred only if:** maintainer skips tag intentionally (document reason here): *— none by default.*

## Final recommendation

1. **Publish or share** `FabricAuraSkills-2.3.122.jar` + `RELEASE_NOTES_2.3.122.md` for broader **Java** testing.  
2. **Optional:** **Docs triage sprint** for deferred untracked `docs/` / `fabric/*.txt` files.  
3. **When a Bedrock tester exists:** **`SPRINT_87`** runtime validation.

## Remaining release blockers (honest)

- **Cross-play “done” claim** still needs **Sprint 87** evidence.  
- **Full Bukkit/Slate menu parity** remains a **program**, not this RC.  
- **Destructive** admin paths: matrix **Skip** until disposable worlds/data.
