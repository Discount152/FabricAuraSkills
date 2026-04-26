# Sprint 83 — Vanilla Java client text + `/mana view` (manual / acceptance)

| Field | Value |
|--------|--------|
| **Sprint 83 status** | **Code accepted** (in-repo). **Operator:** Sprint **82** core rows (usage/help, Skills/Stats titles & feedback, mana self + staff **other**) **validated** on **`2.3.116+`** — see **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`**. **Next operator work:** Sprint **84** admin/permissioned retest — **`SPRINT_84_MANUAL_JAVA_ADMIN_PERMISSIONED_RETEST.md`**. |
| **Code acceptance (in-repo)** | **2026-04-26** — root cause and fix reviewed; **`:fabric:test`** / **`:fabric:build`** **PASS**; artifact **`FabricAuraSkills-2.3.116.jar`**. |
| **Tested artifact (operator target)** | **`FabricAuraSkills-2.3.116.jar`** (`projectVersion` **2.3.116**). |
| **Depends on** | **Sprint 82** smoke findings (raw `auraskills.granite.*` on vanilla clients; `/mana view` Brigadier). |

## Root cause (vanilla Java client)

The server sent **`Text.translatable("auraskills.granite.…")`** for Granite copy. **Vanilla** Java clients do **not** load **`assets/auraskills/lang/en_us.json`** from the mod, so the **wire format** still contained the **translation key**, not English text. The server’s JSON was fine; **`translatable`** was the wrong tool for **server-only** strings when the client has no mod lang table.

## Fix (bounded patch)

- **`FabricGraniteText`:** Resolve the key with **`Language.getInstance().get(key)`** on the **server** and send **`Text.literal(...)`** (with **`String.format`** when args are used). Same `en_us.json` on the server drives the strings.
- **Wired surfaces (Sprint 82 hit list):** **`FabricBrigadierCommands`** (usage, help, skills/stats “opened” feedback), **`FabricMenuRegistry`** (built-in **Skills/Stats** window titles from YAML), **`FabricOpenMenuCommands#sendListMenuFeedback`**, **`FabricManaCommands`** (mana-related feedback as touched in this sprint).
- **`/mana view`:** **`literal("view").executes(executeSelf)`** so **self** view requires only **`auraskills.command.mana`**. **`auraskills.command.mana.other`** applies **only** to **`/mana view <player>`** — previously **`MANA_OTHER`** gated the whole `view` subtree, so Brigadier hid **`view`** for players without **other**, producing “wrong syntax” for **`/mana view`**.

## Code acceptance checklist (complete)

- Raw keys fixed for: **`/auraskills`**, **`/auraskills help`**, skills/stats **opened** feedback, **Skills/Stats** window titles, **`openmenu` list** feedback.
- **`FabricGraniteText`** added and used on the above paths.
- **`/mana view`** works as **self** view with normal mana permission; **`/mana view <player>`** still requires **`mana.other`**.
- Smoke docs / matrix cross-links updated; **`:fabric:test`** and **`:fabric:build`** **PASS**.
- **Bedrock / Geyser:** **not** claimed for this sprint.

## Operator validation — minimum (vanilla Java client, no client mod)

Use **`FabricAuraSkills-2.3.116.jar`**. Full restart recommended after JAR swap.

| Check | Expected |
|--------|----------|
| **`/auraskills`** | No raw **`auraskills.granite.command.*`** keys in chat. |
| **`/auraskills help`** | Same — readable English (or configured server lang), not raw keys. |
| **`/skills`** | Opens menu; window title **not** raw **`auraskills.granite.menu.skills_title`**. |
| **`/stats`** | Opens menu; title **not** raw **`auraskills.granite.menu.stats_title`**. |
| **`/mana`** | Works; readable output. |
| **`/mana view`** | Same as self **`/mana`** with **`auraskills.command.mana`** only (no **`mana.other`** required). |
| **`/mana view <player>`** | Works **only** with proper **other-player** permission; without it, clean permission failure (not wrong syntax for **`view`**). |

After this minimum pass, **continue** the **Sprint 82** matrix on **2.3.116** (remaining rows).

## Known limitation (documented follow-up)

**Deeper menu paths** — item **names**, **lore**, and other **`Text.translatable`** call sites in **`Fabric*MenuGui`** (and similar) were **not** fully converted in Sprint 83. Operators may still see raw **`auraskills.granite.*`** there on vanilla clients. **Follow-up:** extend the same **`FabricGraniteText`** pattern (or equivalent) per path as issues are filed.

## Build (reference)

```powershell
Set-Location "F:\FabricAuraSkills"
.\gradlew -PfabricOnly=true :fabric:test
.\gradlew -PfabricOnly=true :fabric:build
```

## Related docs

- **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`** — full Java smoke matrix; re-run rows touched above first, then the rest.
- **`SPRINT_84_MANUAL_JAVA_ADMIN_PERMISSIONED_RETEST.md`** — next operator sprint: admin/permissioned commands (**not** Sprint 83 scope).
- **`SPRINT_78_JAVA_SMOKE_EXECUTION_RESULTS.md`** — minimum set; sync Pass/Fail when you run.
- **`COMMAND_PARITY_AUDIT.md`** — command surface (Mana row).
- **`Changelog.md`** — **2.3.116** user-visible notes.
