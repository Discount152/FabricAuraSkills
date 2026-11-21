# -*- coding: utf-8 -*-
"""R8: expand MATRIX deps, pin 2.3.240 R6/R7 evidence, provenance notes. Overall stays INCOMPLETE."""
from __future__ import annotations

import hashlib
import json
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(r"F:\FabricAuraSkills\fabricauraskills")
ACCEPT = ROOT / ".cursor" / "granite-completion" / "acceptance-state.json"
SERVER = Path(r"F:\gs-lifecycle-server")
JAR = ROOT / "fabric" / "build" / "libs" / "GraniteSkills-2.3.240.jar"
SOURCES = ROOT / "fabric" / "build" / "libs" / "GraniteSkills-2.3.240-sources.jar"
LIB = ROOT / "granitelib-fabric" / "build" / "libs" / "granitelib-fabric-0.2.0-alpha.21.jar"
LIB_SOURCES = ROOT / "granitelib-fabric" / "build" / "libs" / "granitelib-fabric-0.2.0-alpha.21-sources.jar"
DOCS = ROOT / "docs" / "solutions"


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest().upper()


def last_line_pass(path: Path) -> bool:
    if not path.exists():
        return False
    lines = [ln.strip() for ln in path.read_text(encoding="utf-8", errors="replace").splitlines() if ln.strip()]
    return bool(lines) and lines[-1] == "PASS"


def pin(case: dict, ref: Path, kind: str = "dedicated-server", note: str | None = None, validated: bool = True):
    ev = case.setdefault("evidence", [])
    entry = {"kind": kind, "ref": str(ref), "validated": validated}
    if note:
        entry["note"] = note
    for existing in ev:
        if existing.get("ref") == str(ref):
            existing.update(entry)
            return
    ev.append(entry)


def mark_historical(case: dict):
    """Keep historical 2.3.237 evidence but mark applicability — never relabel as 2.3.240."""
    for e in case.get("evidence", []):
        ref = str(e.get("ref", ""))
        note = e.get("note") or ""
        if "2.3.237" in ref or "2.3.237" in note or "on 2.3.237" in note:
            e["applicability"] = (
                "HISTORICAL — executed on GraniteSkills 2.3.237. "
                "Valid as prior wiring/smoke evidence only; not a 2.3.239/2.3.240 matrix execution."
            )


