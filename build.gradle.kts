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
    implementation("org.springframework:spring-webflux")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.telegram:telegrambots-spring-boot-starter:6.9.7.1")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
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
//            buildArgs.add("-H:+AllowIncompleteClasspath")
//            buildArgs.add("--features=org.graalvm.nativeimage.hosted.Feature")

//            buildArgs.add("--strict-image-heap")

            // HTTP/HTTPS + URL protocols
            buildArgs.add("--enable-url-protocols=http,https")
            buildArgs.add("--enable-http")
            buildArgs.add("--enable-https")

            // Логи Netty — BUILD TIME init
            buildArgs.add("--initialize-at-build-time=io.netty.util.internal.logging")

            // Caffeine — BUILD TIME
            buildArgs.add("--initialize-at-build-time=com.github.benmanes.caffeine")

            // TelegramBots — RUN TIME
//            buildArgs.add("--initialize-at-run-time=org.telegram")

            // Reactor Netty — RUN TIME
//            buildArgs.add("--initialize-at-run-time=reactor.netty")

            // Подробный лог
            buildArgs.add("--verbose")
        }
    }
}