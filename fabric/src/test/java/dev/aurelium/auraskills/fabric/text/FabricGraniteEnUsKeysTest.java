package dev.aurelium.auraskills.fabric.text;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class FabricGraniteEnUsKeysTest {

    @Test
    void sprint83GraniteCommandAndMenuKeysPresentInEnUs() throws Exception {
        String json;
        try (InputStream in =
                FabricGraniteEnUsKeysTest.class.getResourceAsStream(
                        "/assets/auraskills/lang/en_us.json")) {
            assertTrue(in != null, "classpath /assets/auraskills/lang/en_us.json");
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (String key :
                new String[] {
                    "auraskills.granite.command.auraskills_usage",
                    "auraskills.granite.command.help",
                    "auraskills.granite.menu.skills_title",
                    "auraskills.granite.menu.stats_title",
                    "auraskills.granite.menu.skills_opened",
                    "auraskills.granite.menu.stats_opened",
                    "auraskills.granite.command.xp.view_line",
                    "auraskills.granite.command.skilllevel.view_line",
                    "auraskills.granite.command.item.usage_root",
                    "auraskills.granite.command.storage.usage_root",
                    "auraskills.granite.command.backup.usage_root",
                    "auraskills.granite.command.preset.usage_root"
                }) {
            assertTrue(json.contains("\"" + key + "\""), "missing " + key);
        }
    }

    @Test
    void sprint88BuiltInMenuAndSortKeysPresentInEnUs() throws Exception {
        String json;
        try (InputStream in =
                FabricGraniteEnUsKeysTest.class.getResourceAsStream(
                        "/assets/auraskills/lang/en_us.json")) {
            assertTrue(in != null, "classpath /assets/auraskills/lang/en_us.json");
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (String key :
                new String[] {
                    "auraskills.granite.menu.skills_lore_level",
                    "auraskills.granite.menu.skills_lore_xp",
                    "auraskills.granite.menu.skills_lore_open_detail",
                    "auraskills.granite.menu.skills_lore_no_permission",
                    "auraskills.granite.menu.built_in_disabled",
                    "auraskills.granite.menu.user_not_loaded",
                    "auraskills.granite.menu.level_progression_opened",
                    "auraskills.granite.menu.level_progression_open_sources",
                    "auraskills.granite.menu.level_progression_open_sources_lore",
                    "auraskills.granite.menu.sources_opened",
                    "auraskills.granite.menu.stat_info_opened",
                    "auraskills.granite.menu.stat_info_title",
                    "auraskills.granite.menu.stat_info_main_title",
                    "auraskills.granite.menu.stat_info_lore_value",
                    "auraskills.granite.menu.stat_info_back",
                    "auraskills.granite.menu.stat_info_back_lore",
                    "auraskills.granite.menu.level_progression_title",
                    "auraskills.granite.menu.level_progression_main_title",
                    "auraskills.granite.menu.level_progression_lore_level",
                    "auraskills.granite.menu.level_progression_lore_xp",
                    "auraskills.granite.menu.level_progression_lore_to_next",
                    "auraskills.granite.menu.level_progression_lore_maxed",
                    "auraskills.granite.menu.level_progression_back",
                    "auraskills.granite.menu.level_progression_back_lore",
                    "auraskills.granite.menu.level_progression_status_unlocked",
                    "auraskills.granite.menu.level_progression_status_current",
                    "auraskills.granite.menu.level_progression_status_locked",
                    "auraskills.granite.menu.level_progression_row_xp_required",
                    "auraskills.granite.menu.level_progression_row_progress",
                    "auraskills.granite.menu.level_progression_row_maxed_compact",
                    "auraskills.granite.menu.list_paging_subtitle",
                    "auraskills.granite.menu.level_progression_levels_suffix",
                    "auraskills.granite.menu.stat_info_title_prefix",
                    "auraskills.granite.menu.sources_title_secondary",
                    "auraskills.granite.menu.abilities_title_suffix",
                    "auraskills.granite.menu.unclaimed_items_title",
                    "auraskills.granite.menu.paging_prev",
                    "auraskills.granite.menu.paging_prev_lore",
                    "auraskills.granite.menu.paging_next",
                    "auraskills.granite.menu.paging_next_lore",
                    "auraskills.granite.menu.sources_title",
                    "auraskills.granite.menu.sources_back",
                    "auraskills.granite.menu.sources_back_lore",
                    "auraskills.granite.menu.sources_sort",
                    "auraskills.granite.menu.sources_sort_lore",
                    "auraskills.granite.menu.sources_lore_xp",
                    "auraskills.granite.menu.sources_lore_xp_per",
                    "auraskills.granite.menu.sources_lore_id",
                    "auraskills.granite.menu.sources_lore_type",
                    "auraskills.granite.menu.unclaimed_opened",
                    "auraskills.granite.menu.unclaimed_claim_all",
                    "auraskills.granite.menu.unclaimed_claim_all_lore",
                    "auraskills.granite.menu.unclaimed_unknown",
                    "auraskills.granite.menu.leaderboard_title_power",
                    "auraskills.granite.menu.leaderboard_title_average",
                    "auraskills.granite.menu.leaderboard_title_skill",
                    "auraskills.granite.menu.leaderboard_entry_name",
                    "auraskills.granite.menu.leaderboard_entry_value",
                    "auraskills.granite.menu.leaderboard_back",
                    "auraskills.granite.menu.leaderboard_back_lore",
                    "auraskills.granite.menu.leaderboard_refresh",
                    "auraskills.granite.menu.leaderboard_refresh_lore",
                    "auraskills.granite.menu.leaderboard_opened",
                    "auraskills.granite.menu.abilities_title",
                    "auraskills.granite.menu.abilities_back",
                    "auraskills.granite.menu.abilities_back_lore",
                    "auraskills.granite.menu.abilities_state_locked",
                    "auraskills.granite.menu.abilities_state_level",
                    "auraskills.granite.menu.abilities_mana_ability",
                    "auraskills.granite.menu.abilities_unlocks_at",
                    "auraskills.granite.menu.abilities_maxed",
                    "auraskills.granite.menu.abilities_lore_truncated",
                    "auraskills.granite.menu.abilities_opened",
                    "auraskills.granite.sort.ascending",
                    "auraskills.granite.sort.descending",
                    "auraskills.granite.sort.alphabetical",
                    "auraskills.granite.sort.reverse_alphabetical",
                    "auraskills.granite.command.sources.empty",
                    "auraskills.granite.command.sources.menu_fallback",
                    "auraskills.granite.command.claimitems.all_claimed",
                    "auraskills.granite.command.claimitems.partial",
                    "auraskills.granite.command.openmenu.console_specify",
                    "auraskills.granite.command.openmenu.context.console_specify",
                    "auraskills.granite.command.openmenu.unknown_skill",
                    "auraskills.granite.command.openmenu.unknown_stat",
                    "auraskills.granite.command.openmenu.unclaimed_console",
                    "auraskills.granite.command.openmenu.opened_for",
                    "auraskills.granite.command.openmenu.opened_for_context",
                    "auraskills.granite.command.openmenu.abilities_console",
                    "auraskills.granite.command.openmenu.abilities_no_enabled_skill",
                    "auraskills.granite.command.openmenu.abilities_bad_token",
                    "auraskills.granite.command.openmenu.leaderboard_bad_token",
                    "auraskills.granite.command.openmenu.leaderboard_console",
                    "auraskills.granite.command.sources.bad_sort",
                    "auraskills.granite.command.skill_root.not_enabled",
                    "auraskills.granite.command.xp.player_not_found"
                }) {
            assertTrue(json.contains("\"" + key + "\""), "missing " + key);
        }
    }

    @Test
    void sprint89SourcesPagedChatKeysPresentInEnUs() throws Exception {
        String json;
        try (InputStream in =
                FabricGraniteEnUsKeysTest.class.getResourceAsStream(
                        "/assets/auraskills/lang/en_us.json")) {
            assertTrue(in != null, "classpath /assets/auraskills/lang/en_us.json");
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (String key :
                new String[] {
                    "auraskills.granite.command.sources.header",
                    "auraskills.granite.command.sources.continuation",
                    "auraskills.granite.command.sources.line"
                }) {
            assertTrue(json.contains("\"" + key + "\""), "missing " + key);
        }
    }

    @Test
    void sprint90GlobalCommandGraniteKeysPresentInEnUs() throws Exception {
        String json;
        try (InputStream in =
                FabricGraniteEnUsKeysTest.class.getResourceAsStream(
                        "/assets/auraskills/lang/en_us.json")) {
            assertTrue(in != null, "classpath /assets/auraskills/lang/en_us.json");
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (String key :
                new String[] {
                    "auraskills.granite.command.profile.player_not_found",
                    "auraskills.granite.command.profile.no_userdata_file",
                    "auraskills.granite.command.profile.source_not_loaded",
                    "auraskills.granite.command.profile.load_failed",
                    "auraskills.granite.command.profile.userstate_disclaimer",
                    "auraskills.granite.command.profile.mana_line",
                    "auraskills.granite.command.profile.stat_mods_header",
                    "auraskills.granite.command.profile.trait_mods_header",
                    "auraskills.granite.command.profile.modifier_none",
                    "auraskills.granite.command.confirmation.no_pending",
                    "auraskills.granite.command.confirmation.mismatch",
                    "auraskills.granite.command.templates.overwrite.confirm_console_only",
                    "auraskills.granite.command.templates.overwrite.preview_arm_from_console",
                    "auraskills.granite.command.xp.amount_positive",
                    "auraskills.granite.command.xp.amount_non_negative",
                    "auraskills.granite.command.level.count_positive",
                    "auraskills.granite.command.storage.info_header",
                    "auraskills.granite.command.resethealth.console_only",
                    "auraskills.granite.command.item_armor_mod.players_only",
                    "auraskills.granite.command.item_armor_mod.empty_hand"
                }) {
            assertTrue(json.contains("\"" + key + "\""), "missing " + key);
        }
    }

    @Test
    void sprint120MenuPresentationRepairKeysPresentInEnUs() throws Exception {
        String json;
        try (InputStream in =
                FabricGraniteEnUsKeysTest.class.getResourceAsStream(
                        "/assets/auraskills/lang/en_us.json")) {
            assertTrue(in != null, "classpath /assets/auraskills/lang/en_us.json");
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (String key :
                new String[] {
                    "auraskills.granite.menu.stat_info_title_suffix",
                    "auraskills.granite.menu.sources_title_suffix",
                    "auraskills.granite.menu.sources_sort_current",
                    "auraskills.granite.menu.sources_placeholder_name",
                    "auraskills.granite.source_type.unknown",
                    "auraskills.granite.source_type.other",
                    "auraskills.granite.source_type.block",
                    "auraskills.granite.source_type.mob",
                    "auraskills.granite.source_type.fishing",
                    "auraskills.granite.source_type.farming",
                    "auraskills.granite.source_type.damage",
                }) {
            assertTrue(json.contains("\"" + key + "\""), "missing " + key);
        }
    }
}
