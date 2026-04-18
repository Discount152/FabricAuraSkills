# Local Fabric development and testing

This guide is for **Fabric AuraSkills** in this repo (`FabricAuraSkills`). It uses **Fabric Loom** built into the `:fabric` Gradle project so you can run a **local dedicated server** and a **local game client** without copying jars to a real host.

**Open this folder as the Cursor / VS Code workspace root** (`FabricAuraSkills`) so paths and tasks match.

---

## What you are running

| Item | Value (see `fabric/build.gradle.kts`) |
|------|----------------------------------------|
| Minecraft | `1.21.11` |
| Yarn mappings | `1.21.11+build.4:v2` |
| Fabric Loader | `0.18.4` |
| Fabric API | `0.141.3+1.21.11` |
| Java | **21** (Gradle toolchain) |
| Loom | `1.13.6` |

Use **`-PfabricOnly=true`** so Gradle only loads `api`, `common`, `slate-fabric`, and `fabric` (skips Bukkit and other subprojects). All commands below include it.

---

## One-time: Git submodule (Slate reference)

The **Bukkit Slate** source is a submodule at **`vendor/Slate`**. If you cloned without `--recursive`, run once from the repo root:

```bat
git submodule update --init --recursive
```

See **`fabric/MENU_AND_COMMANDS_PORT.md`** for the Fabric Slate port plan.

---

## One-time: Mojang EULA (local server)

