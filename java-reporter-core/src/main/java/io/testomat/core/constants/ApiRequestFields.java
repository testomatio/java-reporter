package io.testomat.core.constants;

/**
 * Constants for API request field names used in JSON payloads.
 * Ensures consistency across different request builders and API calls.
 */
public class ApiRequestFields {

    public static final String TITLE = "title";
    public static final String TEST_ID = "test_id";
    public static final String SUITE_TITLE = "suite_title";
    public static final String FILE = "file";
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final String STACK = "stack";
    public static final String EXAMPLE = "example";
    public static final String RID = "rid";

    public static final String STATUS_EVENT = "status_event";
    public static final String DURATION = "duration";
    public static final String ENVIRONMENT = "environment";
    public static final String GROUP_TITLE = "group_title";
    public static final String BUILD_URL = "ci_build_url";

    private ApiRequestFields() {
    }
}
