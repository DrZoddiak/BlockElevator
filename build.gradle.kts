import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    id("org.spongepowered.gradle.plugin") version "2.2.0"
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    id("com.gradleup.shadow") version "8.3.3"
}

group = "me.zodd"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.spongepowered:configurate-hocon:4.1.2")
    implementation("org.spongepowered:configurate-extra-kotlin:4.1.2") {
        isTransitive = false
    }
    kotlin("kotlin-reflect")
}

sponge {
    apiVersion("12.0.0-SNAPSHOT")
    license("All-Rights-Reserved")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("blockelevator") {
        displayName("BlockElevator")
        entrypoint("me.zodd.blockelevator.BlockElevator")
        description("My plugin description")
        links {
            // homepageLink("https://spongepowered.org")
            // sourceLink("https://spongepowered.org/source")
            // issuesLink("https://spongepowered.org/issues")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

val javaTarget = 21 // Sponge targets a minimum of Java 17
java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
    if (JavaVersion.current() < JavaVersion.toVersion(javaTarget)) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaTarget))
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
        if (JavaVersion.current().isJava10Compatible) {
            release.set(javaTarget)
        }
    }
}

tasks.shadowJar {
    relocate("kotlin", "me.zodd.libs.kt")
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
