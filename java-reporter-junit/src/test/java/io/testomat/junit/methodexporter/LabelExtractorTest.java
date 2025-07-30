package io.testomat.junit.methodexporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import io.testomat.junit.methodexporter.extractors.LabelExtractor;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("LabelExtractor Tests")
class LabelExtractorTest {

    @Mock
    private MethodDeclaration mockMethodDeclaration;
    
    @Mock
    private Comment mockComment;
    
    private LabelExtractor labelExtractor;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        labelExtractor = new LabelExtractor();
    }

    @Nested
    @DisplayName("Extract Labels Tests")
    class ExtractLabelsTests {
        
        @Test
        @DisplayName("Should extract labels from annotations, comments and method name")
        void shouldExtractLabelsFromAnnotationsCommentsAndMethodName() {
            // Given
            AnnotationExpr testAnnotation = createMockAnnotation("Test");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(testAnnotation);
            
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            when(mockMethodDeclaration.getComment()).thenReturn(Optional.of(mockComment));
            when(mockComment.getContent()).thenReturn("This is a test @smoke #fast");
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testIntegrationFeature");
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("unit")); // from @Test annotation
            assertTrue(result.contains("smoke")); // from comment @smoke
            assertTrue(result.contains("fast")); // from comment #fast
            assertTrue(result.contains("integration")); // from method name
        }
        
        @Test
        @DisplayName("Should return empty list when method has no annotations, comments or patterns")
        void shouldReturnEmptyListWhenMethodHasNoAnnotationsCommentsOrPatterns() {
            // Given
            when(mockMethodDeclaration.getAnnotations()).thenReturn(new NodeList<>());
            when(mockMethodDeclaration.getComment()).thenReturn(Optional.empty());
            when(mockMethodDeclaration.getNameAsString()).thenReturn("plainTestMethod");
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Should return empty list when exception occurs")
        void shouldReturnEmptyListWhenExceptionOccurs() {
            // Given
            when(mockMethodDeclaration.getAnnotations())
                .thenThrow(new RuntimeException("Unexpected error"));
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Annotation Labels Tests")
    class AnnotationLabelsTests {
        
        @Test
        @DisplayName("Should extract unit label from Test annotation")
        void shouldExtractUnitLabelFromTestAnnotation() {
            // Given
            AnnotationExpr testAnnotation = createMockAnnotation("Test");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(testAnnotation);
            
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            when(mockMethodDeclaration.getComment()).thenReturn(Optional.empty());
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testMethod");
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("unit"));
        }
        
        @Test
        @DisplayName("Should extract integration label from SpringBootTest annotation")
        void shouldExtractIntegrationLabelFromSpringBootTestAnnotation() {
            // Given
            AnnotationExpr springBootTestAnnotation = createMockAnnotation("SpringBootTest");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(springBootTestAnnotation);
            
            setupBasicMocks();
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("integration"));
        }
        
        @Test
        @DisplayName("Should extract integration label from IntegrationTest annotation")
        void shouldExtractIntegrationLabelFromIntegrationTestAnnotation() {
            // Given
            AnnotationExpr integrationTestAnnotation = createMockAnnotation("IntegrationTest");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(integrationTestAnnotation);
            
            setupBasicMocks();
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("integration"));
        }
        
        @Test
        @DisplayName("Should extract parameterized label from ParameterizedTest annotation")
        void shouldExtractParameterizedLabelFromParameterizedTestAnnotation() {
            // Given
            AnnotationExpr parameterizedTestAnnotation = createMockAnnotation("ParameterizedTest");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(parameterizedTestAnnotation);
            
            setupBasicMocks();
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("parameterized"));
        }
        
        @Test
        @DisplayName("Should extract repeated label from RepeatedTest annotation")
        void shouldExtractRepeatedLabelFromRepeatedTestAnnotation() {
            // Given
            AnnotationExpr repeatedTestAnnotation = createMockAnnotation("RepeatedTest");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(repeatedTestAnnotation);
            
            setupBasicMocks();
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("repeated"));
        }
        
        @Test
        @DisplayName("Should extract dynamic label from TestFactory annotation")
        void shouldExtractDynamicLabelFromTestFactoryAnnotation() {
            // Given
            AnnotationExpr testFactoryAnnotation = createMockAnnotation("TestFactory");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(testFactoryAnnotation);
            
            setupBasicMocks();
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("dynamic"));
        }
        
        @Test
        @DisplayName("Should extract disabled label from Disabled annotation")
        void shouldExtractDisabledLabelFromDisabledAnnotation() {
            // Given
            AnnotationExpr disabledAnnotation = createMockAnnotation("Disabled");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(disabledAnnotation);
            
            setupBasicMocks();
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("disabled"));
        }
        
        @Test
        @DisplayName("Should extract disabled label from Ignore annotation")
        void shouldExtractDisabledLabelFromIgnoreAnnotation() {
            // Given
            AnnotationExpr ignoreAnnotation = createMockAnnotation("Ignore");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(ignoreAnnotation);
            
            setupBasicMocks();
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("disabled"));
        }
        
        @Test
        @DisplayName("Should extract timeout label from Timeout annotation")
        void shouldExtractTimeoutLabelFromTimeoutAnnotation() {
            // Given
            AnnotationExpr timeoutAnnotation = createMockAnnotation("Timeout");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(timeoutAnnotation);
            
            setupBasicMocks();
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("timeout"));
        }
        
        @Test
        @DisplayName("Should extract web label from WebMvcTest annotation")
        void shouldExtractWebLabelFromWebMvcTestAnnotation() {
            // Given
            AnnotationExpr webMvcTestAnnotation = createMockAnnotation("WebMvcTest");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(webMvcTestAnnotation);
            
            setupBasicMocks();
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("web"));
        }
        
        @Test
        @DisplayName("Should extract jpa label from DataJpaTest annotation")
        void shouldExtractJpaLabelFromDataJpaTestAnnotation() {
            // Given
            AnnotationExpr dataJpaTestAnnotation = createMockAnnotation("DataJpaTest");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(dataJpaTestAnnotation);
            
            setupBasicMocks();
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("jpa"));
        }
        
        @Test
        @DisplayName("Should extract json label from JsonTest annotation")
        void shouldExtractJsonLabelFromJsonTestAnnotation() {
            // Given
            AnnotationExpr jsonTestAnnotation = createMockAnnotation("JsonTest");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(jsonTestAnnotation);
            
            setupBasicMocks();
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("json"));
        }
        
        @Test
        @DisplayName("Should extract custom label from annotation ending with Test")
        void shouldExtractCustomLabelFromAnnotationEndingWithTest() {
            // Given
            AnnotationExpr customTestAnnotation = createMockAnnotation("PerformanceTest");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(customTestAnnotation);
            
            setupBasicMocks();
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("performance"));
        }
        
        @Test
        @DisplayName("Should ignore unknown annotations")
        void shouldIgnoreUnknownAnnotations() {
            // Given
            AnnotationExpr unknownAnnotation = createMockAnnotation("SomeRandomAnnotation");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(unknownAnnotation);
            
            setupBasicMocks();
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Tag Annotation Tests")
    class TagAnnotationTests {
        
        @Test
        @DisplayName("Should extract value from Tag annotation with SingleMemberAnnotationExpr")
        void shouldExtractValueFromTagAnnotationWithSingleMember() {
            // Given
            SingleMemberAnnotationExpr tagAnnotation = mock(SingleMemberAnnotationExpr.class);
            Expression memberValue = mock(Expression.class);
            StringLiteralExpr stringLiteral = mock(StringLiteralExpr.class);
            
            when(tagAnnotation.getNameAsString()).thenReturn("Tag");
            when(tagAnnotation.getMemberValue()).thenReturn(memberValue);
            when(memberValue.asStringLiteralExpr()).thenReturn(stringLiteral);
            when(stringLiteral.getValue()).thenReturn("smoke");
            
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(tagAnnotation);
            
            setupBasicMocks();
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("smoke"));
        }
        
        @Test
        @DisplayName("Should extract value from Tag annotation with NormalAnnotationExpr")
        void shouldExtractValueFromTagAnnotationWithNormalAnnotation() {
            // Given
            NormalAnnotationExpr tagAnnotation = mock(NormalAnnotationExpr.class);
            MemberValuePair valuePair = mock(MemberValuePair.class);
            Expression valueExpr = mock(Expression.class);
            StringLiteralExpr stringLiteral = mock(StringLiteralExpr.class);
            NodeList<MemberValuePair> pairs = new NodeList<>();
            pairs.add(valuePair);
            
            when(tagAnnotation.getNameAsString()).thenReturn("Tag");
            when(tagAnnotation.getPairs()).thenReturn(pairs);
            when(valuePair.getNameAsString()).thenReturn("value");
            when(valuePair.getValue()).thenReturn(valueExpr);
            when(valueExpr.asStringLiteralExpr()).thenReturn(stringLiteral);
            when(stringLiteral.getValue()).thenReturn("integration");
            
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(tagAnnotation);
            
            setupBasicMocks();
            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("integration"));
        }
    }

    @Nested
    @DisplayName("Comment Labels Tests")
    class CommentLabelsTests {
        
        @Test
        @DisplayName("Should extract labels from comment with @tag:value pattern")
        void shouldExtractLabelsFromCommentWithTagValuePattern() {
            // Given
            setupBasicMocks();
            when(mockMethodDeclaration.getComment()).thenReturn(Optional.of(mockComment));
            when(mockComment.getContent()).thenReturn("This test checks @priority:high feature");
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("priority:high"));
        }
        
        @Test
        @DisplayName("Should extract labels from comment with @tag pattern")
        void shouldExtractLabelsFromCommentWithTagPattern() {
            // Given
            setupBasicMocks();
            when(mockMethodDeclaration.getComment()).thenReturn(Optional.of(mockComment));
            when(mockComment.getContent()).thenReturn("This is a @smoke test for the feature");
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("smoke"));
        }
        
        @Test
        @DisplayName("Should extract labels from comment with #tag pattern")
        void shouldExtractLabelsFromCommentWithHashTagPattern() {
            // Given
            setupBasicMocks();
            when(mockMethodDeclaration.getComment()).thenReturn(Optional.of(mockComment));
            when(mockComment.getContent()).thenReturn("This is a fast test #fast #regression");
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("fast"));
            assertTrue(result.contains("regression"));
        }
        
        @Test
        @DisplayName("Should extract multiple labels from comment with mixed patterns")
        void shouldExtractMultipleLabelsFromCommentWithMixedPatterns() {
            // Given
            setupBasicMocks();
            when(mockMethodDeclaration.getComment()).thenReturn(Optional.of(mockComment));
            when(mockComment.getContent()).thenReturn("Test @priority:high @smoke #fast #regression");
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("priority:high"));
            assertTrue(result.contains("smoke"));
            assertTrue(result.contains("fast"));
            assertTrue(result.contains("regression"));
        }
        
        @Test
        @DisplayName("Should return empty when comment has no patterns")
        void shouldReturnEmptyWhenCommentHasNoPatterns() {
            // Given
            setupBasicMocks();
            when(mockMethodDeclaration.getComment()).thenReturn(Optional.of(mockComment));
            when(mockComment.getContent()).thenReturn("This is just a regular comment without any patterns");
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle empty comment")
        void shouldHandleEmptyComment() {
            // Given
            setupBasicMocks();
            when(mockMethodDeclaration.getComment()).thenReturn(Optional.of(mockComment));
            when(mockComment.getContent()).thenReturn("");
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Name Pattern Labels Tests")
    class NamePatternLabelsTests {
        
        @Test
        @DisplayName("Should extract integration label from method name containing integration")
        void shouldExtractIntegrationLabelFromMethodNameContainingIntegration() {
            // Given
            setupBasicMocks();
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testIntegrationFeature");
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("integration"));
        }
        
        @Test
        @DisplayName("Should extract integration label case insensitive")
        void shouldExtractIntegrationLabelCaseInsensitive() {
            // Given
            setupBasicMocks();
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testINTEGRATIONFeature");
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("integration"));
        }
        
        @Test
        @DisplayName("Should extract smoke label from method name containing smoke")
        void shouldExtractSmokeLabelFromMethodNameContainingSmoke() {
            // Given
            setupBasicMocks();
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testSmokeScenario");
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("smoke"));
        }
        
        @Test
        @DisplayName("Should extract performance label from method name containing performance")
        void shouldExtractPerformanceLabelFromMethodNameContainingPerformance() {
            // Given
            setupBasicMocks();
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testPerformanceUnderLoad");
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("performance"));
        }
        
        @Test
        @DisplayName("Should extract multiple labels from method name with multiple keywords")
        void shouldExtractMultipleLabelsFromMethodNameWithMultipleKeywords() {
            // Given
            setupBasicMocks();
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testIntegrationSmokePerformance");
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.contains("integration"));
            assertTrue(result.contains("smoke"));
            assertTrue(result.contains("performance"));
        }
        
        @Test
        @DisplayName("Should return empty when method name has no keywords")
        void shouldReturnEmptyWhenMethodNameHasNoKeywords() {
            // Given
            setupBasicMocks();
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testRegularFeature");
            
            // When
            List<String> result = labelExtractor.extractLabels(mockMethodDeclaration);
            
            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Annotation Value Tests")
    class GetAnnotationValueTests {
        
        @Test
        @DisplayName("Should return value from SingleMemberAnnotationExpr")
        void shouldReturnValueFromSingleMemberAnnotationExpr() {
            // Given
            SingleMemberAnnotationExpr annotation = mock(SingleMemberAnnotationExpr.class);
            Expression memberValue = mock(Expression.class);
            StringLiteralExpr stringLiteral = mock(StringLiteralExpr.class);
            
            when(annotation.getMemberValue()).thenReturn(memberValue);
            when(memberValue.asStringLiteralExpr()).thenReturn(stringLiteral);
            when(stringLiteral.getValue()).thenReturn("testValue");
            
            // When
            String result = labelExtractor.getAnnotationValue(annotation);
            
            // Then
            assertEquals("testValue", result);
        }
        
        @Test
        @DisplayName("Should return value from NormalAnnotationExpr with value parameter")
        void shouldReturnValueFromNormalAnnotationExprWithValueParameter() {
            // Given
            NormalAnnotationExpr annotation = mock(NormalAnnotationExpr.class);
            MemberValuePair valuePair = mock(MemberValuePair.class);
            Expression valueExpr = mock(Expression.class);
            StringLiteralExpr stringLiteral = mock(StringLiteralExpr.class);
            NodeList<MemberValuePair> pairs = new NodeList<>();
            pairs.add(valuePair);
            
            when(annotation.getPairs()).thenReturn(pairs);
            when(valuePair.getNameAsString()).thenReturn("value");
            when(valuePair.getValue()).thenReturn(valueExpr);
            when(valueExpr.asStringLiteralExpr()).thenReturn(stringLiteral);
            when(stringLiteral.getValue()).thenReturn("normalValue");
            
            // When
            String result = labelExtractor.getAnnotationValue(annotation);
            
            // Then
            assertEquals("normalValue", result);
        }
        
        @Test
        @DisplayName("Should return null from NormalAnnotationExpr without value parameter")
        void shouldReturnNullFromNormalAnnotationExprWithoutValueParameter() {
            // Given
            NormalAnnotationExpr annotation = mock(NormalAnnotationExpr.class);
            MemberValuePair otherPair = mock(MemberValuePair.class);
            NodeList<MemberValuePair> pairs = new NodeList<>();
            pairs.add(otherPair);
            
            when(annotation.getPairs()).thenReturn(pairs);
            when(otherPair.getNameAsString()).thenReturn("name"); // Not "value"
            
            // When
            String result = labelExtractor.getAnnotationValue(annotation);
            
            // Then
            assertEquals(null, result);
        }
        
        @Test
        @DisplayName("Should return null for unknown annotation type")
        void shouldReturnNullForUnknownAnnotationType() {
            // Given
            AnnotationExpr annotation = mock(AnnotationExpr.class);
            
            // When
            String result = labelExtractor.getAnnotationValue(annotation);
            
            // Then
            assertEquals(null, result);
        }
    }

    // Helper methods
    private AnnotationExpr createMockAnnotation(String name) {
        AnnotationExpr annotation = mock(AnnotationExpr.class);
        when(annotation.getNameAsString()).thenReturn(name);
        return annotation;
    }
    
    private void setupBasicMocks() {
        when(mockMethodDeclaration.getComment()).thenReturn(Optional.empty());
        when(mockMethodDeclaration.getNameAsString()).thenReturn("testMethod");
        when(mockMethodDeclaration.getAnnotations()).thenReturn(new NodeList<>());
    }
}