plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sardonicus.tobaccocellar"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "2.2.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        ksp {
            arg("room.schemaLocation", "${projectDir}/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    namespace = "com.sardonicus.tobaccocellar"
}

dependencies {

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.material3)
    implementation(libs.ui)
    implementation(libs.ui.tooling)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.viewpager2)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.commons.csv)
    implementation(libs.material)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.androidx.core.splashscreen)



    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.monitor)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.androidx.datastore.preferences.core.jvm)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

}