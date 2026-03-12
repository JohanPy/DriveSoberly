plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.vaudibert.canidrive"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vaudibert.canidrive"
        minSdk = 21
        targetSdk = 35
        versionCode = 23
        versionName = "0.3.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("com.h6ah4i.android.widget.verticalseekbar:verticalseekbar:1.0.0")

    // Kotlin
    implementation("androidx.core:core-ktx:1.15.0")

    // Android
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("com.google.android.material:material:1.12.0")

    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Kotlin needs for testing
    testImplementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:5.11.0")

    // Navigation (Kotlin)
    val navigationVersion = "2.8.5"
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")

    // ViewModel and LiveData
    val lifecycleVersion = "2.8.7"
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")

    // optional - ReactiveStreams support for LiveData
    implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:$lifecycleVersion")

    // optional - Test helpers for LiveData
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // Preferences
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Security – encrypted SharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Koin for Dependency Injection
    val koinVersion = "3.5.3"
    implementation("io.insert-koin:koin-android:$koinVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
