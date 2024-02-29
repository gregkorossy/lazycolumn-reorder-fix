@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    id("maven-publish")
    id("signing")
}

group = "me.gingerninja.lazylist"
version = "1.1.0"

android {
    namespace = "me.gingerninja.lazylist.hijacker"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        aarMetadata {
            minCompileSdk = 21
        }

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    //api(libs.compose.foundation)
    compileOnly(libs.compose.foundation)
}

afterEvaluate {
    publishing {
        repositories {
            maven {
                url = if (project.version.toString().contains("SNAPSHOT")) {
                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                } else {
                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                }

                credentials {
                    // ossrhUsername and ossrhPassword are global properties
                    username = (project.properties["ossrhUsername"] ?: "").toString()
                    password = (project.properties["ossrhPassword"] ?: "").toString()
                }
            }
        }

        publications {
            register<MavenPublication>("release") {
                groupId = project.group.toString()
                artifactId = "hijacker"
                version = project.version.toString()

                afterEvaluate {
                    from(components["release"])
                }

                pom {
                    name = "${project.group}:hijacker"
                    description = "LazyList reorder fix by hijacking internal state with reflection"
                    url = "https://github.com/gregkorossy/lazylist-state-hijack"

                    scm {
                        url = "https://github.com/gregkorossy/lazylist-state-hijack"
                        connection = "scm:git@github.com:gregkorossy/lazylist-state-hijack.git"
                        developerConnection =
                            "scm:git@github.com:gregkorossy/lazylist-state-hijack.git"
                    }

                    licenses {
                        license {
                            name = "MIT License"
                            url =
                                "https://github.com/gregkorossy/lazylist-state-hijack/blob/main/LICENSE"
                            distribution = "repo"
                        }
                    }

                    developers {
                        developer {
                            id = "GregKorossy"
                            name = "Gergely Kőrössy"
                        }
                    }

                    issueManagement {
                        system = "GitHub"
                        url = "https://github.com/gregkorossy/lazylist-state-hijack/issues"
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}