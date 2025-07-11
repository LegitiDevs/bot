plugins {
    kotlin("jvm") version "2.1.21"
    id("com.google.devtools.ksp") version "2.1.21-2.0.2"
    id("fabric-loom") version "1.9-SNAPSHOT"
    id("com.gradleup.shadow") version "9.0.0-beta17"
}

version = project.property("mod_version") as String

group = project.property("maven_group") as String

base { archivesName = project.property("archives_base_name") as String }

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
    maven("https://maven.fabricmc.net/")
    maven("https://maven.wispforest.io")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://maven.kosmx.dev/")
    maven("https://maven.parchmentmc.org")
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings(
            loom.layered {
                officialMojangMappings()
                parchment(
                        "org.parchmentmc.data:parchment-${project.property("minecraft_version")}:${project.property("parchment_mappings")}@zip"
                )
            }
    )
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    modImplementation(
            "net.fabricmc:fabric-language-kotlin:${project.property("fabric_kotlin_version")}"
    )

    modImplementation("io.wispforest:owo-lib:${project.property("owo_version")}")
    ksp("dev.kosmx.kowoconfig:ksp-owo-config:0.2.0")

    shadow(implementation("org.mongodb:mongodb-driver-kotlin-sync:5.5.1")!!)
    shadow(implementation("org.mongodb:bson-kotlinx:5.5.1")!!)
    shadow(implementation("net.dv8tion:JDA:5.6.1") { exclude("opus-java") })

    // modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    inputs.property("owo_version", project.property("owo_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
                "version" to inputs.properties["version"],
                "minecraft_version" to inputs.properties["minecraft_version"],
                "loader_version" to inputs.properties["loader_version"],
                "owo_version" to inputs.properties["owo_version"]
        )
    }
}

tasks.withType<JavaCompile> { options.encoding = "UTF-8" }

tasks {
    shadowJar {
        from(sourceSets["main"].output)
        from(sourceSets["client"].output)
        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier = ""
        minimize()
    }
    remapJar {
        dependsOn(shadowJar)
        mustRunAfter(shadowJar)
        inputFile = file(shadowJar.get().archiveFile)
    }
}

java { toolchain.languageVersion = JavaLanguageVersion.of(21) }

kotlin {
    jvmToolchain(21)
}
