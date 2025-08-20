import io.testomat.junit.extractor.strategy.ParameterExtractorService;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DebugTest {
    
    public static void main(String[] args) throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("stringEmptySourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] \"\"");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        // When
        Object result = service.extractParameters(mockContext);

        // Debug output
        System.out.println("Result type: " + (result != null ? result.getClass().getName() : "null"));
        System.out.println("Result value: " + result);
        System.out.println("Is Map: " + (result instanceof Map));
        
        if (result instanceof Map) {
            Map<String, Object> paramMap = (Map<String, Object>) result;
            System.out.println("Map contents: " + paramMap);
            System.out.println("Keys: " + paramMap.keySet());
        }
    }
    
    public static class TestClass {
        @ParameterizedTest
        @EmptySource
        public void stringEmptySourceTest(String inputText) {
            // Real parameterized test method with String parameter
        }
    }
}