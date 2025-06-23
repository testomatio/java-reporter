package com.testomatio.reporter.core.extractor;

import com.testomatio.reporter.model.TestMetadata;

public interface MetaDataExtractor<T> {

    /**
     * Extracts metadata from test case result or method to construct a report of a test case
     * @param source is a metadata source object or a wrapper in any particular implementation
     */
    TestMetadata extractTestMetadata(T source);
}