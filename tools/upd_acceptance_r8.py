# -*- coding: utf-8 -*-
"""R8: expand AC-MATRIX-FINAL depends_on, reopen unsupported PASS, provenance for 2.3.240 + alpha.21."""
from __future__ import annotations

import hashlib
import json
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(r"F:\FabricAuraSkills\fabricauraskills")
ACCEPT = ROOT / ".cursor" / "granite-completion" / "acceptance-state.json"
LEDGER = ROOT / "fabric" / "FULL_COMPLETION_LEDGER.md"
SERVER = Path(r"F:\gs-lifecycle-server")
JAR = ROOT / "fabric" / "build" / "libs" / "GraniteSkills-2.3.240.jar"
LIB = ROOT / "granitelib-fabric" / "build" / "libs" / "granitelib-fabric-0.2.0-alpha.21.jar"
FAIL_HASH_SCRIPT = SERVER / "scripts" / "mysql-fail-hash.ps1"

# Cases whose PASS rested on helper/suite artifacts, not defensible event/migration proof for R8.
REOPEN = {
    "AC-MIGRATION-SQL": {
        "rejects": [SERVER / "mysql-suite-probe-result.txt"],
        "prefers": SERVER / "mysql-migration-probe-result.txt",
        "note": "Suite probe only marks schema_migrations; real proof is mysql-migration-probe (rerun on current pair before PASS).",
    },
    "AC-ABILITY-RETRIEVAL": {
        "rejects": ["remaining-ability-probe", "ability-wire-probe"],
        "prefers": SERVER / "retrieval-event-probe-result.txt",
        "note": "Helper/remaining-ability probes are wiring smoke only; event-path PASS requires validated event probe on current pair.",
    },
    "AC-ABILITY-REPAIRING": {
        "rejects": ["remaining-ability-probe", "ability-wire-probe"],
        "prefers": SERVER / "repairing-event-probe-result.txt",
        "note": "Helper/remaining-ability probes are wiring smoke only; event-path PASS requires validated event probe on current pair.",
    },
    "AC-ABILITY-SPLASHER": {
        "rejects": ["remaining-ability-probe", "ability-wire-probe"],
        "prefers": SERVER / "splasher-event-probe-result.txt",
        "note": "Helper/remaining-ability probes are wiring smoke only; event-path PASS requires validated event probe on current pair.",
    },
    "AC-XP-JUMP": {
        "rejects": [],
        "prefers": SERVER / "jump-xp-event-probe-result.txt",
        "note": "Unit + jumpFromGround on 2.3.237 remain historical; jump-xp event probe must be validated on current pair.",
    },
}


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest().upper()


def last_line(path: Path) -> str:
    if not path.exists():
        return "missing"
    lines = [ln.strip() for ln in path.read_text(encoding="utf-8", errors="replace").splitlines() if ln.strip()]
    return lines[-1] if lines else "empty"


def matrix_in_scope_depends(data: dict) -> list[str]:
    by_id = {c["id"]: c for c in data["cases"]}
    out = []
    for cid in data["required_ids"]:
        if cid == "AC-MATRIX-FINAL":
            continue
        case = by_id.get(cid)
        if case and case.get("status") == "EXCLUDED BY EXISTING SCOPE":
            continue
        out.append(cid)
    return out


def mark_evidence_historical(evidence: list) -> None:
    hist_markers = ("2.3.235", "2.3.236", "2.3.237", "2.3.239", "alpha.19", "alpha.20")
    for e in evidence:
        ref = str(e.get("ref", ""))
        if any(m in ref for m in hist_markers) and e.get("validated"):
            e.setdefault("applicability", "historical-2.3.237-or-earlier-freeze")
        if "GraniteSkills-2.3.239" in ref or "2.3.239" in ref:
            e.setdefault("applicability", "historical-2.3.239-freeze-not-current-pair")


def reopen_case(case: dict, spec: dict) -> None:
    case["status"] = "NOT STARTED"
    case["blockers"] = []
    pref = spec["prefers"]
    case["next_action"] = (
        f"R8 reopened: {spec['note']} Preferred artifact: {pref.name} (last={last_line(pref)})."
    )
    ev = case.setdefault("evidence", [])
    rejects = spec.get("rejects") or []
    filtered = []
    for e in ev:
        ref = str(e.get("ref", ""))
        if any(str(r) == ref or (isinstance(r, str) and r in ref) for r in rejects):
            e = dict(e)
            e["validated"] = False
            e["applicability"] = "superseded-r8"
        filtered.append(e)
    case["evidence"] = filtered
    for e in case["evidence"]:
        e["validated"] = False
    if pref.exists():
        entry = {
            "kind": "dedicated-server",
            "ref": str(pref),
            "validated": False,
            "note": spec["note"],
            "applicability": "pending-2.3.240-alpha.21",
        }
        for i, e in enumerate(case["evidence"]):
            if str(e.get("ref")) == str(pref):
                case["evidence"][i] = {**e, **entry}
                break
        else:
            case["evidence"].append(entry)


