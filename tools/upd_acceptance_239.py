# -*- coding: utf-8 -*-
from pathlib import Path
import json
import hashlib
from collections import Counter
from datetime import datetime, timezone

ROOT = Path(r"F:\FabricAuraSkills\fabricauraskills")
ACCEPT = ROOT / ".cursor" / "granite-completion" / "acceptance-state.json"
SERVER = Path(r"F:\gs-lifecycle-server")
JAR = ROOT / "fabric" / "build" / "libs" / "GraniteSkills-2.3.239.jar"
SOURCES = ROOT / "fabric" / "build" / "libs" / "GraniteSkills-2.3.239-sources.jar"
LIB = ROOT / "granitelib-fabric" / "build" / "libs" / "granitelib-fabric-0.2.0-alpha.20.jar"
LIB_SOURCES = ROOT / "granitelib-fabric" / "build" / "libs" / "granitelib-fabric-0.2.0-alpha.20-sources.jar"


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest().upper()


def last_pass(path: Path) -> bool:
    if not path.exists():
        return False
    lines = [ln.strip() for ln in path.read_text(encoding="utf-8", errors="replace").splitlines() if ln.strip()]
    return bool(lines) and lines[-1] == "PASS"


def pin(case, ref: Path, kind="dedicated-server", note=None):
    ev = case.setdefault("evidence", [])
    entry = {"kind": kind, "ref": str(ref), "validated": True}
    if note:
        entry["note"] = note
    if not any(e.get("ref") == str(ref) for e in ev):
        ev.append(entry)
    jar_ref = str(JAR)
    if not any(e.get("ref") == jar_ref for e in ev):
        ev.append({"kind": "packaged-jar", "ref": jar_ref, "validated": True, "sha256": sha256(JAR)})


d = json.loads(ACCEPT.read_text(encoding="utf-8"))

updates = {
    "AC-PERM-PROVIDER": (SERVER / "perm-provider-probe-result.txt", "real LuckPerms 5.5.85 allow/deny/inheritance/non-op; Floodgate UUID-shaped path"),
    "AC-HUD-MYSQL": (SERVER / "mysql-suite-probe-result.txt", "idle_mode ALWAYS round-trip on MySQL 8.4.13"),
    "AC-MYSQL-SCHEMA": (SERVER / "mysql-suite-probe-result.txt", "graniteskills_* tables including schema_migrations"),
    "AC-MYSQL-USERS": (SERVER / "mysql-suite-probe-result.txt", "concurrent two-user save/load"),
    "AC-MYSQL-OFFLINE": (SERVER / "mysql-suite-probe-result.txt", "loadState offline mining profile"),
    "AC-MYSQL-MODIFIERS": (SERVER / "mysql-suite-probe-result.txt", "stat/trait modifiers SQL round-trip"),
    "AC-MYSQL-ITEMS": (SERVER / "mysql-suite-probe-result.txt", "registered unclaimed items SQL round-trip"),
    "AC-MIGRATION-SQL": (SERVER / "mysql-suite-probe-result.txt", "schema_migrations present; second table list no-op"),
    "AC-MYSQL-RESTART": (SERVER / "mysql-restart-probe-result.txt", "prior-boot seed survives new process"),
    "AC-MYSQL-FAIL": (SERVER / "mysql-fail-probe-result.txt", "dead SQL port leaves YAML userdata hash intact"),
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

for case in d["cases"]:
    if case["id"] != "AC-MATRIX-FINAL":
        continue
    ref = SERVER / "matrix-final-partial-probe-result.txt"
    case["status"] = "NOT STARTED"
    case["blockers"] = [
        {
            "external": True,
            "action": (
                "MATRIX PASS waits on remaining external BLOCKED: Java client, Bedrock/Geyser+Floodgate, "
                "production pack. MySQL suite and LuckPerms provider are PASS on 2.3.239."
            ),
        }
    ]
    if last_pass(ref):
        pin(case, ref, note="partial freeze boot on GraniteSkills-2.3.239 + granitelib 0.2.0-alpha.20")
        case["next_action"] = (
            "Partial matrix freeze recorded for GraniteSkills-2.3.239 + granitelib 0.2.0-alpha.20. "
            "MATRIX PASS waits on AC-CLIENT-JAVA, AC-CLIENT-BEDROCK, AC-LIFECYCLE-GEYSER, AC-PACK-PRODUCTION."
        )
    else:
        case["next_action"] = "Rerun matrix-final-partial probe on 2.3.239 jars."

counts = Counter(c["status"] for c in d["cases"])
d["summary"] = dict(counts)
d["overall"] = "INCOMPLETE"
d["updated_at"] = datetime.now(timezone.utc).isoformat()
d["unfinished_ids"] = [c["id"] for c in d["cases"] if c["status"] not in ("PASS", "EXCLUDED BY EXISTING SCOPE")]
ACCEPT.write_text(json.dumps(d, indent=2) + "\n", encoding="utf-8")
print("summary", d["summary"])
print("unfinished", d["unfinished_ids"])
print("jar", sha256(JAR), JAR.name)
if SOURCES.exists():
    print("sources", sha256(SOURCES), SOURCES.name)
print("lib", sha256(LIB), LIB.name)
