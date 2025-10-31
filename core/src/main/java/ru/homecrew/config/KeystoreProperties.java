package ru.homecrew.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ssl.keystore")
public class KeystoreProperties {
    private String file;
    private String alias;
    private String password;
}
