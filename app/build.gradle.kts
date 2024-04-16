plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.dearmyhealth"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.dearmyhealth"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    dataBinding {
        enable = true
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.6.0")
    val lifecycle_version = "2.5.1"
    val room_version = "2.5.0"
    val nav_version = "2.5.3"
    val splitties_version = "3.0.0"

    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")

    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    implementation ("androidx.appcompat:appcompat:1.6.1")

    implementation("com.louiscad.splitties:splitties-alertdialog:$splitties_version")
    implementation("com.louiscad.splitties:splitties-activities:$splitties_version")
    implementation("com.wdullaer:materialdatetimepicker:4.2.3")

    // Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    kapt("android.arch.lifecycle:compiler:1.1.1")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Room
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // graph
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // page indicator view
    implementation("com.romandanylyk:pageindicatorview:1.0.3")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    // Okhttp3
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.10.0")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Add a dependency of Health Connect SDK
    implementation ("androidx.health.connect:connect-client:1.0.0-alpha11")
    //그래프뷰
    implementation ("com.jjoe64:graphview:4.2.2")

}