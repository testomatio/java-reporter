package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.annotation.TestId;
import com.testomatio.reporter.annotation.Title;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JUnitExtensionTest {

    @Test
    @TestId("T0")
    @Title("Skipped test")
//    @Disabled
    void test() {
        assertTrue(true);
    }
}