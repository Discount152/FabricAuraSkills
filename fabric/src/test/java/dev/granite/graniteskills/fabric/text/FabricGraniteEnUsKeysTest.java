package dev.granite.graniteskills.fabric.text;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricGraniteEnUsKeysTest {

    @Test
    void sprint83GraniteCommandAndMenuKeysPresentInEnUs() throws Exception {
        String json;
        try (InputStream in =
                FabricGraniteEnUsKeysTest.class.getResourceAsStream(
                        "/assets/graniteskills/lang/en_us.json")) {
            assertTrue(in != null, "classpath /assets/graniteskills/lang/en_us.json");
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (String key :
                new String[] {
                        "graniteskills.granite.command.graniteskills_usage",
                        "graniteskills.granite.command.help",
                        "graniteskills.granite.menu.skills_title",
                        "graniteskills.granite.menu.stats_title",
                        "graniteskills.granite.menu.skills_opened",
                        "graniteskills.granite.menu.stats_opened",
                        "graniteskills.granite.command.xp.view_line",
                        "graniteskills.granite.command.skilllevel.view_line",
                        "graniteskills.granite.command.item.usage_root",
                        "graniteskills.granite.command.storage.usage_root",
                        "graniteskills.granite.command.backup.usage_root",
                        "graniteskills.granite.command.preset.usage_root"
                }) {
            assertTrue(json.contains("\"" + key + "\""), "missing " + key);
        }
    }

    @Test
    void sprint88BuiltInMenuAndSortKeysPresentInEnUs() throws Exception {
        String json;
        try (InputStream in =
                FabricGraniteEnUsKeysTest.class.getResourceAsStream(
                        "/assets/graniteskills/lang/en_us.json")) {
            assertTrue(in != null, "classpath /assets/graniteskills/lang/en_us.json");
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (String key :
                new String[] {
                        "graniteskills.granite.menu.skills_lore_level",
                        "graniteskills.granite.menu.skills_lore_xp",
                        "graniteskills.granite.menu.skills_lore_open_detail",
                        "graniteskills.granite.menu.skills_lore_no_permission",
                        "graniteskills.granite.menu.built_in_disabled",
                        "graniteskills.granite.menu.user_not_loaded",
                        "graniteskills.granite.menu.level_progression_opened",
                        "graniteskills.granite.menu.level_progression_open_sources",
                        "graniteskills.granite.menu.level_progression_open_sources_lore",
                        "graniteskills.granite.menu.sources_opened",
                        "graniteskills.granite.menu.stat_info_opened",
                        "graniteskills.granite.menu.stat_info_title",
                        "graniteskills.granite.menu.stat_info_main_title",
                        "graniteskills.granite.menu.stat_info_lore_value",
                        "graniteskills.granite.menu.stat_info_back",
                        "graniteskills.granite.menu.stat_info_back_lore",
                        "graniteskills.granite.menu.level_progression_title",
                        "graniteskills.granite.menu.level_progression_main_title",
                        "graniteskills.granite.menu.level_progression_lore_level",
                        "graniteskills.granite.menu.level_progression_lore_xp",
                        "graniteskills.granite.menu.level_progression_lore_to_next",
                        "graniteskills.granite.menu.level_progression_lore_maxed",
                        "graniteskills.granite.menu.level_progression_back",
                        "graniteskills.granite.menu.level_progression_back_lore",
                        "graniteskills.granite.menu.level_progression_status_unlocked",
                        "graniteskills.granite.menu.level_progression_status_current",
                        "graniteskills.granite.menu.level_progression_status_locked",
                        "graniteskills.granite.menu.level_progression_row_xp_required",
                        "graniteskills.granite.menu.level_progression_row_progress",
                        "graniteskills.granite.menu.level_progression_row_maxed_compact",
                        "graniteskills.granite.menu.list_paging_subtitle",
                        "graniteskills.granite.menu.level_progression_levels_suffix",
                        "graniteskills.granite.menu.stat_info_title_prefix",
                        "graniteskills.granite.menu.sources_title_secondary",
                        "graniteskills.granite.menu.abilities_title_suffix",
                        "graniteskills.granite.menu.unclaimed_items_title",
                        "graniteskills.granite.menu.paging_prev",
                        "graniteskills.granite.menu.paging_prev_lore",
                        "graniteskills.granite.menu.paging_next",
                        "graniteskills.granite.menu.paging_next_lore",
                        "graniteskills.granite.menu.sources_title",
                        "graniteskills.granite.menu.sources_back",
                        "graniteskills.granite.menu.sources_back_lore",
                        "graniteskills.granite.menu.sources_sort",
                        "graniteskills.granite.menu.sources_sort_lore",
                        "graniteskills.granite.menu.sources_lore_xp",
                        "graniteskills.granite.menu.sources_lore_xp_per",
                        "graniteskills.granite.menu.sources_lore_id",
                        "graniteskills.granite.menu.sources_lore_type",
                        "graniteskills.granite.menu.unclaimed_opened",
                        "graniteskills.granite.menu.unclaimed_claim_all",
                        "graniteskills.granite.menu.unclaimed_claim_all_lore",
                        "graniteskills.granite.menu.unclaimed_unknown",
                        "graniteskills.granite.menu.leaderboard_title_power",
                        "graniteskills.granite.menu.leaderboard_title_average",
                        "graniteskills.granite.menu.leaderboard_title_skill",
                        "graniteskills.granite.menu.leaderboard_entry_name",
                        "graniteskills.granite.menu.leaderboard_entry_value",
                        "graniteskills.granite.menu.leaderboard_back",
                        "graniteskills.granite.menu.leaderboard_back_lore",
                        "graniteskills.granite.menu.leaderboard_refresh",
                        "graniteskills.granite.menu.leaderboard_refresh_lore",
                        "graniteskills.granite.menu.leaderboard_opened",
                        "graniteskills.granite.menu.abilities_title",
                        "graniteskills.granite.menu.abilities_back",
                        "graniteskills.granite.menu.abilities_back_lore",
                        "graniteskills.granite.menu.abilities_state_locked",
                        "graniteskills.granite.menu.abilities_state_level",
                        "graniteskills.granite.menu.abilities_mana_ability",
                        "graniteskills.granite.menu.abilities_unlocks_at",
                        "graniteskills.granite.menu.abilities_maxed",
                        "graniteskills.granite.menu.abilities_lore_truncated",
                        "graniteskills.granite.menu.abilities_opened",
                        "graniteskills.granite.sort.ascending",
                        "graniteskills.granite.sort.descending",
                        "graniteskills.granite.sort.alphabetical",
                        "graniteskills.granite.sort.reverse_alphabetical",
                        "graniteskills.granite.command.sources.empty",
                        "graniteskills.granite.command.sources.menu_fallback",
                        "graniteskills.granite.command.claimitems.all_claimed",
                        "graniteskills.granite.command.claimitems.partial",
                        "graniteskills.granite.command.openmenu.console_specify",
                        "graniteskills.granite.command.openmenu.context.console_specify",
                        "graniteskills.granite.command.openmenu.unknown_skill",
                        "graniteskills.granite.command.openmenu.unknown_stat",
                        "graniteskills.granite.command.openmenu.unclaimed_console",
                        "graniteskills.granite.command.openmenu.opened_for",
                        "graniteskills.granite.command.openmenu.opened_for_context",
                        "graniteskills.granite.command.openmenu.abilities_console",
                        "graniteskills.granite.command.openmenu.abilities_no_enabled_skill",
                        "graniteskills.granite.command.openmenu.abilities_bad_token",
                        "graniteskills.granite.command.openmenu.leaderboard_bad_token",
                        "graniteskills.granite.command.openmenu.leaderboard_console",
                        "graniteskills.granite.command.sources.bad_sort",
                        "graniteskills.granite.command.skill_root.not_enabled",
                        "graniteskills.granite.command.xp.player_not_found"
                }) {
            assertTrue(json.contains("\"" + key + "\""), "missing " + key);
        }
    }

    @Test
    void sprint89SourcesPagedChatKeysPresentInEnUs() throws Exception {
        String json;
        try (InputStream in =
                FabricGraniteEnUsKeysTest.class.getResourceAsStream(
                        "/assets/graniteskills/lang/en_us.json")) {
            assertTrue(in != null, "classpath /assets/graniteskills/lang/en_us.json");
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (String key :
                new String[] {
                        "graniteskills.granite.command.sources.header",
                        "graniteskills.granite.command.sources.continuation",
                        "graniteskills.granite.command.sources.line"
                }) {
            assertTrue(json.contains("\"" + key + "\""), "missing " + key);
        }
    }

    @Test
    void sprint90GlobalCommandGraniteKeysPresentInEnUs() throws Exception {
        String json;
        try (InputStream in =
                FabricGraniteEnUsKeysTest.class.getResourceAsStream(
                        "/assets/graniteskills/lang/en_us.json")) {
            assertTrue(in != null, "classpath /assets/graniteskills/lang/en_us.json");
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (String key :
                new String[] {
                        "graniteskills.granite.command.profile.player_not_found",
                        "graniteskills.granite.command.profile.no_userdata_file",
                        "graniteskills.granite.command.profile.source_not_loaded",
                        "graniteskills.granite.command.profile.load_failed",
                        "graniteskills.granite.command.profile.userstate_disclaimer",
                        "graniteskills.granite.command.profile.mana_line",
                        "graniteskills.granite.command.profile.stat_mods_header",
                        "graniteskills.granite.command.profile.trait_mods_header",
                        "graniteskills.granite.command.profile.modifier_none",
                        "graniteskills.granite.command.confirmation.no_pending",
                        "graniteskills.granite.command.confirmation.mismatch",
                        "graniteskills.granite.command.templates.overwrite.confirm_console_only",
                        "graniteskills.granite.command.templates.overwrite.preview_arm_from_console",
                        "graniteskills.granite.command.xp.amount_positive",
                        "graniteskills.granite.command.xp.amount_non_negative",
                        "graniteskills.granite.command.level.count_positive",
                        "graniteskills.granite.command.storage.info_header",
                        "graniteskills.granite.command.resethealth.console_only",
                        "graniteskills.granite.command.item_armor_mod.players_only",
                        "graniteskills.granite.command.item_armor_mod.empty_hand"
                }) {
            assertTrue(json.contains("\"" + key + "\""), "missing " + key);
        }
    }

    @Test
    void sprint120MenuPresentationRepairKeysPresentInEnUs() throws Exception {
        String json;
        try (InputStream in =
                FabricGraniteEnUsKeysTest.class.getResourceAsStream(
                        "/assets/graniteskills/lang/en_us.json")) {
            assertTrue(in != null, "classpath /assets/graniteskills/lang/en_us.json");
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (String key :
                new String[] {
                        "graniteskills.granite.menu.stat_info_title_suffix",
                        "graniteskills.granite.menu.sources_title_suffix",
                        "graniteskills.granite.menu.sources_sort_current",
                        "graniteskills.granite.menu.sources_placeholder_name",
                        "graniteskills.granite.source_type.unknown",
                        "graniteskills.granite.source_type.other",
                        "graniteskills.granite.source_type.block",
                        "graniteskills.granite.source_type.mob",
                        "graniteskills.granite.source_type.fishing",
                        "graniteskills.granite.source_type.farming",
                        "graniteskills.granite.source_type.damage",
                }) {
            assertTrue(json.contains("\"" + key + "\""), "missing " + key);
        }
    }
}
