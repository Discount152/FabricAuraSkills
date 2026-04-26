# Cursor agent handoff — FabricAuraSkills / Granite (post–Sprint 83)

**Generated:** 2026-04-21  
**Purpose:** Let a new Cursor agent continue work with **no ambiguity** about repo state, scope, build commands, and what was last shipped in code.

---

## 1. What this project is

- **Name:** [FabricAuraSkills](https://github.com/Discount152/FabricAuraSkills) — a **Java Fabric** server mod shipping **Granite Skills** (AuraSkills port) and **GraniteLib**.
- **Reference:** The in-repo **`bukkit`** module is a **capability reference** (ACF, Slate); **Bukkit must not** be a runtime dependency of the shipped Fabric mod.
- **Supported players in product terms:** **Java** clients and **Bedrock** through **Geyser + Floodgate** (validation is a **separate** track; see parity docs).

**Hard product scope (do not drift without explicit product decision):**

- Granite Skills, GraniteLib, Fabric runtime, parity with the **in-repo** Bukkit module **where practical**.
- **Do not** add: WorldGuard, Vault, PAPI, ProtocolLib, MythicMobs, Slimefun, Towny, Jobs/economy stacks, or unrelated plugin ecosystems.
- **JobsCommand** is **out of default scope**.
- **Do not** add Bukkit/Paper **as a runtime** dependency of the Fabric deliverable.

---

## 2. Workspaces in Cursor (multi-root)

The user may have **two** roots open:

| Path | Role |
|------|------|
| `F:\FabricAuraSkills` | **Primary** — this monorepo (Fabric mod, `common`, `bukkit` ref, `granitelib-fabric`, `slate-fabric`, etc.) |
| `F:\Firefox extension project` | **Separate** — not part of this handoff; ignore unless the user’s query is about it |

**Git:** The monorepo at `F:\FabricAuraSkills` is a **git repo**. Current work has been on branch **`feature/granitelib-module-ids`**.

---

## 3. Authoritative version and artifact

- **`projectVersion`:** **`2.3.116`** in `gradle.properties` at repo root.
- **Released-style artifact name:** `FabricAuraSkills-2.3.116.jar` (produced by `:fabric:build` / release bundle; verify with `verifyFabricReleaseBundle` task output).
- **GraniteLib (standalone) version** is tracked separately: **`granitelibVersion=0.1.0-alpha.2`** in `gradle.properties` (not the same as `projectVersion`).

---

## 4. Last committed work (Sprint 83) — *must read before changing related code*

**Commit:** `42e565aa` — *fix(fabric): Sprint 83 Granite literals + /mana view self*

### 4.1 Problem that was fixed

1. **Raw `auraskills.granite.*` strings in chat and built-in menu titles (vanilla Java client):**  
   The server was sending **`Text.translatable(...)`** for mod keys. **Vanilla clients** do not load `assets/auraskills/lang/en_us.json`, so the **translation key** was shown. The JSON on the **server** was not missing; the **wire format** was wrong for clients without the mod’s lang.

2. **`/mana view` “wrong argument” in smoke tests:**  
   The entire **`view`** subcommand was gated with **`auraskills.command.mana.other`**, so players with only **`auraskills.command.mana`** could not “see” the `view` literal in Brigadier and failed at `/mana view`.

### 4.2 Code solution (do not reintroduce the bug)

- **`dev.aurelium.auraskills.fabric.text.FabricGraniteText`:** Resolves a key with **`Language.getInstance().get(key)`** on the **server** and returns **`Text.literal(...)`** (and `String.format` when args are used). **Use this** for the same class of “player must see text without client mod assets” for **new** `auraskills.granite.*` chat or **simple** titles if you add more paths.
- **Touched for Sprint 83:** `FabricBrigadierCommands` (usage, help, skills/stats “opened” feedback), `FabricMenuRegistry` (built-in **Skills/Stats** window titles from YAML), `FabricOpenMenuCommands#sendListMenuFeedback` (self + `opened_for`), **`FabricManaCommands`**: `literal("view").executes(executeSelf)` + **`MANA_OTHER` only** on the **`player` argument** for `/mana view <player>`.

- **Not fully converted:** Deeper `Fabric*MenuGui` item lore and many `Text.translatable` calls in other menus may still show raw keys on **vanilla** clients; Sprint 83 fixed the **Sprint 82**-reported surfaces only.

### 4.3 Tests

- **`FabricGraniteEnUsKeysTest`:** Asserts the six critical keys exist in **`fabric/src/main/resources/assets/auraskills/lang/en_us.json`** (classpath). Extend if you add new mandatory keys.

**Standard post-change commands (user expectation):**

```powershell
Set-Location "F:\FabricAuraSkills"
.\gradlew -PfabricOnly=true :fabric:test
.\gradlew -PfabricOnly=true :fabric:build
```

If **`common`** or **shared** command code used by Bukkit is changed:

```powershell
.\gradlew :bukkit:compileJava
```

Sprint 83 did **not** require the Bukkit line.

---

## 5. Sprints 82 / 83 — process vs code

| Sprint | Type | Key doc |
|--------|------|---------|
| **82** | **Operator** full Java dedicated **smoke matrix** (NRP for agents) | `fabric/SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md` |
| **83** | **Code** + manual doc for translation wire + `/mana view` | `fabric/SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md` |

- **Sprint 82** tables are still **for humans** to fill (Pass / Fail / Skip) on a **disposable** dedicated Fabric server. The agent **cannot** join a world; treat operator rows as the source of truth.
- **Matrix row for mana (post–83):** document **`/mana`**, **`/mana view`** (self), **`/mana view <player>`** (requires **`mana.other`**) in `SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`.

**Bedrock / Geyser** validation remains **backlog** (see `fabric/PARITY_BACKLOG.md`); it was **not** in Sprint 82/83.

---

## 6. Documentation map (edit when behavior or parity changes)

| File | When to update |
|------|----------------|
| `Changelog.md` | Any user-visible or protocol-worthy change; follow existing style; bump `projectVersion` in `gradle.properties` when the team’s release discipline says so. |
| `fabric/COMMAND_PARITY_AUDIT.md` | Fabric command **surface** or parity meaning changes. |
| `fabric/MENU_AND_COMMANDS_PORT.md` | Menus, commands, Slate/Fabric plan. |
| `fabric/PARITY_BACKLOG.md` | Product gaps, sprint bullets, Bedrock follow-up. |
| `fabric/PARITY_AUDIT_LEDGER_PHASE1.md` | High-level program status line. |
| `fabric/SPRINT_7*_…` / `SPRINT_8*_…` | Manual / smoke docs — per sprint instructions. |
| `FABRIC_PORT_HANDOFF.md` (repo root) | Big-picture port / roadmap. |

---

## 7. Build / fabric-only discipline

- Use **`-PfabricOnly=true`** when the user or docs ask for a **fast Fabric-only** subset (see `settings.gradle.kts` / comments in `gradle.properties`).
- Loom/Gradle may print deprecation warnings; **treat** `BUILD SUCCESSFUL` and failing tests as the gate, not the warnings alone.

**Windows shell:** The user is on **PowerShell**; `&&` can fail in older PowerShell. Prefer:

```powershell
Set-Location "F:\FabricAuraSkills"
.\gradlew -PfabricOnly=true :fabric:test
```

---

## 8. Git state you must be aware of (as of 2026-04-21)

- **Last commit (included work):** `42e565aa` — **Sprint 83** (above).
- **Branch:** `feature/granitelib-module-ids` **ahead of** `origin` by a large number of commits (the user’s branch may or may not be pushed).
- **Uncommitted (NOT in Sprint 83 commit) — do not assume clean tree:**
  - **Modified (tracked):** `fabric/SPRINT_21_ANVIL_DISCOUNT_PARITY.md`, `fabric/.../AnvilScreenHandlerAnvilDiscountMixin.java`, `slate-fabric/build.gradle.kts` — anvil-mixin / slate work **in progress or parallel**; **coordinate** with the user before merging or discarding.
  - **Untracked:** `docs/architecture/granitelib-*.md`, `docs/handoffs/2026-07-*.md` (dated **July** in filenames — may be from another pass), `fabric/MOD_COMMANDS.txt`, `fabric/SPRINT_76A_*.md`, `fabric/SPRINT_SUMMARY_SPEC.md`, `fabric/logs/`, `run-strict/`, `slate-fabric/src/main/resources/`, etc. **Do not** treat as canonical unless the user points you at them.

**Rule of thumb:** Before starting a new task, run `git status` and ask the user if the dirty files are part of the **current** request.

---

## 9. Recommended next steps (product process)

1. **Operator:** Re-run the **Sprint 82** matrix (or **Sprint 78** minimum) on **`2.3.116+`**, with a **vanilla** Java client, and update Pass/Fail in `SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md` / `SPRINT_78_JAVA_SMOKE_EXECUTION_RESULTS.md`.
2. **Code (if smoke finds more raw keys):** Extend **literal resolution** to additional `Fabric*MenuGui` / command paths, or add more keys to `FabricGraniteEnUs` tests.
3. **Backlog:** When Java matrix is **green**, schedule **Bedrock/Geyser** action bar and menu checks per `PARITY_BACKLOG.md` (separate scope).

---

## 10. User / agent preferences (from ongoing sessions)

- **Execute** shell and builds yourself; do not only tell the user to run commands.
- **Date authority:** use the **“Today’s date”** from the user_info block when the user provides it (e.g. 2026).
- **Code style:** **Minimal, focused diffs**; do not refactorscope; match existing patterns; avoid drive-by renames.
- **Citations** in chat: use the project’s ` ```start:end:path ` **code reference** format when pointing at code.
- **Optional Cursor skills** live under the user’s skills path (e.g. `ce-sessions`, `ce-debug`, `git-commit`); use when the task matches, but this repo’s **Sprint** docs are the **source of truth** for Granite process.

---

## 11. One-line summary for the next agent

**You are in `F:\FabricAuraSkills` on `feature/granitelib-module-ids` at `42e565aa`:** Sprint **83** fixed **raw Granite translation keys** for vanilla clients via **`FabricGraniteText`** and fixed **`/mana view`** (self = `/mana`, **other** = `view <player>` + **`mana.other`**). **Version `2.3.116`**. The **Sprint 82** smoke **matrix** is still **operator-filled**; the working tree has **separate** anvil-mixin and **untracked** docs — check **`git status`** first. **Bedrock** not validated. Use **`.\gradlew -PfabricOnly=true :fabric:test`** (and **`:fabric:build`** if you change `fabric` sources) before handing work back.
