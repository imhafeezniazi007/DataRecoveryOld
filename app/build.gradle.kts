plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.datarecoverynew"
    compileSdk = 35

    defaultConfig {
        applicationId =
            "com.data.recovery.restore.deleted.photos.videos.audios.docs.gallery.remove.duplicate.files"
        minSdk = 24
        targetSdk = 35
        versionCode = 15
        versionName = "15.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    bundle {
        language {
            enableSplit = false
        }
    }
    signingConfigs {

        create("release") {
            storeFile = file("../KEYSTORE/key_store_file.jks")
            keyAlias = "key0"
            keyPassword = "12345678"
            storePassword = "12345678"
        }
    }

    buildTypes {
        release {
            resValue("string", "admob_appid", "ca-app-pub-3940256099942544~3347511713")
            resValue("string", "admob_inter_statial", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_banner", "ca-app-pub-3940256099942544/6300978111")
            resValue("string", "admob_native_ad", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_app_open", "ca-app-pub-3940256099942544/9257395921")

            resValue("string", "iron_source_app_key", "1ded9629d")

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            resValue("string", "admob_appid", "ca-app-pub-3940256099942544~3347511713")
            resValue("string", "admob_inter_statial", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_banner", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_native_ad", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_app_open", "ca-app-pub-3940256099942544/9257395921")

            resValue("string", "iron_source_app_key", "1ded9629d")
//            isMinifyEnabled = true
//            isShrinkResources = true
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
            isDebuggable = true

            signingConfig = signingConfigs.getByName("release")
        }
    }



    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    viewBinding {
        enable = true
    }
    dataBinding {
        enable = true
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.room:room-common:2.6.1")
    implementation("androidx.activity:activity-ktx:1.9.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")

    implementation("com.intuit.ssp:ssp-android:1.1.1")
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
//md5 checksum
    implementation("com.blankj:utilcodex:1.31.0")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.akexorcist:localization:1.2.11")

    implementation("com.airbnb.android:lottie:6.4.1")

    implementation("com.hbb20:android-country-picker:0.0.7")

    implementation("com.google.firebase:firebase-core:21.1.1")


    implementation("com.google.firebase:firebase-database")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Add the dependency for the Analytics library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-config-ktx")

    implementation("com.google.android.gms:play-services-ads:23.6.0")
    implementation("com.google.ads.mediation:facebook:6.18.0.0")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.squareup.okhttp3:logging-interceptor:4.7.2")
    implementation("com.google.android.ump:user-messaging-platform:3.1.0")

//    implementation("com.facebook.android:facebook-android-sdk:17.0.1")

//    implementation("com.facebook.android:audience-network-sdk:6.18.0")

// Facebook Android SDK

    implementation("com.github.ybq:Android-SpinKit:1.4.0")
//
//    implementation("com.adjust.sdk:adjust-android:4.38.5")
    implementation("com.android.installreferrer:installreferrer:2.2")

    implementation("com.google.android.gms:play-services-ads-identifier:18.2.0")

    implementation("com.google.android.gms:play-services-appset:16.1.0")
    implementation("com.facebook.infer.annotation:infer-annotation:0.18.0")

    implementation("com.android.billingclient:billing-ktx:7.1.1")

    implementation("com.google.android.play:review-ktx:2.0.2")
    implementation("com.google.android.ads:mediation-test-suite:3.0.0")
    implementation("com.karumi:dexter:6.2.3")

}