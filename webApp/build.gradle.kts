import org.gradle.api.tasks.Copy

plugins {
    kotlin("multiplatform")
}

val copyPddToWeb = tasks.register<Copy>("copyPddToWeb") {
    from(project(":app").file("src/main/assets/pdd"))
    into(layout.projectDirectory.dir("src/jsMain/resources/pdd"))
}

kotlin {
    js(IR) {
        browser {
            runTask {
                devServerProperty.set(devServerProperty.get().copy(port = 8081, open = true))
            }
        }
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(npm("firebase", "10.14.0"))
            }
        }
    }
}

tasks.named("jsProcessResources").configure { dependsOn(copyPddToWeb) }
