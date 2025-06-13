package com.constants;


import okhttp3.MediaType;

public class CommonConstants {
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public static final String API_KEY_PROPERTY_NAME = "testomatio.api.key";
    public static final String RUN_TITLE_PROPERTY_NAME = "testomatio.run.title";
    public static final String HOST_URL_PROPERTY_NAME = "testomatio.url";
    public static final String BATCH_SIZE_PROPERTY_NAME = "testomatio.batch.size";
    public static final String BATCH_FLUSH_INTERVAL_PROPERTY_NAME = "testomatio.batch.flush.interval";

    public static final String PROPERTIES_FILE_NAME = "testomat.properties";
}