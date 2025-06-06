package unblonded.packets.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Toggleable {
    /**
     * Aliases for the feature - used for commands and lookups
     */
    String[] aliases();

    /**
     * Display name for the feature - shown in UI
     */
    String displayName();

    /**
     * Category for grouping features
     */
    String category();
}