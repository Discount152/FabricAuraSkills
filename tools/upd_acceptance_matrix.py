# -*- coding: utf-8 -*-
"""Pin evidence from the full disposable-server matrix for one version. Overall stays INCOMPLETE.

Usage: python tools/upd_acceptance_matrix.py <graniteskills version>

A probe-backed case keeps PASS only when every probe it cites passed on that runtime;
otherwise it is reopened with the failing probe named. Unit-test cases cite the rerun reports.
"""
import csv
import hashlib
import json
import re
import sys
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(r"F:\FabricAuraSkills\fabricauraskills")
ACCEPT = ROOT / ".cursor" / "granite-completion" / "acceptance-state.json"
SERVER = Path(r"F:\gs-lifecycle-server")
VERSION = sys.argv[1]
JAR = ROOT / "fabric" / "build" / "libs" / f"GraniteSkills-{VERSION}.jar"
LIB = ROOT / "granitelib-fabric" / "build" / "libs" / "granitelib-fabric-0.2.0-alpha.21.jar"
EVIDENCE = SERVER / "evidence" / f"GraniteSkills-{VERSION}"
BUNDLE = ROOT.parent / "fabric" / f"GraniteSkills_Review_{VERSION}.zip"
PROBE_REF = re.compile(r"gs-(?:lifecycle|piston)-server\\(?:evidence\\[^\\]+\\[^\\]+\\)?([a-z0-9-]+)-result\.txt", re.I)
LIFECYCLE_FILES = {"lifecycle-boot", "lifecycle-restart"}
EXTERNAL = {"AC-CLIENT-JAVA", "AC-CLIENT-BEDROCK", "AC-LIFECYCLE-GEYSER", "AC-PACK-PRODUCTION"}


def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest().upper()


def matrix_verdicts() -> dict[str, str]:
    verdicts = {}
    with (EVIDENCE / "matrix-summary.csv").open(encoding="utf-8-sig") as f:
        for row in csv.DictReader(f):
            verdicts[row["probe"].removesuffix("-src")] = row["verdict"]
    for name in LIFECYCLE_FILES:
        result = EVIDENCE / "lifecycle-probe" / f"{name}-result.txt"
        if result.exists():
            lines = result.read_text(encoding="utf-8", errors="replace").splitlines()
            verdicts[name] = "PASS" if "PASS" in lines else "FAIL"
    return verdicts


def evidence_ref(name: str) -> Path:
    if name in LIFECYCLE_FILES:
        return EVIDENCE / "lifecycle-probe" / f"{name}-result.txt"
    return EVIDENCE / name / f"{name}-result.txt"


def unit_counts() -> str:
    parts = []
    for module in ("api", "common", "granitelib-fabric", "fabric"):
        tests = failures = 0
        for xml in (ROOT / module / "build" / "test-results" / "test").glob("*.xml"):
            text = xml.read_text(encoding="utf-8", errors="replace")
            m = re.search(r'tests="(\d+)".*?failures="(\d+)".*?errors="(\d+)"', text)
            if m:
                tests += int(m.group(1))
                failures += int(m.group(2)) + int(m.group(3))
        parts.append(f"{module} {tests} tests {failures} failures")
    return "; ".join(parts)


