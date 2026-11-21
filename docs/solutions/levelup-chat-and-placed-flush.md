# Level-up chat styles and placed-block flush ordering

## Level-up chat
Problem: `FabricTextAdapter` fell back to `Component.literal` of a legacy string when Adventure JSON did not decode. Players saw plain text or raw formatting codes.
Fix: walk the Adventure tree and copy color, bold, italic, underline, strikethrough, and obfuscated onto the native component.
Verify: `levelup-chat-probe` on GraniteSkills 2.3.242 (`6E5234A0…`). Farming XP crosses level 1→2. Chat contains the skill name, levels, and stat symbols, with color/bold/strikethrough and no `<`/`§` text.

## Placed blocks
Problem: `flush()` wrote the file on the caller thread while an older snapshot was still queued on the IO executor, so the older write replaced the newer file.
Fix: every snapshot, including the final flush and shutdown, is submitted to the single worker and copied from the live set when that task runs. Shutdown stops accepting new submits.
Verify: `FabricPlacedBlocksTest.finalFlushWaitsForOlderWriteAndKeepsNewerRecords`.