def main() -> None:
    data = json.loads(ACCEPT.read_text(encoding="utf-8"))
    by_id = {c["id"]: c for c in data["cases"]}
    matrix = by_id["AC-MATRIX-FINAL"]
    before_dep = len(matrix.get("depends_on", []))
    new_dep = matrix_in_scope_depends(data)
    matrix["depends_on"] = new_dep
    after_dep = len(new_dep)

    reopened = []
    for cid, spec in REOPEN.items():
        if cid in by_id:
            reopen_case(by_id[cid], spec)
            reopened.append(cid)

    # AC-MYSQL-FAIL: reference external hash script without re-proving here.
    fail = by_id.get("AC-MYSQL-FAIL")
    if fail and FAIL_HASH_SCRIPT.exists():
        ev = fail.setdefault("evidence", [])
        ref = str(FAIL_HASH_SCRIPT)
        if not any(e.get("ref") == ref for e in ev):
            ev.append(
                {
                    "kind": "script",
                    "ref": ref,
                    "validated": True,
                    "note": "External YAML digest compare for dead SQL port boot (before/after SHA256).",
                    "applicability": "historical-2.3.239-probe; rerun with current jar",
                }
            )

    for case in data["cases"]:
        mark_evidence_historical(case.get("evidence", []))

    matrix["status"] = "NOT STARTED"
    matrix["blockers"] = [
        {
            "external": True,
            "action": (
                "MATRIX PASS waits on AC-CLIENT-JAVA, AC-CLIENT-BEDROCK, AC-LIFECYCLE-GEYSER, "
                "AC-PACK-PRODUCTION, and all reopened in-scope NOT STARTED cases (incl. AC-MIGRATION-SQL, "
                "ability event paths). MySQL round-trip cases remain PASS on historical 2.3.239 evidence "
                "until rerun on GraniteSkills-2.3.240 + granitelib-fabric-0.2.0-alpha.21."
            ),
        }
    ]
    matrix["next_action"] = (
        "R8: depends_on expanded to full in-scope required set. Partial matrix freeze on disk is "
        "2.3.239 / alpha.20 (matrix-final-partial-probe-result.txt) — historical only. "
        "Current review pair is 2.3.240 + alpha.21; rerun partial matrix after blockers clear."
    )
    for e in matrix.get("evidence", []):
        if "2.3.239" in str(e.get("ref", "")) or "2.3.236" in str(e.get("ref", "")):
            e["applicability"] = "historical-freeze-boot"
            e["validated"] = False
    if JAR.exists():
        matrix.setdefault("evidence", []).append(
            {
                "kind": "packaged-jar",
                "ref": str(JAR),
                "validated": False,
                "sha256": sha256(JAR),
                "note": "Current R8 review bytes — no dedicated-server MATRIX run recorded yet.",
                "applicability": "current-pair-candidate",
            }
        )
    if LIB.exists():
        matrix.setdefault("evidence", []).append(
            {
                "kind": "packaged-jar",
                "ref": str(LIB),
                "validated": False,
                "sha256": sha256(LIB),
                "applicability": "current-pair-candidate",
            }
        )

    data["applicability_assessment"] = {
        "current_pair": {
            "graniteskills": "2.3.240",
            "granitelib": "0.2.0-alpha.21",
            "graniteskills_sha256": sha256(JAR) if JAR.exists() else None,
            "granitelib_sha256": sha256(LIB) if LIB.exists() else None,
        },
        "historical_execution_labels": [
            "Most gameplay/provider disposable-server logs and partial matrix freeze: 2.3.237–2.3.239.",
            "Do not treat historical jar pins as evidence of 2.3.240 execution.",
        ],
        "reopened_r8": reopened,
        "matrix_depends_on_before": before_dep,
        "matrix_depends_on_after": after_dep,
        "updated_at": datetime.now(timezone.utc).isoformat(),
    }

    counts = Counter(c["status"] for c in data["cases"])
    data["summary"] = dict(counts)
    data["overall"] = "INCOMPLETE"
    data["updated_at"] = datetime.now(timezone.utc).isoformat()
    data["unfinished_ids"] = [
        c["id"] for c in data["cases"] if c["status"] not in ("PASS", "EXCLUDED BY EXISTING SCOPE")
    ]
    ACCEPT.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")

    print("MATRIX depends_on", before_dep, "->", after_dep)
    print("reopened", reopened)
    print("summary", data["summary"])
    print("unfinished", len(data["unfinished_ids"]))


if __name__ == "__main__":
    main()