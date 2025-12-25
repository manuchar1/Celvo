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
    implementation(projects.common)
    implementation("com.google.firebase:firebase-admin:9.2.0")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}