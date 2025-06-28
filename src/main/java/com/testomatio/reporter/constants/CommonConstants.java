package com.testomatio.reporter.constants;

import okhttp3.MediaType;

public class CommonConstants {
    public static final String REPORTER_VERSION = "0.1.0";
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public static final String TESTS_STRING = "tests";
    public static final String API_KEY_STRING = "api_key";
    public static final String PASSED = "passed";
    public static final String SKIPPED = "skipped";
    public static final String FAILED = "failed";
    public static final String RESPONSE_UID_KEY = "uid";

    public static final int HTTP_TIMEOUT_SECONDS = 10;
    public static final int REQUEST_TIMEOUT_SECONDS = 30;

}
