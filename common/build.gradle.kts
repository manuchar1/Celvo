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
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}