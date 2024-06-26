plugins {
    id("com.android.application")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.softwaredesign.novelreader"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.softwaredesign.novelreader"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(project(mapOf("path" to ":scraper_library")))
    implementation(project(mapOf("path" to ":exporter_library")))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))

    // Import other Firebase Plugins
    implementation("com.google.firebase:firebase-analytics")

    // Import Picasso Image Loader
    implementation ("com.squareup.picasso:picasso:2.8")

    // Import JSoup
    implementation ("org.jsoup:jsoup:1.17.2")

    // Import Android Paging Runtime
    implementation ("androidx.paging:paging-runtime:3.3.0")
//    implementation ("org.apache.poi:poi:5.2.2")
//    implementation ("org.apache.poi:poi-ooxml:5.2.2")

    implementation("com.positiondev.epublib:epublib-core:3.1") {
        exclude(group = "xmlpull", module = "xmlpull")
    }

    implementation("androidx.test.espresso:espresso-intents:3.5.1")

    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Add the dependency for the Cloud Storage library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-storage")
}