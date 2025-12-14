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

}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}