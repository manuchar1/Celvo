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
}

