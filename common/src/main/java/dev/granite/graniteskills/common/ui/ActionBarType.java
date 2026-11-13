package dev.granite.graniteskills.common.ui;

import dev.granite.graniteskills.common.config.Option;

public enum ActionBarType {

    IDLE,
    ABILITY,
    XP,
    MAXED;

    private final Option option;

    ActionBarType() {
        this.option = Option.valueOf("ACTION_BAR_" + this.name());
    }

    public Option getOption() {
        return this.option;
    }

}
