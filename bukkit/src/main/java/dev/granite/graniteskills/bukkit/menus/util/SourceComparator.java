package dev.granite.graniteskills.bukkit.menus.util;

import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.bukkit.GraniteSkills;

import java.util.Comparator;
import java.util.Locale;

public abstract class SourceComparator implements Comparator<XpSource> {

    protected final GraniteSkills plugin;

    public SourceComparator(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    public static class Ascending extends SourceComparator {

        public Ascending(GraniteSkills plugin) {
            super(plugin);
        }

        @Override
        public int compare(XpSource source1, XpSource source2) {
            return (int) (source1.getXp() * 100) - (int) (source2.getXp() * 100);
        }

    }

    public static class Descending extends SourceComparator {

        public Descending(GraniteSkills plugin) {
            super(plugin);
        }

        @Override
        public int compare(XpSource source1, XpSource source2) {
            return (int) (source2.getXp() * 100) - (int) (source1.getXp() * 100);
        }

    }

    public static class Alphabetical extends SourceComparator {

        private final Locale locale;

        public Alphabetical(GraniteSkills plugin, Locale locale) {
            super(plugin);
            this.locale = locale;
        }

        @Override
        public int compare(XpSource source1, XpSource source2) {
            return source1.getDisplayName(locale).compareTo(source2.getDisplayName(locale));
        }

    }

    public static class ReverseAlphabetical extends SourceComparator {

        private final Locale locale;

        public ReverseAlphabetical(GraniteSkills plugin, Locale locale) {
            super(plugin);
            this.locale = locale;
        }

        @Override
        public int compare(XpSource source1, XpSource source2) {
            return source2.getDisplayName(locale).compareTo(source1.getDisplayName(locale));
        }

    }

}
