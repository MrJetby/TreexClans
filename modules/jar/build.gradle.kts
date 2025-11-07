import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.processResources

plugins {
    id("com.gradleup.shadow") version "8.3.0"
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.0-Beta2" apply false
}


repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":modules:api"))
    implementation(project(":modules:plugin"))
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    // Основная задача сборки
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("TreexClans")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("") // без "-all"

        // Если есть внутренние библиотеки, которые стоит переименовать
        // relocate("kotlin", "mc.lpvania.libs.kotlin")

        manifest {
            attributes(
                "Main-Class" to "mc.lpvania.treexclans.TreexClans", // если нужно
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }

        // чтобы JAR содержал нужные модули
        from(project(":modules:plugin").tasks.named("jar"))
        from(project(":modules:api").tasks.named("jar"))
    }
}
