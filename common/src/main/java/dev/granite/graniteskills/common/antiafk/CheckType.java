package dev.granite.graniteskills.common.antiafk;

public interface CheckType {

    Class<? extends Check> getCheckClass();

    String name();

}
