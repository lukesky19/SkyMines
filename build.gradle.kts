plugins {
    `java-library`
}

group = "com.github.lukesky19"
version = "3.3.2.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.onarandombox.com/content/groups/public/")
    maven("https://repo.rosewooddev.io/repository/public/")
}

dependencies {
    // Paper
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")

    // SkyLib
    compileOnly("com.github.lukesky19:SkyLib:2.0.2.0")

    // Integration
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.17-SNAPSHOT") {
        exclude(group = "com.google.code.gson")
        exclude(group = "com.google.guava")
    }
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("org.black_ixx:playerpoints:3.3.3")
    compileOnly("net.luckperms:api:5.4")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    // This allows usage of @apiNode in javadocs
    javadoc {
        (options as StandardJavadocDocletOptions).tags("apiNote:a:API Note:", "implNote:a:IMPL Note:")
    }

    jar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
        archiveClassifier.set("")
    }

    build {
        dependsOn(javadoc)
    }
}