package dev.granitelib.fabric.template.model;

import java.util.Optional;

/**
 * Optional full-inventory fill item (v0 schema: {@code fill} block).
 */
public record GraniteTemplateFill(String material, Optional<String> displayName) {

    public GraniteTemplateFill {
        if (material == null || material.isBlank()) {
            throw new IllegalArgumentException("fill.material must be non-blank");
        }
    }
}
