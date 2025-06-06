package unblonded.packets.util;

import imgui.type.ImBoolean;
import unblonded.packets.cfg;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ToggleManager {

    private static final Map<String, ToggleableFeature> FEATURE_MAP = new HashMap<>();
    private static boolean initialized = false;

    private static class ToggleableFeature {
        final Field field;
        final String displayName;
        final String category;
        final String[] aliases;

        ToggleableFeature(Field field, Toggleable annotation) {
            this.field = field;
            this.displayName = annotation.displayName();
            this.category = annotation.category();
            this.aliases = annotation.aliases();
        }

        boolean isEnabled() {
            try {
                Object value = field.get(null);
                if (value instanceof ImBoolean) {
                    return ((ImBoolean) value).get();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        }

        boolean toggle() {
            try {
                Object value = field.get(null);
                if (value instanceof ImBoolean) {
                    ImBoolean imBool = (ImBoolean) value;
                    imBool.set(!imBool.get());
                    return imBool.get();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        }

        void setEnabled(boolean enabled) {
            try {
                Object value = field.get(null);
                if (value instanceof ImBoolean) {
                    ((ImBoolean) value).set(enabled);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void initialize() {
        if (initialized) return;

        Field[] fields = cfg.class.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Toggleable.class)) {
                Toggleable annotation = field.getAnnotation(Toggleable.class);
                ToggleableFeature feature = new ToggleableFeature(field, annotation);

                // Register all aliases for this feature
                for (String alias : annotation.aliases()) {
                    FEATURE_MAP.put(alias.toLowerCase(), feature);
                }
            }
        }

        initialized = true;
    }

    /**
     * Toggle a feature by alias
     * @param alias The feature alias
     * @return Optional containing the new state, empty if feature not found
     */
    public static Optional<Boolean> toggleFeature(String alias) {
        initialize();
        ToggleableFeature feature = FEATURE_MAP.get(alias.toLowerCase());
        if (feature != null) {
            return Optional.of(feature.toggle());
        }
        return Optional.empty();
    }

    /**
     * Set a feature's state
     * @param alias The feature alias
     * @param enabled The desired state
     * @return true if successful, false if feature not found
     */
    public static boolean setFeature(String alias, boolean enabled) {
        initialize();
        ToggleableFeature feature = FEATURE_MAP.get(alias.toLowerCase());
        if (feature != null) {
            feature.setEnabled(enabled);
            return true;
        }
        return false;
    }

    /**
     * Check if a feature is enabled
     * @param alias The feature alias
     * @return Optional containing the state, empty if feature not found
     */
    public static Optional<Boolean> isFeatureEnabled(String alias) {
        initialize();
        ToggleableFeature feature = FEATURE_MAP.get(alias.toLowerCase());
        if (feature != null) {
            return Optional.of(feature.isEnabled());
        }
        return Optional.empty();
    }

    /**
     * Get feature display name by alias
     * @param alias The feature alias
     * @return Optional containing the display name, empty if feature not found
     */
    public static Optional<String> getFeatureDisplayName(String alias) {
        initialize();
        ToggleableFeature feature = FEATURE_MAP.get(alias.toLowerCase());
        if (feature != null) {
            return Optional.of(feature.displayName);
        }
        return Optional.empty();
    }

    /**
     * Get all available feature aliases
     * @return Set of all aliases
     */
    public static Set<String> getAllAliases() {
        initialize();
        return new HashSet<>(FEATURE_MAP.keySet());
    }

    /**
     * Get features grouped by category
     * @return Map of category -> list of features
     */
    public static Map<String, List<String>> getFeaturesByCategory() {
        initialize();
        return FEATURE_MAP.values().stream()
                .collect(Collectors.groupingBy(
                        f -> f.category,
                        Collectors.mapping(f -> f.displayName, Collectors.toList())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().distinct().collect(Collectors.toList())
                ));
    }

    /**
     * Get all enabled features
     * @return List of enabled feature display names
     */
    public static List<String> getEnabledFeatures() {
        initialize();
        return FEATURE_MAP.values().stream()
                .distinct()
                .filter(ToggleableFeature::isEnabled)
                .map(f -> f.displayName)
                .collect(Collectors.toList());
    }

    /**
     * Enable multiple features at once
     * @param aliases List of feature aliases to enable
     * @return Map of alias -> success status
     */
    public static Map<String, Boolean> enableFeatures(List<String> aliases) {
        Map<String, Boolean> results = new HashMap<>();
        for (String alias : aliases) {
            results.put(alias, setFeature(alias, true));
        }
        return results;
    }

    /**
     * Disable multiple features at once
     * @param aliases List of feature aliases to disable
     * @return Map of alias -> success status
     */
    public static Map<String, Boolean> disableFeatures(List<String> aliases) {
        Map<String, Boolean> results = new HashMap<>();
        for (String alias : aliases) {
            results.put(alias, setFeature(alias, false));
        }
        return results;
    }

    /**
     * Disable all features
     */
    public static void disableAllFeatures() {
        initialize();
        FEATURE_MAP.values().stream()
                .distinct()
                .forEach(feature -> feature.setEnabled(false));
    }
}