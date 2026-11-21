package dev.granite.graniteskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricMenuHelperTest {

    @Test
    void missingMenuKeepsTheFormatNameInsteadOfABlankString() {
        assertEquals("reward", new FabricMenuHelper().getFormat("skills", "reward"));
    }

    @Test
    void statRewardEntryIsATemplateNotTheLookupKey() {
        String format = new FabricMenuHelper().getFormat("level_progression", "stat_reward_entry");
        assertEquals("\n  {color}+{num} {symbol} {stat}", format);
    }

    @Test
    void emptyFormatNameStaysEmpty() {
        assertEquals("", new FabricMenuHelper().getFormat("skills", ""));
    }
}
