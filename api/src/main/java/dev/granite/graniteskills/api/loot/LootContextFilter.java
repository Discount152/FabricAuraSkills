package dev.granite.graniteskills.api.loot;

@FunctionalInterface
public interface LootContextFilter {

    boolean passesFilter(Loot loot);

}
