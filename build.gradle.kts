plugins {
    id("fabric-loom") version "1.9-SNAPSHOT"
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName = project.property("archives_base_name") as String
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("legitimoose-bot") {
            sourceSet("main")
            sourceSet("client")
        }
    }
}

repositories {
    mavenCentral()
    maven("https://maven.wispforest.io")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    annotationProcessor(modImplementation("io.wispforest:owo-lib:${project.property("owo_version")}")!!)

    shadow(implementation("org.mongodb:mongodb-driver-sync:5.2.1")!!)
    shadow(implementation("org.mongodb:mongodb-driver-core:5.2.1")!!)
    shadow(implementation("org.mongodb:bson:5.2.1")!!)
    shadow(implementation("net.dv8tion:JDA:5.3.0") {
        exclude(module = "opus-java")
    })

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to inputs.properties["version"],
            "minecraft_version" to inputs.properties["minecraft_version"],
            "loader_version" to inputs.properties["loader_version"]
        )
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks {
    shadowJar {
        archiveClassifier = ""
        from(sourceSets["main"].output)
        from(sourceSets["client"].output)
        configurations = listOf(project.configurations.shadow.get())
        minimize()
    }
    remapJar {
        dependsOn(shadowJar)
        mustRunAfter(shadowJar)
        inputFile = shadowJar.get().archiveFile
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}
