import org.jreleaser.model.Active

plugins {
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"

    id("maven-publish")
    id("org.jreleaser") version "1.20.0"
    id("signing")
}

group = "de.tschuehly"
version = "0.3.7-SNAPSHOT"

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
    implementation("com.auth0:java-jwt:4.5.0")

    implementation("io.github.jan-tennert.supabase:auth-kt:3.0.3")
    runtimeOnly("io.ktor:ktor-client-java:3.1.3")
    testRuntimeOnly("io.ktor:ktor-client-java:3.1.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("io.ktor:ktor-client-mock:3.1.3")


    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.github.wimdeblauwe:htmx-spring-boot:3.4.1")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.htmlunit:htmlunit:4.11.1")
    testImplementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.wiremock:wiremock:3.13.0")
    testImplementation("org.springframework.boot:spring-boot-devtools")
    testImplementation("com.russhwolf:multiplatform-settings-test:1.3.0")

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
            mavenCentral {

                create("release-deploy") {
                    active.set(Active.ALWAYS)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepositories.add("build/staging-deploy")
                }
            }
            nexus2 {
                create("snapshot-deploy") {
                    active.set(Active.SNAPSHOT)
                    snapshotSupported.set(true)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    snapshotUrl.set("https://central.sonatype.com/repository/maven-snapshots")

                    closeRepository.set(true)
                    releaseRepository.set(true)
                    stagingRepositories.add("build/staging-deploy")
                    applyMavenCentralRules = true
                }
            }
        }
    }
}
