plugins {
    kotlin("multiplatform")
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
