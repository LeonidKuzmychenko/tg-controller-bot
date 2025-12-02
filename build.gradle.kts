plugins {
    java
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.11.3"
}

group = "lk.tech"
version = "0.0.1-SNAPSHOT"
description = "tg-controller-bot"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.postgresql:r2dbc-postgresql")
    implementation("io.r2dbc:r2dbc-pool")

    implementation("org.telegram:telegrambots-spring-boot-starter:6.9.7.1")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}

graalvmNative {
    toolchainDetection.set(true)

    binaries {
        named("main") {
            imageName.set("tg-controller-bot")
            fallback.set(false)

            // ===== GC (только serial или epsilon) =====
            buildArgs.add("--gc=serial")

            // ===== Разрешённые протоколы =====
            buildArgs.add("--enable-http")
            buildArgs.add("--enable-https")
            buildArgs.add("--enable-url-protocols=http,https,dns")

            // ===== Netty — строго initialize-at-run-time =====
            buildArgs.add("--initialize-at-run-time=io.netty")

            // ===== Security / Crypto =====
            buildArgs.add("--enable-all-security-services")

            // ===== Native access =====
            buildArgs.add("--enable-native-access=ALL-UNNAMED")

            // ===== Логирование при сборке =====
            buildArgs.add("--verbose")
        }
    }
}


