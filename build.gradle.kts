import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
}

dependencies {
    kover(project(":basic-sound"))
}

allprojects {
    group = "app.lexilabs.basic"
    version = "0.2.5"

    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                name = "maven"
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
                credentials {
                    username = gradleLocalProperties(rootDir, providers).getProperty("sonatypeUsername")
                    password = gradleLocalProperties(rootDir, providers).getProperty("sonatypePassword")
                }
            }
        }

        val javadocJar = tasks.register<Jar>("javadocJar") {
            dependsOn(tasks.dokkaHtml)
            archiveClassifier.set("javadoc")
            from("${layout.buildDirectory}/dokka")
        }

        /** dokka generation **/
        tasks.register<Delete>("clearDokkaHtml") {
            delete("${projectDir.parent}/docs/${project.name}")
        }
        tasks.dokkaHtml {
            dependsOn("clearDokkaHtml")
            outputDirectory.set(file("${projectDir.parent}/docs/${project.name}"))
            moduleName.set(project.name)
            moduleVersion.set(project.version.toString())
            failOnWarning.set(false)
            suppressObviousFunctions.set(true)
            suppressInheritedMembers.set(false)
            offlineMode.set(false)
        }

        publications {
            withType<MavenPublication> {
                artifact(javadocJar)

                pom {
                    name.set("Basic")
                    description.set("Easily integrate audio playback into your Kotlin Multiplatform Mobile (KMP / KMM) project")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://raw.githubusercontent.com/LexiLabs-App/basic-sound/refs/heads/main/LICENSE")
                        }
                    }
                    url.set("https://github.com/LexiLabs-App/basic-sound")
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

    val publishing = extensions.getByType<PublishingExtension>()

    if (gradle.startParameter.taskNames.any { it == "publish" }) {
        extensions.configure<SigningExtension> {
            useInMemoryPgpKeys(
                gradleLocalProperties(rootDir, providers).getProperty("gpgKeyId"),
                gradleLocalProperties(rootDir, providers).getProperty("gpgKeySecret"),
                gradleLocalProperties(rootDir, providers).getProperty("gpgKeyPassword")
            )
            sign(publishing.publications)
        }
    } else {
        extensions.configure<SigningExtension> {
            useGpgCmd()
            sign(publishing.publications)
        }
    }

    // remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
    project.tasks.withType(AbstractPublishToMaven::class.java).configureEach {
        dependsOn(project.tasks.withType(Sign::class.java))
    }
}