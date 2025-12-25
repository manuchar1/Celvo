plugins {
    id("java-library")
    id("celvo.spring-boot-service")
    kotlin("plugin.jpa")
}

group = "com.mtislab"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(projects.common)


    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    runtimeOnly(libs.postgresql)


    // Jakarta Servlet API
    implementation(libs.jakarta.servlet.api)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
