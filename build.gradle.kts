import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.4"
	id("io.spring.dependency-management") version "1.0.14.RELEASE"
	id("maven-publish")
	kotlin("jvm") version "1.7.10"
	kotlin("plugin.spring") version "1.7.10"
	kotlin("plugin.jpa") version "1.7.10"
}

group = "io.supabase"
version = "0.1.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
	jcenter()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework:spring-context-support")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0-rc2")
	implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("com.auth0:java-jwt:4.1.0")
	implementation("io.supabase:gotrue-kt:0.4.0")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	runtimeOnly("org.postgresql:postgresql")

	testImplementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("com.github.tomakehurst:wiremock-jre8:2.34.0")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks {
	bootJar {
		enabled = false
	}
}

java {
	withJavadocJar()
	withSourcesJar()
}


//
//publishing{
//	publications {
//		create<MavenPublication>("Maven") {
//			from(components["java"])
//		}
//		withType<MavenPublication> {
//			pom {
//				packaging = "jar"
//				name.set("supabase-spring-boot-starter")
//				description.set("Supabase Spring Boot Starter")
//				licenses {
//					license {
//						name.set("MIT license")
//						url.set("https://opensource.org/licenses/MIT")
//					}
//				}
//				developers {
//					developer {
//						name.set("Thomas Schuehly")
//						email.set("thomas.schuehly@outlook.com")
//					}
//				}
//			}
//		}
//	}
//}
