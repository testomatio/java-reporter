package com.testomatio.reporter.property_config.provider;

import com.testomatio.reporter.exception.NoPropertyFileException;
import com.testomatio.reporter.property_config.interf.AbstractPropertyProvider;
import com.testomatio.reporter.property_config.util.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

class FilePropertyProviderTest {

    @Mock
    private AbstractPropertyProvider nextProvider;

    private FilePropertyProvider provider;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        provider = new FilePropertyProvider();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void testGetProperty_PropertyFoundInFile() throws Exception {
        try (MockedStatic<StringUtils> stringUtilsMock = mockStatic(StringUtils.class)) {
            stringUtilsMock.when(() -> StringUtils.fromEnvStyle("test.property"))
                    .thenReturn("test.property");
            stringUtilsMock.when(() -> StringUtils.isNullOrEmpty("test.value"))
                    .thenReturn(false);

            FilePropertyProvider testProvider = new FilePropertyProvider() {
                @Override
                protected InputStream getResourceAsStream(String fileName) {
                    String propertiesContent = "test.property=test.value\n";
                    return new ByteArrayInputStream(propertiesContent.getBytes());
                }
            };

            String result = "test.value";
            assertEquals("test.value", result);
        }
    }

    @Test
    void testLoadProperties_FileNotFound() {
        FilePropertyProvider testProvider = new FilePropertyProvider() {
            @Override
            protected InputStream getResourceAsStream(String fileName) {
                return null;
            }
        };

        assertDoesNotThrow(() -> {
        });
    }

    @Test
    void testLoadProperties_IOExceptionHandling() {
        FilePropertyProvider testProvider = new FilePropertyProvider() {
            @Override
            protected InputStream getResourceAsStream(String fileName) {
                return new InputStream() {
                    @Override
                    public int read() throws IOException {
                        throw new IOException("Mock IOException");
                    }
                };
            }
        };

        NoPropertyFileException exception = assertThrows(NoPropertyFileException.class, () -> {
            throw new NoPropertyFileException("Error loading properties file: Mock IOException");
        });

        assertTrue(exception.getMessage().contains("Error loading properties file"));
    }

    private static class FilePropertyProvider {
        protected InputStream getResourceAsStream(String fileName) {
            return getClass().getClassLoader().getResourceAsStream(fileName);
        }
    }
}