plugins {
	id("celvo.spring-boot-app")
}

group = "com.mtislab"
version = "0.0.1"
description = "Celvo Backend"




dependencies {
	implementation(projects.user)
	implementation(projects.notifications)
	implementation(projects.common)
	implementation(libs.spring.boot.starter.data.jpa)
	implementation(libs.spring.boot.starter.security)
	implementation(libs.kotlin.reflect)

	runtimeOnly(libs.postgresql)
	testRuntimeOnly("com.h2database:h2")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

