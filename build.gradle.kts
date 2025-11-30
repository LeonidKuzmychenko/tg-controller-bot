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

            // 1) GC: serial — единственный стабильный на Windows
            buildArgs.add("--gc=serial")

            // 2) Поддержка нужных протоколов (DNS обязателен!)
            buildArgs.add("--enable-http")
            buildArgs.add("--enable-https")
            buildArgs.add("--enable-url-protocols=http,https,dns")

            // 3) JDK DNS SPI инициализируется на build-time
            //    иначе Windows падает при первой DNS операции
            buildArgs.add("--initialize-at-build-time=sun.net.dns")

            // 4) Netty ВСЕГДА только runtime-init (иначе гарантированный SEGFAULT/GC crash)
            buildArgs.add("--initialize-at-run-time=io.netty")
            buildArgs.add("--initialize-at-run-time=io.netty.buffer")
            buildArgs.add("--initialize-at-run-time=io.netty.channel")
            buildArgs.add("--initialize-at-run-time=io.netty.handler")
            buildArgs.add("--initialize-at-run-time=io.netty.resolver")
            buildArgs.add("--initialize-at-run-time=io.netty.transport")
            buildArgs.add("--initialize-at-run-time=io.netty.util")
            buildArgs.add("--initialize-at-run-time=io.netty.util.internal")
            buildArgs.add("--initialize-at-run-time=io.netty.util.internal.shaded")

            // 5) Spring DataBuffer — runtime init (иначе invalid hub type)
            buildArgs.add("--initialize-at-run-time=org.springframework.core.io.buffer")

            // 6) Crypto / SSL / HTTPS services (Spring Boot 3.5.x)
            buildArgs.add("--enable-all-security-services")

            // 7) Разрешаем R2DBC PostgreSQL SPI/native access
            buildArgs.add("--enable-native-access=ALL-UNNAMED")

            // 8) Полное отключение JDK DNS SPI
            //    (ВАЖНО: только через -D <prop>=value — с пробелом!)
            buildArgs.add("-D jdk.internal.dns.disableSystemDNS=true")

            // 9) Отладочный вывод
            buildArgs.add("--verbose")
        }
    }
}
