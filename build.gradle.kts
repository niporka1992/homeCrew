import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import name.remal.gradle_plugins.sonarlint.SonarLintExtension

plugins {
    id("org.springframework.boot") version "3.3.3" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
    id("com.diffplug.spotless") version "6.25.0" apply false
    id("name.remal.sonarlint") version "3.4.3" apply false
    id("org.sonarqube") version "5.0.0.4638" apply false
    id("java")
}

allprojects {
    group = "ru.homecrew"
    version = "1.0.0"

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "name.remal.sonarlint")
    apply(plugin = "com.diffplug.spotless")

    // ✅ Java toolchain 21
    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    // ✅ Общие зависимости (BOM)
    the<DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.3")
        }
        dependencies {
            dependency("org.mapstruct:mapstruct:1.6.2")
            dependency("org.mapstruct:mapstruct-processor:1.6.2")
        }
    }

    dependencies {
        implementation("org.mapstruct:mapstruct")
        annotationProcessor("org.mapstruct:mapstruct-processor")
    }

    // ✅ Spotless (форматирование, чистка импортов)
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            palantirJavaFormat("2.63.0")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    // ✅ SonarLint без тестов
    extensions.configure<SonarLintExtension> {
        testSourceSets = emptyList()
        ignoredPaths.addAll(
            listOf(
                "**/src/test/**",
                "**/test/**",
                "**/build/**",
                "**/out/**"
            )
        )
    }

    tasks.configureEach {
        if (name.contains("sonarlintTest", ignoreCase = true)) enabled = false
    }

    // ✅ Настройки тестов
    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging { showStandardStreams = true }
    }

    //  Общий build порядок
    tasks.named("build") {
        dependsOn("clean", "spotlessApply", "test")
    }
}

// ===  Spring Boot fat-jar только для app ===
project(":app") {
    apply(plugin = "org.springframework.boot")

    //  Убираем shadow, используем bootJar
    tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        archiveClassifier.set("") // создаёт app-1.0.0.jar без "-plain"
        launchScript() // делает jar исполняемым (bash-скрипт сверху)
    }

    tasks.named("build") {
        dependsOn("bootJar")
    }
}


// ===  Полная сборка (clean → формат → тесты → bootJar + вывод результата) ===
tasks.register("fullBuild") {
    group = "build"
    description = "Очищает, форматирует, тестирует и собирает исполняемый boot-jar из :app"

    dependsOn(
        ":core:clean",
        ":bot:clean",
        ":app:clean",
        ":core:spotlessApply",
        ":bot:spotlessApply",
        ":app:spotlessApply",
        ":core:test",
        ":bot:test",
        ":app:test"
    )

    doLast {
        var jarFile = file("app/build/libs/app-1.0.0.jar")

        // если JAR ещё не создан — пробуем собрать вручную
        if (!jarFile.exists()) {
            println("🔁 Запускаю :app:bootJar вручную...")
            exec { commandLine("bash", "-c", "./gradlew :app:bootJar") }
            jarFile = file("app/build/libs/app-1.0.0.jar")
        }

        // финальная проверка и вывод результата
        if (jarFile.exists()) {
            val sizeMB = String.format("%.2f", jarFile.length() / 1024.0 / 1024.0)
            println("\u001B[32m✅ Полная сборка завершена успешно!\u001B[0m")
            println("📦 Fat-JAR: ${jarFile.absolutePath} (${sizeMB} MB)")
            println("▶ Запуск: java -jar ${jarFile.absolutePath} --spring.profiles.active=dev")
        } else {
            println("\u001B[31m❌ Даже ручная сборка :app:bootJar не создала JAR.\u001B[0m")
        }
    }
}

