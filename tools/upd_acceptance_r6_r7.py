# -*- coding: utf-8 -*-
"""Reopen R6/R7 probe cases as NOT STARTED when fresh probe artifacts exist — never auto-PASS."""
from __future__ import annotations

import json
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(r"F:\FabricAuraSkills\fabricauraskills")
ACCEPT = ROOT / ".cursor" / "granite-completion" / "acceptance-state.json"
SERVER = Path(r"F:\gs-lifecycle-server")

PROBE_REFS = {
    "AC-MIGRATION-SQL": SERVER / "mysql-migration-probe-result.txt",
    "AC-MYSQL-FAIL": SERVER / "mysql-fail-probe-result.txt",
    "AC-ABILITY-RETRIEVAL": SERVER / "retrieval-event-probe-result.txt",
    "AC-ABILITY-REPAIRING": SERVER / "repairing-event-probe-result.txt",
    "AC-ABILITY-SPLASHER": SERVER / "splasher-event-probe-result.txt",
    "AC-XP-JUMP": SERVER / "jump-xp-event-probe-result.txt",
}

# Map legacy ids that share mysql migration evidence
ALIASES = {
    "AC-MIGRATION-SQL": [SERVER / "mysql-migration-probe-result.txt"],
}


def note_for(path: Path) -> str:
    if not path.exists():
        return "probe artifact missing"
    tail = [ln.strip() for ln in path.read_text(encoding="utf-8", errors="replace").splitlines() if ln.strip()]
    last = tail[-1] if tail else "empty"
    return f"R6/R7 probe present; last line={last} (manual PASS only)"


def main() -> None:
    data = json.loads(ACCEPT.read_text(encoding="utf-8"))
    touched = []
    for case in data["cases"]:
        cid = case["id"]
        paths = ALIASES.get(cid, [PROBE_REFS.get(cid)]) if cid in PROBE_REFS or cid in ALIASES else []
        paths = [p for p in paths if p is not None]
        if not paths:
            continue
        if not any(p.exists() for p in paths):
            continue
        case["status"] = "NOT STARTED"
        case["blockers"] = []
        ref = next(p for p in paths if p.exists())
        case["next_action"] = note_for(ref)
        ev = case.setdefault("evidence", [])
        entry = {
            "kind": "dedicated-server",
            "ref": str(ref),
            "validated": False,
            "note": "reopened by upd_acceptance_r6_r7.py — do not auto-PASS",
        }
        if not any(e.get("ref") == str(ref) for e in ev):
            ev.append(entry)
        touched.append(cid)

    counts = Counter(c["status"] for c in data["cases"])
    data["summary"] = dict(counts)
    data["overall"] = "INCOMPLETE"
    data["updated_at"] = datetime.now(timezone.utc).isoformat()
    data["unfinished_ids"] = [c["id"] for c in data["cases"] if c["status"] not in ("PASS", "EXCLUDED BY EXISTING SCOPE")]
    ACCEPT.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
    print("reopened", touched)
    print("summary", data["summary"])


if __name__ == "__main__":
    main()