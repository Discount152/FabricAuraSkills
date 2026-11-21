# -*- coding: utf-8 -*-
"""Write the buildable source archive and provenance files for a review bundle.

Usage: python tools/make_source_archive.py <out-dir> <version>
Archive = tracked files present on disk + untracked files git does not ignore.
"""
import subprocess
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
EXCLUDE_PREFIXES = ("fabric/logs/", "graphify-out/")


def git(*args: str) -> str:
    return subprocess.run(["git", *args], cwd=ROOT, check=True, capture_output=True, text=True,
                          encoding="utf-8").stdout


def main() -> None:
    out = Path(sys.argv[1])
    version = sys.argv[2]
    out.mkdir(parents=True, exist_ok=True)
    files = sorted({f for f in git("ls-files", "--cached", "--others", "--exclude-standard", "-z").split("\0")
                    if f and not f.startswith(EXCLUDE_PREFIXES) and (ROOT / f).is_file()})
    untracked = sorted(f for f in git("ls-files", "--others", "--exclude-standard", "-z").split("\0")
                       if f and not f.startswith(EXCLUDE_PREFIXES))
    archive = out / f"GraniteSkills-{version}-buildable-source.zip"
    with zipfile.ZipFile(archive, "w", zipfile.ZIP_DEFLATED) as z:
        for f in files:
            info = zipfile.ZipInfo(f, date_time=(2026, 1, 1, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = 0o644 << 16
            z.writestr(info, (ROOT / f).read_bytes())
    (out / "BASE_COMMIT.txt").write_text(
        f"commit: {git('rev-parse', 'HEAD').strip()}\n"
        f"branch: {git('rev-parse', '--abbrev-ref', 'HEAD').strip()}\n"
        f"remote: {git('remote', 'get-url', 'origin').strip() if git('remote').strip() else '(none)'}\n",
        encoding="utf-8")
    (out / "tracked-changes.patch").write_text(git("diff", "--binary", "HEAD"), encoding="utf-8")
    (out / "git-status.txt").write_text(git("status", "--porcelain=v1"), encoding="utf-8")
    (out / "untracked-files.txt").write_text("\n".join(untracked) + "\n", encoding="utf-8")
    print(f"{archive} files={len(files)} untracked={len(untracked)}")


if __name__ == "__main__":
    main()
