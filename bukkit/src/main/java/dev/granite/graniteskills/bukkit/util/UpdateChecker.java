package dev.granite.graniteskills.bukkit.util;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UpdateChecker {

    public UpdateChecker(GraniteSkills plugin) {
        Objects.requireNonNull(plugin);
    }

    public void sendUpdateMessageAsync(CommandSender sender) {
    }

    public void getVersion(final BiConsumer<Optional<String>, Optional<String>> consumer) {
        consumer.accept(Optional.empty(), Optional.empty());
    }

    @VisibleForTesting
    public boolean isOutdated(String localVersion, String resourceVersion) {
        if (localVersion.equalsIgnoreCase(resourceVersion)) { // Versions match exactly
            return false;
        }
        String[] localSplit = localVersion.split(" ");
        String[] resourceSplit = resourceVersion.split(" ");
        String localNum;
        String resourceNum;
        if (localSplit.length >= 2 && resourceSplit.length >= 2) {
            localNum = localSplit[1];
        } else {
            localNum = localSplit[0];
        }
        if (resourceSplit.length >= 2) {
            resourceNum = resourceSplit[1];
        } else {
            resourceNum = resourceSplit[0];
        }
        // Remove part after any hyphens including the hyphen
        int localIndex = localNum.indexOf("-");
        int localSuffix = getSuffix(localNum);
        if (localIndex != -1) {
            localNum = localNum.substring(0, localIndex);
        }
        int resourceIndex = resourceNum.indexOf("-");
        int resourceSuffix = getSuffix(resourceNum);
        if (resourceIndex != -1) {
            resourceNum = resourceNum.substring(0, resourceIndex);
        }

        String[] localVersionSplit = localNum.split("\\.");
        String[] resourceVersionSplit = resourceNum.split("\\.");

        // Check each part of the version number between the dots
        for (int i = 0; i < localVersionSplit.length; i++) {
            if (i >= resourceVersionSplit.length) {
                break;
            }
            try {
                int local = Integer.parseInt(localVersionSplit[i]);
                int resource = Integer.parseInt(resourceVersionSplit[i]);
                if (local < resource) { // If local is less than resource, return as outdated
                    return true;
                } else if (local > resource) { // If local is greater than resource, return as not outdated
                    return false;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (localSuffix != -1 && resourceSuffix != -1) {
            return resourceSuffix > localSuffix;
        }
        return true;
    }

    private int getSuffix(String str) {
        int resourceIndex = str.indexOf("-");
        if (resourceIndex != -1) {
            String suffixStr = str.substring(resourceIndex + 1);
            if (suffixStr.contains("+")) {
                String[] arr = dev.aurelium.slate.util.TextUtil.substringsBetween(suffixStr, ".", "+");
                if (arr != null && arr.length >= 1) {
                    try {
                        return Integer.parseInt(arr[0]);
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else {
                String buildNumStr = suffixStr.substring(suffixStr.indexOf(".") + 1);
                try {
                    return Integer.parseInt(buildNumStr);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return -1;
    }

}
