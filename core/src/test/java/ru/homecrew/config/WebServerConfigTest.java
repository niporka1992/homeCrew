package ru.homecrew.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.core.env.Environment;

@DisplayName("WebServerConfig — проверка логики HTTP/HTTPS для профилей")
class WebServerConfigTest {

    @Test
    @DisplayName("Prod — создаётся только HTTPS, без HTTP-коннектора")
    void prod_createsOnlyHttps() throws Exception {
        KeystoreProperties props = new KeystoreProperties();
        props.setFile("fake.jks");
        props.setAlias("alias");
        props.setPassword("secret");

        WebServerConfig config = spy(new WebServerConfig(props));

        doNothing().when(config).configureSsl(any());

        Environment env = mock(Environment.class);
        when(env.getProperty("server.https-port", "8000")).thenReturn("9443");

        TomcatServletWebServerFactory factory = config.prodFactory(env);

        assertThat(factory.getPort()).isEqualTo(9443);
        assertThat(factory.getAdditionalTomcatConnectors()).isEmpty();
    }
}
