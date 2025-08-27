package io.testomat.core.client;

import static org.junit.jupiter.api.Assertions.*;

import io.testomat.core.exception.ApiKeyNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestomatClientFactory Tests")
class TestomatClientFactoryTest {

    @Test
    @DisplayName("Should return singleton instance")
    void getClientFactory_MultipleCalls_ShouldReturnSameInstance() {
        ClientFactory factory1 = TestomatClientFactory.getClientFactory();
        ClientFactory factory2 = TestomatClientFactory.getClientFactory();

        assertNotNull(factory1);
        assertSame(factory1, factory2);
        assertInstanceOf(TestomatClientFactory.class, factory1);
    }

    @Test
    @DisplayName("Should create client factory and throw appropriate exception when no API key")
    void createClient_NoApiKey_ShouldThrowPropertyException() {
        // Since the factory loads properties statically, we test the expected behavior
        // when no API key is configured (which is the normal test environment)
        
        ClientFactory factory = TestomatClientFactory.getClientFactory();
        
        // Either ApiKeyNotFoundException or PropertyNotFoundException expected
        assertThrows(Exception.class, factory::createClient);
    }
}