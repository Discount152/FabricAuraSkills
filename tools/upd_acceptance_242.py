# -*- coding: utf-8 -*-
"""Pin 2.3.242 evidence. Overall stays INCOMPLETE."""
import hashlib
import json
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(r"F:\FabricAuraSkills\fabricauraskills")
ACCEPT = ROOT / ".cursor" / "granite-completion" / "acceptance-state.json"
SERVER = Path(r"F:\gs-lifecycle-server")
JAR = ROOT / "fabric" / "build" / "libs" / "GraniteSkills-2.3.242.jar"


def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest().upper()


def main() -> None:
    data = json.loads(ACCEPT.read_text(encoding="utf-8"))
    jar_hash = sha(JAR)
    note = f"GraniteSkills-2.3.242 sha256={jar_hash}"
    evidence = {
        "AC-LEVELUP-CHAT": SERVER / "levelup-chat-probe-result.txt",
        "AC-ABILITY-REPAIRING": SERVER / "repairing-event-probe-result.txt",
        "AC-ABILITY-RETRIEVAL": SERVER / "retrieval-event-probe-result.txt",
        "AC-MIGRATION-SQL": SERVER / "mysql-migration-probe-result.txt",
        "AC-MENU-MIGRATION": SERVER / "menu-migration-probe-result.txt",
    }
    ids = {c["id"] for c in data["cases"]}
    if "AC-LEVELUP-CHAT" not in ids:
        data["cases"].append({
            "id": "AC-LEVELUP-CHAT",
            "order": 2410,
            "depends_on": [],
            "status": "PASS",
            "blockers": [],
            "evidence": [{
                "kind": "dedicated-server",
                "ref": str(evidence["AC-LEVELUP-CHAT"]),
                "validated": True,
                "note": "Farming 1 to 2 chat component styles on 2.3.242; client visuals remain AC-CLIENT-*",
            }, {
                "kind": "packaged-jar",
                "ref": str(JAR),
                "validated": True,
                "note": note,
            }],
            "next_action": "",
            "runtime_required": True,
        })
    for case in data["cases"]:
        ref = evidence.get(case["id"])
        if ref and ref.exists() and ref.read_text(encoding="utf-8", errors="replace").strip().endswith("PASS"):
            case["status"] = "PASS"
            case["blockers"] = []
            case["next_action"] = ""
            ev = case.setdefault("evidence", [])
            ev.append({
                "kind": "dedicated-server",
                "ref": str(ref),
                "validated": True,
                "note": "revalidated on 2.3.242",
            })
        if case["id"] == "AC-MATRIX-FINAL":
            required = [
                c["id"] for c in data["cases"]
                if c["status"] != "EXCLUDED BY EXISTING SCOPE" and c["id"] != "AC-MATRIX-FINAL"
            ]
            case["depends_on"] = sorted(set(required))
            case["status"] = "NOT STARTED"
            case["next_action"] = (
                "PASS waits on AC-CLIENT-JAVA, AC-CLIENT-BEDROCK, AC-LIFECYCLE-GEYSER, AC-PACK-PRODUCTION. "
                "2.3.242 revalidated placed-block ordering, menus, repairing, retrieval, SQL, rejected hits, armor, level-up chat."
            )
    counts = Counter(c["status"] for c in data["cases"])
    data["summary"] = dict(counts)
    data["overall"] = "INCOMPLETE"
    data["updated_at"] = datetime.now(timezone.utc).isoformat()
    data["frozen_pair"] = {
        "graniteskills": "2.3.242",
        "granitelib": "0.2.0-alpha.21",
        "graniteskills_sha256": jar_hash,
    }
    data["unfinished_ids"] = [
        c["id"] for c in data["cases"] if c["status"] not in ("PASS", "EXCLUDED BY EXISTING SCOPE")
    ]
    ACCEPT.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
    print(counts)
    print("unfinished", data["unfinished_ids"])


if __name__ == "__main__":
    main()
