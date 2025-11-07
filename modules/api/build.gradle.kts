import java.util.Properties

val envFile = rootProject.file(".env")
val envProps = Properties()

if (envFile.exists()) {
    envFile.forEachLine { line ->
        val trimmed = line.trim()
        if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
            val (key, value) = line.split("=", limit = 2)
            envProps[key] = value
        }
    }
}

plugins {
    `java-library`
    `maven-publish`
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = "space.jetby.TreexClans"
            artifactId = "api"
            version = "1.0.0"
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


dependencies {
    compileOnly("com.github.MrJetby:Treex:68c61c48")
    implementation("com.jodexindustries.jguiwrapper:common:1.0.0.9-beta")

    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")
    compileOnly("com.github.retrooper:packetevents-spigot:2.8.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.mojang:authlib:6.0.58")
    compileOnly("me.clip:placeholderapi:2.11.5")
}