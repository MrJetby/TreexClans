plugins {
    id("com.gradleup.shadow") version "8.3.0"
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.0-Beta2" apply false
}


repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
    implementation(project(":plugin"))
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("TreexClans")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("")

        relocate("com.jodexindustries.jguiwrapper", "me.jetby.jodexindustries.jguiwrapper")


        manifest {
            attributes(
                "Main-Class" to "mc.jetby.treexclans.TreexClans",
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }

        from(project(":plugin").tasks.named("jar"))
        from(project(":api").tasks.named("jar"))
    }
}
