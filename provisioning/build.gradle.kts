plugins {
    id("java-library")
    id("celvo.spring-boot-service")
    kotlin("plugin.jpa")
    kotlin("plugin.spring")
}

group = "com.mtislab.celvo.provisioning"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {

    implementation(projects.common)

    implementation(libs.spring.boot.starter.web)
    implementation(libs.jackson.module.kotlin)


    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)

    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql)

    implementation(libs.kotlin.reflect)

    // Cache
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation(libs.caffeine)



    testImplementation(kotlin("test"))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}