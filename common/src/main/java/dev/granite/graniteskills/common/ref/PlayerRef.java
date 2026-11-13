package dev.granite.graniteskills.common.ref;

/**
 * A wrapper class for platform-dependent player instances
 */
public interface PlayerRef {

    Object get();

    LocationRef getLocation();

}
