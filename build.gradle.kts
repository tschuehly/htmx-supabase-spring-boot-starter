import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jreleaser.model.Active

plugins {
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.2"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
	kotlin("plugin.jpa") version "1.8.22"

	id("maven-publish")
	id("org.jreleaser") version "1.5.1"
	id("signing")
}

group = "de.tschuehly"
version = "0.2.5"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
	maven("https://jitpack.io")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework:spring-context-support")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("com.auth0:java-jwt:4.4.0")
	implementation("com.github.tschuehly:gotrue-kt:659f8c6757")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	runtimeOnly("org.postgresql:postgresql")

	testImplementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
	testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("com.github.tomakehurst:wiremock:3.0.1")

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

tasks.jar{
	enabled = true
	// Remove `plain` postfix from jar file name
	archiveClassifier.set("")
}
publishing{
	publications {
		create<MavenPublication>("Maven") {
			from(components["java"])
			groupId = "de.tschuehly"
			artifactId = "supabase-security-spring-boot-starter"
			description = "Create Spring Boot Applications with Supabase Security"
		}
		withType<MavenPublication> {
			pom {
				packaging = "jar"
				name.set("supabase-security-spring-boot-starter")
				description.set("Supabase Security Spring Boot Starter")
				url.set("https://github.com/tschuehly/supabase-security-spring-boot-starter/")
				inceptionYear.set("2023")
				licenses {
					license {
						name.set("MIT license")
						url.set("https://opensource.org/licenses/MIT")
					}
				}
				developers {
					developer {
						id.set("tschuehly")
						name.set("Thomas Schuehly")
						email.set("thomas.schuehly@outlook.com")
					}
				}
				scm {
					connection.set("scm:git:git@github.com:tschuehly/supabase-security-spring-boot-starter.git")
					developerConnection.set("scm:git:ssh:git@github.com:tschuehly/supabase-security-spring-boot-starter.git")
					url.set("https://github.com/tschuehly/supabase-security-spring-boot-starter")
				}
			}
		}
	}
	repositories {
		maven {
			url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
		}
	}
}

jreleaser {
	project {
		copyright.set("Thomas Schuehly")
	}
	gitRootSearch.set(true)
	signing {
		active.set(Active.ALWAYS)
		armored.set(true)
	}
	deploy {
		maven {
			nexus2 {
				create("maven-central") {
					active.set(Active.ALWAYS)
					url.set("https://s01.oss.sonatype.org/service/local")
					snapshotUrl.set("https://s01.oss.sonatype.org/content/repositories/snapshots/")
					closeRepository.set(true)
					releaseRepository.set(true)
					stagingRepositories.add("build/staging-deploy")
				}
			}
		}
	}
}
