plugins {
    id("java")
    id("io.spring.dependency-management")
}

group = "ru.homecrew"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // 🧩 подключаем core (там вся логика, сервисы, репозитории и т.п.)
    implementation(project(":core"))

    // 🚀 Spring Boot базовый функционал
    implementation("org.springframework.boot:spring-boot-starter")

    // 🤖 Telegram Bots 9.x (новая архитектура)
    implementation("org.telegram:telegrambots-longpolling:9.0.0")
    implementation("org.telegram:telegrambots-client:9.0.0")

    // 💬 Для удобного логирования и JSON (если нужно)
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.slf4j:slf4j-api")

    // 🧠 Lombok (аннотации)
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

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
