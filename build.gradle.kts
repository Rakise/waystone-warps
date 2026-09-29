import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.1"
    kotlin("plugin.serialization") version "2.3.0"
}

group = providers.gradleProperty("projectGroup").get()
version = providers.gradleProperty("projectVersion").get()

// 26.x API artifacts require Java 25; the baseline artifact remains Java 21 compatible.
val targetJava = if (providers.gradleProperty("paperApiVersion").get().startsWith("26.")) 25 else 21

val localPropertiesProvider = providers.fileContents(layout.projectDirectory.file("local.properties"))
    .asText
    .map { content ->
        Properties().apply { load(content.reader()) }
    }

fun getProperty(key: String): String {
    return localPropertiesProvider
        .map { it.getProperty(key) }
        .orElse(providers.gradleProperty(key))
        .getOrElse("")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/central")
    }
    maven {
        url = uri("https://repo.aikar.co/content/groups/aikar/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://repo.glaremasters.me/repository/towny/")
    }
    maven {
        name = "Multiverse"
        url = uri("https://repo.onarandombox.com/content/groups/public/")
    }
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.21.0")
    // Supplied by the server at runtime and used by InventoryFramework's constructors.
    testRuntimeOnly("commons-lang:commons-lang:2.6")
    testImplementation("io.papermc.paper:paper-api:${providers.gradleProperty("paperApiVersion").get()}")
    testImplementation("me.xdrop:fuzzywuzzy:1.4.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    compileOnly("io.papermc.paper:paper-api:${providers.gradleProperty("paperApiVersion").get()}")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
    compileOnly("io.insert-koin:koin-core-jvm:4.1.1")
    implementation("co.aikar:idb-core:1.0.0-SNAPSHOT")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    compileOnly("com.zaxxer:HikariCP:7.0.2")
    // 0.12.1 crashes during GUI construction when scanning zero-argument methods.
    // Anvils are not used: text entry goes through Paper's dialog API.
    implementation("com.github.stefvanschie.inventoryframework:IF:0.12.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.xdrop:fuzzywuzzy:1.4.0")
    compileOnly("com.palmergames.bukkit.towny:towny:0.102.0.14")
    compileOnly("org.mvplugins.multiverse.inventories:multiverse-inventories:5.3.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(targetJava.toString()))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(targetJava)
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier = null
}

tasks.jar {
    archiveClassifier.set("plain")
}

tasks.processResources {
    val realName = rootProject.name
    val realVersion = project.version.toString()

    // Ensure Gradle re-runs this task if the version changes
    inputs.property("name", realName)
    inputs.property("version", realVersion)

    filesMatching("plugin.yml") {
        filter { line ->
            when {
                line.trim().startsWith("name:") -> "name: $realName"
                line.trim().startsWith("version:") -> "version: $realVersion"
                else -> line
            }
        }
    }
}

tasks.register<Copy>("deploy") {
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar.flatMap { it.archiveFile })
    into(getProperty("plugin.server.path"))
    rename { fileName -> "${rootProject.name}-${version}.jar" }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    doFirst {
        logger.lifecycle("Target deployment path: ${getProperty("plugin.server.path")}")
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
