plugins {
  kotlin("jvm") version "2.2.10"
  id("com.google.devtools.ksp") version "2.2.0-2.0.2"
  id("fabric-loom") version "1.11-SNAPSHOT"
  id("com.gradleup.shadow") version "9.0.2"
  id("com.diffplug.spotless") version "7.2.1"
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
  maven("https://maven.isxander.dev/releases")
}

dependencies {
  // To change the versions see the gradle.properties file
  minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
  mappings(loom.officialMojangMappings())
  modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

  modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
  modImplementation(
      "net.fabricmc:fabric-language-kotlin:${project.property("fabric_kotlin_version")}")

  shadow(implementation("org.mongodb:mongodb-driver-kotlin-sync:5.5.1")!!)
  shadow(implementation("org.mongodb:bson-kotlinx:5.5.1")!!)
  shadow(implementation("net.dv8tion:JDA:5.6.1") { exclude("opus-java") })
}

tasks.processResources {
  inputs.property("version", project.version)
  inputs.property("minecraft_version", project.property("minecraft_version"))
  inputs.property("loader_version", project.property("loader_version"))
  filteringCharset = "UTF-8"

  filesMatching("fabric.mod.json") {
    expand(
        "version" to inputs.properties["version"]!!,
        "minecraft_version" to inputs.properties["minecraft_version"]!!,
        "loader_version" to inputs.properties["loader_version"]!!,
    )
  }
}

spotless {
  java {
    googleJavaFormat()

    formatAnnotations()
  }
  kotlin { ktfmt() }
  kotlinGradle {
    target("*.gradle.kts")
    ktfmt()
  }
}

tasks.withType<JavaCompile> { options.encoding = "UTF-8" }

tasks {
  shadowJar {
    from(sourceSets["main"].output)
    from(sourceSets["client"].output)
    configurations = listOf(project.configurations.shadow.get())
    archiveClassifier = "shadowed-only"
    minimize()
  }
  remapJar {
    dependsOn(shadowJar)
    mustRunAfter(shadowJar)
    inputFile = file(shadowJar.get().archiveFile)
    archiveClassifier = ""
  }
}

java { toolchain.languageVersion = JavaLanguageVersion.of(21) }

kotlin { jvmToolchain(21) }
