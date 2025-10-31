plugins {
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management")
    kotlin("jvm") version "1.9.24"
}

group = "ru.homecrew"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(project(":bot"))

    // 🚀 Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // 🤖 Telegram
    implementation("org.telegram:telegrambots-longpolling:9.0.0")
    implementation("org.telegram:telegrambots-client:9.0.0")

    // 🗃️ Flyway
    implementation("org.flywaydb:flyway-core:11.0.0")
    implementation("org.flywaydb:flyway-database-postgresql:11.0.0")

    // ✅ JAXB (для Hibernate)
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:2.3.8")

    // 🌱 Dotenv — автоматическая загрузка .env / .env.dev / .env.prod
    implementation("me.paulschwarz:spring-dotenv:4.0.0")

    // 🧪 Тесты
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
}
