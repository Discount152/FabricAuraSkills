package dev.granite.graniteskills.common.user;

import dev.granite.graniteskills.common.region.BlockPosition;
import org.jetbrains.annotations.NotNull;

public record AntiAfkLog(
        long timestamp,
        @NotNull String message,
        BlockPosition coords,
        @NotNull String world
) {

}
