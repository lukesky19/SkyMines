plugins {
    java
    id("com.gradleup.shadow") version "8.3.1"
}

group = "com.github.lukesky19"
version = "3.0.0-Pre-1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.onarandombox.com/content/groups/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.11-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.bstats:bstats-bukkit:3.0.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<ProcessResources> {
    val props = mapOf("version" to version)

    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
    archiveClassifier.set("")
    relocate("org.spongepowered.configurate", "com.github.lukesky19.skynodes.libs.configurate")
    relocate("org.bstats", "com.github.lukesky19.skynodes.libs.bstats")
    minimize()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}