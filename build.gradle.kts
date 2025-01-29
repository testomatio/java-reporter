plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

group = "com.solidgate"
version = "0.2.10"
description = "Testomat Java Reporter"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    maven("https://nexus.solidgate.com/repository/maven-public")
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("org.aspectj:aspectjweaver:1.9.22")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    api("io.qameta.allure:allure-testng:2.29.1")
    implementation("org.testng:testng:7.10.2")
}


tasks.test {
    useTestNG()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"]) // Publishes the Java component

            // Optional: Customize the POM file with project metadata
            pom {
                // url.set("https://github.com/username/my-library")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("Lolik")
                        name.set("Oleksii Lakovych")
                        email.set("lolikla8@gmail.com")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            url = uri("https://nexus.solidgate.com/repository/maven-releases")
            credentials {
                username = findProperty("nexusUsername") as String? ?: System.getenv("NEXUS_USERNAME")
                password = findProperty("nexusPassword") as String? ?: System.getenv("NEXUS_PASSWORD")
            }
        }
    }

}