def main() -> None:
    d = json.loads(ACCEPT.read_text(encoding="utf-8"))
    jar_hash = sha256(JAR)
    lib_hash = sha256(LIB)

    d["frozen_pair"] = {
        "graniteskills": "2.3.240",
        "granitelib": "0.2.0-alpha.21",
        "graniteskills_sha256": jar_hash,
        "granitelib_sha256": lib_hash,
        "sources_sha256": sha256(SOURCES),
        "granitelib_sources_sha256": sha256(LIB_SOURCES),
    }
    d["provenance"] = {
        "current_pair": "GraniteSkills 2.3.240 + GraniteLib 0.2.0-alpha.21",
        "historical_note": (
            "Many gameplay/provider logs under F:\\gs-lifecycle-server were produced on 2.3.237. "
            "Those artifacts retain their original version labels and are assessed as historical "
            "applicability only. Do not treat them as a 2.3.239 or 2.3.240 matrix run."
        ),
        "r2_dep": str(SERVER / "r2-dep-probe-result.txt"),
        "mysql_fail_hash_script": str(SERVER / "scripts" / "mysql-fail-hash.ps1"),
    }

    event_updates = {
        "AC-MIGRATION-SQL": (
            SERVER / "mysql-migration-probe-result.txt",
            "Real LegacySqlTables migrate + SqlMigrator rerun on MySQL 8.4.13; version from server; "
            "disabled/enabled rename; rewrite IDs; second migrate no-op (2.3.240)",
        ),
        "AC-ABILITY-RETRIEVAL": (
            SERVER / "retrieval-event-probe-result.txt",
            "AbstractArrow onHitBlock schedules retrieval; arrow delivered after delay (2.3.240)",
        ),
        "AC-ABILITY-REPAIRING": (
            SERVER / "repairing-event-probe-result.txt",
            "AnvilMenu.createResult repairing path durabilityImproved (2.3.240)",
        ),
        "AC-ABILITY-SPLASHER": (
            SERVER / "splasher-event-probe-result.txt",
            "ThrownSplashPotion.onHitAsPotion duration scaled vs baseline; scale restored (2.3.240)",
        ),
        "AC-XP-JUMP": (
            SERVER / "jump-xp-event-probe-result.txt",
            "FabricJumpingXpHandler END_SERVER_TICK path; interval jumps award XP (2.3.240)",
        ),
    }

    for case in d["cases"]:
        mark_historical(case)
        cid = case["id"]
        if cid in event_updates:
            ref, note = event_updates[cid]
            if last_line_pass(ref):
                case["status"] = "PASS"
                case["blockers"] = []
                case["next_action"] = ""
                pin(case, ref, note=note, validated=True)
                pin(
                    case,
                    JAR,
                    kind="packaged-jar",
                    note=f"GraniteSkills-2.3.240 sha256={jar_hash}",
                    validated=True,
                )

        if cid == "AC-MYSQL-FAIL":
            pin(
                case,
                SERVER / "scripts" / "mysql-fail-hash.ps1",
                kind="script",
                note="External YAML digest compare for dead SQL port boot (before/after)",
                validated=True,
            )

        if cid == "AC-MATRIX-FINAL":
            required = [
                c["id"]
                for c in d["cases"]
                if c["status"] != "EXCLUDED BY EXISTING SCOPE" and c["id"] != "AC-MATRIX-FINAL"
            ]
            before = len(case.get("depends_on") or [])
            case["depends_on"] = sorted(set(required))
            case["status"] = "NOT STARTED"
            case["blockers"] = [
                {
                    "external": True,
                    "action": (
                        "MATRIX PASS waits on remaining external BLOCKED: AC-CLIENT-JAVA, "
                        "AC-CLIENT-BEDROCK, AC-LIFECYCLE-GEYSER, AC-PACK-PRODUCTION. "
                        "R1–R8 executable work updated on 2.3.240 + alpha.21; overall INCOMPLETE."
                    ),
                }
            ]
            case["next_action"] = (
                f"depends_on expanded {before} -> {len(case['depends_on'])} required in-scope cases. "
                "Rerun affected probes + partial matrix against frozen 2.3.240 + alpha.21 bytes; "
                "full MATRIX PASS withheld while external blockers remain."
            )
            pin(
                case,
                SERVER / "r2-dep-probe-result.txt",
                note="R2 reject alpha.18 / accept alpha.21 on 2.3.240",
                validated=last_line_pass(SERVER / "r2-dep-probe-result.txt"),
            )

    counts = Counter(c["status"] for c in d["cases"])
    d["summary"] = dict(counts)
    d["overall"] = "INCOMPLETE"
    d["updated_at"] = datetime.now(timezone.utc).isoformat()
    d["unfinished_ids"] = [
        c["id"] for c in d["cases"] if c["status"] not in ("PASS", "EXCLUDED BY EXISTING SCOPE")
    ]
    ACCEPT.write_text(json.dumps(d, indent=2) + "\n", encoding="utf-8")

    DOCS.mkdir(parents=True, exist_ok=True)
    (DOCS / "r8-armor-backup-findings.md").write_text(
        """# R8 armor ownership + automatic backup findings (2.3.240)

## Armor stack ownership — mitigated (confirmed by code)

`LivingEntityArmorRequirementMixin` cancels failed equip at HEAD. It restores via
`placeItemBackInInventory(stack.copy())` **only when** the rejected stack is no longer
in main/off hand (`ItemStack.isSameItemSameComponents`). If the caller still owns the
stack (inventory/shift-click paths), it does not duplicate.

Verdict: prior duplicate-restore defect addressed; remaining risk is equip-path coverage
on a real Java client (AC-CLIENT-JAVA still BLOCKED).

## Automatic backup failure/retry ordering — Fabric fixed; Bukkit still wrong

Fabric `FabricGraniteSkills.backupAutomatically()`:
1. `backupProvider.saveBackupSync(false)`
2. only then write `last_automatic_backup` + save meta.yml

So a failed backup does not advance the interval timestamp (can retry).

Bukkit `GraniteSkills.backupAutomatically()` still writes `last_automatic_backup`
**before** `saveBackupSync` — confirmed defect on Bukkit path, out of Fabric pair scope
unless Bukkit parity is required.

## Evidence pointers

- Mixin: `fabric/.../LivingEntityArmorRequirementMixin.java`
- Fabric backup: `FabricGraniteSkills.backupAutomatically` (after-success meta write)
- Bukkit backup: `bukkit/.../GraniteSkills.java` lines ~338–357 (before-success meta write)
""",
        encoding="utf-8",
    )

    print("summary", d["summary"])
    print("unfinished", d["unfinished_ids"])
    print("matrix_depends", len(next(c for c in d["cases"] if c["id"] == "AC-MATRIX-FINAL")["depends_on"]))
    print("jar", jar_hash)
    print("lib", lib_hash)


if __name__ == "__main__":
    main()
