import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
}

dependencies { kover(project(":basic-sound")) }

buildscript {
    plugins { alias(libs.plugins.maven.publish) }
    dependencies { classpath(libs.dokka.base) }
}

allprojects {
    group = "app.lexilabs.basic"
    version = rootProject.libs.versions.sound.get()

    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.vanniktech.maven.publish")

    tasks.withType<DokkaTask>().configureEach{
        pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
            dependsOn("clearDokkaHtml")
            outputDirectory = file("${projectDir.parent}/docs")
            moduleName = project.name
            moduleVersion = project.version.toString()
            customAssets = listOf(file("${projectDir.parent}/images/logo-icon.svg"))
            // Need to create a cool looking theme at some point
            //customStyleSheets = listOf(file("${projectDir.parent}/dokka/styles.css"))
            footerMessage = "(c) 2025 LexiLabs"
            failOnWarning = false
            suppressObviousFunctions = true
            suppressInheritedMembers = false
            offlineMode = false
        }
    }

    /** dokka generation **/
    tasks.register<Delete>("clearDokkaHtml") {
        delete("${projectDir.parent}/docs")
    }

//    val javadocJar = tasks.register<Jar>("javadocJar") {
//        dependsOn(tasks.dokkaHtml)
//        archiveClassifier.set("javadoc")
//        from("${layout.buildDirectory}/dokka")
//    }

    extensions.configure<MavenPublishBaseExtension> {

        mavenPublishing {
            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

            signAllPublications()
            coordinates(group.toString(), project.name, version.toString())
            pom {
                name.set("Basic")
                description.set("Easily integrate audio playback into your Kotlin Multiplatform Mobile (KMP / KMM) project")
                url.set("https://github.com/LexiLabs-App/basic-sound")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://raw.githubusercontent.com/LexiLabs-App/basic-sound/refs/heads/main/LICENSE")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/LexiLabs-App/basic-sound/issues")
                }
                scm {
                    connection.set("https://github.com/LexiLabs-App/basic-sound.git")
                    url.set("https://github.com/LexiLabs-App/basic-sound")
                }
                developers {
                    developer {
                        id.set("rjamison")
                        name.set("Robert Jamison")
                        email.set("rjamison@lexilabs.app")
                        url.set("https://sound.basic.lexilabs.app")
                    }
                }
            }
        }
    }
}