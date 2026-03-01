import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import net.kyori.indra.IndraPlugin
import net.kyori.indra.IndraPublishingPlugin
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("net.kyori.indra") version "3.1.3"
    id("net.kyori.indra.publishing") version "3.1.3"
    id("net.kyori.indra.license-header") version "3.1.3"
    id("com.gradleup.shadow") version "8.3.5"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.github.ben-manes.versions") version "0.51.0"
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "me.glaremasters"
version = "3.5.7.2-SNAPSHOT"
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
base {
    archivesBaseName = "Guilds"
}

apply {
    plugin<ShadowPlugin>()
    plugin<IndraPlugin>()
    plugin<IndraPublishingPlugin>()
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        content {
            includeGroup("org.bukkit")
            includeGroup("org.spigotmc")
        }
    }
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.aikar.co/content/groups/aikar/") {
        content { includeGroup("co.aikar") }
    }
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    maven("https://repo.codemc.org/repository/maven-public/") {
        content { includeGroup("org.codemc.worldguardwrapper") }
    }
    maven("https://repo.glaremasters.me/repository/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("co.aikar:taskchain-bukkit:3.7.2")
    implementation("org.codemc.worldguardwrapper:worldguardwrapper:1.1.9-SNAPSHOT")
    implementation("ch.jalu:configme:1.3.0")
    implementation("com.dumptruckman.minecraft:JsonConfiguration:1.1")
    implementation("com.github.cryptomorin:XSeries:12.1.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.4")
    implementation("dev.triumphteam:triumph-gui:3.1.10")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.jdbi:jdbi3-core:3.8.2")
    implementation("org.jdbi:jdbi3-sqlobject:3.8.2")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.7.2")

    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("net.milkbowl:vault:1.7")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.mojang:authlib:1.5.21")
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            moduleName.set("Guilds")

            includes.from(project.files(), "Module.md")

            sourceLink {
                localDirectory.set(projectDir.resolve("src"))
                remoteUrl.set(URL("https://github.com/guilds-plugin/Guilds/tree/master/src"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}

tasks {
    build {
        dependsOn(named("shadowJar"))
    }

    indra {
        mitLicense()

        javaVersions {
            target(21)
        }

        github("guilds-plugin", "guilds") {
            publishing(true)
        }

        publishAllTo("guilds", "https://repo.glaremasters.me/repository/guilds/")
    }

    compileKotlin {
        kotlinOptions.javaParameters = true
        kotlinOptions.jvmTarget = "21"
    }

    compileJava {
        options.compilerArgs = listOf("-parameters")
    }

    runServer {
        minecraftVersion("1.21.1")
    }

    license {
        header.set(resources.text.fromFile(rootProject.file("LICENSE")))
        exclude("me/glaremasters/guilds/scanner/ZISScanner.java")
        exclude("me/glaremasters/guilds/updater/UpdateChecker.java")
        exclude("me/glaremasters/guilds/utils/PremiumFun.java")
    }

    shadowJar {
        // REQUIRED: bStats must be relocated or it will refuse to run
        relocate("org.bstats", "me.glaremasters.guilds.libs.bstats")

        // Optional but recommended: keep other shaded libs out of the global namespace
        relocate("co.aikar.taskchain", "me.glaremasters.guilds.libs.taskchain")
        relocate("co.aikar.acf", "me.glaremasters.guilds.libs.acf")
        relocate("dev.triumphteam.gui", "me.glaremasters.guilds.libs.triumphgui")
        relocate("ch.jalu.configme", "me.glaremasters.guilds.libs.configme")
        relocate("org.codemc.worldguardwrapper", "me.glaremasters.guilds.libs.worldguardwrapper")
        relocate("com.dumptruckman.minecraft", "me.glaremasters.guilds.libs.jsonconfiguration")
        relocate("net.kyori.adventure", "me.glaremasters.guilds.libs.adventure")
        relocate("net.kyori.examination", "me.glaremasters.guilds.libs.examination")
        relocate("net.kyori.option", "me.glaremasters.guilds.libs.option")
        relocate("com.zaxxer.hikari", "me.glaremasters.guilds.libs.hikari")
        relocate("org.jdbi", "me.glaremasters.guilds.libs.jdbi")
        relocate("org.mariadb", "me.glaremasters.guilds.libs.mariadb")

        // NOTE: minimize can break runtime if it strips "unused" classes loaded reflectively.
        // Start with it OFF while debugging.
        // minimize()

        archiveClassifier.set(null as String?)
        archiveFileName.set("Guilds-${project.version}.jar")
    }
    //shadowJar {
        //fun relocates(vararg dependencies: String) {
            //dependencies.forEach {
                //val split = it.split(".")
                //val name = split.last()
                //relocate(it, "me.glaremasters.guilds.libs.$name")
            //}
        //}

        //minimize()

        //archiveClassifier.set(null as String?)
        //archiveFileName.set("Guilds-${project.version}.jar")
        // keep Gradle Shadow default destination (build/libs)
    //}

    processResources {
        expand("version" to rootProject.version)
    }
}
