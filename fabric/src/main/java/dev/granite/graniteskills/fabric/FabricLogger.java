package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.common.message.PlatformLogger;
import org.slf4j.Logger;

public final class FabricLogger implements PlatformLogger {

    private final Logger logger;

    public FabricLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    @Override
    public void severe(String message) {
        logger.error(message);
    }

    @Override
    public void severe(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }
}
