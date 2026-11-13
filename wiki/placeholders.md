---
description: List of PlaceholderAPI placeholders
---

# Placeholders

GraniteSkills provides PlaceholderAPI placeholders that work out of the box, without needing to download an ecloud expansion. Due to the plugin renaming in 2.0, existing placeholders prefixed with `%aureliumskills_` will still work alongside `%graniteskills_` placeholders. However, you should slowly migrate your placeholders to use `%graniteskills_`, since the old prefix may be removed in later versions.

## List

* `%graniteskills_power%` - Combined skill level
* `%graniteskills_[skill]%` - Level for a certain skill (ex: %graniteskills\_farming%)
* `%graniteskills_[skill]_roman%` - Level for a certain skill as a roman numeral
* `%graniteskills_[stat]%` - Level for a certain stat (ex: %graniteskills\_health%)
* `%graniteskills_[stat]_int%` - Level for a certain stat rounded to an integer
* `%graniteskills_average%` - Average skill level
* `%graniteskills_average_int%` - Average skill level rounded to an integer
* `%graniteskills_average_1%` - Average skill level rounded to one decimal
* `%graniteskills_average_stat%` - Average stat level
* `%graniteskills_average_stat_int%` - Average stat level rounded to an integer
* `%graniteskills_average_stat_1%` - Average stat level rounded to one decimal
* `%graniteskills_hp%` - Hp as an integer
* `%graniteskills_hp_1%` - Hp rounded to 1 decimal
* `%graniteskills_hp_2%` - Hp rounded to 2 decimals
* `%graniteskills_hp_max%` - Max hp
* `%graniteskills_hp_percent%` - Percent hp as an integer
* `%graniteskills_mana%` - Current player mana
* `%graniteskills_mana_int%` - Mana of a player rounded to an integer &#x20;
* `%graniteskills_mana_max%` - Max mana
* `%graniteskills_mana_max_int%` - Max mana rounded to an integer
* `%graniteskills_lb_power_[place]%` - Gets a formatted value of username and level for the power leaderboard at a certain place
* `%graniteskills_lb_power_[place]_name%` - Gets the username for the power leaderboard at a certain place
* `%graniteskills_lb_power_[place]_value%` - Gets the level for the power leaderboard at a certain place
* `%graniteskills_lb_[skill]_[place]%` - Gets a formatted value of username and level for a skill leaderboard at a certain place
* `%graniteskills_lb_[skill]_[place]_name%` - Gets the username for a skill leaderboard at a certain place
* `%graniteskills_lb_[skill]_[place]_value%` - Gets the level for a skill leaderboard at a certain place
* `%graniteskills_rank%` - Gets the power leaderboard rank
* `%graniteskills_rank_[skill]%` - Gets a skill leaderboard rank
* `%graniteskills_xp_required_formatted_[skill]%` - Gets the required XP for the next level of a player formatted with suffixes (ex: 5k)
* `%graniteskills_xp_required_[skill]%` - Gets the required XP for the next level of a player as an exact number
* `%graniteskills_xp_progress_int_[skill]%` - Gets the percent progress to the next level rounded to an integer
* `%graniteskills_xp_progress_1_[skill]%` - Gets the percent progress to the next level rounded to 1 decimal place
* `%graniteskills_xp_progress_[skill]%` - Gets the exact percent progress to the next level
* `%graniteskills_xp_int_[skill]%` - Gets the current XP for a player rounded to an integer
* `%graniteskills_xp_formatted_[skill]%` - Gets the current XP for a player formatted with suffixes
* `%graniteskills_xp_bar_[skill]%` - Gets the progress bar for a skill as shown in menus
* `%graniteskills_xp_[skill]%` - Gets the exact skill XP for a player
* `%graniteskills_multiplier%` - Gets a player's permission XP multiplier rounded up to 2 decimal places (1 = normal, 2 = 2x XP)
* `%graniteskills_multiplier_[skill]%` - Gets a player's permission XP multiplier for a specific skill rounded up to 2 decimal places (1 = normal, 2 = 2x XP)
* `%graniteskills_multiplier_percent%` - Gets a player's permission XP multiplier as a percent more value rounded to an integer (40 = 40% more XP)
* `%graniteskills_multiplier_percent_[skill]%` - Gets a player's permission XP multiplier for a specific skill as a percent more value rounded to an integer (40 = 40% more XP)
* `%graniteskills_actionbar_status%` - Gets whether the idle action bar is enabled for a player. Returns true or false.
* `%graniteskills_[ability]%` - Gets an ability level. Replace `[ability]` with the default English name of an ability in lowercase.
* `%graniteskills_[ability]_roman%` - Gets ability level as a Roman numeral.
* `%graniteskills_[ability]_value%` - Gets an ability value.
* `%graniteskills_[ability]_value_int%` - Gets ability value rounded to an integer.
* `%graniteskills_[ability]_value_2%` - Gets the secondary value of an ability if it exists.
* `%graniteskills_[ability]_value_2_int%` - Gets secondary ability value rounded to an integer if it exists.
* `%graniteskills_mability_[ability]%` - Gets the player's mana ability level.
* `%graniteskills_mability_[ability]_roman%` - Gets mana ability level as a Roman numeral.
* `%graniteskills_mability_[ability]_value%` - Gets the mana ability value.
* `%graniteskills_mability_[ability]_value_int%` - Gets mana ability value rounded to an integer
* `%graniteskills_mability_[ability]_active%` - Returns true if mana ability is active, false otherwise.
* `%graniteskills_trait_[trait]%` - Gets the effective level of a trait.
* `%graniteskills_trait_[trait]_bonus%` - Gets the bonus level of a trait (level excluding the base value).
* `%graniteskills_trait_[trait]_menu%` - Gets the trait in the same format displayed in the stats menu.
* `%graniteskills_jobs_list%` - Lists active jobs in a comma separated list of default skill names in all lowercase.
* `%graniteskills_jobs_list_formatted%` - Lists active jobs in a comma separated list with the skill display name of the config default\_language.
* `%graniteskills_jobs_count%` - Gets the number of jobs the player currently has active.
* `%graniteskills_jobs_limit%` - Gets the maximum number of jobs the player is allowed to have active at the same time.
* `%graniteskills_jobs_active_[skill]%` - Returns true/false whether the job is active (ex: %graniteskills\_jobs\_active\_farming%)
