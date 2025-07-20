package io.testomat.junit.methodexporter;

import com.github.javaparser.ast.CompilationUnit;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.junit.methodexporter.extractors.MethodCaseExtractor;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MethodExportManager {
    public static final String EXPORT_REQUIRED_PROPERTY_NAME = "testomatio.export.required";
    private static final ConcurrentHashMap<String, Boolean> processedClasses =
            new ConcurrentHashMap<>();

    private final PropertyProvider provider;

    private final PathFinder pathFinder;
    private final ExportSender exportSender;
    private final FileParser fileParser;
    private final MethodCaseExtractor methodCaseExtractor;

    public MethodExportManager() {
        this.provider =
                PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
        this.pathFinder = new PathFinder();
        this.fileParser = new FileParser();
        this.exportSender = new ExportSender();
        this.methodCaseExtractor = new MethodCaseExtractor();
    }

    /**
     * Constructor for testing
     */
    public MethodExportManager(PropertyProvider provider, PathFinder pathFinder,
                               ExportSender exportSender, FileParser fileParser,
                               MethodCaseExtractor methodCaseExtractor) {
        this.provider = provider;
        this.pathFinder = pathFinder;
        this.exportSender = exportSender;
        this.fileParser = fileParser;
        this.methodCaseExtractor = methodCaseExtractor;
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

        String filepath = pathFinder.getTestClassFilePath(extensionContext);
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
            return provider.getProperty(EXPORT_REQUIRED_PROPERTY_NAME) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
