dependencies {
    implementation(project(":api"))

    compileOnly("com.github.MrJetby:Treex:79b0b65e")
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
tasks.processResources {
    filteringCharset = "UTF-8"
    val props = mapOf(
        "project" to mapOf("version" to project.version.toString())
    )
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}