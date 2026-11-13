plugins {
    id("fabric-loom") version "1.9-SNAPSHOT"
}

base { archivesName = "king-esp" }

repositories {
    mavenCentral()
    maven("https://maven.meteordev.org/releases") { name = "Meteor Releases" }
    maven("https://maven.meteordev.org/snapshots") { name = "Meteor Snapshots" }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("meteordevelopment:meteor-client:${property("meteor_version")}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> { options.release = 21 }
