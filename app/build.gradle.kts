import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    // google-services / crashlytics는 Phase 7에서 활성화
}

android {
    namespace = "com.jewan.zipkr"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jewan.zipkr"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // 외부 키/ID는 모두 한 곳(defaultConfig)에서 default를 잡고, release만 Phase 7에서 override한다.
        // - JUSO_API_KEY: local.properties → Properties 객체 → BuildConfig 주입 (헌법 §9 시크릿 관리).
        // - ADMOB_*: Google 공식 테스트 ID를 default. release는 Phase 7에서 local.properties로
        //   BuildConfig·manifestPlaceholders를 동시에 override (실 ID 주입).
        // 테스트 ID 출처: https://developers.google.com/admob/android/test-ads
        // (자기 실 ID로 본인 클릭 시 AdMob 계정 정지 → 디버그·검증은 무조건 테스트 ID.)
        val localProps =
            Properties().apply {
                val file = rootProject.file("local.properties")
                if (file.exists()) load(file.inputStream())
            }
        val jusoKey = localProps.getProperty("juso.api.key", "")
        buildConfigField("String", "JUSO_API_KEY", "\"$jusoKey\"")

        // 카카오 키 두 개. REST = geocoding (Authorization header), JS = WebView 카카오맵 (HTML appkey).
        // 둘 다 같은 dev console 앱 등록에서 발급 (Web 플랫폼 등록 + 도메인 https://localhost 추가 필요).
        val kakaoRestKey = localProps.getProperty("kakao.rest.api.key", "")
        buildConfigField("String", "KAKAO_REST_API_KEY", "\"$kakaoRestKey\"")
        val kakaoJsKey = localProps.getProperty("kakao.js.api.key", "")
        buildConfigField("String", "KAKAO_JS_KEY", "\"$kakaoJsKey\"")
        if (kakaoRestKey.isBlank() || kakaoJsKey.isBlank()) {
            println("⚠ kakao.rest.api.key / kakao.js.api.key가 local.properties에 없다. 상세 시트 지도가 동작하지 않는다.")
        }
        buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-3940256099942544~3347511713\"")
        buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
        manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            // BuildConfig·manifestPlaceholders 모두 defaultConfig의 테스트 ID를 그대로 사용한다.
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // 실 AdMob ID는 Phase 7에서 local.properties → BuildConfig + manifestPlaceholders 동시 override.
            // Phase 7 전까지 release를 빌드하면 테스트 ID로 나가므로 출시 절대 금지.
        }
    }

    sourceSets {
        getByName("main") { java.srcDirs("src/main/kotlin") }
        getByName("debug") { java.srcDirs("src/debug/kotlin") }
        getByName("test") { java.srcDirs("src/test/kotlin") }
        getByName("androidTest") { java.srcDirs("src/androidTest/kotlin") }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion =
            libs.versions.compose.compiler
                .get()
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // Image
    implementation(libs.coil.compose)

    // Accompanist
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.systemuicontroller)

    // Logging
    implementation(libs.timber)
    debugImplementation(libs.leakcanary)

    // Ads
    implementation(libs.play.services.ads)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}

detekt {
    config.setFrom(files("$rootDir/detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = true
}

ktlint {
    version.set("1.3.1")
    android.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
    }
}
