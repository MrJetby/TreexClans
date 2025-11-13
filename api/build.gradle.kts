import java.util.Properties

val envFile = rootProject.file(".env")
val envProps = Properties()

if (envFile.exists()) {
    envFile.forEachLine { line ->
        val trimmed = line.trim()
        if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
            val (key, value) = trimmed.split("=", limit = 2)
            envProps[key] = value
        }
    }
}

plugins {
    `java-library`
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val exposedDependencies = listOf(
    "com.jodexindustries.jguiwrapper:common:1.0.0.9-beta",
)

val compileOnlyDependencies = listOf(
    "com.github.MilkBowl:VaultAPI:1.7",
    "space.jetby.libs:Treex:0.1.5",
    "com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT",
    "org.projectlombok:lombok:1.18.42",
    "com.github.retrooper:packetevents-spigot:2.8.0",
    "com.mojang:authlib:6.0.58",
    "me.clip:placeholderapi:2.11.5"
)

dependencies {
    compileOnlyDependencies.forEach { compileOnly(it) }
    exposedDependencies.forEach { api(it) }

    annotationProcessor("org.projectlombok:lombok:1.18.42")
    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "space.jetby.TreexClans"
            artifactId = "api"
            version = "2.1"

            pom {
                name.set("TreexClans API")
                description.set("TreexClans Addon API for developers")
                url.set("https://jetby.space")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("JetBy")
                        name.set("JetBy")
                        email.set("support@jetby.space")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "vds"
            url = uri("sftp://maven.jetby.space:22/var/www/maven")
            credentials {
                username = envProps["JETBY_VDS_USER"]?.toString() ?: ""
                password = envProps["JETBY_VDS_PASS"]?.toString() ?: ""
            }
        }
    }
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}