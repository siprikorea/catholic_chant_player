plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.siprikorea.catholicchant"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.siprikorea.catholicchant"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2
        versionName = "2.0"
    }

    // 디버그 APK 에는 media/ 를 직접 assets 로 포함해 바로 실행해 볼 수 있게 한다.
    // 릴리스(AAB)는 용량 제한 때문에 install-time asset pack(:androidMediaPack)으로 전달한다.
    sourceSets["debug"].assets.directories += "../media"

    assetPacks += ":androidMediaPack"

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    androidResources {
        // mp3/jpg 는 이미 압축된 포맷이고, MediaPlayer 가 openFd 로 읽으려면 비압축이어야 한다.
        noCompress += listOf("mp3", "jpg")
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
}
