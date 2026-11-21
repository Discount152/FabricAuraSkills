# -*- coding: utf-8 -*-
"""Assemble the review ZIP for one frozen GraniteSkills + GraniteLib pair.

Usage: python tools/make_review_bundle.py <graniteskills version> <granitelib version>
Writes F:/FabricAuraSkills/fabric/review-bundle-<version>/ and GraniteSkills_Review_<version>.zip.
"""
import hashlib
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SERVER = Path(r"F:\gs-lifecycle-server")
OUT_PARENT = ROOT.parent / "fabric"
MODULES = ("api", "common", "granitelib-fabric", "fabric")
SERVER_SCRIPTS = ("scripts/compile-probe.ps1", "scripts/run-probe.ps1", "scripts/run-matrix.ps1",
                  "scripts/run-lifecycle.ps1", "scripts/mysql-fail-hash.ps1", "scripts/check-review-v2.py",
                  "run_lifecycle.py")
REPO_TOOLS = ("tools/upd_acceptance_matrix.py", "tools/make_source_archive.py", "tools/make_review_bundle.py")


def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest().upper()


def copytree(src: Path, dst: Path) -> None:
    shutil.copytree(src, dst, ignore=shutil.ignore_patterns("classes"), dirs_exist_ok=True)


def main() -> None:
    version, lib_version = sys.argv[1], sys.argv[2]
    bundle = OUT_PARENT / f"review-bundle-{version}"
    if bundle.exists():
        shutil.rmtree(bundle)
    bundle.mkdir(parents=True)

    jars = bundle / "jars"
    jars.mkdir()
    for name in (f"GraniteSkills-{version}.jar", f"GraniteSkills-{version}-sources.jar"):
        shutil.copy2(ROOT / "fabric" / "build" / "libs" / name, jars)
    for name in (f"granitelib-fabric-{lib_version}.jar", f"granitelib-fabric-{lib_version}-sources.jar"):
        shutil.copy2(ROOT / "granitelib-fabric" / "build" / "libs" / name, jars)

    subprocess.run([sys.executable, str(ROOT / "tools" / "make_source_archive.py"), str(bundle / "provenance"),
                    version], check=True)

    for module in MODULES:
        build = ROOT / module / "build"
        copytree(build / "test-results" / "test", bundle / "test-reports" / module / "test-results")
        copytree(build / "reports" / "tests" / "test", bundle / "test-reports" / module / "html")

    evidence = SERVER / "evidence"
    copytree(evidence / f"GraniteSkills-{version}", bundle / "evidence" / f"GraniteSkills-{version}")
    for extra in evidence.glob("matrix-*-attempt*.csv"):
        shutil.copy2(extra, bundle / "evidence" / extra.name)
    for console in SERVER.glob(f"matrix-{version.rsplit('.', 1)[-1]}*-console.txt"):
        shutil.copy2(console, bundle / "evidence" / console.name)

    probes = bundle / "probes"
    for src in sorted(SERVER.glob("*-probe-src")) + [SERVER / "probe-src", SERVER / "matrix-harness-src"]:
        copytree(src, probes / src.name)

    scripts = bundle / "scripts"
    for rel in SERVER_SCRIPTS:
        (scripts / "server" / rel).parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(SERVER / rel, scripts / "server" / rel)
    for rel in REPO_TOOLS:
        (scripts / "repo" / rel).parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(ROOT / rel, scripts / "repo" / rel)

    state = bundle / "state"
    state.mkdir()
    shutil.copy2(ROOT / ".cursor" / "granite-completion" / "acceptance-state.json", state)
    shutil.copy2(ROOT / "fabric" / "FULL_COMPLETION_LEDGER.md", state)
    shutil.copytree(ROOT / "docs" / "solutions", bundle / "docs" / "solutions")

    (bundle / "REPRODUCE.md").write_text(f"""# Reproduce GraniteSkills {version} + GraniteLib {lib_version}

## Build from the bundled source
1. Unzip `provenance/GraniteSkills-{version}-buildable-source.zip` (or check out the commit in
   `provenance/BASE_COMMIT.txt` and apply `tracked-changes.patch` plus the files in `untracked-files.txt`).
2. Java 25: `gradlew -PfabricOnly=true :fabric:build :granitelib-fabric:jar -x test --no-daemon`
3. Compare `fabric/build/libs/GraniteSkills-{version}*.jar` and
   `granitelib-fabric/build/libs/granitelib-fabric-{lib_version}.jar` with `HASHES.txt` (`jars/`).

## Unit tests
`gradlew :api:test :common:test :granitelib-fabric:test :fabric:test` (reports in `test-reports/`).
The aggregate `gradlew test` hits a Gradle implicit-dependency error in `granitelib-consumer`; run per module.

## Disposable-server probes
Server: Fabric 26.3, loader 0.20.1, Fabric API 0.166.2+26.3, at `F:\\gs-lifecycle-server`; mods = the two jars in
`jars/` plus Fabric API (and LuckPerms). MySQL probes need MySQL 8.4 on 127.0.0.1:3307 (gs/gs, db graniteskills).
- Copy `probes/*` into the server root and `scripts/server/*` into it (keeping `scripts/`).
- Full matrix: `scripts\\run-matrix.ps1` (resets config from `_config-baseline` per probe, writes
  `evidence\\GraniteSkills-{version}\\matrix-summary.csv` and one folder per probe with `runtime.txt` jar hashes).
- One probe: `scripts\\run-matrix.ps1 -Only <name>-probe-src`.
- Two-boot lifecycle: `scripts\\run-lifecycle.ps1`.
- Acceptance update: `python tools/upd_acceptance_matrix.py {version}`.

## Not covered here (manual testing)
AC-CLIENT-JAVA, AC-CLIENT-BEDROCK, AC-LIFECYCLE-GEYSER, AC-PACK-PRODUCTION.
""", encoding="utf-8")

    lines = [f"{sha(p)}  {p.relative_to(bundle).as_posix()}" for p in sorted(bundle.rglob("*")) if p.is_file()]
    (bundle / "HASHES.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")

    archive = OUT_PARENT / f"GraniteSkills_Review_{version}.zip"
    with zipfile.ZipFile(archive, "w", zipfile.ZIP_DEFLATED, strict_timestamps=False) as z:
        for p in sorted(bundle.rglob("*")):
            if p.is_file():
                z.write(p, Path(bundle.name) / p.relative_to(bundle))
    print(f"{archive} sha256={sha(archive)} files={len(lines) + 1}")


if __name__ == "__main__":
    main()
