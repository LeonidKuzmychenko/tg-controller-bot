plugins {
    java
    id("org.springframework.boot") version "3.5.7"
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
            // Оптимальный GC + стабильность
            buildArgs.add("--gc=serial")
            // HTTP/HTTPS + URL protocols
            buildArgs.add("--enable-url-protocols=http,https")
            buildArgs.add("--enable-http")
            buildArgs.add("--enable-https")
            // Логи Netty — BUILD TIME init
            buildArgs.add("--initialize-at-build-time=io.netty.util.internal.logging")
            // Подробный лог
            buildArgs.add("--verbose")
        }
    }
}