plugins {
    id("java")
}

group = "io.testomat"
version = "0.1.0"
description = "Testomat Java Reporter"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("org.aspectj:aspectjweaver:1.9.22")
    testImplementation("org.testng:testng:7.10.2")
}

tasks.test {
    useTestNG()
}