package tesromat;

import io.testomat.core.facade.Testomatio;
import io.testomat.testomat.TestomatClientImpl;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class TestomatClientImplTest {

    @Test
    void shouldSendArtifact() {
        TestomatClientImpl client = new TestomatClientImpl();

        try (MockedStatic<Testomatio> testomatio = mockStatic(Testomatio.class)) {
            client.artifact("file.txt");
            testomatio.verify(() -> Testomatio.artifact("file.txt"));
        }
    }

    @Test
    void shouldSendStepArtifact() {
        TestomatClientImpl client = new TestomatClientImpl();

        try (MockedStatic<Testomatio> testomatio = mockStatic(Testomatio.class)) {
            client.stepArtifact("file.txt");
            testomatio.verify(() -> Testomatio.stepArtifact("file.txt"));
        }
    }

}
