plugins {
    id("java-library")
    id("celvo.kotlin-common")
    id("org.springframework.boot")
}

group = "com.mtislab"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.spring.boot.starter.security)
    api(libs.spring.boot.starter.oauth2.resource.server)
    api(libs.kotlin.reflect)
    api(libs.jackson.module.kotlin)
    api(libs.springdoc.openapi.ui)

    annotationProcessor(libs.spring.boot.configuration.processor)
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}