package dev.granite.graniteskills.common.config.preset;

import java.util.List;

public record PresetLoadResult(List<String> created, List<String> modified, List<String> replaced, List<String> deleted,
        List<String> skipped) {

}
