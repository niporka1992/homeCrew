package ru.homecrew.config;

import java.io.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import ru.homecrew.exception.WebServerConfigException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebServerConfig {

    private final KeystoreProperties keystoreProps;

    @Bean
    @Profile("prod")
    public TomcatServletWebServerFactory prodFactory(Environment env) {
        try {
            int httpsPort = Integer.parseInt(env.getProperty("server.https-port", "8000"));
            return createFactory(null, httpsPort, "prod", true);
        } catch (Exception e) {
            throw new WebServerConfigException("Ошибка при инициализации prodFactory", e);
        }
    }

    @Bean
    @Profile("dev")
    public TomcatServletWebServerFactory devFactory(Environment env) {
        try {
            int httpPort = Integer.parseInt(env.getProperty("server.http-port", "1111"));
            int httpsPort = Integer.parseInt(env.getProperty("server.https-port", "4174"));
            boolean httpsEnabled = Boolean.parseBoolean(env.getProperty("server.https-enabled", "false"));
            return createFactory(httpPort, httpsPort, "dev", httpsEnabled);
        } catch (Exception e) {
            throw new WebServerConfigException("Ошибка при инициализации devFactory", e);
        }
    }

    private TomcatServletWebServerFactory createFactory(
            Integer httpPort, int httpsPort, String profile, boolean enableHttps) {
        try {
            TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();

            if (enableHttps) {
                factory.setPort(httpsPort);
                configureSsl(factory);
                log.info("✅ HTTPS enabled on port {} for profile={}", httpsPort, profile);
            } else {
                factory.setPort(httpPort);
                log.warn("⚠️ HTTPS disabled, running HTTP-only on port {}", httpPort);
            }

            if (enableHttps && httpPort != null) {
                Connector httpConnector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
                httpConnector.setScheme("http");
                httpConnector.setPort(httpPort);
                httpConnector.setSecure(false);
                factory.addAdditionalTomcatConnectors(httpConnector);
                log.info("🌐 HTTP {} + HTTPS {} active for {}", httpPort, httpsPort, profile);
            }

            return factory;
        } catch (Exception e) {
            throw new WebServerConfigException("Ошибка при создании TomcatServletWebServerFactory", e);
        }
    }

    void configureSsl(TomcatServletWebServerFactory factory) {
        try {
            Ssl ssl = new Ssl();
            String keystoreName = keystoreProps.getFile();
            File keystoreFile = resolveKeystoreFile(keystoreName);

            ssl.setKeyStore(keystoreFile.getAbsolutePath());
            ssl.setKeyStorePassword(keystoreProps.getPassword());
            ssl.setKeyAlias(keystoreProps.getAlias());
            factory.setSsl(ssl);
        } catch (Exception e) {
            throw new WebServerConfigException("Ошибка при настройке SSL", e);
        }
    }

    private File resolveKeystoreFile(String keystoreName) {
        ClassPathResource resource = new ClassPathResource(keystoreName);
        File keystoreFile = null;

        if (resource.exists()) {
            keystoreFile = extractKeystoreFromResource(resource);
        }

        if (keystoreFile == null || !keystoreFile.exists()) {
            String dockerPath = "/app/" + keystoreName;
            File dockerFile = new File(dockerPath);
            if (dockerFile.exists()) {
                log.info("🔐 Using keystore from Docker path: {}", dockerPath);
                return dockerFile;
            }
            throw new WebServerConfigException(
                    "Keystore not found: neither classpath nor /app/", new FileNotFoundException(dockerPath));
        }

        return keystoreFile;
    }

    private File extractKeystoreFromResource(ClassPathResource resource) {
        try {
            File file = resource.getFile();
            log.info("🔐 Using keystore from classpath file: {}", file.getAbsolutePath());
            return file;
        } catch (FileNotFoundException e) {
            try {
                File tempFile = File.createTempFile("keystore-", ".jks");
                try (InputStream in = resource.getInputStream();
                        OutputStream out = new FileOutputStream(tempFile)) {
                    in.transferTo(out);
                }
                tempFile.deleteOnExit();
                log.info("🔐 Using keystore from classpath (copied to temp file): {}", tempFile.getAbsolutePath());
                return tempFile;
            } catch (IOException io) {
                throw new WebServerConfigException("Не удалось создать временный keystore-файл", io);
            }
        } catch (IOException e) {
            throw new WebServerConfigException("Ошибка чтения keystore из classpath", e);
        }
    }
}
