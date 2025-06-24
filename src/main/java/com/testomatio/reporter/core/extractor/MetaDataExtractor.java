package com.testomatio.reporter.core.extractor;

import com.testomatio.reporter.model.TestMetadata;

/**
 * Extracts test metadata from framework-specific test objects.
 *
 * @param <T> type of framework-specific test object
 */
public interface MetaDataExtractor<T> {

    /**
     * Extracts test metadata from framework-specific source.
     *
     * @param source framework-specific test object or wrapper
     * @return extracted test metadata
     */
    TestMetadata extractTestMetadata(T source);
}