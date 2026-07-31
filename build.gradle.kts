plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")

    compileOnly("net.raidstone:WorldGuardEvents:1.18.1")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.13")

    implementation("com.g4vrk:FunctionalActions:2.0.0")
    implementation("com.g4vrk:FastTextFormatter:1.0.0")
    implementation("com.g4vrk:FunctionalConfiguration:1.0.0")
    implementation("org.spongepowered:configurate-yaml:4.2.0")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.18.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
