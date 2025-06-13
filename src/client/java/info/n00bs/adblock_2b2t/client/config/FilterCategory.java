package info.n00bs.adblock_2b2t.client.config;

/**
 * Constants for filter types.
 */
public class FilterCategory {
    public static final String REMOTE_FILTERS_FILENAME = "remote.txt";
    public static final String CUSTOM_FILTERS_FILENAME = "custom.txt";

    public static final String REMOTE_FILTERS_DISPLAY_NAME = "Remote Filters";
    public static final String CUSTOM_FILTERS_DISPLAY_NAME = "Custom Filters";

    public static final String REMOTE_FILTERS_DESCRIPTION = "Filters loaded from remote source";
    public static final String CUSTOM_FILTERS_DESCRIPTION = "User-defined custom filters";

    private FilterCategory() {
        // Private constructor to prevent instantiation
    }
}
