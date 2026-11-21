# -*- coding: utf-8 -*-
"""Pin R1 menu ACs and freeze pair to 2.3.241 + alpha.21. Overall stays INCOMPLETE."""
from __future__ import annotations

import hashlib
import json
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(r"F:\FabricAuraSkills\fabricauraskills")
ACCEPT = ROOT / ".cursor" / "granite-completion" / "acceptance-state.json"
SERVER = Path(r"F:\gs-lifecycle-server")
JAR = ROOT / "fabric" / "build" / "libs" / "GraniteSkills-2.3.241.jar"
SOURCES = ROOT / "fabric" / "build" / "libs" / "GraniteSkills-2.3.241-sources.jar"
LIB = ROOT / "granitelib-fabric" / "build" / "libs" / "granitelib-fabric-0.2.0-alpha.21.jar"
LIB_SOURCES = ROOT / "granitelib-fabric" / "build" / "libs" / "granitelib-fabric-0.2.0-alpha.21-sources.jar"


def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest().upper()


def last_pass(path: Path) -> bool:
    if not path.exists():
        return False
    lines = [ln.strip() for ln in path.read_text(encoding="utf-8", errors="replace").splitlines() if ln.strip()]
    return bool(lines) and lines[-1] == "PASS"


def pin(case: dict, ref: Path, kind: str = "dedicated-server", note: str | None = None) -> None:
    ev = case.setdefault("evidence", [])
    entry = {"kind": kind, "ref": str(ref), "validated": True}
    if note:
        entry["note"] = note
    for existing in ev:
        if existing.get("ref") == str(ref):
            existing.update(entry)
            return
    ev.append(entry)


def main() -> None:
    d = json.loads(ACCEPT.read_text(encoding="utf-8"))
    jh = sha(JAR)
    lh = sha(LIB)
    d["frozen_pair"] = {
        "graniteskills": "2.3.241",
        "granitelib": "0.2.0-alpha.21",
        "graniteskills_sha256": jh,
        "granitelib_sha256": lh,
        "sources_sha256": sha(SOURCES),
        "granitelib_sources_sha256": sha(LIB_SOURCES),
    }
    updates = {
        "AC-MENU-MIGRATION": (
            SERVER / "menu-migration-probe-result.txt",
            "All 8 ordinary opens set lastOpenedId via library bridge; registry size 8; alpha.21 (2.3.241)",
        ),
        "AC-REVIEW-F2": (
            SERVER / "review-f2-probe-result.txt",
            "loadDefinition changes title+leaderboard slot on next TemplateSupport open (2.3.241)",
        ),
    }
    for case in d["cases"]:
        cid = case["id"]
        if cid in updates:
            ref, note = updates[cid]
            if last_pass(ref):
                case["status"] = "PASS"
                case["blockers"] = []
                case["next_action"] = ""
                pin(case, ref, note=note)
                pin(case, JAR, kind="packaged-jar", note=f"GraniteSkills-2.3.241 sha256={jh}")
        if cid == "AC-MATRIX-FINAL":
            required = [
                c["id"]
                for c in d["cases"]
                if c["status"] != "EXCLUDED BY EXISTING SCOPE" and c["id"] != "AC-MATRIX-FINAL"
            ]
            case["depends_on"] = sorted(set(required))
            case["status"] = "NOT STARTED"
            case["next_action"] = (
                f"depends_on={len(case['depends_on'])}; PASS waits external blockers on 2.3.241+alpha.21"
            )

    counts = Counter(c["status"] for c in d["cases"])
    d["summary"] = dict(counts)
    d["overall"] = "INCOMPLETE"
    d["updated_at"] = datetime.now(timezone.utc).isoformat()
    d["unfinished_ids"] = [
        c["id"] for c in d["cases"] if c["status"] not in ("PASS", "EXCLUDED BY EXISTING SCOPE")
    ]
    ACCEPT.write_text(json.dumps(d, indent=2) + "\n", encoding="utf-8")
    print("summary", counts)
    print("jar", jh)
    print("lib", lh)


if __name__ == "__main__":
    main()
