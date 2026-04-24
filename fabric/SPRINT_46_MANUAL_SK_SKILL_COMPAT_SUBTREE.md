# Sprint 46 — manual validation: `/sk` and `/skill` **compatibility** subtree

**Scope:** On Fabric, **`/sk`** and **`/skill`** now expose **`openmenu`**, **`xp`**, **`mana`**, and **`skilllevel`** as **sibling** subcommands (same **logic** and **permission** rules as the standalone **`/openmenu`**, **`/xp`**, **`/mana`**, **`/skilllevel`** roots; **Sprint 46** only adds **Brigadier** routing). **Java** and **Geyser** players use the same server-side commands.

**Environment:** 2+ online players; a mix of **default** and **op** (or LP) to exercise **view** vs **mutate** perms for **xp** / **mana** / **skilllevel**. Menus and families must be **unchanged in behavior** vs **pre–Sprint-46** when using the **standalone** roots.

## 1) Regression: standalone roots

1. Run **`/xp view <A> <skill>`** (read-only) — then **`/sk xp view <A> <skill>`** — expect **the same** permission checks and line output.
2. Run **`/mana`** (self) and **`/sk mana`** (self) — same display path.
3. Run **`/skilllevel view <A> <skill>`** and **`/sk skilllevel view <A> <skill>`** — same.
4. Run **`/openmenu skills`** and **`/sk openmenu skills`** — same (and **`/skill openmenu skills`**).

## 2) “Reference shape” on `/sk` / `/skill`

5. As a **player** with perms, **`/sk xp add …`**, **`/sk mana add …`**, **`/sk skilllevel add …`** — match **`/xp`**, **`/mana`**, **`/skilllevel`** (same **errors** and **mutations**).
6. Repeat with **`/skill …`** instead of **`/sk …`**.
7. **Permission denial:** with **`auraskills.command.openmenu`** removed, **`/sk openmenu skills`** should fail as before; with **`auraskills.command.xp.add`** at **deny**, **`/sk xp add …`** should **not** succeed.

## 3) Intentional non-parity (documented)

8. Bukkit/ACF often uses **`/… skill <subcommand>`** for **SkillCommand**; on Granite Fabric, **`/sk skill` is *not*** added as a **subcommand of `/sk`**, so **`/sk skill setlevel …`** is **not** a sprint target. The compatibility word for the **skilllevel** family is **`/sk skilllevel …`** (same as standalone). Expect **unknown** if you type **`/sk skill …`** in vanilla Brigadier (unless another mod added it).

## 4) Bedrock / Geyser

9. In chat, use **`/sk xp view <player> <skill>`** (no leading slash in some UIs) — should execute like Java if the string reaches the server.

## Checklist

- [ ] Standalone **xp**, **mana**, **skilllevel**, **openmenu** still work and match prior behavior.
- [ ] **/sk|skill** + same subcommands do **not** grant extra capabilities (no permission widening).
- [ ] **No** duplicate **feedback** or **double-apply** bugs when comparing alias vs top-level for the same action.
