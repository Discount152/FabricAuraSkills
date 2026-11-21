# Audit handoff for Claude Fable 5.2

Prepared: 2026-11-16. Product tree: Granite Skills (Fabric) + GraniteLib. Working freeze: **Granite Skills 2.3.236** + **GraniteLib 0.2.0-alpha.19**.

## What this zip is

Full project sources for `F:\FabricAuraSkills\fabricauraskills` with `build/`, `.gradle/`, and `run/` omitted. Extra audit materials are under `_audit_handoff/`.

GitHub (if preferred over the zip): https://github.com/Discount152/FabricAuraSkills  
Local remote at handoff time: `origin` → `https://github.com/discount152/fabricauraskills`  
Starting restoration commit recorded in the ledger: `0c204cb86689b5a4d4609202c97ea47ba9f041b7` (2.3.179 packaging). Working tree has continued past that to **2.3.236**.

## Upstream / port-from references

| Role | Reference |
| --- | --- |
| Original AuraSkills (Archyx), GPL-3.0 | https://github.com/ArchyxDev/AuraSkills — pull current upstream yourself for formula/feature comparison. In-repo Bukkit sources under `bukkit/` are the actively used Paper/Bukkit reference for this port (package rewritten to `dev.granite.graniteskills`). |
| Feature-rich Fabric baseline used for restoration | branch `feature/granitelib-module-ids` commit `7fae18a8d62c98ec4f38bde84e35004bdd4bd3ad` (labeled 2.3.176, MC 1.21.11 / Java 21). |
| 26.3 gameplay base (historical) | `cursor/granite-skills-26.3-65b4` / `c504b25f…` |
| This completion line | Minecraft **26.3** Mojmap, Fabric Loader **0.20.1**, Fabric API **0.166.2+26.3**, Java **25**. |

License in-repo: `LICENSE.md` is **GPL-3.0**. Mod metadata credits Archyx (AuraSkills) and Discount152 (Fabric port).

## Deployment target

- **Server-side only.** Vanilla / unmodded Java clients join a dedicated Fabric 26.3 server. No required client mod.
- Bedrock is intended via **Geyser + Floodgate** (not bundled; not verified in this environment).
- Single-player / integrated client is not the acceptance target; dedicated-server probes are the proof path.

## Scope: ported / dropped / unfinished

### Meant to be ported (in scope)

- Core skills, stats, traits, passive abilities, mana abilities, XP sources, loot, requirements, items/modifiers, YAML (+ MySQL) storage, commands, menus via **GraniteLib** (Slate successor, not a second Slate engine).
- Placed-block tracking / `check_replace`, permission XP multipliers, backups/presets, identity migration from AuraSkills folders/items.
- MiniMessage / Adventure text (shaded Kyori in the gameplay jar so Geyser’s Adventure 5 is not shared).

### Deliberately dropped / EXCLUDED BY EXISTING SCOPE

- Jobs / economy products.
- Optional Bukkit bridges as products: WorldGuard, Vault, PlaceholderAPI, MythicMobs, ProtocolLib (and the same class of plugin bridges).
- **Not** excluded merely because they sat next to those bridges: placed-block protection, native permissions, loot requirements.

### Not done yet (authoritative unfinished acceptance rows)

See `fabric/FULL_COMPLETION_LEDGER.md` and `.cursor/granite-completion/acceptance-state.json`. At handoff:

- **BLOCKED (external):** `AC-HUD-MYSQL`, `AC-MYSQL-*`, `AC-MIGRATION-SQL`, `AC-PERM-PROVIDER` (real LuckPerms), `AC-CLIENT-JAVA`, `AC-CLIENT-BEDROCK`, `AC-PACK-PRODUCTION`, `AC-LIFECYCLE-GEYSER`.
- **NOT STARTED:** `AC-MATRIX-FINAL` (partial freeze boot only on 2.3.236; PASS withheld while blockers remain).
- Overall status: **INCOMPLETE** (258 PASS / 14 BLOCKED / 7 excluded / 1 matrix not started).

Implementation vs provider: `AC-REVIEW-F7` and `AC-PERM-*` cover the multiplier **parser/summation** and grant-list contract. Live LuckPerms allow/deny/inheritance/Floodgate is still `AC-PERM-PROVIDER`.

## Known worries / prioritize these

1. **Permission multipliers without LuckPerms** — Fabric walks LuckPerms’ resolved map (or a probe seam). No LP ⇒ multiplier 0. Confirm this matches intended production ops.
2. **Hot paths** — block break (Speed Mine / Terraform / Treecapitator / placed-block checks), damage (Absorption, Meal Steal, combat XP), tick/scheduler work, chunk-load lifecycle (2.3.178 hang fix must stay).
3. **Data loss** — YAML/MySQL save paths, backup/restore confirm gates, identity migration idempotence; MySQL suite never run here (Docker engine unavailable).
4. **Client rendering** — dedicated-server packet proofs exist; Java/Bedrock visual confirmation missing.
5. **Ability/mana evidence mix** — many ability probes call helpers (incl. forced rolls for probability). Event-path integration was added for Speed Mine, Terraform, Absorption, and Meal Steal; other abilities may still be helper-direct.
6. **Kyori shade / Geyser** — gameplay jar relocates `net.kyori`; Geyser isolation not re-proven with real Geyser jars in this environment.
7. **Stale plan todos U1–U17** — plan checklist drifted; trust acceptance-state + ledger, not the plan YAML statuses.

## Design notes (Paper-free pieces)

| Area | Fabric approach |
| --- | --- |
| Storage | `FileStorageProvider` / `SqlStorageProvider`; MySQL required for SQL PASS (not MariaDB substitute). |
| Permissions | `fabric-permissions-api` for `hasPermission` / skill nodes; XP multipliers via LuckPerms cached map when present (`FabricLuckPermsGrants`). |
| Placeholders | Not a PAPI product; menu/message placeholders via GraniteLib / internal formatters. |
| Menus | GraniteLib file-backed menus; gameplay menus migrated onto those contracts. |
| Economy | Excluded. |
| Region protection | Core placed-block store + world disable lists; WorldGuard bridge excluded. |

## Artifacts in `_audit_handoff/`

- `FULL_COMPLETION_LEDGER.md`, `acceptance-state.json`
- `BUNDLE_MANIFEST.md` and frozen jar hashes for 2.3.236 / GraniteLib alpha.19
- Recent disposable-server logs (`matrix-final-partial-probe.log`, `latest.log` if present)
- Selected probe result texts for F7, perm, mana event paths, matrix partial
- Pointers to packaged jars (hashes in the manifest); jars themselves may be large — prefer building from this tree with `./gradlew :fabric:jar :fabric:relocateFabricKyori :fabric:sourcesJar`

## Build hints

```text
Java 25
./gradlew :fabric:jar :fabric:relocateFabricKyori :fabric:sourcesJar
./gradlew :granitelib-fabric:jar
```

Do not treat `./gradlew :fabric:runServer` alone as release proof; packaged jar + dedicated `fabric-server-launch.jar` is the acceptance runtime.
