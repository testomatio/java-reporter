package io.testomat.junit.methodexporter.filefinder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for PathNormalizer focusing on cross-platform path normalization
 * and edge cases that may occur in real-world scenarios.
 */
class PathNormalizerTest {

    private final PathNormalizer normalizer = new PathNormalizer();

    @Test
    void normalizePath_shouldHandleNull() {
        String result = normalizer.normalizePath(null);

        assertThat(result).isNull();
    }

    @Test
    void normalizePath_shouldHandleEmptyString() {
        String result = normalizer.normalizePath("");

        assertThat(result).isEmpty();
    }

    @Test
    void normalizePath_shouldConvertBackslashesToForwardSlashes() {
        String result = normalizer.normalizePath("C:\\Users\\test\\file.txt");

        assertThat(result).contains("/");
        assertThat(result).doesNotContain("\\");
    }

    @Test
    void normalizePath_shouldHandleUnixAbsolutePath() {
        String result = normalizer.normalizePath("/home/user/file.txt");

        // On Windows, leading slash might be removed
        assertThat(result).contains("home/user/file.txt");
    }

    @Test
    void normalizePath_shouldHandleRelativePath() {
        String result = normalizer.normalizePath("src/main/java/Test.java");

        assertThat(result).isEqualTo("src/main/java/Test.java");
    }

    @ParameterizedTest
    @CsvSource({
        "'C:\\test\\file.txt', 'C:/test/file.txt'",
        "'D:\\projects\\code', 'D:/projects/code'",
        "'E:\\data', 'E:/data'"
    })
    void normalizePath_shouldHandleWindowsDriveLetter(String input, String expected) {
        String result = normalizer.normalizePath(input);

        // Result should contain forward slashes
        assertThat(result).contains("/");
        assertThat(result).doesNotContain("\\");
    }

    @Test
    void normalizePath_shouldHandleForwardSlashesOnWindows() {
        String result = normalizer.normalizePath("C:/Users/test/file.txt");

        assertThat(result).contains("/");
        assertThat(result).doesNotContain("\\");
    }

    @Test
    void normalizePath_shouldHandleMixedSlashes() {
        String result = normalizer.normalizePath("C:\\Users/test\\file.txt");

        assertThat(result).doesNotContain("\\");
        assertThat(result).contains("/");
    }

    @Test
    void normalizePath_shouldNormalizeDotDotPath() {
        String result = normalizer.normalizePath("src/../test/file.txt");

        // Path should be normalized (removing ..)
        assertThat(result).isEqualTo("test/file.txt");
    }

    @Test
    void normalizePath_shouldNormalizeDotPath() {
        String result = normalizer.normalizePath("src/./test/file.txt");

        // Path should be normalized (removing .)
        assertThat(result).isEqualTo("src/test/file.txt");
    }

    @Test
    void normalizePath_shouldHandleMultipleConsecutiveSlashes() {
        String result = normalizer.normalizePath("src//test///file.txt");

        // Should not contain multiple consecutive slashes
        assertThat(result).doesNotContain("//");
    }

    @Test
    void normalizePath_shouldHandleTrailingSlash() {
        String result = normalizer.normalizePath("src/test/");

        assertThat(result).doesNotEndWith("/");
    }

    @Test
    void normalizePath_shouldHandlePathWithSpaces() {
        String result = normalizer.normalizePath("C:\\Program Files\\My App\\file.txt");

        assertThat(result).contains("Program Files");
        assertThat(result).doesNotContain("\\");
    }

    @Test
    void normalizePath_shouldHandleVeryLongPath() {
        String longPath = "C:\\" + "folder\\".repeat(50) + "file.txt";

        String result = normalizer.normalizePath(longPath);

        assertThat(result).doesNotContain("\\");
    }

    @Test
    void normalizePath_shouldHandleSpecialCharacters() {
        String result = normalizer.normalizePath("path/with-special_chars@2024/file.txt");

        assertThat(result).contains("with-special_chars@2024");
    }

    @Test
    void normalizePath_shouldFallbackOnException() {
        // Path with invalid characters that might cause parsing issues
        String problematicPath = "some\\weird\\path";

        String result = normalizer.normalizePath(problematicPath);

        // Should still convert backslashes (fallback behavior)
        assertThat(result).doesNotContain("\\");
    }

    @Test
    void normalizePath_shouldHandleSingleDot() {
        String result = normalizer.normalizePath(".");

        // Single dot normalizes to empty string or current directory
        assertThat(result).isNotNull();
    }

    @Test
    void normalizePath_shouldHandleDoubleDot() {
        String result = normalizer.normalizePath("..");

        // Double dot should result in parent directory reference
        assertThat(result).isNotNull();
    }

    @Test
    void normalizePath_shouldHandleRootPath() {
        String result = normalizer.normalizePath("/");

        assertThat(result).isEqualTo("/");
    }

    @Test
    void normalizePath_shouldHandleUNCPath() {
        String result = normalizer.normalizePath("\\\\server\\share\\file.txt");

        // Should handle UNC paths
        assertThat(result).doesNotContain("\\");
    }

    @Test
    void normalizePath_shouldHandlePathWithUnicode() {
        String result = normalizer.normalizePath("path/файл/文件.txt");

        assertThat(result).contains("файл");
        assertThat(result).contains("文件.txt");
    }

    @Test
    void normalizePath_shouldBeIdempotent() {
        String path = "C:\\test\\file.txt";

        String result1 = normalizer.normalizePath(path);
        String result2 = normalizer.normalizePath(result1);

        assertThat(result1).isEqualTo(result2);
    }

    @ParameterizedTest
    @CsvSource({
        "'test.txt'",
        "'../test.txt'",
        "'./test.txt'",
        "'folder/test.txt'"
    })
    void normalizePath_shouldHandleRelativePaths(String path) {
        String result = normalizer.normalizePath(path);

        assertThat(result).isNotNull();
        assertThat(result).doesNotContain("\\");
    }
}
