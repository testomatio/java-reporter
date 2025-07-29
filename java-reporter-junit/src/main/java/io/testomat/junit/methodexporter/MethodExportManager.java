package io.testomat.junit.methodexporter;

import com.github.javaparser.ast.CompilationUnit;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.junit.methodexporter.extractors.MethodCaseExtractor;
import io.testomat.junit.methodexporter.parser.FileParser;
import io.testomat.junit.methodexporter.patfinder.FileFinder;
import io.testomat.junit.methodexporter.sender.ExportSender;
import io.testomat.junit.model.ExporterTestCase;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodExportManager {
    private static final Logger log = LoggerFactory.getLogger(MethodExportManager.class);

    public static final String EXPORT_REQUIRED_PROPERTY_NAME = "testomatio.export.required";
    private static final ConcurrentHashMap<String, Boolean> processedClasses =
            new ConcurrentHashMap<>();

    private final PropertyProvider provider;
    private final FileFinder fileFinder;
    private final ExportSender exportSender;
    private final FileParser fileParser;
    private final MethodCaseExtractor methodCaseExtractor;

    public MethodExportManager() {
        this.provider =
                PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
        this.fileFinder = new FileFinder();
        this.fileParser = new FileParser();
        this.exportSender = new ExportSender();
        this.methodCaseExtractor = new MethodCaseExtractor();
    }

    /**
     * Constructor for testing
     */
    public MethodExportManager(PropertyProvider provider, FileFinder fileFinder,
                               ExportSender exportSender, FileParser fileParser,
                               MethodCaseExtractor methodCaseExtractor) {
        this.provider = provider;
        this.fileFinder = fileFinder;
        this.exportSender = exportSender;
        this.fileParser = fileParser;
        this.methodCaseExtractor = methodCaseExtractor;
    }

    /**
     * Loads and exports test method bodies for the specified test class.
     */
    public void loadTestBodyForClass(Class<?> testClass) {
        log.info("loadTestBodyForClass called for class: {}", testClass.getName());

        try {
            if (!isInitializeExportRequired()) {
                log.info("Export not required - property {} not set", EXPORT_REQUIRED_PROPERTY_NAME);
                return;
            }

            log.info("Export is required - proceeding with test body loading");

            if (testClass == null) {
                log.error("Test class is null");
                throw new IllegalArgumentException("testClass is null");
            }

            String className = testClass.getName();
            log.info("Processing class: {}", className);

            if (processedClasses.putIfAbsent(className, true) != null) {
                log.info("Class {} already processed, skipping", className);
                return;
            }

            log.info("Getting file path for class: {}", className);
            String filepath = fileFinder.getTestClassFilePath(testClass);
            log.info("Found filepath: {}", filepath);

            if (filepath == null) {
                log.warn("Filepath is null for class: {}", className);
                return;
            }

            log.info("About to parse file: {}", filepath);
            CompilationUnit cu = null;
            try {
                cu = fileParser.parseFile(filepath);
                log.info("File parsing completed. CompilationUnit is null: {}", cu == null);
            } catch (Exception e) {
                log.error("Exception during file parsing: {}", e.getMessage(), e);
                return;
            }

            if (cu == null) {
                log.warn("CompilationUnit is null for file: {}", filepath);
                return;
            }

            log.info("Successfully parsed file: {}", filepath);

            log.info("About to extract test cases from CompilationUnit");
            List<ExporterTestCase> testCases = null;
            try {
                testCases = methodCaseExtractor.extractTestCases(cu, filepath);
                log.info("Test case extraction completed. Extracted {} test cases", testCases != null ? testCases.size() : 0);
            } catch (Exception e) {
                log.error("Exception during test case extraction: {}", e.getMessage(), e);
                return;
            }

            if (testCases == null) {
                log.warn("Test cases list is null for file: {}", filepath);
                return;
            }

            log.info("Extracted {} test cases from file: {}", testCases.size(), filepath);

            if (testCases.isEmpty()) {
                log.warn("No test cases extracted from file: {}", filepath);
                return;
            }

            log.info("About to send {} test cases to server", testCases.size());
            try {
                exportSender.sendTestCases(testCases);
                log.info("Successfully sent test cases for class: {}", className);
            } catch (Exception e) {
                log.error("Exception during sending test cases: {}", e.getMessage(), e);
                return;
            }

            log.info("Finished processing class: {}", className);

        } catch (Exception e) {
            log.error("Unexpected exception in loadTestBodyForClass for class {}: {}", testClass.getName(), e.getMessage(), e);
        }
    }

    public void loadTestBodyIfRequired(final ExtensionContext extensionContext) {
        if (!isInitializeExportRequired()) {
            return;
        }

        if (extensionContext == null) {
            throw new IllegalArgumentException("extensionContext is null");
        }

        String className = extensionContext.getRequiredTestClass().getName();
        if (processedClasses.putIfAbsent(className, true) != null) {
            return;
        }

        String filepath = fileFinder.getTestClassFilePath(extensionContext.getRequiredTestClass());
        if (filepath == null) {
            return;
        }

        CompilationUnit cu = fileParser.parseFile(filepath);
        if (cu == null) {
            return;
        }

        List<ExporterTestCase> testCases = methodCaseExtractor.extractTestCases(cu, filepath);

        if (testCases.isEmpty()) {
            return;
        }

        exportSender.sendTestCases(testCases);
    }

    private boolean isInitializeExportRequired() {
        try {
            String propertyValue = provider.getProperty(EXPORT_REQUIRED_PROPERTY_NAME);
            log.debug("Property {} value: {}", EXPORT_REQUIRED_PROPERTY_NAME, propertyValue);
            return propertyValue != null;
        } catch (Exception e) {
            log.error("Error checking export required property: {}", e.getMessage(), e);
            return false;
        }
    }
}