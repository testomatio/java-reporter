package io.testomat.junit.listener;

import static io.testomat.core.constants.ArtifactPropertyNames.ARTIFACT_DISABLE_PROPERTY_NAME;
import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;

import io.testomat.core.artifact.client.AwsService;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import io.testomat.junit.methodexporter.MethodExportManager;
import io.testomat.junit.reporter.JunitTestReporter;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit 5 extension for Testomat.io integration.
 * Reports JUnit test execution results to Testomat.io platform.
 */
public class JunitListener implements BeforeEachCallback, BeforeAllCallback,
        AfterAllCallback, AfterEachCallback, TestWatcher {

    private static final Logger log = LoggerFactory.getLogger(JunitListener.class);
    private boolean artifactDisabled = false;

    private final MethodExportManager methodExportManager;
    private final GlobalRunManager runManager;
    private final JunitTestReporter reporter;
    private final PropertyProvider provider;
    private final AwsService awsService;
    private final Set<String> processedClasses;

    public JunitListener() {
        this.methodExportManager = new MethodExportManager();
        this.runManager = GlobalRunManager.getInstance();
        this.reporter = new JunitTestReporter();
        this.processedClasses = ConcurrentHashMap.newKeySet();
        this.awsService = new AwsService();
        this.provider =
                PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
        this.artifactDisabled = defineArtifactsDisabled();
    }

    /**
     * Constructor for testing
     *
     * @param methodExportManager the method export manager
     * @param runManager          the global run manager
     * @param reporter            the JUnit test reporter
     * @param provider            the property provider
     */
    public JunitListener(MethodExportManager methodExportManager,
                         GlobalRunManager runManager,
                         JunitTestReporter reporter,
                         PropertyProvider provider,
                         AwsService awsService) {
        this.methodExportManager = methodExportManager;
        this.runManager = runManager;
        this.reporter = reporter;
        this.provider = provider;
        this.processedClasses = ConcurrentHashMap.newKeySet();
        this.awsService = awsService;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!isListeningRequired()) {
            return;
        }
        runManager.incrementSuiteCounter();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (!isListeningRequired()) {
            return;
        }

        exportTestClassIfNotProcessed(context);
        runManager.decrementSuiteCounter();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        if (!isListeningRequired()) {
            return;
        }

        reporter.reportTestResult(context, SKIPPED, reason.orElse("Test disabled"));
        exportTestClassIfNotProcessed(context);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        if (!isListeningRequired()) {
            return;
        }

        reporter.reportTestResult(context, PASSED, null);
        exportTestClassIfNotProcessed(context);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        if (!isListeningRequired()) {
            return;
        }

        reporter.reportTestResult(context, SKIPPED, cause.getMessage());
        exportTestClassIfNotProcessed(context);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        if (!isListeningRequired()) {
            return;
        }

        reporter.reportTestResult(context, FAILED, cause.getMessage());
        exportTestClassIfNotProcessed(context);
    }

    private void exportTestClassIfNotProcessed(ExtensionContext context) {
        if (!isListeningRequired()) {
            return;
        }

        Optional<Class<?>> testClass = context.getTestClass();
        if (testClass.isEmpty()) {
            return;
        }

        Class<?> clazz = testClass.get();
        String className = clazz.getName();

        if (processedClasses.add(className)) {
            methodExportManager.loadTestBodyForClass(clazz);
        }
    }

    private boolean isListeningRequired() {
        try {
            return provider.getProperty(API_KEY_PROPERTY_NAME) != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (!artifactDisabled) {
            awsService.uploadAllArtifactsForTest(context.getDisplayName(), context.getUniqueId(),
                    JunitMetaDataExtractor.extractTestId(context.getTestMethod().get()));
        }
    }

    private boolean defineArtifactsDisabled() {
        boolean result;
        String property;
        try {
            property = provider.getProperty(ARTIFACT_DISABLE_PROPERTY_NAME);
            result = property != null
                    && !property.trim().isEmpty()
                    && !property.equalsIgnoreCase("0");

        } catch (Exception e) {
            return false;
        }
        return result;
    }
}
