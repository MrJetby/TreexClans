plugins {
    kotlin("jvm") version "2.3.0-Beta2" apply false
    id("com.gradleup.shadow") version "8.3.0" apply false
    id("xyz.jpenilla.run-paper") version "2.3.1" apply false
    `java-library`
}

allprojects {
    group = "me.jetby"
    version = "2.0"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "papermc-repo"
        }
        maven("https://repo.codemc.io/repository/maven-releases/") {
            name = "codemc-releases"
        }
        maven("https://jitpack.io") {
            name = "jitpack.io"
        }
        maven("https://libraries.minecraft.net/") {
            name = "minecraft-repo"
        }
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
            name = "placeholderapi"
        }
        maven("https://repo.jodex.xyz/releases") {
            name = "Jodexindustries-releases"
        }
        maven {
            name = "jetby-repo"
            url = uri("https://maven.jetby.space")
        }
    }
}

subprojects {
    apply(plugin = "java")
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
}