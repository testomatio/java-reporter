package aspect;

import io.testomat.allure.AllureClient;
import io.testomat.aspect.AllureAttachmentAspect;
import io.testomat.resolver.AttachmentFileResolver;
import io.testomat.testomat.TestomatClient;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AllureAttachmentAspectTest {

    AllureClient allure;
    TestomatClient testomatio;
    AttachmentFileResolver resolver;
    AllureAttachmentAspect aspect;

    @BeforeEach
    void setup() throws Exception {
        clearStaticState();
        allure = mock(AllureClient.class);
        testomatio = mock(TestomatClient.class);
        resolver = mock(AttachmentFileResolver.class);
        aspect = new AllureAttachmentAspect(allure, testomatio, resolver);
    }

    void clearStaticState() throws Exception {
        Field attachments = AllureAttachmentAspect.class.getDeclaredField("attachments");
        attachments.setAccessible(true);
        ((Map<?, ?>) attachments.get(null)).clear();
    }

    @Test
    void shouldCreateTestLevelMeta() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(allure.getCurrentTest()).thenReturn(Optional.of("test"));
        when(allure.getCurrentTestOrStep()).thenReturn(Optional.of("test"));
        when(joinPoint.getArgs()).thenReturn(new Object[]{"file", "text/plain"});
        when(joinPoint.proceed()).thenReturn("uuid");

        Object result = aspect.interceptPrepare(joinPoint);

        assertThat(result).isEqualTo("uuid");
    }

    @Test
    void shouldCreateStepLevelMeta() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(allure.getCurrentTest()).thenReturn(Optional.of("test"));
        when(allure.getCurrentTestOrStep()).thenReturn(Optional.of("step"));
        when(joinPoint.getArgs()).thenReturn(new Object[]{"file", "text/plain"});
        when(joinPoint.proceed()).thenReturn("uuid");

        aspect.interceptPrepare(joinPoint);
    }

    @Test
    void shouldCreateFixtureLevelMeta() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(allure.getCurrentTest()).thenReturn(Optional.empty());
        when(allure.getCurrentTestOrStep()).thenReturn(Optional.empty());
        when(joinPoint.getArgs()).thenReturn(new Object[]{"file", "text/plain"});
        when(joinPoint.proceed()).thenReturn("uuid");

        aspect.interceptPrepare(joinPoint);
    }

    @Test
    void shouldHandleByteAttachment() throws Throwable {
        String uuid = "uuid";
        ProceedingJoinPoint prepare = mock(ProceedingJoinPoint.class);

        when(allure.getCurrentTest()).thenReturn(Optional.of("test"));
        when(allure.getCurrentTestOrStep()).thenReturn(Optional.of("test"));
        when(prepare.getArgs()).thenReturn(new Object[]{"file", "text/plain"});
        when(prepare.proceed()).thenReturn(uuid);
        aspect.interceptPrepare(prepare);
        ProceedingJoinPoint write = mock(ProceedingJoinPoint.class);
        when(write.getArgs()).thenReturn(new Object[]{uuid, "data".getBytes()});
        when(write.proceed()).thenReturn(null);
        when(resolver.find(uuid)).thenReturn("file.txt");
        aspect.interceptWrite(write);

        verify(resolver).find(uuid);
        verify(testomatio).artifact("file.txt");
    }

    @Test
    void shouldHandleStreamAttachment() throws Throwable {
        String uuid = "uuid";
        ProceedingJoinPoint prepare = mock(ProceedingJoinPoint.class);

        when(allure.getCurrentTest()).thenReturn(Optional.of("test"));
        when(allure.getCurrentTestOrStep()).thenReturn(Optional.of("step"));
        when(prepare.getArgs()).thenReturn(new Object[]{"file", "text/plain"});
        when(prepare.proceed()).thenReturn(uuid);
        aspect.interceptPrepare(prepare);
        ProceedingJoinPoint write = mock(ProceedingJoinPoint.class);
        when(write.getArgs()).thenReturn(new Object[]{uuid, new ByteArrayInputStream("data".getBytes())});
        when(write.proceed()).thenReturn(null);
        when(resolver.find(uuid)).thenReturn("file.txt");
        aspect.interceptWrite(write);

        verify(testomatio).stepArtifact("file.txt");
    }

    @Test
    void shouldIgnoreUnknownAttachment() throws Throwable {
        ProceedingJoinPoint write = mock(ProceedingJoinPoint.class);

        when(write.getArgs()).thenReturn(new Object[]{"unknown", "data".getBytes()});
        when(write.proceed()).thenReturn(null);
        Object result = aspect.interceptWrite(write);

        assertThat(result).isNull();
        verifyNoInteractions(testomatio);
    }

    @Test
    void shouldNotSendFixtureAttachment() throws Throwable {
        String uuid = "uuid";

        ProceedingJoinPoint prepare = mock(ProceedingJoinPoint.class);
        when(allure.getCurrentTest()).thenReturn(Optional.empty());
        when(allure.getCurrentTestOrStep()).thenReturn(Optional.empty());
        when(prepare.getArgs()).thenReturn(new Object[]{"file", "text/plain"});
        when(prepare.proceed()).thenReturn(uuid);

        aspect.interceptPrepare(prepare);

        ProceedingJoinPoint write = mock(ProceedingJoinPoint.class);
        when(write.getArgs()).thenReturn(new Object[]{uuid, "data".getBytes()});
        when(write.proceed()).thenReturn(null);
        when(resolver.find(uuid)).thenReturn("file.txt");

        aspect.interceptWrite(write);

        verifyNoInteractions(testomatio);
    }

    @Test
    void shouldRemoveAttachmentAfterWrite() throws Throwable {
        String uuid = "uuid";

        ProceedingJoinPoint prepare = mock(ProceedingJoinPoint.class);

        when(allure.getCurrentTest()).thenReturn(Optional.of("test"));
        when(allure.getCurrentTestOrStep()).thenReturn(Optional.of("test"));
        when(prepare.getArgs()).thenReturn(new Object[]{"file", "text/plain"});
        when(prepare.proceed()).thenReturn(uuid);

        aspect.interceptPrepare(prepare);

        ProceedingJoinPoint write = mock(ProceedingJoinPoint.class);
        when(write.getArgs()).thenReturn(new Object[]{uuid, "data".getBytes()});
        when(write.proceed()).thenReturn(null);
        when(resolver.find(uuid)).thenReturn("file.txt");

        aspect.interceptWrite(write);

        Field field = AllureAttachmentAspect.class.getDeclaredField("attachments");
        field.setAccessible(true);

        Map<?, ?> attachments = (Map<?, ?>) field.get(null);

        assertThat(attachments.containsKey(uuid)).isFalse();
    }

    @Test
    void shouldReturnProceedResultFromWrite() throws Throwable {
        String uuid = "uuid";

        ProceedingJoinPoint prepare = mock(ProceedingJoinPoint.class);

        when(allure.getCurrentTest()).thenReturn(Optional.of("test"));
        when(allure.getCurrentTestOrStep()).thenReturn(Optional.of("test"));
        when(prepare.getArgs()).thenReturn(new Object[]{"file", "text/plain"});
        when(prepare.proceed()).thenReturn(uuid);

        aspect.interceptPrepare(prepare);

        ProceedingJoinPoint write = mock(ProceedingJoinPoint.class);
        Object expected = new Object();

        when(write.getArgs()).thenReturn(new Object[]{uuid, "data".getBytes()});
        when(write.proceed()).thenReturn(expected);
        when(resolver.find(uuid)).thenReturn("file.txt");

        Object result = aspect.interceptWrite(write);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void shouldNotResolveUnknownAttachment() throws Throwable {
        ProceedingJoinPoint write = mock(ProceedingJoinPoint.class);

        when(write.getArgs()).thenReturn(new Object[]{"unknown", "data".getBytes()});
        when(write.proceed()).thenReturn(null);

        aspect.interceptWrite(write);

        verifyNoInteractions(resolver);
        verifyNoInteractions(testomatio);
    }

    @Test
    void shouldReturnOriginalFileWhenMimeTypeIsInvalid() throws Exception {
        String result = invokeAddExtension("file", "invalid/type");

        assertThat(result).isEqualTo("file");
    }

    @Test
    void shouldReturnOriginalFileWhenSourceDoesNotExist() throws Exception {
        String file = "not-existing-file";

        String result = invokeAddExtension(file, "text/plain");

        assertThat(result).isEqualTo(file);
    }

    @Test
    void shouldNotAddExtensionWhenAlreadyPresent() throws Exception {
        String result = invokeAddExtension("file.txt", "text/plain");

        assertThat(result).isEqualTo("file.txt");
    }

    @Test
    void shouldCreateCopyWithExtension() throws Exception {
        Path source = Files.createTempFile("attachment", "");

        String result = invokeAddExtension(source.toString(), "text/plain");

        assertThat(result).endsWith(".txt");
        assertThat(Files.exists(Path.of(result))).isTrue();

        Files.deleteIfExists(source);
        Files.deleteIfExists(Path.of(result));
    }

    private String invokeAddExtension(String fileName, String mimeType) throws Exception {
        Method method = AllureAttachmentAspect.class
            .getDeclaredMethod("addExtension", String.class, String.class);

        method.setAccessible(true);

        return (String) method.invoke(aspect, fileName, mimeType);
    }

}