The first time you start the dev server, Loom creates `fabric/run/`. You must agree to the [Minecraft EULA](https://aka.ms/MinecraftEULA) for **your own machine**:

1. Start the server once (it may exit immediately).
2. Open `fabric/run/eula.txt`.
3. Set `eula=true` and save.

Without this, the server will stop with a message about the EULA.

---

## Start the local dedicated test server

**Option A — Command line (PowerShell or CMD), from repo root:**

```bat
.\gradlew.bat -PfabricOnly=true :fabric:runServer
```

**Option B — Helper script:**

```bat
scripts\fabric-dev-server.bat
```

**Option C — Cursor / VS Code:** `Terminal → Run Task…` → **Fabric: run local dedicated server**.

The mod is on the runtime classpath automatically; you do **not** need to copy `FabricAuraSkills-*.jar` into `mods` for this workflow.

Stop the server: type `stop` in the server console, or close the terminal (not as clean).

---

## Start the local game client

**From repo root:**

```bat
.\gradlew.bat -PfabricOnly=true :fabric:runClient
```

Or: `scripts\fabric-dev-client.bat`, or the task **Fabric: run local game client**.

The dev client uses the same Loom `run` folder layout under `fabric/run/` (separate from production installs).

---

## Connect the client to your local server

1. Start **Fabric: run local dedicated server** and wait until you see **Done** in the log (after EULA is OK).
2. Default **port** is **25565** (see `fabric/run/server.properties` if you change it).
3. Start **Fabric: run local game client**.
4. In Minecraft: **Multiplayer → Direct Connection →** `localhost` (or `127.0.0.1`).

If the server uses a different port, use `localhost:<port>`.

---

## Rebuild the mod after code changes

From repo root:

```bat
.\gradlew.bat -PfabricOnly=true :fabric:build
```

Output jar:

`fabric\build\libs\FabricAuraSkills-<version>.jar`

(`<version>` comes from `gradle.properties` → `projectVersion`.)

Or: **Fabric: build mod JAR** task, or `scripts\fabric-build.bat`.

### Keeping every built JAR (all iterations)

Each successful **`:fabric:build`** finishes by running **`:fabric:archiveFabricModJar`**, which copies the remapped mod to:

**`fabric-mod-jar-history/FabricAuraSkills-<version>-<timestamp>.jar`**

Those files are **not** removed when you bump `projectVersion` or run another build. **`fabric-mod-jar-history/`** is the long-term archive; it is listed in `.gitignore` so jars stay local unless you commit them on purpose.

**`:fabric:clean`** still deletes **`fabric/build/`** (Gradle’s normal behavior), but **before** that happens Gradle copies any existing **`fabric/build/libs/FabricAuraSkills-*.jar`** into **`fabric-mod-jar-history/`** as **`…-preclean-<timestamp>-<n>.jar`**, so you keep the previous remapped JAR for documentation / diff / rollback. Prefer **`:fabric:build`** without **`clean`** when you only need a fresh compile.

---

## Fastest edit → test loop

1. Change Java/resources under `fabric/src` or `common/src` (Fabric pulls common resources into the fabric jar).
2. Run `.\gradlew.bat -PfabricOnly=true :fabric:build` if you need a jar artifact; for **Loom runServer/runClient**, you often only need to **restart** the run task so Gradle recompiles (stop server/client, run the same task again).
3. For many mixin or static-init issues, a **full restart** of the server (not `/reload`) is safer.

Tip: keep **two terminals** — one for `runServer`, one for `runClient`.

---

## Where files go (`fabric/run/`)

All of this is **local** and listed in `.gitignore` as `fabric/run/` so worlds and logs are not committed.

| Path | Purpose |
|------|---------|
| `fabric/run/world/` | Default overworld save (name may match `level-name` in `server.properties`) |
| `fabric/run/logs/latest.log` | Main server log |
| `fabric/run/logs/debug.log` | Extra debug log |
| `fabric/run/server.properties` | Port, motd, `level-name`, etc. |
| `fabric/run/eula.txt` | EULA flag |
| `fabric/run/config/` | Mod and Fabric config files created at runtime |

**Reset the test world:** stop the server, delete the `world` folder (or the folder named in `level-name`), then start `runServer` again. You can delete all of `fabric/run/` for a full reset (you will need to set `eula=true` again).

---

## Logs and crashes

- **Server console:** output in the terminal where `runServer` runs.
- **File:** `fabric/run/logs/latest.log`.
- **Client:** launcher output in the `runClient` terminal; game logs are under your OS user data for Minecraft dev runs (Loom uses a dev game dir under `fabric/run` for server; client layout is similar under `fabric/run` for singleplayer assets — if in doubt, watch the Gradle task output for “Game directory” paths).

---

## Unit tests vs manual testing

| Kind | Command | What it is good for |
|------|---------|---------------------|
| **`:fabric` unit tests** | `.\gradlew.bat -PfabricOnly=true :fabric:test` | Pure Java / API checks **without** starting Minecraft (example: `FabricModuleSanityTest`). |
| **`:common` tests** | `.\gradlew.bat :common:test` (full multi-project) or include in CI | Config, parsing, shared logic. |
| **`runServer` / `runClient`** | Gradle tasks above | Mixins, registration, gameplay, permissions — **needs** a real game. |

**GameTests** (structure tests in a generated world) are not set up here: they need extra Fabric Game Test API wiring and CI time. For this mod, **manual server + client** is the practical default.

---

## Automated JiJ audit (release bundle)

After **`remapJar`**, Gradle runs **`verifyFabricReleaseBundle`**: it opens the remapped **`FabricAuraSkills-*.jar`** and checks **`META-INF/jars/`** for:

- **`api-*.jar`** and **`common-*.jar`** (must be embedded for one-jar installs)
- **`configurate-core-4.1.2.jar`** and **`configurate-yaml-4.1.2.jar`**
- **Polyglot** present
- **No** `configurate-*-4.2*.jar` (avoids the server crash when another mod ships Configurate 4.1 core)

It runs automatically as part of **`:fabric:build`**. To run only the audit (still builds the remapped jar first):

```bat
.\gradlew.bat -PfabricOnly=true :fabric:verifyFabricReleaseBundle
```

Cursor: **Tasks → Fabric: verify JiJ release bundle**.

---

## Integration test (modpack parity) on your PC

`runServer` alone only loads **Fabric API + your dev mod**. To approximate **the same mod list as your Apex Hosting Minecraft Java Fabric server** (or any production Fabric server) and catch **classpath / mixin / ordering** issues early:

1. Stop any local `runServer`.
2. Create **`fabric/run/mods/`** if it does not exist.
3. Copy your production **`mods/*.jar`** into **`fabric/run/mods/`**, **except** do **not** put a second copy of **FabricAuraSkills** there — Loom already puts **your workspace mod** on the classpath. Two copies can confuse debugging.
4. Optional: use a **fresh world** — delete **`fabric/run/world`** (or your `level-name` folder) so a bad test does not reuse broken state.
5. Run **`.\gradlew.bat -PfabricOnly=true :fabric:runServer`** and watch **`fabric/run/logs/latest.log`** for `Done` and any `ERROR` lines.

**Manual smoke checklist** (5–10 minutes after boot): join with `runClient`, run **`/auraskills`** (or tab-complete), gain XP in one skill, try **one** anvil operation, **one** brew takeout, **one** fish — enough to prove the main hooks still fire with the pack loaded.

---

## Troubleshooting

| Problem | What to try |
|---------|-------------|
| “You need to agree to the EULA” | Edit `fabric/run/eula.txt` → `eula=true`. |
| Port already in use | Change `server-port` in `fabric/run/server.properties` or stop the other process using 25565. |
| Client cannot connect | Confirm server shows **Done**; try `127.0.0.1:25565`; check firewall. |
| Gradle tries to configure Bukkit / fails on unrelated projects | Always use **`-PfabricOnly=true`** for Fabric-only work. |
| Old mod code still running | Stop server completely; try `.\gradlew.bat -PfabricOnly=true :fabric:build` then `runServer` again. Only use **`clean`** if you truly need a wiped `fabric/build/` — any jars in `build/libs` are copied to **`fabric-mod-jar-history/*-preclean-*`** first. |
| “run configuration null” in task list | Harmless Loom/Gradle label quirk; `runServer` / `runClient` still work. |

---

## Optional: generate IDE run configs (IntelliJ)

If you use IntelliJ IDEA:

```bat
.\gradlew.bat -PfabricOnly=true :fabric:genSources
```

Use IDEA’s Gradle tool window to run **runClient** / **runServer** from the **fabric** project. Cursor users rely on **tasks** or the scripts above.

---

## Summary commands

```bat
REM Build jar
.\gradlew.bat -PfabricOnly=true :fabric:build

REM Local dedicated server (mod loaded automatically)
.\gradlew.bat -PfabricOnly=true :fabric:runServer

REM Local client
.\gradlew.bat -PfabricOnly=true :fabric:runClient

REM Quick JVM tests (no Minecraft)
.\gradlew.bat -PfabricOnly=true :fabric:test

REM JiJ / embedded-library audit (runs automatically on build; optional standalone)
.\gradlew.bat -PfabricOnly=true :fabric:verifyFabricReleaseBundle
```

Everything above stays **on your PC**; nothing here deploys to your personal server.
