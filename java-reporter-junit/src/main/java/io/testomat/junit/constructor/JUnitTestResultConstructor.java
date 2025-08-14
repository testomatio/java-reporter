package io.testomat.junit.constructor;

import io.testomat.core.model.ExceptionDetails;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JUnitTestResultConstructor {

    private static final Logger log = LoggerFactory.getLogger(JUnitTestResultConstructor.class);
    private final JunitMetaDataExtractor metaDataExtractor;

    public JUnitTestResultConstructor() {
        this.metaDataExtractor = new JunitMetaDataExtractor();
    }

    public JUnitTestResultConstructor(JunitMetaDataExtractor metaDataExtractor) {
        this.metaDataExtractor = metaDataExtractor;
    }

    /**
     * Constructs test result with enhanced parameterized test support.
     * Follows testomat.io documentation for example field format.
     */
    public TestResult constructTestRunResult(TestMetadata metadata, String message,
                                             String status, ExtensionContext context) {
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata is null");
        }

        String stack;
        Object example = null;
        String rid = null;

        // Handle parameterized tests
        
        if (metaDataExtractor.isParameterizedTest(context)) {
            
            example = metaDataExtractor.extractTestParameters(context);
            if (example != null) {
                rid = context.getUniqueId();
            } else {
            }
        } else {
        }

        // Handle exception details
        if (message != null) {
            stack = extractStackTrace(context);
        } else {
            ExceptionDetails details = extractExceptionDetails(context);
            message = details.getMessage();
            stack = details.getStack();
        }

        return createTestResult(metadata, message, status, stack, example, rid);
    }

    /**
     * Creates TestResult with conditional example and rid fields.
     */
    private TestResult createTestResult(TestMetadata metadata, String message, String status,
                                        String stack, Object example, String rid) {
        TestResult.Builder builder = TestResult.builder()
                .withSuiteTitle(metadata.getSuiteTitle())
                .withTestId(metadata.getTestId())
                .withTitle(metadata.getTitle())
                .withFile(metadata.getFile())
                .withMessage(message)
                .withStatus(status)
                .withStack(stack);

        // Only add example and rid for parameterized tests
        if (example != null) {
            builder.withExample(example);
        } else {
        }

        if (rid != null) {
            builder.withRid(rid);
        } else {
        }

        TestResult result = builder.build();
        return result;
    }

    private ExceptionDetails extractExceptionDetails(ExtensionContext context) {
        return Optional.ofNullable(context)
                .flatMap(ExtensionContext::getExecutionException)
                .filter(this::isReportableException)
                .map(this::createExceptionDetails)
                .orElse(ExceptionDetails.empty());
    }

    private String extractStackTrace(ExtensionContext context) {
        return Optional.ofNullable(context)
                .flatMap(ExtensionContext::getExecutionException)
                .filter(this::isReportableException)
                .map(this::getStackTrace)
                .orElse(null);
    }

    private ExceptionDetails createExceptionDetails(Throwable throwable) {
        String message = throwable.getMessage();
        String stack = getStackTrace(throwable);
        return new ExceptionDetails(message, stack);
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private boolean isReportableException(Throwable throwable) {
        return !(throwable instanceof TestAbortedException);
    }

    /**
     * Formats the example parameter for console display, handling special cases.
     */
    private String formatExampleForConsole(Object example) {
        if (example == null) {
            return "NULL";
        }
        
        if (example instanceof String) {
            String str = (String) example;
            if (str.isEmpty()) {
                return "EMPTY_STRING (\"\")";
            }
            
            // Replace invisible characters with visible representations
            String display = str
                .replace(" ", "·")        // space -> middle dot
                .replace("\t", "→")       // tab -> arrow
                .replace("\n", "↵")       // newline -> return symbol
                .replace("\r", "⤶");      // carriage return -> symbol
            
            if (str.isBlank()) {
                return "WHITESPACE (\"" + display + "\") [length=" + str.length() + "]";
            }
            
            return "\"" + display + "\"";
        }
        
        if (example instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) example;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (java.util.Map.Entry<String, Object> entry : map.entrySet()) {
                if (!first) sb.append(", ");
                sb.append(entry.getKey()).append(": ");
                sb.append(formatExampleForConsole(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        
        return example.toString() + " (" + example.getClass().getSimpleName() + ")";
    }
}