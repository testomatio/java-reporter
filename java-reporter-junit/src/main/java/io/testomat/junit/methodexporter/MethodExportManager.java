package io.testomat.junit.methodexporter;

import com.github.javaparser.ast.CompilationUnit;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.junit.methodexporter.extractors.MethodCaseExtractor;
import io.testomat.junit.methodexporter.filefinder.FileFinder;
import io.testomat.junit.methodexporter.parser.FileParser;
import io.testomat.junit.methodexporter.sender.ExportSender;
import io.testomat.junit.model.ExporterTestCase;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodExportManager {
    public static final String EXPORT_REQUIRED_PROPERTY_NAME = "testomatio.export.required";

    private static final Logger log = LoggerFactory.getLogger(MethodExportManager.class);
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
        log.debug("loadTestBodyForClass called for class: {}", testClass.getName());

        try {
            if (!isInitializeExportRequired()) {

                return;
            }

            log.debug("Export is required - proceeding with test body loading");

            String className = testClass.getName();
            log.debug("Processing class: {}", className);

            if (processedClasses.putIfAbsent(className, true) != null) {
                log.debug("Class {} already processed, skipping", className);
                return;
            }

            log.debug("Getting file path for class: {}", className);
            String filepath = fileFinder.getTestClassFilePath(testClass);
            log.debug("Found filepath: {}", filepath);

            if (filepath == null) {
                log.warn("Filepath is null for class: {}", className);
                return;
            }

            log.debug("About to parse file: {}", filepath);
            CompilationUnit cu;
            try {
                cu = fileParser.parseFile(filepath);
                log.debug("File parsing completed. CompilationUnit is null: {}", cu == null);
            } catch (Exception e) {
                log.error("Exception during file parsing: {}", e.getMessage(), e);
                return;
            }

            if (cu == null) {
                log.warn("CompilationUnit is null for file: {}", filepath);
                return;
            }

            log.debug("Successfully parsed file: {}", filepath);

            log.debug("About to extract test cases from CompilationUnit");
            List<ExporterTestCase> testCases;
            try {
                testCases = methodCaseExtractor.extractTestCases(cu, filepath);

            } catch (Exception e) {
                log.error("Exception during test case extraction: {}", e.getMessage(), e);
                return;
            }

            if (testCases == null) {
                log.warn("Test cases list is null for file: {}", filepath);
                return;
            }

            log.debug("Extracted {} test cases from file: {}", testCases.size(), filepath);

            if (testCases.isEmpty()) {
                log.warn("No test cases extracted from file: {}", filepath);
                return;
            }

            log.debug("About to send {} test cases to server", testCases.size());
            try {
                exportSender.sendTestCases(testCases);
                log.debug("Successfully sent test cases for class: {}", className);
            } catch (Exception e) {
                log.error("Exception during sending test cases: {}", e.getMessage(), e);
                return;
            }

            log.debug("Finished processing class: {}", className);

        } catch (Exception e) {
            log.warn("Exception during processing class: {}", e.getMessage(), e);
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
            return propertyValue != null;
        } catch (Exception e) {
            return false;
        }
    }
}
