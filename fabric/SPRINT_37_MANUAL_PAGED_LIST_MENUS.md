# Sprint 37 — manual validation (paged **Skills** + **Stats**)

**Scope:** When enabled skills or stats do not fit in one **page** (list content is capped **below** slot **45**; default list **0..45** = 45 items per page), **Previous** and **Next** (slots from **`granite.paging`**, default **45** and **53**) change **`FabricMenuContext#listPage`**. **Runtime:** Fabric; **clients:** Java and Bedrock (Geyser + Floodgate) if available.

## Prereq

- `menus/skills.yml` / `menus/stats.yml` with **`paging:`** (or JAR defaults). For local testing, temporarily reduce **`list_end_slot_exclusive`** in **`layout`** to force many pages, or add many skills/stats in the server config.

## Java

1. **Skills — paging:** With more than **45** enabled skills, open **Skills**; the title should show **(Page 1 of N)** with **N** greater than **1**. **Next** on slot **53** (default) should open **page 2**; same entries must map to the correct **global** skill. **Previous** on **45** returns. Click a skill on **page 2** — **Level Progression** must match that skill, not a skill from page 1.
2. **Stats — paging** (with **`menus.stat_info.enabled`** and enough stats): same as (1) for **Stat Info** and **Back** to stats.
3. **Stats read-only (stat_info off):** Paging should still work; list clicks are ignored; no crash.

## Bedrock / Geyser

- Repeat **4** and **3**; confirm bottom-row arrows are visible and tappable, and that wrong-index opens do not appear.

## Back navigation

- Open **Level Progression** from a **paged** skill; **Back** to **Skills** should show **page 1** (default context), not a random page. If you need the same page preserved, that is **not** in Sprint 37.

## Invalid / edge

- Bad **`granite.paging`** (overlap with list, prev == next): server log **warn**; **defaults 45/53** used.

## Checklist

| Check | Java | Bedrock |
|-------|------|---------|
| Skills multi-page + detail from page 2+ | [ ] | [ ] |
| Stats multi-page + stat info from page 2+ | [ ] | [ ] |
| `/auraskills reload` after YAML change | [ ] | [ ] |
