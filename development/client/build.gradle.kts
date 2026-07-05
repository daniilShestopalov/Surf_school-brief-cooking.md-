plugins {
    kotlin("multiplatform") version "1.9.23"
    id("org.jetbrains.compose") version "1.6.1"
    kotlin("plugin.serialization") version "1.9.23"
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.runtime)
                
                // Koin
                implementation("io.insert-koin:koin-core:3.5.3")
                implementation("io.insert-koin:koin-compose:1.1.2")
                
                // Voyager
                val voyagerVersion = "1.0.0"
                implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
                implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion")
                implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion")
                implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVersion")
                implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")
                implementation("cafe.adriel.voyager:voyager-koin:$voyagerVersion")

                // Ktor
                val ktorVersion = "2.3.9"
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-mock:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-auth:$ktorVersion")

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")

                // Settings
                implementation("com.russhwolf:multiplatform-settings:1.1.1")
                implementation("com.russhwolf:multiplatform-settings-test:1.1.1")

                // Datetime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.surfschool.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            packageName = "surfschool-client"
            packageVersion = "1.0.0"
        }
    }
}
