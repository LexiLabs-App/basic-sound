import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.multiplatform.library).apply(false)
    alias(libs.plugins.composeMultiplatform).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
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

    /** dokka generation **/
    dokka {
        moduleName.set(project.name)
        moduleVersion.set(project.version.toString())
        dokkaPublications.html {
            outputDirectory.set(rootDir.resolve("docs"))
            suppressObviousFunctions.set(true)
            suppressInheritedMembers.set(false)
            failOnWarning.set(false)
            offlineMode.set(false)
        }
        pluginsConfiguration.html {
            customAssets.from(rootDir.resolve("images/logo-icon.svg"))
            footerMessage.set("(c) 2026 LexiLabs")
        }
    }

    extensions.configure<MavenPublishBaseExtension> {

        mavenPublishing {
            publishToMavenCentral(automaticRelease = true)
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
