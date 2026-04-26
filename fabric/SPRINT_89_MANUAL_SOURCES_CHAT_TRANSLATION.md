# Sprint 89 — Manual validation: `/sources` paged chat (vanilla Java + console)

| Field | Value |
|--------|--------|
| **Sprint** | **89** |
| **Build** | **`FabricAuraSkills-2.3.120.jar`** (`projectVersion` **2.3.120**) |
| **Reason** | Sprint **88** converted built-in **menu GUIs** and **`openmenu`** feedback to **`FabricGraniteText`**; **`FabricSourcesOpenSupport#sendChatListing`** (console/RCON and GUI-exception fallback) still used **`Text.translatable`**, so vanilla clients could see raw **`auraskills.granite.command.sources.*`** keys. Sprint **89** resolves those strings on the server. **`sources.bad_sort`** in **`FabricSourcesCommands`** uses the same pattern. |
| **Bedrock / Geyser** | **Not tested** — **87** remains **deferred**. |

## Paths fixed (code)

| Path | Keys |
|------|------|
| **`FabricSourcesOpenSupport#sendChatListing`** | **`sources.header`**, **`sources.continuation`**, **`sources.line`** |
| **`FabricSourcesOpenSupport#runGuiOrChatFor`** (empty list) | **`sources.empty`** |
| **`FabricSourcesCommands#runWithSortArg`** (invalid sort) | **`sources.bad_sort`** |

**Unchanged behavior:** sort order, pagination chunk size (**28** lines), GUI open logic.

## Sort tokens (current implementation)

Use tab-complete / strict parser tokens, e.g. **`ascending`**, **`descending`**, **`alphabetical`**, **`reverse_alphabetical`** — **not** “highest/lowest” unless your build maps them (default Fabric tree does not).

## Java operator checklist (vanilla client)

Deploy **`FabricAuraSkills-2.3.120.jar`**. Use a **vanilla Java** client.

| Step | Command | Expect |
|------|---------|--------|
| 1 | `/sources farming` | **GUI** opens; if you force fallback (not required), chat lines have **no** raw **`auraskills.granite.*`** |
| 2 | `/sources farming descending` | Same; sort name appears readable in GUI title / chat header |
| 3 | `/sources farming ascending` | Same |
| 4 | `/sources farming not_a_sort` | Readable **`bad_sort`** message (no raw key) |

## Console / RCON

With permission, run e.g. **`sources farming`** (no in-game player): **paged chat** listing only — **no** raw Granite keys in header, continuation pages, or lines.

## Pass/fail

**In-repo agent:** **NRP** in-game; **`.\gradlew -PfabricOnly=true :fabric:test :fabric:build`** **PASS** at doc time.

| Check | Pass | Fail |
|-------|------|------|
| Chat header / continuation / lines | | |
| Empty sources message | | |
| Bad sort error | | |

## Tests

- **`FabricGraniteEnUsKeysTest.sprint89SourcesPagedChatKeysPresentInEnUs`** — **`header`**, **`continuation`**, **`line`**
- **`FabricMenuGuiTranslatableScanTest`** — **`FabricSourcesOpenSupport`**, **`FabricSourcesCommands`** must not contain **`Text.translatable("auraskills.granite`**

## Known limitations

- Other commands may still use **`Text.translatable`** for **`auraskills.granite.*`** outside this sprint’s scope; operator smoke can file follow-ups.
- **Bedrock** path for **`/sources`** not validated here.

## Recommended next sprint

- Operator **`SPRINT_88`** + **`SPRINT_89`** spot-check on **`2.3.120+`**, or **Sprint 87** when a **Bedrock** tester is available.