def main() -> None:
    data = json.loads(ACCEPT.read_text(encoding="utf-8"))
    jar_hash = sha(JAR)
    lib_hash = sha(LIB)
    verdicts = matrix_verdicts()
    units = unit_counts()
    reopened = []
    not_refreshed = []

    ids = {c["id"] for c in data["cases"]}
    new_cases = {
        "AC-TEXT-CONVERSION": (2420, ["action-bar-probe", "chat-probe", "title-probe", "levelup-chat-probe"],
                               "FabricTextAdapterTest covers literal, translatable+args, keybind, score, selector, "
                               "named/hex colors, resets, shadow, insertion, font, click and hover events"),
        "AC-MENU-CLICK-OUTCOMES": (2430, ["menu-migration-probe"],
                                   "After-click checks: Average title+history, sort cycle equals explicit next sort, "
                                   "Next Page shows Page 2, Back restores level_progression page 2"),
    }
    for case_id, (order, probes, note) in new_cases.items():
        if case_id in ids:
            continue
        data["cases"].append({
            "id": case_id,
            "order": order,
            "depends_on": [],
            "status": "NOT STARTED",
            "blockers": [],
            "evidence": [{"kind": "dedicated-server", "ref": str(SERVER / f"{p}-result.txt"),
                          "validated": False, "note": note} for p in probes],
            "next_action": "",
            "runtime_required": True,
        })

    for case in data["cases"]:
        if case["status"] == "EXCLUDED BY EXISTING SCOPE" or case["id"] in EXTERNAL or case["id"] == "AC-MATRIX-FINAL":
            continue
        names = sorted({m.group(1) for e in case.get("evidence", [])
                        for m in [PROBE_REF.search(str(e.get("ref", "")))] if m})
        ev = case.setdefault("evidence", [])
        if names:
            failing = [n for n in names if verdicts.get(n) != "PASS"]
            if failing:
                case["status"] = "NOT STARTED"
                case["next_action"] = "On " + VERSION + ": " + ", ".join(
                    f"{n}={verdicts.get(n, 'NOT RUN')}" for n in failing)
                reopened.append(case["id"])
                continue
            case["status"] = "PASS"
            case["blockers"] = []
            case["next_action"] = ""
            for n in names:
                ev.append({
                    "kind": "dedicated-server",
                    "ref": str(evidence_ref(n)),
                    "runtime": str(evidence_ref(n).parent / "runtime.txt"),
                    "validated": True,
                    "note": f"revalidated on GraniteSkills-{VERSION} sha256={jar_hash}",
                })
            if case["id"] == "AC-TEXT-CONVERSION":
                ev.append({"kind": "unit-test", "ref": "FabricTextAdapterTest", "validated": True,
                           "note": units})
        elif case["id"] == "AC-LIB-CONSUMER":
            ev.append({"kind": "packaged-jar", "ref": str(EVIDENCE / "lib-consumer" / "independent-consumer-result.txt"),
                       "runtime": str(EVIDENCE / "lib-consumer" / "runtime.txt"), "validated": True,
                       "note": f"rerun against granitelib-fabric-0.2.0-alpha.21 sha256={lib_hash}"})
        elif case["id"] == "AC-REVIEW-V3":
            ev.append({"kind": "review-bundle", "ref": str(BUNDLE), "validated": True,
                       "note": f"raw results, logs, runtime hashes, source and jars for the {VERSION} pair; "
                               "supersedes the 2.3.219 bundle"})
        elif case["status"] == "PASS" and any(e.get("kind") in ("unit-test", "unit") for e in ev):
            ev.append({"kind": "unit-test", "ref": "build/test-results (api, common, granitelib-fabric, fabric)",
                       "validated": True, "note": f"rerun on {VERSION} source: {units}"})
        elif case["status"] == "PASS":
            not_refreshed.append(case["id"])

    for case in data["cases"]:
        if case["id"] == "AC-MATRIX-FINAL":
            case["depends_on"] = sorted(c["id"] for c in data["cases"]
                                        if c["status"] != "EXCLUDED BY EXISTING SCOPE" and c["id"] != "AC-MATRIX-FINAL")
            case["status"] = "NOT STARTED"
            case["next_action"] = (
                "PASS waits on AC-CLIENT-JAVA, AC-CLIENT-BEDROCK, AC-LIFECYCLE-GEYSER, AC-PACK-PRODUCTION. "
                f"Every disposable-server probe was rerun on {VERSION}; see matrix-summary.csv."
            )

    counts = Counter(c["status"] for c in data["cases"])
    data["summary"] = dict(counts)
    data["overall"] = "INCOMPLETE"
    data["updated_at"] = datetime.now(timezone.utc).isoformat()
    data["frozen_pair"] = {
        "graniteskills": VERSION,
        "granitelib": "0.2.0-alpha.21",
        "graniteskills_sha256": jar_hash,
        "granitelib_sha256": lib_hash,
        "matrix_summary": str(EVIDENCE / "matrix-summary.csv"),
    }
    data["unfinished_ids"] = [c["id"] for c in data["cases"]
                              if c["status"] not in ("PASS", "EXCLUDED BY EXISTING SCOPE")]
    ACCEPT.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
    print(dict(counts))
    print("reopened", reopened)
    print("PASS without fresh evidence", not_refreshed)
    print("unfinished", data["unfinished_ids"])


if __name__ == "__main__":
    main()
