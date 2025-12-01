import org.gradle.kotlin.dsl.internal.relocated.kotlin.metadata.internal.metadata.deserialization.VersionRequirementTable.Companion.create

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
        // ----------------------------------------------------
        // WINDOWS BUILD (safe, no DNS)
        // ----------------------------------------------------
        create("windowsMain") {
            imageName.set("tg-controller-bot-windows")
            fallback.set(false)

            buildArgs.add("--gc=serial")

            buildArgs.add("--enable-http")
            buildArgs.add("--enable-https")
            buildArgs.add("--enable-url-protocols=http,https")

            // Netty must load at runtime on Windows
            listOf(
                "io.netty",
                "io.netty.buffer",
                "io.netty.channel",
                "io.netty.handler",
                "io.netty.resolver",
                "io.netty.transport",
                "io.netty.util",
                "io.netty.util.internal"
            ).forEach { pkg ->
                buildArgs.add("--initialize-at-run-time=$pkg")
            }

            // Spring buffer fixes
            buildArgs.add("--initialize-at-run-time=org.springframework.core.io.buffer")

            buildArgs.add("--enable-all-security-services")
            buildArgs.add("--enable-native-access=ALL-UNNAMED")

            // ❗ Windows: disable DNS SPI
            buildArgs.add("-Djdk.internal.dns.disableSystemDNS=true")
            buildArgs.add("-Dio.netty.resolver.dns.native=disabled")

            buildArgs.add("--verbose")
        }

        // ----------------------------------------------------
        // LINUX BUILD (docker)
        // ----------------------------------------------------
        create("linuxMain") {
            imageName.set("tg-controller-bot-linux")
            fallback.set(false)

            buildArgs.add("--gc=G1")

            buildArgs.add("--enable-http")
            buildArgs.add("--enable-https")
            buildArgs.add("--enable-url-protocols=http,https,dns")

            // Linux: allow Netty DNS + epoll
            listOf(
                "io.netty",
                "io.netty.buffer",
                "io.netty.channel",
                "io.netty.handler",
                "io.netty.resolver",
                "io.netty.transport",
                "io.netty.util",
                "io.netty.util.internal"
            ).forEach { pkg ->
                buildArgs.add("--initialize-at-run-time=$pkg")
            }

            buildArgs.add("--initialize-at-run-time=org.springframework.core.io.buffer")

            buildArgs.add("--enable-all-security-services")
            buildArgs.add("--enable-native-access=ALL-UNNAMED")

            // Linux: DO NOT disable DNS
            // It is stable and required

            buildArgs.add("--verbose")
        }
    }
}


