package io.testomat.junit.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.junit.methodexporter.MethodExportManager;
import io.testomat.junit.reporter.JunitTestReporter;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class JunitListenerTest {

    @Mock
    private MethodExportManager methodExportManager;

    @Mock
    private GlobalRunManager runManager;

    @Mock
    private JunitTestReporter reporter;

    @Mock
    private PropertyProvider propertyProvider;

    @Mock
    private FacadeFunctionsHandler functionsHandler;

    @Mock
    private ExtensionContext context;

    @Mock
    private Method testMethod;

    private JunitListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener = new JunitListener(methodExportManager, runManager, reporter, propertyProvider, functionsHandler);
    }


    @Test
    void beforeAll_WhenListeningNotRequired_ShouldSkipIncrement() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn(null);

        listener.beforeAll(context);

        verify(runManager, never()).incrementSuiteCounter();
    }

    @Test
    void beforeAll_WhenListeningRequired_ShouldIncrementSuiteCounter() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn("test-api-key");

        listener.beforeAll(context);

        verify(runManager).incrementSuiteCounter();
    }

    @Test
    void testSuccessful_WhenListeningRequired_ShouldReportPassed() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn("test-api-key");
        when(context.getTestClass()).thenReturn(Optional.of(String.class));

        listener.testSuccessful(context);

        verify(reporter).reportTestResult(context, PASSED, null);
        verify(methodExportManager).loadTestBodyForClass(String.class);
    }

    @Test
    void afterEach_ShouldCallFunctionsHandler() {
        listener.afterEach(context);

        verify(functionsHandler).handleFacadeFunctions(context);
    }

    @Test
    void defaultConstructor_ShouldInitializeAllDependencies() {
        JunitListener defaultListener = new JunitListener();

        assertDoesNotThrow(() -> {
            when(context.getTestClass()).thenReturn(Optional.of(String.class));
            defaultListener.testSuccessful(context);
        });
    }
}