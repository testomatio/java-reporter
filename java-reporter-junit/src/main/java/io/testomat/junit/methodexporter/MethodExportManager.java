package io.testomat.junit.methodexporter;

import com.github.javaparser.ast.CompilationUnit;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MethodExportManager {
    public static final String EXPORT_REQUIRED_PROPERTY_NAME = "testomatio.export.required";
    private static final ConcurrentHashMap<String, Boolean> processedClasses =
            new ConcurrentHashMap<>();

    private final PropertyProvider provider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    private final PathFinder pathFinder = new PathFinder();
    private final ExportSender exportSender = new ExportSender();
    private final FileParser fileParser = new FileParser();
    private final String exportRequired = initializeExportRequired();
    private final MethodCaseExtractor methodCaseExtractor = new MethodCaseExtractor();

    public void loadTestBodyIfRequired(final ExtensionContext extensionContext) {
        if (exportRequired == null) {
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

        exportSender.sendTestCases(testCases);
    }

    private String initializeExportRequired() {
        try {
            return provider.getProperty(EXPORT_REQUIRED_PROPERTY_NAME);
        } catch (Exception e) {
            return null;
        }
    }
}
