import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

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
	implementation("com.h2database:h2")
	implementation("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}


tasks.withType<Test> {
	useJUnitPlatform()
}
//export DOCKER_USERNAME=your-dockerhub-username
//export DOCKER_PASSWORD=your-dockerhub-password
tasks.named<BootBuildImage>("bootBuildImage") {
	publish.set(true)
	imageName.set("your-dockerhub-username/quizzzyyy:latest")
	docker {
		publishRegistry {
			username.set(System.getenv("DOCKER_USERNAME"))
			password.set(System.getenv("DOCKER_PASSWORD"))
			url.set("https://index.docker.io/v1/")
		}
	}
}
