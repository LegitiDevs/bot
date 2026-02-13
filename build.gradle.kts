plugins {
  id("fabric-loom") version "1.15-SNAPSHOT"
  id("com.gradleup.shadow") version "9.3.1"
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
  maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

dependencies {
  // To change the versions see the gradle.properties file
  minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
  mappings(loom.officialMojangMappings())
  modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

  modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

  shadow(implementation("org.mongodb:mongodb-driver-sync:5.6.3")!!)
  shadow(implementation("net.dv8tion:JDA:6.3.0") { exclude("opus-java") })
  shadow(implementation("org.json:json:20251224")!!)

  shadow(implementation("com.sparkjava:spark-core:2.9.4")!!)

  modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")
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

tasks.withType<JavaCompile> { options.encoding = "UTF-8" }

tasks {
  shadowJar {
    from(sourceSets["main"].output)
    from(sourceSets["client"].output)
    configurations = listOf(project.configurations.shadow.get())
    archiveClassifier = "shadowed-only"
    //    minimize()
  }
  remapJar {
    dependsOn(shadowJar)
    mustRunAfter(shadowJar)
    inputFile = file(shadowJar.get().archiveFile)
    archiveClassifier = ""
  }
}

java { toolchain.languageVersion = JavaLanguageVersion.of(21) }
