plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

ksp {
    arg("room.schemaLocation", "${projectDir}/schemas")
}


kotlin {
//    compilerOptions {
//        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
//    }
    android {
        compileSdk = 36

        defaultConfig {
            applicationId = "com.sardonicus.tobaccocellar"
            minSdk = 26
            targetSdk = 36
            versionCode = 38
            versionName = "5.0.0"

            vectorDrawables {
                useSupportLibrary = true
            }
        }

//    sourceSets.all {
//        val variantName = name
//        kotlin.directories += "generated/ksp/$variantName/kotlin"
//        java.directories += "generated/ksp/$variantName/java"
//    }

        sourceSets.all {
            val variantName = name
            kotlin.directories.add(layout.buildDirectory.dir("generated/ksp/$variantName/kotlin").get().asFile.absolutePath)
            java.directories.add(layout.buildDirectory.dir("generated/ksp/$variantName/java").get().asFile.absolutePath)
        }

        buildTypes {
            release {
                isMinifyEnabled = true
                isShrinkResources = true

                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )

                signingConfig = signingConfigs.getByName("debug")

                ndk {
                    debugSymbolLevel = "SYMBOL_TABLE"
                }
            }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }

        buildFeatures {
            compose = true
            buildConfig = true
            resValues = true
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
                excludes += "/META-INF/INDEX.LIST"
                excludes += "/META-INF/DEPENDENCIES"
            }
        }

        namespace = "com.sardonicus.tobaccocellar"
    }
}

//android {
//    compileSdk = 36
//
//    defaultConfig {
//        applicationId = "com.sardonicus.tobaccocellar"
//        minSdk = 26
//        targetSdk = 36
//        versionCode = 38
//        versionName = "5.0.0"
//
//        vectorDrawables {
//            useSupportLibrary = true
//        }
//    }
//
////    sourceSets.all {
////        val variantName = name
////        kotlin.directories += "generated/ksp/$variantName/kotlin"
////        java.directories += "generated/ksp/$variantName/java"
////    }
//
//    sourceSets.all {
//        val variantName = name
//        kotlin.directories.add(layout.buildDirectory.dir("generated/ksp/$variantName/kotlin").get().asFile.absolutePath)
//        java.directories.add(layout.buildDirectory.dir("generated/ksp/$variantName/java").get().asFile.absolutePath)
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = true
//            isShrinkResources = true
//
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//
//            signingConfig = signingConfigs.getByName("debug")
//
//            ndk {
//                debugSymbolLevel = "SYMBOL_TABLE"
//            }
//        }
//    }
//
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_21
//        targetCompatibility = JavaVersion.VERSION_21
//    }
//
//    buildFeatures {
//        compose = true
//        buildConfig = true
//        resValues = true
//    }
//
//    packaging {
//        resources {
//            excludes += "/META-INF/{AL2.0,LGPL2.1}"
//            excludes += "/META-INF/INDEX.LIST"
//            excludes += "/META-INF/DEPENDENCIES"
//        }
//    }
//
//    namespace = "com.sardonicus.tobaccocellar"
//}

dependencies {

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.material3)
    implementation(libs.ui)
    implementation(libs.ui.tooling)
   // implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
  //  implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
  //  implementation(libs.androidx.viewpager2)
    runtimeOnly(libs.kotlinx.coroutines.android)
    implementation(libs.commons.csv)
  //  implementation(libs.material)
  //  implementation(libs.accompanist.system.ui.controller)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.core)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.google.play.services.auth)
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.drive)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.playservices)
    implementation(libs.google.id)

    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.windowsizeclass)
    implementation(libs.androidx.adaptive.layout)


    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.ui.text.google.fonts)
    ksp(libs.androidx.room.compiler)
  //  implementation(libs.androidx.room.ktx)

}