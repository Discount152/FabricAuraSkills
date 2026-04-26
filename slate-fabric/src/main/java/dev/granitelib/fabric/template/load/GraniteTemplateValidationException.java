package dev.granitelib.fabric.template.load;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Aggregate validation failures from template YAML (Spike 0). */
public final class GraniteTemplateValidationException extends IllegalArgumentException {

    private final List<String> errors;

    public GraniteTemplateValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public List<String> errors() {
        return errors;
    }
}
