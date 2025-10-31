plugins {
    id("java-library") // core — библиотека, без Spring Boot
    id("io.spring.dependency-management")
}

group = "ru.homecrew"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // === Spring (через api, чтобы app всё видел) ===
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-jdbc")
    api("org.springframework.boot:spring-boot-starter-quartz")
    api("org.springframework.boot:spring-boot-starter-validation")

    // === Database ===
    runtimeOnly("org.postgresql:postgresql")

    // === Flyway ===
    api("org.flywaydb:flyway-core:11.0.0")
    api("org.flywaydb:flyway-database-postgresql:11.0.0")

    // === JWT ===
    api("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // === Lombok ===
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // === MapStruct ===
    api("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // === Testing ===
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(project(":app"))
    testImplementation("com.h2database:h2:2.3.232")


}

tasks.named<Jar>("jar") {
    enabled = true
}

tasks.register("bootJar") {
    enabled = false
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    description = "Runs unit tests only"
    useJUnitPlatform {
        excludeTags("integration")
    }
}

