plugins {
    java
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "green-ribbon-claim-assignment"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
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
    // Spring Boot Starter
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Database
    runtimeOnly("com.h2database:h2")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // --- QueryDSL 설정 (Spring Boot 3.x / Jakarta) ---
    // 1. QueryDSL JPA 라이브러리 (Jakarta 버전)
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")

    // 2. QClass 생성을 위한 Annotation Processor (Jakarta 버전)
    annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")

    // 3. java.lang.NoClassDefFoundError (javax.annotation.Generated) 대응
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")

    // 4. java.lang.NoClassDefFoundError (javax.persistence.Entity) 대응
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // Swagger (SpringDoc for Boot 3)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/sources/annotationProcessor/java/main")
        }
    }
}