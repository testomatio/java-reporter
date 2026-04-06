package aspect;

import io.testomat.allure.AllureClient;
import io.testomat.aspect.AllureAttachmentAspect;
import io.testomat.resolver.AttachmentFileResolver;
import io.testomat.testomat.TestomatClient;
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

        Field threadLocal = AllureAttachmentAspect.class.getDeclaredField("userAttachment");
        threadLocal.setAccessible(true);
        ThreadLocal<?> tl = (ThreadLocal<?>) threadLocal.get(null);
        tl.remove();
    }

    @Test
    void shouldInterceptUserAttachment() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(joinPoint.proceed()).thenReturn("ok");
        Object result = aspect.interceptUserAttachment(joinPoint);

        assertThat(result).isEqualTo("ok");
        verify(joinPoint).proceed();
    }

    @Test
    void shouldCreateTestLevelMeta() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(allure.getCurrentTest()).thenReturn(Optional.of("test"));
        when(allure.getCurrentTestOrStep()).thenReturn(Optional.of("test"));
        when(joinPoint.getArgs()).thenReturn(new Object[]{"file", "text/plain"});
        when(joinPoint.proceed()).thenReturn("uuid");

        aspect.interceptUserAttachment(joinPoint);
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

        aspect.interceptUserAttachment(joinPoint);
        aspect.interceptPrepare(joinPoint);
    }

    @Test
    void shouldCreateFixtureLevelMeta() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(allure.getCurrentTest()).thenReturn(Optional.empty());
        when(allure.getCurrentTestOrStep()).thenReturn(Optional.empty());
        when(joinPoint.getArgs()).thenReturn(new Object[]{"file", "text/plain"});
        when(joinPoint.proceed()).thenReturn("uuid");

        aspect.interceptUserAttachment(joinPoint);
        aspect.interceptPrepare(joinPoint);
    }

    @Test
    void shouldHandleByteAttachment() throws Throwable {
        String uuid = "uuid";
        Field field = AllureAttachmentAspect.class.getDeclaredField("userAttachment");
        field.setAccessible(true);
        ThreadLocal<Boolean> tl = (ThreadLocal<Boolean>) field.get(null);
        tl.set(true);

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

        tl.remove();
    }

    @Test
    void shouldHandleStreamAttachment() throws Throwable {
        String uuid = "uuid";
        Field field = AllureAttachmentAspect.class.getDeclaredField("userAttachment");
        field.setAccessible(true);
        ThreadLocal<Boolean> tl = (ThreadLocal<Boolean>) field.get(null);
        tl.set(true);

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

        tl.remove();
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

}