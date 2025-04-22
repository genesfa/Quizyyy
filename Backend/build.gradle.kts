plugins {
	id("org.springframework.boot") version("3.2.5")
	id("io.spring.dependency-management") version("1.1.6")
	java
}

group = "com.quiz"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("com.corundumstudio.socketio:netty-socketio:2.0.13")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}


tasks.withType<Test> {
	useJUnitPlatform()
}
