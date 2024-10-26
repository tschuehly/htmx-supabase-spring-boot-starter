import org.jreleaser.model.Active

plugins {
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0"

    id("maven-publish")
    id("org.jreleaser") version "1.14.0"
    id("signing")
}

group = "de.tschuehly"
version = "0.3.6-SNAPSHOT"
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("org.springframework:spring-jdbc")

    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework:spring-context-support")

    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
    implementation("com.auth0:java-jwt:4.4.0")

    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.6.0")
    runtimeOnly("io.ktor:ktor-client-java:2.3.12")
    testRuntimeOnly("io.ktor:ktor-client-java:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("io.ktor:ktor-client-mock:2.3.12")


    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.github.wimdeblauwe:htmx-spring-boot:3.4.1")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.htmlunit:htmlunit:4.2.0")
    testImplementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.github.tomakehurst:wiremock:3.0.1")
    testImplementation("org.springframework.boot:spring-boot-devtools")
    testImplementation("com.russhwolf:multiplatform-settings-test:1.1.1")

}


kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
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

tasks.jar {
    enabled = true
    // Remove `plain` postfix from jar file name
    archiveClassifier.set("")
}
publishing {
    publications {
        create<MavenPublication>("Maven") {
            from(components["java"])
            groupId = "de.tschuehly"
            artifactId = "htmx-supabase-spring-boot-starter"
            description = "Spring Security with htmx and supabase with ease"
        }
        withType<MavenPublication> {
            pom {
                packaging = "jar"
                name.set("htmx-supabase-spring-boot-starter")
                description.set("Spring Security with htmx and supabase with ease")
                url.set("https://github.com/tschuehly/htmx-supabase-spring-boot-starter")
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
                    connection.set("scm:git:git@github.com:tschuehly/htmx-supabase-spring-boot-starter.git")
                    developerConnection.set("scm:git:ssh:git@github.com:tschuehly/htmx-supabase-spring-boot-starter.git")
                    url.set("https://github.com/tschuehly/htmx-supabase-spring-boot-starter")
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
                    snapshotSupported.set(true)
                    url.set("https://s01.oss.sonatype.org/service/local")
                    snapshotUrl.set("https://s01.oss.sonatype.org/content/repositories/snapshots")
                    closeRepository.set(true)
                    releaseRepository.set(true)
                    stagingRepositories.add("build/staging-deploy")
                }
            }
        }
    }
}
