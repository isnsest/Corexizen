plugins {
    id("java-library")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("com.gradleup.shadow") version "9.4.2"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://maven.citizensnpcs.co/repo")
}

dependencies {
    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")

    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    compileOnly("com.denizenscript:denizen:1.3.2-SNAPSHOT")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}
