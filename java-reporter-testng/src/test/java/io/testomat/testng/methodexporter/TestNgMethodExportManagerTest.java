package io.testomat.testng.methodexporter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.javaparser.ast.CompilationUnit;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.testng.exception.MethodExporterException;
import io.testomat.testng.methodexporter.extractor.TestNgMethodCaseExtractor;
import io.testomat.testng.methodexporter.model.TestNgExporterTestCase;
import io.testomat.testng.methodexporter.parser.TestNgFileParser;
import io.testomat.testng.methodexporter.pathfinder.TestNgFileFinder;
import io.testomat.testng.methodexporter.sender.TestNgExportSender;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TestNgMethodExportManagerTest {

    private PropertyProvider provider;
    private TestNgFileFinder fileFinder;
    private TestNgExportSender exportSender;
    private TestNgFileParser fileParser;
    private TestNgMethodCaseExtractor methodCaseExtractor;
    private TestNgMethodExportManager manager;

    @BeforeEach
    void setUp() {
        provider = mock(PropertyProvider.class);
        fileFinder = mock(TestNgFileFinder.class);
        exportSender = mock(TestNgExportSender.class);
        fileParser = mock(TestNgFileParser.class);
        methodCaseExtractor = mock(TestNgMethodCaseExtractor.class);
        manager = new TestNgMethodExportManager(provider, fileFinder, exportSender, fileParser,
                methodCaseExtractor);
    }

    @Test
    @DisplayName("Should export test cases when export is required")
    void shouldExportTestCasesWhenExportIsRequired() {
        when(provider.getProperty(TestNgMethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                .thenReturn("true");
        when(fileFinder.getTestClassFilePath(ExportClassSuccess.class))
                .thenReturn("src/test/java/ExportClassSuccess.java");
        when(fileParser.parseFile(anyString())).thenReturn(mock(CompilationUnit.class));
        when(methodCaseExtractor.extractTestCases(any(CompilationUnit.class), anyString()))
                .thenReturn(List.of(createTestCase()));

        manager.loadTestBodyForClass(ExportClassSuccess.class);

        verify(exportSender).sendTestCases(argThat(testCases -> testCases.size() == 1));
    }

    @Test
    @DisplayName("Should not export when export is not required")
    void shouldNotExportWhenExportIsNotRequired() {
        when(provider.getProperty(TestNgMethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                .thenReturn("false");

        manager.loadTestBodyForClass(ExportClassNotRequired.class);

        verifyNoInteractions(fileFinder, fileParser, methodCaseExtractor, exportSender);
    }

    @Test
    @DisplayName("Should export class only once")
    void shouldExportClassOnlyOnce() {
        when(provider.getProperty(TestNgMethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                .thenReturn("true");
        when(fileFinder.getTestClassFilePath(ExportClassDedup.class))
                .thenReturn("src/test/java/ExportClassDedup.java");
        when(fileParser.parseFile(anyString())).thenReturn(mock(CompilationUnit.class));
        when(methodCaseExtractor.extractTestCases(any(CompilationUnit.class), anyString()))
                .thenReturn(List.of(createTestCase()));

        manager.loadTestBodyForClass(ExportClassDedup.class);
        manager.loadTestBodyForClass(ExportClassDedup.class);

        verify(exportSender).sendTestCases(anyList());
    }

    @Test
    @DisplayName("Should not send when file is not found")
    void shouldNotSendWhenFileIsNotFound() {
        when(provider.getProperty(TestNgMethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                .thenReturn("true");
        when(fileFinder.getTestClassFilePath(ExportClassNoFile.class)).thenReturn(null);

        manager.loadTestBodyForClass(ExportClassNoFile.class);

        verify(fileParser, never()).parseFile(anyString());
        verifyNoInteractions(methodCaseExtractor, exportSender);
    }

    @Test
    @DisplayName("Should not send when file parsing fails")
    void shouldNotSendWhenFileParsingFails() {
        when(provider.getProperty(TestNgMethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                .thenReturn("true");
        when(fileFinder.getTestClassFilePath(ExportClassParseError.class))
                .thenReturn("src/test/java/ExportClassParseError.java");
        when(fileParser.parseFile(anyString()))
                .thenThrow(new MethodExporterException("parse failed"));

        manager.loadTestBodyForClass(ExportClassParseError.class);

        verifyNoInteractions(methodCaseExtractor, exportSender);
    }

    @Test
    @DisplayName("Should not send when no test cases extracted")
    void shouldNotSendWhenNoTestCasesExtracted() {
        when(provider.getProperty(TestNgMethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                .thenReturn("true");
        when(fileFinder.getTestClassFilePath(ExportClassEmpty.class))
                .thenReturn("src/test/java/ExportClassEmpty.java");
        when(fileParser.parseFile(anyString())).thenReturn(mock(CompilationUnit.class));
        when(methodCaseExtractor.extractTestCases(any(CompilationUnit.class), anyString()))
                .thenReturn(Collections.emptyList());

        manager.loadTestBodyForClass(ExportClassEmpty.class);

        verify(exportSender, never()).sendTestCases(anyList());
    }

    @Test
    @DisplayName("Should swallow exception for null test class")
    void shouldSwallowExceptionForNullTestClass() {
        when(provider.getProperty(TestNgMethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                .thenReturn("true");

        assertDoesNotThrow(() -> manager.loadTestBodyForClass(null));
        verifyNoInteractions(fileFinder, fileParser, methodCaseExtractor, exportSender);
    }

    private TestNgExporterTestCase createTestCase() {
        TestNgExporterTestCase testCase = new TestNgExporterTestCase();
        testCase.setName("testName");
        testCase.setSuites(List.of("ExportSuite"));
        return testCase;
    }
}

class ExportClassSuccess {
}

class ExportClassNotRequired {
}

class ExportClassDedup {
}

class ExportClassNoFile {
}

class ExportClassParseError {
}

class ExportClassEmpty {
}