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
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}