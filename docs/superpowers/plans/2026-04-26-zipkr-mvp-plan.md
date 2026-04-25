# zipkr MVP 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 한국 우편번호 조회 안드로이드 앱(zipkr) v1.0 MVP를 구현해 Google Play Production에 출시한다. 동시에 후속 프로젝트들이 재사용할 공통 자산(테마·컴포넌트·DI·Repository 패턴·AdMob 래퍼·i18n 셋업)의 시드를 추출 가능한 구조로 만든다.

**Architecture:** Kotlin + Jetpack Compose 네이티브 안드로이드. MVVM + Repository + 다중 Provider 추상화 + Hilt(DI). 행안부 도로명주소 API 메인 데이터 소스. AdMob 하단 배너. KO/EN 다국어. Single-module로 시작하되 추후 multi-module 추출이 자연스러운 패키지 경계.

**Tech Stack:** Kotlin · Jetpack Compose · Material3 · Navigation Compose · Hilt · Retrofit · OkHttp · kotlinx.serialization · Coil · AndroidX Splash Screen API · Accompanist · KSP · Detekt · Ktlint · Timber · LeakCanary · Firebase Crashlytics · MockK · Turbine · Truth · Google Mobile Ads SDK · Pretendard

**관련 문서:** spec [`../specs/2026-04-25-postal-app-design.md`](../specs/2026-04-25-postal-app-design.md) · ADR [`../../decisions/0001-android-library-stack.md`](../../decisions/0001-android-library-stack.md)

**준수 표준:** 내부 개발 헌법(JEWAN_DEV_CONSTITUTION) — 함수 ≤50줄, 파일 ≤400줄, 인자 ≤4개, 한글 주석 `~다.` 문체, Conventional Commits 한글, develop 직접 커밋 금지(Phase 끝마다 feat → PR), `latest` 버전 금지(Version Catalog로 명시 고정).

---

## 작업 흐름 표준

- **브랜치 전략 (헌법 §6):**
  - 매 Phase 시작 시 `develop`에서 `feat/<phase>-v1` 브랜치 분기
  - Phase 내부 task 단위로 자주 commit (atomic, Conventional Commits 한글)
  - Phase 완료 후 GitHub에서 PR → `develop` 병합
  - main은 출시 시점에만 develop → main PR
- **TDD 적용 범위:**
  - 비즈니스 로직(Repository, Provider, ViewModel, util) → 단위 테스트 우선 (TDD red-green-refactor)
  - Compose UI → Preview + Instrumented UI 테스트 1개 (핵심 동선)
- **Commit 메시지:** Conventional + 한글 (예: `feat: 검색 입력 화면 추가`, `test: AddressRepository 단위 테스트 추가`, `chore: Hilt DI 모듈 셋업`)
- **자동 push 금지:** Phase 완료 PR 시점에만 형 컨펌 후 push

---

## File Structure (생성 예정)

```
zipkr/
├─ build.gradle.kts                              ← 프로젝트 빌드 (KSP·Hilt 플러그인)
├─ settings.gradle.kts                           ← 모듈 선언
├─ gradle/
│   └─ libs.versions.toml                        ← Version Catalog (모든 의존성 명시)
├─ local.properties                              ← (gitignore) JUSO_API_KEY, ADMOB_APP_ID 등
├─ detekt.yml                                    ← Detekt 설정
├─ app/
│   ├─ build.gradle.kts                          ← 앱 모듈 빌드 (buildTypes 분리)
│   ├─ proguard-rules.pro                        ← Hilt·Retrofit·Serialization 보존 룰
│   ├─ src/
│   │   ├─ main/
│   │   │   ├─ AndroidManifest.xml
│   │   │   ├─ kotlin/com/jewan/zipkr/
│   │   │   │   ├─ ZipkrApp.kt                   ← @HiltAndroidApp + Timber 초기화
│   │   │   │   ├─ MainActivity.kt               ← @AndroidEntryPoint + Splash + Nav
│   │   │   │   ├─ ui/
│   │   │   │   │   ├─ ZipkrApp.kt               ← Composable 진입 (NavHost)
│   │   │   │   │   ├─ theme/
│   │   │   │   │   │   ├─ Color.kt
│   │   │   │   │   │   ├─ Type.kt               ← Pretendard 폰트
│   │   │   │   │   │   ├─ Shape.kt
│   │   │   │   │   │   └─ Theme.kt              ← ZipkrTheme Composable
│   │   │   │   │   ├─ components/
│   │   │   │   │   │   ├─ EmptyState.kt
│   │   │   │   │   │   ├─ ErrorView.kt
│   │   │   │   │   │   ├─ LoadingSkeleton.kt
│   │   │   │   │   │   └─ AddressResultCard.kt  ← 카드에서 1탭 복사
│   │   │   │   │   ├─ search/
│   │   │   │   │   │   ├─ SearchScreen.kt
│   │   │   │   │   │   ├─ SearchViewModel.kt
│   │   │   │   │   │   └─ SearchUiState.kt
│   │   │   │   │   └─ detail/
│   │   │   │   │       ├─ DetailScreen.kt
│   │   │   │   │       ├─ DetailViewModel.kt
│   │   │   │   │       └─ DetailUiState.kt
│   │   │   │   ├─ data/
│   │   │   │   │   ├─ Address.kt                ← 도메인 모델
│   │   │   │   │   ├─ AddressRepository.kt      ← 인터페이스
│   │   │   │   │   ├─ AddressRepositoryImpl.kt
│   │   │   │   │   ├─ AppError.kt               ← 도메인 에러 (sealed)
│   │   │   │   │   └─ provider/
│   │   │   │   │       ├─ AddressProvider.kt    ← 인터페이스
│   │   │   │   │       └─ juso/
│   │   │   │   │           ├─ JusoApi.kt        ← Retrofit 인터페이스
│   │   │   │   │           ├─ JusoModels.kt     ← DTO + 매핑
│   │   │   │   │           └─ JusoApiProvider.kt
│   │   │   │   ├─ ads/
│   │   │   │   │   └─ AdBanner.kt               ← AdMob Compose 래퍼
│   │   │   │   ├─ util/
│   │   │   │   │   ├─ Clipboard.kt
│   │   │   │   │   ├─ Share.kt
│   │   │   │   │   └─ Haptic.kt
│   │   │   │   └─ di/
│   │   │   │       ├─ NetworkModule.kt          ← Retrofit·OkHttp 제공
│   │   │   │       ├─ DataModule.kt             ← Repository·Provider 바인딩
│   │   │   │       └─ AppModule.kt              ← 앱 전역 객체
│   │   │   └─ res/
│   │   │       ├─ values/strings.xml            ← KO 기본
│   │   │       ├─ values-en/strings.xml         ← EN
│   │   │       ├─ values/themes.xml
│   │   │       ├─ font/pretendard_variable.ttf
│   │   │       └─ ...
│   │   ├─ test/                                 ← 단위 테스트
│   │   │   └─ kotlin/com/jewan/zipkr/
│   │   │       ├─ data/
│   │   │       │   ├─ AddressRepositoryImplTest.kt
│   │   │       │   └─ provider/juso/
│   │   │       │       ├─ JusoApiProviderTest.kt
│   │   │       │       └─ JusoModelsTest.kt
│   │   │       └─ ui/search/
│   │   │           └─ SearchViewModelTest.kt
│   │   └─ androidTest/                          ← UI 테스트
│   │       └─ kotlin/com/jewan/zipkr/
│   │           └─ ui/search/
│   │               └─ SearchScreenTest.kt
│   └─ google-services.json                      ← (gitignore 권장) Firebase 설정
├─ docs/
│   ├─ superpowers/specs/...                     ← (이미 있음)
│   ├─ superpowers/plans/...                     ← (이 문서)
│   ├─ decisions/0001-android-library-stack.md   ← (이미 있음)
│   ├─ ARCHITECTURE.md                           ← Phase 10에서 생성
│   └─ RUNBOOK.md                                ← 출시 후 작성 (이번 plan 범위 외)
├─ CHANGELOG.md                                  ← Phase 10에서 생성
├─ README.md                                     ← Phase 10에서 보강
└─ .gitignore                                    ← (이미 있음, 보강)
```

---

## Phase 0 — 프로젝트 부트스트랩

**브랜치:** `feat/bootstrap-v1`

**목표:** 빈 zipkr 디렉토리에 Kotlin + Compose Android Studio 프로젝트 골격을 만들고, Version Catalog로 모든 의존성 버전을 명시 고정한 상태로 빌드가 통과하게 한다.

### Task 0.1: feat 브랜치 생성

- [ ] **Step 1: develop 최신화 확인**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr fetch origin
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr pull origin develop
```

Expected: `Already up to date.` 또는 fast-forward.

- [ ] **Step 2: feat 브랜치 분기**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout -b feat/bootstrap-v1
```

Expected: `Switched to a new branch 'feat/bootstrap-v1'`.

### Task 0.2: Android Studio 프로젝트 생성 (형 직접)

**Files:**
- Create: `app/`, `gradle/`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `app/src/main/kotlin/com/jewan/zipkr/MainActivity.kt`, `app/src/main/res/...`

- [ ] **Step 1: Android Studio에서 프로젝트 생성**

Android Studio → New Project → **Empty Activity (Compose)** 선택.
- Name: `zipkr`
- Package name: `com.jewan.zipkr`
- Save location: `/Users/jewan/Desktop/git/98_jewan/zipkr`
- Language: Kotlin
- Build configuration: Kotlin DSL
- Minimum SDK: API 24 (Android 7.0)
- Compile/Target SDK: 34
- Build configuration language: Kotlin DSL with Catalog (Version Catalog 자동 생성)

> 기존 zipkr 폴더에 그대로 생성. `docs/`, `README.md`, `.git/` 보존.

- [ ] **Step 2: 빌드 정상 확인**

Android Studio Sync 완료 후 Run → 에뮬레이터/기기에서 기본 "Hello Android!" 화면 표시 확인.

- [ ] **Step 3: kotlin 소스 디렉토리 표준화**

Android Studio가 `app/src/main/java/...` 로 생성한 경우, `app/src/main/kotlin/...` 으로 이동하고 `app/build.gradle.kts`의 `sourceSets`에 다음 추가:

```kotlin
android {
    sourceSets {
        getByName("main") { java.srcDirs("src/main/kotlin") }
        getByName("test") { java.srcDirs("src/test/kotlin") }
        getByName("androidTest") { java.srcDirs("src/androidTest/kotlin") }
    }
}
```

이후 한 번 더 빌드 통과 확인.

- [ ] **Step 4: 패키지 구조 빈 폴더 생성**

`app/src/main/kotlin/com/jewan/zipkr/` 아래에 빈 폴더 생성:
- `ui/theme/`
- `ui/components/`
- `ui/search/`
- `ui/detail/`
- `data/provider/juso/`
- `ads/`
- `util/`
- `di/`

(빈 폴더 git 추적용 `.gitkeep` 파일 한 개씩.)

- [ ] **Step 5: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add .
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "chore: Android Studio Compose 프로젝트 부트스트랩

- Empty Activity (Compose) 템플릿 기반 zipkr 프로젝트 생성.
- 패키지: com.jewan.zipkr (minSdk 24, targetSdk 34).
- src/main/kotlin 표준화, 패키지 구조 빈 폴더(.gitkeep) 셋업.
"
```

### Task 0.3: Version Catalog 셋업

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: libs.versions.toml 작성**

```toml
[versions]
# Kotlin / Build
kotlin = "1.9.24"
agp = "8.5.2"
ksp = "1.9.24-1.0.20"

# AndroidX
core-ktx = "1.13.1"
lifecycle = "2.8.4"
activity-compose = "1.9.1"
splashscreen = "1.0.1"

# Compose
compose-bom = "2024.08.00"
compose-compiler = "1.5.14"
navigation-compose = "2.7.7"
hilt-navigation-compose = "1.2.0"

# Hilt
hilt = "2.51.1"

# Network
retrofit = "2.11.0"
okhttp = "4.12.0"
kotlinx-serialization = "1.7.1"
retrofit-kotlinx-serialization = "1.0.0"

# Image
coil = "2.7.0"

# Accompanist
accompanist = "0.34.0"

# Logging / Quality
timber = "5.0.1"
leakcanary = "2.14"
detekt = "1.23.6"
ktlint = "12.1.1"

# Firebase / Ads
firebase-bom = "33.1.2"
google-services = "4.4.2"
crashlytics-plugin = "3.0.2"
admob = "23.2.0"

# Test
junit = "4.13.2"
mockk = "1.13.12"
turbine = "1.1.0"
truth = "1.4.4"
androidx-test-junit = "1.2.1"
espresso-core = "3.6.1"
compose-ui-test = "1.6.8"

[libraries]
# AndroidX core
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "core-ktx" }
androidx-lifecycle-runtime = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activity-compose" }
androidx-splashscreen = { module = "androidx.core:core-splashscreen", version.ref = "splashscreen" }

# Compose
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-material-icons = { module = "androidx.compose.material:material-icons-extended" }
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation-compose" }
hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hilt-navigation-compose" }

# Hilt
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hilt" }

# Network
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
retrofit-kotlinx-serialization = { module = "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter", version.ref = "retrofit-kotlinx-serialization" }
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
okhttp-logging = { module = "com.squareup.okhttp3:logging-interceptor", version.ref = "okhttp" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }

# Image
coil-compose = { module = "io.coil-kt:coil-compose", version.ref = "coil" }

# Accompanist
accompanist-permissions = { module = "com.google.accompanist:accompanist-permissions", version.ref = "accompanist" }
accompanist-systemuicontroller = { module = "com.google.accompanist:accompanist-systemuicontroller", version.ref = "accompanist" }

# Logging / Quality
timber = { module = "com.jakewharton.timber:timber", version.ref = "timber" }
leakcanary = { module = "com.squareup.leakcanary:leakcanary-android", version.ref = "leakcanary" }

# Firebase
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebase-bom" }
firebase-crashlytics = { module = "com.google.firebase:firebase-crashlytics" }
firebase-analytics = { module = "com.google.firebase:firebase-analytics" }

# Ads
play-services-ads = { module = "com.google.android.gms:play-services-ads", version.ref = "admob" }

# Test
junit = { module = "junit:junit", version.ref = "junit" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
truth = { module = "com.google.truth:truth", version.ref = "truth" }
androidx-test-junit = { module = "androidx.test.ext:junit", version.ref = "androidx-test-junit" }
espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "espresso-core" }
compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
google-services = { id = "com.google.gms.google-services", version.ref = "google-services" }
firebase-crashlytics = { id = "com.google.firebase.crashlytics", version.ref = "crashlytics-plugin" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint" }
```

- [ ] **Step 2: 빌드 통과 확인**

Android Studio에서 Sync, 또는:

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew help
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add gradle/libs.versions.toml
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "chore: Version Catalog 전체 의존성 명시 고정

- 헌법 §8: latest 금지, 모든 버전 명시.
- AndroidX·Compose·Hilt·Retrofit·Coil·Accompanist·
  Timber·LeakCanary·Detekt·Ktlint·Firebase·AdMob·
  MockK·Turbine·Truth 일괄 등록.
"
```

### Task 0.4: 루트 build.gradle.kts에 플러그인 등록

**Files:**
- Modify: `build.gradle.kts` (project root)

- [ ] **Step 1: 플러그인 등록**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
}
```

- [ ] **Step 2: Sync 통과 확인**

Android Studio Sync 또는 `./gradlew help`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add build.gradle.kts
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "chore: 루트 build에 KSP·Hilt·Firebase·Detekt·Ktlint 플러그인 등록"
```

### Task 0.5: app/build.gradle.kts — 플러그인·의존성·buildTypes 셋업

**Files:**
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: 전체 내용 작성**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
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

        // local.properties → BuildConfig 노출 (다음 Task에서 채움)
        buildConfigField("String", "JUSO_API_KEY", "\"\"")
        buildConfigField("String", "ADMOB_APP_ID", "\"\"")
        buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            // Google 공식 테스트 광고 ID (출시 절대 사용 금지)
            buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-3940256099942544~3347511713\"")
            buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 실제 광고 ID는 Phase 7에서 local.properties → BuildConfig 주입으로 대체한다.
        }
    }

    sourceSets {
        getByName("main") { java.srcDirs("src/main/kotlin") }
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
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
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
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
```

- [ ] **Step 2: Sync + 빌드 통과 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr/.worktrees/bootstrap && ./gradlew :app:assembleDebug
```

Expected: BUILD SUCCESSFUL. (이 시점엔 Hilt @HiltAndroidApp 미적용으로 런타임은 아직 실패할 수 있으나 컴파일은 통과해야 함.)

- [ ] **Step 3: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/build.gradle.kts
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "chore: app 모듈 빌드 셋업 (Hilt·Compose·Network·Ads·buildTypes)

- compileSdk 34 / minSdk 24 / Java 17.
- buildTypes: debug(테스트 광고 ID) / release(실 광고 ID는 Phase 7에서 주입).
- 단순 앱이므로 flavor 미사용 — buildTypes만으로 광고 ID 분리.
- Compose BOM, Hilt, Retrofit, OkHttp, Coil, Accompanist, Timber,
  LeakCanary(debug), AdMob, 테스트 라이브러리(MockK·Turbine·Truth) 포함.
"
```

### Task 0.6: .gitignore 보강

**Files:**
- Modify: `.gitignore`

- [ ] **Step 1: Android 표준 항목 + 시크릿 추가**

`.gitignore` 끝에 추가:

```gitignore
# === Android / Kotlin ===
*.iml
.gradle/
local.properties
.idea/
.DS_Store
build/
captures/
.externalNativeBuild/
.cxx/
*.apk
*.aab
*.ap_
*.dex
*.class
proguard/

# === Secrets ===
google-services.json
**/google-services.json
*.keystore
*.jks
keystore.properties

# === Reports ===
**/build/reports/
```

- [ ] **Step 2: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add .gitignore
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "chore: .gitignore에 Android 빌드 산출물·시크릿 패턴 추가

local.properties, *.keystore, google-services.json 모두 제외.
"
```

### Task 0.7: Detekt + Ktlint 설정

**Files:**
- Create: `detekt.yml`
- Modify: `app/build.gradle.kts` (detekt/ktlint 블록 추가)

- [ ] **Step 1: detekt.yml 기본 설정 생성**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew detektGenerateConfig
```

생성된 `app/config/detekt/detekt.yml`을 루트 `detekt.yml`로 이동.

헌법 수치 기준 반영을 위해 다음 룰 override:

```yaml
complexity:
  LongMethod:
    active: true
    threshold: 50
  LongParameterList:
    active: true
    functionThreshold: 4
    constructorThreshold: 4
  TooManyFunctions:
    active: true
    thresholdInClasses: 15
  ComplexCondition:
    active: true
    threshold: 4
  CyclomaticComplexMethod:
    active: true
    threshold: 10
  NestedBlockDepth:
    active: true
    threshold: 3

style:
  MagicNumber:
    active: true
  MaxLineLength:
    active: true
    maxLineLength: 120
```

- [ ] **Step 2: app/build.gradle.kts에 detekt/ktlint 설정 추가**

`app/build.gradle.kts` 파일 하단에 추가:

```kotlin
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
```

- [ ] **Step 3: 통과 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:detekt :app:ktlintCheck
```

Expected: BUILD SUCCESSFUL (또는 일부 위반 → autoCorrect 또는 수정).

- [ ] **Step 4: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add detekt.yml app/build.gradle.kts
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "chore: Detekt·Ktlint 셋업 — 헌법 수치 기준 룰 적용

- LongMethod 50, LongParameterList 4, CyclomaticComplexity 10,
  NestedBlockDepth 3 등 헌법 §1.7 강제.
- ktlint autoCorrect 활성화.
"
```

### Task 0.8: ZipkrApp Application 클래스 + AndroidManifest 보강

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ZipkrApp.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: ZipkrApp.kt 작성**

```kotlin
package com.jewan.zipkr

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * 앱 전역 진입점이다.
 * 디버그 빌드에서만 Timber DebugTree를 심어 로컬 콘솔로 로그를 흘린다.
 * 릴리스 빌드는 Phase 7에서 Crashlytics Tree로 교체한다.
 */
@HiltAndroidApp
class ZipkrApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
```

- [ ] **Step 2: AndroidManifest.xml에 application 등록**

`<application>` 태그에 다음 속성/요소 추가:

```xml
<application
    android:name=".ZipkrApp"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:theme="@style/Theme.Zipkr"
    android:supportsRtl="true">

    <!-- 인터넷 권한은 manifest 상단에 -->

    <activity
        android:name=".MainActivity"
        android:exported="true"
        android:theme="@style/Theme.App.Starting">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>
```

`<manifest>` 직속에 권한 추가:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

- [ ] **Step 3: 빌드 + 실행 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:assembleDevDebug
```

Expected: BUILD SUCCESSFUL. 에뮬레이터에서 실행 시 기본 화면 표시.

- [ ] **Step 4: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/ZipkrApp.kt app/src/main/AndroidManifest.xml
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat: Hilt 진입점(ZipkrApp) + Timber 디버그 로깅 셋업

- @HiltAndroidApp 적용으로 DI 컨테이너 부트스트랩.
- 디버그 빌드에서만 Timber DebugTree 심음.
- AndroidManifest에 application 등록 + 인터넷·네트워크 상태 권한 추가.
"
```

### Task 0.9: PR 생성 + develop 병합

- [ ] **Step 1: 푸시 (형 컨펌 후)**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr push -u origin feat/bootstrap-v1
```

- [ ] **Step 2: GitHub에서 PR 생성 (형 직접)**

`feat/bootstrap-v1` → `develop` PR. 제목: `feat: zipkr 프로젝트 부트스트랩 (Phase 0)`. 본문은 Phase 0 task 요약.

- [ ] **Step 3: 형 셀프 리뷰 + Squash Merge 또는 Merge Commit**

병합 후 로컬에서:

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr pull origin develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr branch -d feat/bootstrap-v1
```

---

## Phase 1 — 디자인 시스템 (theme + 폰트 + 공통 컴포넌트)

**브랜치:** `feat/design-system-v1`

**목표:** 압도적 디자인 차별화의 토대인 `ui/theme/`와 공통 컴포넌트(`ui/components/`) 자산을 만든다. 후속 프로젝트들이 그대로 복붙 가능한 형태로.

### Task 1.1: feat 브랜치 생성

- [ ] **Step 1: develop 최신화 + 분기**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr pull origin develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout -b feat/design-system-v1
```

### Task 1.2: Pretendard 폰트 추가

**Files:**
- Create: `app/src/main/res/font/pretendard_variable.ttf`
- Create: `app/src/main/res/font/pretendard.xml`

- [ ] **Step 1: 폰트 다운로드 (형 직접)**

https://github.com/orioncactus/pretendard/releases 에서 최신 PretendardVariable.ttf 다운로드.
파일을 `app/src/main/res/font/pretendard_variable.ttf`로 저장. 라이센스: SIL Open Font License 1.1 (상업 사용 OK).

- [ ] **Step 2: font family 정의**

`app/src/main/res/font/pretendard.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<font-family xmlns:android="http://schemas.android.com/apk/res/android">
    <font
        android:font="@font/pretendard_variable"
        android:fontWeight="400"
        android:fontStyle="normal" />
    <font
        android:font="@font/pretendard_variable"
        android:fontWeight="500"
        android:fontStyle="normal" />
    <font
        android:font="@font/pretendard_variable"
        android:fontWeight="700"
        android:fontStyle="normal" />
</font-family>
```

- [ ] **Step 3: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/res/font/
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(theme): Pretendard 폰트 추가 (한·영 우아함 베이스라인)

SIL Open Font License 1.1 — 상업 사용 가능.
"
```

### Task 1.3: theme/Color.kt 정의

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/theme/Color.kt`

- [ ] **Step 1: 컬러 토큰 작성**

```kotlin
package com.jewan.zipkr.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 앱의 컬러 토큰을 정의한다.
 * - Light/Dark 시스템 자동 추종 (Theme.kt에서 결정).
 * - 브랜드 액센트는 단일 색(Indigo 600 계열) — 절제된 모던 톤.
 */
internal object ZipkrColors {
    val BrandAccent = Color(0xFF4F46E5)      // Indigo 600
    val BrandAccentDark = Color(0xFF818CF8)  // Indigo 400 (다크 모드용)

    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceDark = Color(0xFF111827)
    val OnSurfaceLight = Color(0xFF111827)
    val OnSurfaceDark = Color(0xFFF9FAFB)

    val SubtleLight = Color(0xFFF3F4F6)
    val SubtleDark = Color(0xFF1F2937)
    val OnSubtleLight = Color(0xFF6B7280)
    val OnSubtleDark = Color(0xFF9CA3AF)

    val ErrorLight = Color(0xFFDC2626)
    val ErrorDark = Color(0xFFF87171)
}
```

- [ ] **Step 2: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/ui/theme/Color.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(theme): Color 토큰 정의 (Light/Dark + 브랜드 액센트 1색)"
```

### Task 1.4: theme/Type.kt — Pretendard 기반 타이포

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/theme/Type.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.jewan.zipkr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jewan.zipkr.R

/**
 * Pretendard 가변 폰트 기반 타이포그래피를 정의한다.
 * Material3의 Typography 카테고리에 매핑한다.
 */
private val Pretendard = FontFamily(
    Font(R.font.pretendard_variable, FontWeight.Normal),
    Font(R.font.pretendard_variable, FontWeight.Medium),
    Font(R.font.pretendard_variable, FontWeight.Bold),
)

internal val ZipkrTypography = Typography(
    displayLarge = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 56.sp),
    headlineLarge = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    titleLarge = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
)
```

- [ ] **Step 2: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/ui/theme/Type.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(theme): Pretendard 기반 Typography 매핑 (Material3 카테고리)"
```

### Task 1.5: theme/Shape.kt + Theme.kt

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/theme/Shape.kt`
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/theme/Theme.kt`

- [ ] **Step 1: Shape.kt 작성**

```kotlin
package com.jewan.zipkr.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 카드·버튼·다이얼로그의 라운딩 토큰을 정의한다.
 * 기본 카드 라운딩은 16dp — 모던 톤의 핵심.
 */
internal val ZipkrShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
```

- [ ] **Step 2: Theme.kt 작성**

```kotlin
package com.jewan.zipkr.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * 앱의 단일 진입 테마이다.
 * - Android 12+ Dynamic Color 자동 적용 (사용자 시스템 컬러 추종).
 * - 그 외 OS는 Color.kt의 자체 토큰 fallback.
 * - 시스템 다크 모드 자동 추종.
 */
@Composable
fun ZipkrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = ZipkrColors.BrandAccentDark,
            background = ZipkrColors.SurfaceDark,
            surface = ZipkrColors.SurfaceDark,
            onSurface = ZipkrColors.OnSurfaceDark,
            error = ZipkrColors.ErrorDark,
        )
        else -> lightColorScheme(
            primary = ZipkrColors.BrandAccent,
            background = ZipkrColors.SurfaceLight,
            surface = ZipkrColors.SurfaceLight,
            onSurface = ZipkrColors.OnSurfaceLight,
            error = ZipkrColors.ErrorLight,
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = ZipkrTypography,
        shapes = ZipkrShapes,
        content = content,
    )
}
```

- [ ] **Step 3: 빌드 통과 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:assembleDevDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/ui/theme/Shape.kt app/src/main/kotlin/com/jewan/zipkr/ui/theme/Theme.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(theme): Shape + Theme(Dynamic Color/Dark 자동 추종) 진입점 추가"
```

### Task 1.6: 공통 컴포넌트 — EmptyState

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/components/EmptyState.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.jewan.zipkr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jewan.zipkr.ui.theme.ZipkrTheme

/**
 * 검색 결과가 없거나 진입 직후의 "비어있는 상태"를 표현한다.
 * 일러스트는 v2에서 추가, MVP는 텍스트 중심으로 깔끔하게 처리한다.
 */
@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    ZipkrTheme {
        EmptyState(title = "어떤 주소든 입력해보세요", description = "도로명·지번·건물명 모두 검색됩니다.")
    }
}
```

- [ ] **Step 2: Preview 렌더 확인**

Android Studio Preview 창에서 EmptyStatePreview 표시 정상 확인.

- [ ] **Step 3: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/ui/components/EmptyState.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(ui): EmptyState 공통 컴포넌트 추가"
```

### Task 1.7: 공통 컴포넌트 — ErrorView

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/components/ErrorView.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.jewan.zipkr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jewan.zipkr.ui.theme.ZipkrTheme

/**
 * API 호출 실패·네트워크 오류 등 사용자에게 재시도를 권할 때 노출한다.
 * 사용자 친화 메시지만 표시하고, 내부 예외 원문은 노출하지 않는다 (헌법 §1.9).
 */
@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) { Text("다시 시도") }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorViewPreview() {
    ZipkrTheme {
        ErrorView(message = "잠시 후 다시 시도해주세요", onRetry = {})
    }
}
```

- [ ] **Step 2: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/ui/components/ErrorView.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(ui): ErrorView 공통 컴포넌트 추가 (재시도 버튼 포함)"
```

### Task 1.8: 공통 컴포넌트 — LoadingSkeleton

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/components/LoadingSkeleton.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.jewan.zipkr.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jewan.zipkr.ui.theme.ZipkrTheme

/**
 * 검색 로딩 중 표시하는 스켈레톤 카드 N장이다.
 * 사용자에게 "곧 결과가 온다"는 즉시성 신호를 준다.
 */
@Composable
fun LoadingSkeleton(
    cardCount: Int = 3,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(cardCount) { SkeletonCard() }
    }
}

@Composable
private fun SkeletonCard() {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 800), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
            .alpha(alpha),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.outline))
        Spacer(Modifier.fillMaxWidth(0.7f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.outline))
        Spacer(Modifier.fillMaxWidth(0.5f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.outline))
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingSkeletonPreview() {
    ZipkrTheme { LoadingSkeleton() }
}
```

- [ ] **Step 2: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/ui/components/LoadingSkeleton.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(ui): LoadingSkeleton 공통 컴포넌트 추가 (펄스 애니메이션)"
```

### Task 1.9: PR 생성 + develop 병합

- [ ] **Step 1: 푸시 (형 컨펌 후) + PR**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr push -u origin feat/design-system-v1
```

GitHub에서 `feat/design-system-v1` → `develop` PR. 제목: `feat: zipkr 디자인 시스템 (Phase 1)`.

- [ ] **Step 2: Squash Merge 후 로컬 정리**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr pull origin develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr branch -d feat/design-system-v1
```

---

## Phase 2 — 데이터 레이어 (Repository + Provider + 행안부 API)

**브랜치:** `feat/data-v1`

**목표:** 헥사고날 정신에 따라 Repository/Provider 인터페이스를 먼저 정의하고, 행안부 도로명주소 API의 Retrofit 구현체를 TDD로 만든다. 이 패턴이 후속 프로젝트의 데이터 레이어 자산이 된다.

### Task 2.1: feat 브랜치 생성

- [ ] **Step 1: 분기**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr pull origin develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout -b feat/data-v1
```

### Task 2.2: Address 도메인 모델 + AppError 정의

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/data/Address.kt`
- Create: `app/src/main/kotlin/com/jewan/zipkr/data/AppError.kt`

- [ ] **Step 1: Address.kt 작성**

```kotlin
package com.jewan.zipkr.data

/**
 * 도메인 주소 모델이다.
 * 외부 API DTO(JusoModels)와 분리되며, UI는 항상 이 모델만 본다.
 */
data class Address(
    val zipCode: String,
    val roadAddress: String,
    val jibunAddress: String,
    val englishAddress: String,
)
```

- [ ] **Step 2: AppError.kt 작성**

```kotlin
package com.jewan.zipkr.data

/**
 * 도메인 에러 타입이다.
 * 사용자 응답에 내부 예외 원문은 노출하지 않으며 (헌법 §1.9),
 * UI는 type만 보고 친화 메시지를 매핑한다.
 */
sealed class AppError(open val cause: Throwable? = null) {
    data class Network(override val cause: Throwable? = null) : AppError(cause)
    data class ApiBadResponse(val code: String, val message: String) : AppError()
    data class Unknown(override val cause: Throwable? = null) : AppError(cause)
}
```

- [ ] **Step 3: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/data/Address.kt app/src/main/kotlin/com/jewan/zipkr/data/AppError.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(data): Address 도메인 모델 + AppError sealed 타입 정의"
```

### Task 2.3: AddressProvider + AddressRepository 인터페이스

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/data/provider/AddressProvider.kt`
- Create: `app/src/main/kotlin/com/jewan/zipkr/data/AddressRepository.kt`

- [ ] **Step 1: AddressProvider.kt 작성**

```kotlin
package com.jewan.zipkr.data.provider

import com.jewan.zipkr.data.Address
import com.jewan.zipkr.data.AppError

/**
 * 외부 데이터 소스의 일반화 인터페이스이다.
 * 행안부·카카오·자체 캐시 등 어떤 구현체든 이 계약만 따르면 교체 가능하다.
 */
interface AddressProvider {
    suspend fun search(query: String): Result<List<Address>>
}

/**
 * 도메인 에러를 포함한 결과 타입이다.
 */
sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val error: AppError) : Result<Nothing>()
}
```

- [ ] **Step 2: AddressRepository.kt 작성**

```kotlin
package com.jewan.zipkr.data

import com.jewan.zipkr.data.provider.Result

/**
 * 도메인 레포지토리 인터페이스이다.
 * UI/ViewModel은 이 계약만 알며, 내부 Provider 구현은 모른다.
 * v2에서 카카오 Provider 추가 시 본 인터페이스는 변경 없다.
 */
interface AddressRepository {
    suspend fun search(query: String): Result<List<Address>>
}
```

- [ ] **Step 3: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/data/provider/AddressProvider.kt app/src/main/kotlin/com/jewan/zipkr/data/AddressRepository.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(data): AddressProvider·AddressRepository 인터페이스 + Result 정의"
```

### Task 2.4: 행안부 JusoModels (DTO + 매핑) — TDD

**Files:**
- Create: `app/src/test/kotlin/com/jewan/zipkr/data/provider/juso/JusoModelsTest.kt`
- Create: `app/src/main/kotlin/com/jewan/zipkr/data/provider/juso/JusoModels.kt`

- [ ] **Step 1: 실패하는 테스트 작성**

`app/src/test/kotlin/com/jewan/zipkr/data/provider/juso/JusoModelsTest.kt`:

```kotlin
package com.jewan.zipkr.data.provider.juso

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class JusoModelsTest {

    @Test
    fun `JusoAddressDto는 Address 도메인 모델로 매핑된다`() {
        val dto = JusoAddressDto(
            roadAddr = "서울특별시 강남구 테헤란로 123",
            jibunAddr = "서울특별시 강남구 역삼동 736-1",
            engAddr = "123, Teheran-ro, Gangnam-gu, Seoul",
            zipNo = "06234",
        )
        val address = dto.toDomain()

        assertThat(address.zipCode).isEqualTo("06234")
        assertThat(address.roadAddress).isEqualTo("서울특별시 강남구 테헤란로 123")
        assertThat(address.jibunAddress).isEqualTo("서울특별시 강남구 역삼동 736-1")
        assertThat(address.englishAddress).isEqualTo("123, Teheran-ro, Gangnam-gu, Seoul")
    }

    @Test
    fun `JusoSearchResponse가 정상 코드면 results 리스트를 반환한다`() {
        val response = JusoSearchResponse(
            results = JusoResults(
                common = JusoCommon(errorCode = "0", errorMessage = "정상"),
                juso = listOf(
                    JusoAddressDto(
                        roadAddr = "A", jibunAddr = "B", engAddr = "C", zipNo = "12345",
                    )
                )
            )
        )
        assertThat(response.results.common.errorCode).isEqualTo("0")
        assertThat(response.results.juso).hasSize(1)
    }
}
```

- [ ] **Step 2: 테스트 실행 → 컴파일 실패 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:testDevDebugUnitTest --tests "com.jewan.zipkr.data.provider.juso.JusoModelsTest"
```

Expected: COMPILE FAILURE (JusoModels 미존재).

- [ ] **Step 3: JusoModels.kt 구현**

`app/src/main/kotlin/com/jewan/zipkr/data/provider/juso/JusoModels.kt`:

```kotlin
package com.jewan.zipkr.data.provider.juso

import com.jewan.zipkr.data.Address
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 행안부 도로명주소 API 응답 DTO이다.
 * (참고) https://business.juso.go.kr/addrlink/openApi/searchApi.do
 */
@Serializable
data class JusoSearchResponse(
    val results: JusoResults,
)

@Serializable
data class JusoResults(
    val common: JusoCommon,
    val juso: List<JusoAddressDto> = emptyList(),
)

@Serializable
data class JusoCommon(
    val errorCode: String,
    val errorMessage: String,
)

@Serializable
data class JusoAddressDto(
    @SerialName("roadAddr") val roadAddr: String,
    @SerialName("jibunAddr") val jibunAddr: String,
    @SerialName("engAddr") val engAddr: String,
    @SerialName("zipNo") val zipNo: String,
) {
    /** API DTO를 도메인 모델로 변환한다. */
    fun toDomain(): Address = Address(
        zipCode = zipNo,
        roadAddress = roadAddr,
        jibunAddress = jibunAddr,
        englishAddress = engAddr,
    )
}
```

- [ ] **Step 4: 테스트 통과 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:testDevDebugUnitTest --tests "com.jewan.zipkr.data.provider.juso.JusoModelsTest"
```

Expected: BUILD SUCCESSFUL, 2 tests passed.

- [ ] **Step 5: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/data/provider/juso/JusoModels.kt app/src/test/kotlin/com/jewan/zipkr/data/provider/juso/JusoModelsTest.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "test+feat(data): JusoModels DTO·매핑 — TDD red→green

행안부 API 응답을 Address 도메인 모델로 변환한다.
"
```

### Task 2.5: JusoApi (Retrofit 인터페이스)

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/data/provider/juso/JusoApi.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.jewan.zipkr.data.provider.juso

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 행안부 도로명주소 검색 API의 Retrofit 인터페이스이다.
 * Endpoint: https://business.juso.go.kr/addrlink/addrLinkApi.do
 */
interface JusoApi {

    @GET("addrlink/addrLinkApi.do?resultType=json")
    suspend fun search(
        @Query("confmKey") apiKey: String,
        @Query("keyword") keyword: String,
        @Query("currentPage") currentPage: Int = 1,
        @Query("countPerPage") countPerPage: Int = 10,
    ): JusoSearchResponse
}
```

- [ ] **Step 2: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/data/provider/juso/JusoApi.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(data): JusoApi Retrofit 인터페이스 정의"
```

### Task 2.6: JusoApiProvider 구현 — TDD (MockK + Turbine)

**Files:**
- Create: `app/src/test/kotlin/com/jewan/zipkr/data/provider/juso/JusoApiProviderTest.kt`
- Create: `app/src/main/kotlin/com/jewan/zipkr/data/provider/juso/JusoApiProvider.kt`

- [ ] **Step 1: 실패하는 테스트 작성**

```kotlin
package com.jewan.zipkr.data.provider.juso

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.data.provider.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class JusoApiProviderTest {

    private val api: JusoApi = mockk()
    private val apiKey = "TEST_KEY"
    private val provider = JusoApiProvider(api = api, apiKey = apiKey)

    @Test
    fun `정상 응답이면 Address 리스트를 반환한다`() = runTest {
        coEvery { api.search(any(), any(), any(), any()) } returns JusoSearchResponse(
            results = JusoResults(
                common = JusoCommon("0", "정상"),
                juso = listOf(
                    JusoAddressDto("도로명1", "지번1", "Eng1", "11111"),
                    JusoAddressDto("도로명2", "지번2", "Eng2", "22222"),
                )
            )
        )

        val result = provider.search("강남")

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val list = (result as Result.Success).value
        assertThat(list).hasSize(2)
        assertThat(list.first().zipCode).isEqualTo("11111")
    }

    @Test
    fun `errorCode가 0이 아니면 ApiBadResponse를 반환한다`() = runTest {
        coEvery { api.search(any(), any(), any(), any()) } returns JusoSearchResponse(
            results = JusoResults(
                common = JusoCommon("E0006", "API 키 오류"),
                juso = emptyList(),
            )
        )

        val result = provider.search("강남")

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        val error = (result as Result.Failure).error
        assertThat(error).isInstanceOf(AppError.ApiBadResponse::class.java)
        assertThat((error as AppError.ApiBadResponse).code).isEqualTo("E0006")
    }

    @Test
    fun `IOException이면 Network 에러를 반환한다`() = runTest {
        coEvery { api.search(any(), any(), any(), any()) } throws IOException("no internet")

        val result = provider.search("강남")

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        val error = (result as Result.Failure).error
        assertThat(error).isInstanceOf(AppError.Network::class.java)
    }

    @Test
    fun `기타 예외는 Unknown 에러를 반환한다`() = runTest {
        coEvery { api.search(any(), any(), any(), any()) } throws RuntimeException("boom")

        val result = provider.search("강남")

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        val error = (result as Result.Failure).error
        assertThat(error).isInstanceOf(AppError.Unknown::class.java)
    }
}
```

- [ ] **Step 2: 테스트 실행 → 컴파일 실패 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:testDevDebugUnitTest --tests "com.jewan.zipkr.data.provider.juso.JusoApiProviderTest"
```

Expected: COMPILE FAILURE.

- [ ] **Step 3: JusoApiProvider 구현**

```kotlin
package com.jewan.zipkr.data.provider.juso

import com.jewan.zipkr.data.Address
import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.data.provider.AddressProvider
import com.jewan.zipkr.data.provider.Result
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

/**
 * 행안부 도로명주소 API 기반 AddressProvider 구현체이다.
 * 호출 → DTO 매핑 → 도메인 에러 매핑까지 책임진다.
 */
class JusoApiProvider @Inject constructor(
    private val api: JusoApi,
    @Named("juso_api_key") private val apiKey: String,
) : AddressProvider {

    override suspend fun search(query: String): Result<List<Address>> {
        return try {
            val response = api.search(apiKey = apiKey, keyword = query)
            val errorCode = response.results.common.errorCode
            if (errorCode == SUCCESS_CODE) {
                Result.Success(response.results.juso.map { it.toDomain() })
            } else {
                Timber.w("Juso API non-success: code=%s msg=%s", errorCode, response.results.common.errorMessage)
                Result.Failure(
                    AppError.ApiBadResponse(
                        code = errorCode,
                        message = response.results.common.errorMessage,
                    )
                )
            }
        } catch (io: IOException) {
            Timber.w(io, "Juso API network failure")
            Result.Failure(AppError.Network(io))
        } catch (t: Throwable) {
            Timber.e(t, "Juso API unknown failure")
            Result.Failure(AppError.Unknown(t))
        }
    }

    private companion object {
        const val SUCCESS_CODE = "0"
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:testDevDebugUnitTest --tests "com.jewan.zipkr.data.provider.juso.JusoApiProviderTest"
```

Expected: BUILD SUCCESSFUL, 4 tests passed.

- [ ] **Step 5: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/data/provider/juso/JusoApiProvider.kt app/src/test/kotlin/com/jewan/zipkr/data/provider/juso/JusoApiProviderTest.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "test+feat(data): JusoApiProvider — TDD 4 케이스

성공·API 비정상 코드·IOException·일반 예외 4종 도메인 에러 매핑.
"
```

### Task 2.7: AddressRepositoryImpl — TDD

**Files:**
- Create: `app/src/test/kotlin/com/jewan/zipkr/data/AddressRepositoryImplTest.kt`
- Create: `app/src/main/kotlin/com/jewan/zipkr/data/AddressRepositoryImpl.kt`

- [ ] **Step 1: 실패하는 테스트 작성**

```kotlin
package com.jewan.zipkr.data

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.provider.AddressProvider
import com.jewan.zipkr.data.provider.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddressRepositoryImplTest {

    private val provider: AddressProvider = mockk()
    private val repository = AddressRepositoryImpl(provider = provider)

    @Test
    fun `search는 provider 결과를 그대로 반환한다`() = runTest {
        val expected = Result.Success(
            listOf(Address("06234", "도로명", "지번", "Eng"))
        )
        coEvery { provider.search("강남") } returns expected

        val actual = repository.search("강남")

        assertThat(actual).isEqualTo(expected)
    }
}
```

- [ ] **Step 2: 컴파일 실패 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:testDevDebugUnitTest --tests "com.jewan.zipkr.data.AddressRepositoryImplTest"
```

Expected: COMPILE FAILURE.

- [ ] **Step 3: 구현**

```kotlin
package com.jewan.zipkr.data

import com.jewan.zipkr.data.provider.AddressProvider
import com.jewan.zipkr.data.provider.Result
import javax.inject.Inject

/**
 * 도메인 레포지토리 구현체이다.
 * MVP 단계는 단일 Provider 위임. v2에서 다중 Provider 통합 전략 추가 가능.
 */
class AddressRepositoryImpl @Inject constructor(
    private val provider: AddressProvider,
) : AddressRepository {

    override suspend fun search(query: String): Result<List<Address>> = provider.search(query)
}
```

- [ ] **Step 4: 테스트 통과 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:testDevDebugUnitTest --tests "com.jewan.zipkr.data.AddressRepositoryImplTest"
```

Expected: PASS.

- [ ] **Step 5: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/data/AddressRepositoryImpl.kt app/src/test/kotlin/com/jewan/zipkr/data/AddressRepositoryImplTest.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "test+feat(data): AddressRepositoryImpl — Provider 위임 + TDD"
```

### Task 2.8: Hilt DI 모듈 — NetworkModule + DataModule

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/di/NetworkModule.kt`
- Create: `app/src/main/kotlin/com/jewan/zipkr/di/DataModule.kt`

- [ ] **Step 1: NetworkModule.kt 작성**

```kotlin
package com.jewan.zipkr.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.jewan.zipkr.BuildConfig
import com.jewan.zipkr.data.provider.juso.JusoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(JUSO_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideJusoApi(retrofit: Retrofit): JusoApi = retrofit.create(JusoApi::class.java)

    @Provides
    @Named("juso_api_key")
    fun provideJusoApiKey(): String = BuildConfig.JUSO_API_KEY

    private const val JUSO_BASE_URL = "https://business.juso.go.kr/"
}
```

- [ ] **Step 2: DataModule.kt 작성**

```kotlin
package com.jewan.zipkr.di

import com.jewan.zipkr.data.AddressRepository
import com.jewan.zipkr.data.AddressRepositoryImpl
import com.jewan.zipkr.data.provider.AddressProvider
import com.jewan.zipkr.data.provider.juso.JusoApiProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAddressProvider(impl: JusoApiProvider): AddressProvider

    @Binds
    @Singleton
    abstract fun bindAddressRepository(impl: AddressRepositoryImpl): AddressRepository
}
```

- [ ] **Step 3: 빌드 통과 + 단위 테스트 통과 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:assembleDevDebug :app:testDevDebugUnitTest
```

Expected: BUILD SUCCESSFUL, all tests passed.

- [ ] **Step 4: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/di/
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(di): Hilt NetworkModule·DataModule — Retrofit·OkHttp·Provider·Repository 바인딩

- Json: ignoreUnknownKeys, coerceInputValues로 API 응답 호환성 확보.
- OkHttp 로깅: 디버그=BODY, 릴리즈=NONE (헌법 §3 민감정보 노출 방지).
- @Named(juso_api_key)로 BuildConfig 키 주입.
"
```

### Task 2.9: local.properties + BuildConfig.JUSO_API_KEY 주입

**Files:**
- Create: `local.properties` (gitignore)
- Modify: `app/build.gradle.kts` (BuildConfig 주입 로직)

- [ ] **Step 1: 행안부 API 키 발급 (형 직접)**

https://business.juso.go.kr 회원가입 → 도로명주소 → 도로명주소조회 신청 → 승인 (하루 내). 발급받은 confmKey를 다음 단계에 저장.

- [ ] **Step 2: local.properties에 키 저장**

```properties
sdk.dir=...(이미 있음)...
juso.api.key=발급받은_confmKey
```

- [ ] **Step 3: app/build.gradle.kts에 주입 로직 추가**

`android { defaultConfig {} }` 안의 `buildConfigField("String", "JUSO_API_KEY", "\"\"")`를 다음으로 교체:

```kotlin
val localProps = java.util.Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}
val jusoKey = localProps.getProperty("juso.api.key", "")
buildConfigField("String", "JUSO_API_KEY", "\"$jusoKey\"")
```

- [ ] **Step 4: 빌드 + 실기기 테스트**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:assembleDevDebug
```

(이 시점엔 UI 호출 경로가 없으니 단위 테스트로만 확인. 다음 Phase에서 UI 통합.)

- [ ] **Step 5: commit (local.properties는 제외 — gitignore)**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/build.gradle.kts
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "chore: BuildConfig.JUSO_API_KEY local.properties 주입

local.properties는 gitignore로 제외 (헌법 §9 시크릿 관리).
"
```

### Task 2.10: PR 생성 + develop 병합

- [ ] **Step 1: 푸시 + PR**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr push -u origin feat/data-v1
```

GitHub에서 PR → develop. 제목: `feat: zipkr 데이터 레이어 (Phase 2)`.

- [ ] **Step 2: 병합 + 로컬 정리**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr pull origin develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr branch -d feat/data-v1
```

---

## Phase 3 — 검색 화면 (SearchScreen + ViewModel + 즉시성)

**브랜치:** `feat/search-v1`

**목표:** 즉시성 동선의 핵심인 SearchScreen + 즉시 복사 가능한 결과 카드 구현. UI는 Preview 중심 + 단위 테스트(ViewModel) + UI 테스트 1개(핵심 동선).

### Task 3.1: feat 브랜치 + util 헬퍼

- [ ] **Step 1: 분기**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr pull origin develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout -b feat/search-v1
```

- [ ] **Step 2: util/Clipboard.kt 작성**

`app/src/main/kotlin/com/jewan/zipkr/util/Clipboard.kt`:

```kotlin
package com.jewan.zipkr.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.getSystemService

/**
 * 클립보드 복사 헬퍼이다.
 * Android 13+는 시스템이 자동 토스트를 띄우므로 별도 안내가 중복되지 않게 호출부에서 분기 가능하다.
 */
fun Context.copyToClipboard(label: String, text: String) {
    val manager = getSystemService<ClipboardManager>() ?: return
    manager.setPrimaryClip(ClipData.newPlainText(label, text))
}
```

- [ ] **Step 3: util/Haptic.kt 작성**

```kotlin
package com.jewan.zipkr.util

import android.view.HapticFeedbackConstants
import android.view.View

/**
 * 짧은 햅틱 피드백을 발생시킨다.
 * 즉시성 신호의 일부이며, 시스템 햅틱 비활성 사용자에겐 자동 무시된다.
 */
fun View.lightHaptic() {
    performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
}
```

- [ ] **Step 4: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/util/
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(util): Clipboard·Haptic 헬퍼 (즉시성 패키지 자산)"
```

### Task 3.2: SearchUiState + SearchViewModel — TDD

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/search/SearchUiState.kt`
- Create: `app/src/test/kotlin/com/jewan/zipkr/ui/search/SearchViewModelTest.kt`
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/search/SearchViewModel.kt`

- [ ] **Step 1: SearchUiState.kt 작성**

```kotlin
package com.jewan.zipkr.ui.search

import com.jewan.zipkr.data.Address

/**
 * 검색 화면의 단일 진실 상태이다.
 */
data class SearchUiState(
    val query: String = "",
    val phase: Phase = Phase.Idle,
) {
    sealed interface Phase {
        data object Idle : Phase
        data object Loading : Phase
        data class Success(val results: List<Address>) : Phase
        data object Empty : Phase
        data class Error(val message: String) : Phase
    }
}
```

- [ ] **Step 2: SearchViewModelTest.kt 작성 (실패 테스트)**

```kotlin
package com.jewan.zipkr.ui.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.data.AddressRepository
import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.data.provider.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val repository: AddressRepository = mockk()
    private lateinit var viewModel: SearchViewModel

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = SearchViewModel(repository)
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `초기 상태는 Idle이다`() = runTest {
        viewModel.uiState.test {
            val first = awaitItem()
            assertThat(first.query).isEmpty()
            assertThat(first.phase).isEqualTo(SearchUiState.Phase.Idle)
        }
    }

    @Test
    fun `검색 결과가 있으면 Success가 된다`() = runTest {
        val list = listOf(Address("06234", "도로명", "지번", "Eng"))
        coEvery { repository.search("강남") } returns Result.Success(list)

        viewModel.onQueryChange("강남")
        viewModel.searchNow()

        viewModel.uiState.test {
            val state = awaitItem()
            val phase = state.phase as SearchUiState.Phase.Success
            assertThat(phase.results).isEqualTo(list)
        }
    }

    @Test
    fun `검색 결과가 비면 Empty가 된다`() = runTest {
        coEvery { repository.search("zzz") } returns Result.Success(emptyList())

        viewModel.onQueryChange("zzz")
        viewModel.searchNow()

        viewModel.uiState.test {
            assertThat(awaitItem().phase).isEqualTo(SearchUiState.Phase.Empty)
        }
    }

    @Test
    fun `네트워크 실패면 Error가 된다`() = runTest {
        coEvery { repository.search("강남") } returns Result.Failure(AppError.Network())

        viewModel.onQueryChange("강남")
        viewModel.searchNow()

        viewModel.uiState.test {
            val phase = awaitItem().phase
            assertThat(phase).isInstanceOf(SearchUiState.Phase.Error::class.java)
        }
    }
}
```

- [ ] **Step 3: 테스트 → 컴파일 실패 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:testDevDebugUnitTest --tests "com.jewan.zipkr.ui.search.SearchViewModelTest"
```

Expected: COMPILE FAILURE.

- [ ] **Step 4: SearchViewModel.kt 작성**

```kotlin
package com.jewan.zipkr.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewan.zipkr.data.AddressRepository
import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.data.provider.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 검색 화면의 ViewModel이다.
 * 입력 변경에 debounce 400ms를 적용해 자동 검색을 트리거하며,
 * onSearchNow()로 즉시 트리거도 지원한다.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: AddressRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private var inFlight: Job? = null

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(DEBOUNCE_MS)
                .distinctUntilChanged()
                .filter { it.length >= MIN_QUERY_LEN }
                .onEach { runSearch(it) }
                .collect { /* no-op */ }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        queryFlow.value = query
        if (query.length < MIN_QUERY_LEN) {
            _uiState.value = _uiState.value.copy(phase = SearchUiState.Phase.Idle)
        }
    }

    /** 사용자가 명시적으로 즉시 검색을 누른 경우. */
    fun searchNow() {
        runSearch(_uiState.value.query)
    }

    private fun runSearch(query: String) {
        inFlight?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(phase = SearchUiState.Phase.Idle)
            return
        }
        _uiState.value = _uiState.value.copy(phase = SearchUiState.Phase.Loading)
        inFlight = viewModelScope.launch {
            val result = repository.search(query)
            _uiState.value = _uiState.value.copy(phase = mapPhase(result))
        }
    }

    private fun mapPhase(result: Result<List<com.jewan.zipkr.data.Address>>): SearchUiState.Phase {
        return when (result) {
            is Result.Success -> {
                if (result.value.isEmpty()) SearchUiState.Phase.Empty
                else SearchUiState.Phase.Success(result.value)
            }
            is Result.Failure -> SearchUiState.Phase.Error(mapErrorMessage(result.error))
        }
    }

    private fun mapErrorMessage(error: AppError): String = when (error) {
        is AppError.Network -> "인터넷 연결을 확인해주세요"
        is AppError.ApiBadResponse -> "검색 서비스가 일시적으로 불안정합니다"
        is AppError.Unknown -> "잠시 후 다시 시도해주세요"
    }

    private companion object {
        const val DEBOUNCE_MS = 400L
        const val MIN_QUERY_LEN = 2
    }
}
```

- [ ] **Step 5: 테스트 통과 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:testDevDebugUnitTest --tests "com.jewan.zipkr.ui.search.SearchViewModelTest"
```

Expected: 4 tests passed.

- [ ] **Step 6: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/ui/search/ app/src/test/kotlin/com/jewan/zipkr/ui/search/
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "test+feat(ui): SearchViewModel + UiState — TDD 4 케이스

- 입력 변경 시 debounce 400ms 자동 검색.
- searchNow()로 즉시 트리거.
- 결과/빈/에러 phase 매핑 + AppError 친화 메시지.
"
```

### Task 3.3: AddressResultCard (즉시 복사 컴포넌트)

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/components/AddressResultCard.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.jewan.zipkr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.ui.theme.ZipkrTheme

/**
 * 검색 결과 단일 카드이다.
 * - 카드 자체 탭 → 상세 화면 진입.
 * - 우측 복사 아이콘 탭 → 우편번호만 즉시 클립보드 복사 (즉시성 동선 핵심).
 */
@Composable
fun AddressResultCard(
    address: Address,
    onCardClick: () -> Unit,
    onCopyZip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = address.roadAddress, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "📮 ${address.zipCode}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = address.englishAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onCopyZip) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "우편번호 복사",
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddressResultCardPreview() {
    ZipkrTheme {
        AddressResultCard(
            address = Address(
                zipCode = "06234",
                roadAddress = "서울특별시 강남구 테헤란로 123",
                jibunAddress = "역삼동 736-1",
                englishAddress = "123, Teheran-ro, Gangnam-gu, Seoul",
            ),
            onCardClick = {},
            onCopyZip = {},
        )
    }
}
```

- [ ] **Step 2: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/ui/components/AddressResultCard.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(ui): AddressResultCard — 즉시 복사 가능한 결과 카드"
```

### Task 3.4: SearchScreen 작성

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/search/SearchScreen.kt`
- Create: `app/src/main/res/values/strings.xml` (보강)

- [ ] **Step 1: strings.xml에 한글 키 추가**

`app/src/main/res/values/strings.xml`:

```xml
<resources>
    <string name="app_name">우편번호</string>
    <string name="search_placeholder">주소를 입력하세요</string>
    <string name="empty_title">어떤 주소든 입력해보세요</string>
    <string name="empty_description">도로명·지번·건물명 모두 검색됩니다.</string>
    <string name="empty_results_title">검색 결과가 없어요</string>
    <string name="empty_results_description">다른 검색어로 시도해보세요.</string>
    <string name="copy_toast">%1$s 복사됨</string>
    <string name="copy_zip_label">우편번호</string>
</resources>
```

- [ ] **Step 2: SearchScreen.kt 작성**

```kotlin
package com.jewan.zipkr.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jewan.zipkr.R
import com.jewan.zipkr.ui.components.AddressResultCard
import com.jewan.zipkr.ui.components.EmptyState
import com.jewan.zipkr.ui.components.ErrorView
import com.jewan.zipkr.ui.components.LoadingSkeleton
import com.jewan.zipkr.util.copyToClipboard
import com.jewan.zipkr.util.lightHaptic
import widget.Toast as AndroidToast

/**
 * 검색 메인 화면이다.
 * - 진입 시 입력창 자동 포커스 + 키보드 자동 표시 (즉시성).
 * - 결과 카드 우측 복사 버튼은 우편번호만 클립보드 복사 + 토스트 + 햅틱.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onCardClick: (zip: String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = remember { FocusRequester() }
    val copyToastTemplate = stringResource(R.string.copy_toast)
    val zipLabel = stringResource(R.string.copy_zip_label)

    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxSize(0.999f).focusRequester(focus),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )
            when (val phase = state.phase) {
                SearchUiState.Phase.Idle -> EmptyState(
                    title = stringResource(R.string.empty_title),
                    description = stringResource(R.string.empty_description),
                )
                SearchUiState.Phase.Loading -> LoadingSkeleton()
                SearchUiState.Phase.Empty -> EmptyState(
                    title = stringResource(R.string.empty_results_title),
                    description = stringResource(R.string.empty_results_description),
                )
                is SearchUiState.Phase.Error -> ErrorView(
                    message = phase.message,
                    onRetry = { viewModel.searchNow() },
                )
                is SearchUiState.Phase.Success -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(phase.results) { address ->
                        AddressResultCard(
                            address = address,
                            onCardClick = { onCardClick(address.zipCode) },
                            onCopyZip = {
                                context.copyToClipboard(zipLabel, address.zipCode)
                                view.lightHaptic()
                                AndroidToast.makeText(context, copyToastTemplate.format(address.zipCode), AndroidToast.LENGTH_SHORT).show()
                            },
                        )
                    }
                }
            }
        }
    }
}
```

> **참고:** `widget.Toast as AndroidToast` import는 실제로 `import android.widget.Toast as AndroidToast` 입니다 — 위 표기는 plan 가독성용. 작성 시 정확한 import 경로 사용.

- [ ] **Step 3: 빌드 통과 + 에뮬레이터 실행 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:installDevDebug
```

(MainActivity·NavGraph는 다음 task에서 연결. 현재는 컴파일만 통과하면 OK.)

- [ ] **Step 4: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/ui/search/SearchScreen.kt app/src/main/res/values/strings.xml
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(ui): SearchScreen — 즉시 포커스+키보드+자동검색+1탭 복사+토스트+햅틱"
```

### Task 3.5: PR 생성 + develop 병합

- [ ] **Step 1: 푸시 + PR + 병합** (이전과 동일 흐름)

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr push -u origin feat/search-v1
```

PR → develop, 제목 `feat: zipkr 검색 화면 (Phase 3)`. 병합 후 로컬 정리.

---

## Phase 4 — 상세 화면 + 네비게이션 + Splash

**브랜치:** `feat/detail-nav-v1`

**목표:** DetailScreen + Navigation Compose 연결, Splash Screen API로 진입 표준화. MainActivity에서 NavHost 구성.

### Task 4.1: feat 브랜치 + util/Share.kt

- [ ] **Step 1: 분기**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr pull origin develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout -b feat/detail-nav-v1
```

- [ ] **Step 2: util/Share.kt 작성**

```kotlin
package com.jewan.zipkr.util

import android.content.Context
import android.content.Intent

/** 텍스트 공유 인텐트를 띄운다. */
fun Context.shareText(subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    startActivity(Intent.createChooser(intent, subject))
}
```

- [ ] **Step 3: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/util/Share.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(util): shareText 헬퍼 추가"
```

### Task 4.2: DetailUiState + DetailViewModel

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailUiState.kt`
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailViewModel.kt`

- [ ] **Step 1: DetailUiState.kt**

```kotlin
package com.jewan.zipkr.ui.detail

import com.jewan.zipkr.data.Address

data class DetailUiState(
    val address: Address? = null,
)
```

- [ ] **Step 2: DetailViewModel.kt — savedStateHandle로 zipCode 받기**

```kotlin
package com.jewan.zipkr.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewan.zipkr.data.AddressRepository
import com.jewan.zipkr.data.provider.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 상세 화면 ViewModel이다.
 * MVP는 SearchScreen에서 받은 zipCode로 다시 검색해 첫 결과를 표시한다.
 * (v2에서 검색 결과를 캐시·전달 방식으로 개선 예정.)
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AddressRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        val zip: String? = savedStateHandle["zipCode"]
        if (!zip.isNullOrBlank()) {
            viewModelScope.launch {
                val result = repository.search(zip)
                if (result is Result.Success && result.value.isNotEmpty()) {
                    _uiState.value = DetailUiState(address = result.value.first())
                }
            }
        }
    }
}
```

- [ ] **Step 3: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/ui/detail/
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(ui): DetailUiState + DetailViewModel (SavedStateHandle로 zipCode 수령)"
```

### Task 4.3: DetailScreen

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: strings.xml 보강**

기존 `<resources>` 안에 추가:

```xml
<string name="detail_road">도로명</string>
<string name="detail_jibun">지번</string>
<string name="detail_english">English</string>
<string name="action_copy">복사</string>
<string name="action_share">공유</string>
<string name="action_back">뒤로</string>
<string name="share_subject">우편번호</string>
```

- [ ] **Step 2: DetailScreen.kt 작성**

```kotlin
package com.jewan.zipkr.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jewan.zipkr.R
import com.jewan.zipkr.util.copyToClipboard
import com.jewan.zipkr.util.lightHaptic
import com.jewan.zipkr.util.shareText
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        val address = state.address ?: return@Scaffold
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "📮 ${address.zipCode}",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            DetailRow(label = stringResource(R.string.detail_road), value = address.roadAddress)
            DetailRow(label = stringResource(R.string.detail_jibun), value = address.jibunAddress)
            DetailRow(label = stringResource(R.string.detail_english), value = address.englishAddress)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    context.copyToClipboard(stringResource(R.string.app_name).let { it }, address.zipCode)
                    view.lightHaptic()
                    Toast.makeText(context, "${address.zipCode} 복사됨", Toast.LENGTH_SHORT).show()
                }) {
                    Text(stringResource(R.string.action_copy))
                }
                Button(onClick = {
                    val body = "${address.roadAddress}\n${address.zipCode}"
                    context.shareText(stringResource(R.string.share_subject), body)
                }) {
                    Text(stringResource(R.string.action_share))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
```

- [ ] **Step 3: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailScreen.kt app/src/main/res/values/strings.xml
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(ui): DetailScreen — 점보 우편번호 + 복사/공유 액션"
```

### Task 4.4: NavGraph + ZipkrApp 진입 Composable + MainActivity

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/ZipkrAppNav.kt`
- Modify: `app/src/main/kotlin/com/jewan/zipkr/MainActivity.kt`

- [ ] **Step 1: ZipkrAppNav.kt 작성**

```kotlin
package com.jewan.zipkr.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jewan.zipkr.ui.detail.DetailScreen
import com.jewan.zipkr.ui.search.SearchScreen
import com.jewan.zipkr.ui.theme.ZipkrTheme

/**
 * 앱의 단일 NavHost이다.
 * - search: 메인 검색 화면.
 * - detail/{zipCode}: 상세 화면.
 */
@Composable
fun ZipkrAppNav() {
    ZipkrTheme {
        val nav = rememberNavController()
        NavHost(navController = nav, startDestination = "search") {
            composable("search") {
                SearchScreen(onCardClick = { zip -> nav.navigate("detail/$zip") })
            }
            composable(
                route = "detail/{zipCode}",
                arguments = listOf(navArgument("zipCode") { type = NavType.StringType }),
            ) {
                DetailScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}
```

- [ ] **Step 2: MainActivity.kt 보강 — Splash Screen API + Compose 진입**

```kotlin
package com.jewan.zipkr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.jewan.zipkr.ui.ZipkrAppNav
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent { ZipkrAppNav() }
    }
}
```

- [ ] **Step 3: themes.xml에 Splash 테마 추가**

`app/src/main/res/values/themes.xml`:

```xml
<resources>
    <style name="Theme.Zipkr" parent="android:Theme.Material.Light.NoActionBar" />

    <style name="Theme.App.Starting" parent="Theme.SplashScreen">
        <item name="windowSplashScreenBackground">@color/white</item>
        <item name="windowSplashScreenAnimatedIcon">@mipmap/ic_launcher_foreground</item>
        <item name="postSplashScreenTheme">@style/Theme.Zipkr</item>
    </style>
</resources>
```

- [ ] **Step 4: 빌드 + 에뮬레이터 실행**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:installDevDebug
```

수동 검증:
1. 앱 실행 → 스플래시 → SearchScreen 진입.
2. 입력창 자동 포커스 + 키보드 표시 확인.
3. 주소 검색 → 결과 카드 표시.
4. 카드 우측 복사 버튼 → 토스트 + 햅틱.
5. 카드 자체 탭 → DetailScreen 진입 → 뒤로가기.
6. Detail에서 복사·공유 버튼 동작.

- [ ] **Step 5: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/kotlin/com/jewan/zipkr/ui/ZipkrAppNav.kt app/src/main/kotlin/com/jewan/zipkr/MainActivity.kt app/src/main/res/values/themes.xml
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(nav): NavHost(search/detail) + Splash Screen API + MainActivity 진입 통합"
```

### Task 4.5: PR + 병합

- [ ] **Step 1: 푸시 + PR `feat: zipkr 상세·네비·스플래시 (Phase 4)` + 병합 + 로컬 정리**

(이전 흐름 동일.)

---

## Phase 5 — i18n (영문 strings.xml + 영문 메타 준비)

**브랜치:** `feat/i18n-v1`

**목표:** strings.xml 영문(EN) 버전 작성. 시스템 언어 자동 추종 검증. (Play Console 영문 메타는 Phase 7에서.)

### Task 5.1: feat 브랜치 + values-en/strings.xml

- [ ] **Step 1: 분기**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr pull origin develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout -b feat/i18n-v1
```

- [ ] **Step 2: app/src/main/res/values-en/strings.xml 작성**

```xml
<resources>
    <string name="app_name">Postcode KR</string>
    <string name="search_placeholder">Enter an address</string>
    <string name="empty_title">Enter any Korean address</string>
    <string name="empty_description">Search by road, lot, or building name.</string>
    <string name="empty_results_title">No results</string>
    <string name="empty_results_description">Try a different keyword.</string>
    <string name="copy_toast">Copied %1$s</string>
    <string name="copy_zip_label">Postal code</string>
    <string name="detail_road">Road</string>
    <string name="detail_jibun">Lot</string>
    <string name="detail_english">English</string>
    <string name="action_copy">Copy</string>
    <string name="action_share">Share</string>
    <string name="action_back">Back</string>
    <string name="share_subject">Postal code</string>
</resources>
```

- [ ] **Step 3: 에뮬레이터 언어 EN 변경 후 검증**

설정 → System → Languages → English 추가/우선순위 → 앱 재실행 → 모든 라벨 EN으로 표시 확인.

- [ ] **Step 4: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/res/values-en/strings.xml
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(i18n): EN strings.xml — 외국인 사용자 첫 라운드 지원

시스템 언어 자동 추종, KO 기본 fallback.
"
```

### Task 5.2: PR + 병합

- [ ] (이전 흐름 동일, PR 제목 `feat: zipkr 다국어 EN 지원 (Phase 5)`)

---

## Phase 6 — AdMob 배너 통합

**브랜치:** `feat/ads-v1`

**목표:** AdBanner Compose 컴포저블 + debug 빌드 테스트 광고로 검증.

### Task 6.1: feat 브랜치 + AdBanner

- [ ] **Step 1: 분기**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr pull origin develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout -b feat/ads-v1
```

- [ ] **Step 2: AndroidManifest.xml에 AdMob meta-data 추가**

`<application>` 태그 안에:

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="@string/admob_app_id" />
```

- [ ] **Step 3: strings.xml에 admob_app_id placeholder 추가 (BuildConfig에서 동적으로 안 들어가므로 임시 상수, debug 빌드 테스트 ID)**

`app/src/main/res/values/strings.xml`에 추가:

```xml
<string name="admob_app_id">ca-app-pub-3940256099942544~3347511713</string>
```

> 출시 전 release 빌드용 별도 처리 — Phase 7에서 manifest placeholder 방식으로 교체.

- [ ] **Step 4: ads/AdBanner.kt 작성**

```kotlin
package com.jewan.zipkr.ads

import android.content.Context
import android.util.DisplayMetrics
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.jewan.zipkr.BuildConfig

/**
 * 적응형 배너 광고이다.
 * - debug 빌드: 구글 공식 테스트 광고 ID 사용.
 * - release 빌드: BuildConfig.ADMOB_BANNER_UNIT_ID 사용 (Phase 7에서 주입).
 */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current

    LaunchedEffect(Unit) { MobileAds.initialize(context) {} }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(adaptiveBannerSize(ctx))
                adUnitId = BuildConfig.ADMOB_BANNER_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}

private fun adaptiveBannerSize(context: Context): AdSize {
    val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    val widthDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
}
```

- [ ] **Step 5: SearchScreen / DetailScreen 하단에 AdBanner 배치**

`SearchScreen`의 Scaffold `bottomBar`에:

```kotlin
bottomBar = { AdBanner() },
```

`DetailScreen`도 동일.

- [ ] **Step 6: 빌드 + 실기기 검증**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr/.worktrees/<phase-worktree> && ./gradlew :app:installDebug
```

수동: 양 화면 하단에 "Test Ad" 배너 표시 확인.

- [ ] **Step 7: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/src/main/AndroidManifest.xml app/src/main/res/values/strings.xml app/src/main/kotlin/com/jewan/zipkr/ads/AdBanner.kt app/src/main/kotlin/com/jewan/zipkr/ui/search/SearchScreen.kt app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailScreen.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(ads): AdMob 적응형 배너 — 양 화면 bottomBar 연결

debug 빌드: 구글 테스트 광고 ID. release 빌드의 실 ID는 Phase 7에서 주입.
"
```

### Task 6.2: PR + 병합

- [ ] (이전 흐름)

---

## Phase 7 — Firebase Crashlytics + 출시 준비

**브랜치:** `feat/release-prep-v1`

**목표:** Firebase 셋업 + Crashlytics 활성화 + ProGuard 룰 + 키스토어 + 실제 광고 ID 주입 + 개인정보 처리방침 + Play Console 메타.

### Task 7.1: feat 브랜치 + Firebase 프로젝트 생성 (형 직접)

- [ ] **Step 1: 분기**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr pull origin develop
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout -b feat/release-prep-v1
```

- [ ] **Step 2: Firebase 콘솔에서 프로젝트 생성**

https://console.firebase.google.com → 프로젝트 추가 → 이름 `zipkr-prod` → Android 앱 추가:
- 패키지명: `com.jewan.zipkr.prod` 와 `com.jewan.zipkr.debug` 둘 다 등록 권장 (debug는 applicationIdSuffix 적용)

`google-services.json` 다운로드 → `app/google-services.json`에 저장.

> 헌법 §9 + .gitignore에 이미 `google-services.json` 제외됨. 형의 백업 위치 표준화 필수.

### Task 7.2: google-services + Crashlytics 플러그인 활성화

**Files:**
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: 플러그인 추가**

`app/build.gradle.kts`의 `plugins {}` 블록에 추가:

```kotlin
alias(libs.plugins.google.services)
alias(libs.plugins.firebase.crashlytics)
```

`dependencies {}`에 추가:

```kotlin
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.crashlytics)
implementation(libs.firebase.analytics)
```

- [ ] **Step 2: ZipkrApp 보강 — Release Tree (Crashlytics)**

```kotlin
package com.jewan.zipkr

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class ZipkrApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }
}

private class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < android.util.Log.WARN) return
        FirebaseCrashlytics.getInstance().log("[$tag] $message")
        if (t != null) FirebaseCrashlytics.getInstance().recordException(t)
    }
}
```

- [ ] **Step 3: 빌드 통과 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:assembleProdRelease
```

(Release 빌드 시도하면 키스토어 누락으로 실패할 수 있음 — 다음 Task에서 처리.)

- [ ] **Step 4: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/build.gradle.kts app/src/main/kotlin/com/jewan/zipkr/ZipkrApp.kt
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "feat(observability): Firebase Crashlytics 활성화 + Timber Release Tree

WARN 이상은 Crashlytics 로그·예외로 전송.
"
```

### Task 7.3: 키스토어 생성 + 서명 설정

**Files:**
- Create: `~/.android-keystores/zipkr-release.jks` (사용자 홈 외부 안전 위치)
- Create: `keystore.properties` (gitignore)
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: 키스토어 생성 (형 직접)**

```bash
mkdir -p ~/.android-keystores && keytool -genkeypair -v -keystore ~/.android-keystores/zipkr-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias zipkr
```

비밀번호·정보 입력. **백업: 1Password 또는 별도 안전 위치에 키스토어 + 비밀번호 함께 보관 (분실 = 영구 업데이트 불가).**

- [ ] **Step 2: keystore.properties 작성 (gitignore)**

`/Users/jewan/Desktop/git/98_jewan/zipkr/keystore.properties`:

```properties
storeFile=/Users/jewan/.android-keystores/zipkr-release.jks
storePassword=<input>
keyAlias=zipkr
keyPassword=<input>
```

- [ ] **Step 3: app/build.gradle.kts에 signingConfig 추가**

`android {}` 안:

```kotlin
val keystoreProps = java.util.Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) load(file.inputStream())
}

signingConfigs {
    create("release") {
        storeFile = keystoreProps.getProperty("storeFile")?.let(::file)
        storePassword = keystoreProps.getProperty("storePassword")
        keyAlias = keystoreProps.getProperty("keyAlias")
        keyPassword = keystoreProps.getProperty("keyPassword")
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        // (이미 있는 isMinifyEnabled, proguardFiles 유지)
    }
}
```

- [ ] **Step 4: 릴리즈 AAB 빌드 시도**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:bundleProdRelease
```

Expected: `app/build/outputs/bundle/release/app-release.aab` 생성.

- [ ] **Step 5: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/build.gradle.kts
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "chore: 릴리즈 서명 설정 (keystore.properties via gitignore)"
```

### Task 7.4: ProGuard 룰

**Files:**
- Modify: `app/proguard-rules.pro`

- [ ] **Step 1: 룰 추가**

```proguard
# kotlinx.serialization
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.jewan.zipkr.**$$serializer { *; }
-keepclassmembers class com.jewan.zipkr.** {
    *** Companion;
}
-keepclasseswithmembers class com.jewan.zipkr.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Hilt
-keep class dagger.hilt.android.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Timber
-dontwarn org.jetbrains.annotations.**
```

- [ ] **Step 2: 릴리즈 빌드 + 실기기에서 동작 검증 (광고·검색 모두 정상)**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr && ./gradlew :app:installProdRelease
```

수동 검증.

- [ ] **Step 3: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/proguard-rules.pro
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "chore: ProGuard 룰 — Serialization·Retrofit·Hilt·Timber 보존"
```

### Task 7.5: 실제 AdMob ID 주입 (release 빌드)

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: AdMob 콘솔에서 zipkr 앱 등록 + 배너 광고 단위 발급 (형 직접)**

발급된 App ID와 Banner Unit ID를 `local.properties`에 추가:

```properties
admob.app.id=ca-app-pub-실제ID
admob.banner.unit.id=ca-app-pub-실제ID/실제ID
```

- [ ] **Step 2: app/build.gradle.kts — release buildType에 BuildConfig 주입**

`buildTypes { release { ... } }` 블록에 추가:

```kotlin
val localProps = java.util.Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}
val admobAppId = localProps.getProperty("admob.app.id", "")
val admobBannerUnitId = localProps.getProperty("admob.banner.unit.id", "")
buildConfigField("String", "ADMOB_APP_ID", "\"$admobAppId\"")
buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"$admobBannerUnitId\"")
manifestPlaceholders["admobAppId"] = admobAppId
```

`debug` buildType은 기존 테스트 광고 ID 유지 + `manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"` 추가.

`app/src/main/AndroidManifest.xml`의 meta-data 값을 `${admobAppId}` placeholder로:

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="${admobAppId}" />
```

`strings.xml`의 `admob_app_id` 항목 제거 (manifest placeholder 방식으로 일원화).

- [ ] **Step 3: release 빌드에서 실제 광고 단위 ID 호출되는지 검증**

`./gradlew :app:installRelease` 후 실기기에서 광고 노출 (실제 광고는 Google 서버 응답에 시간 걸릴 수 있음).

- [ ] **Step 4: jewan100.github.io의 app-ads.txt 갱신 (형 직접)**

```
google.com, pub-실제publisher_id, DIRECT, f08c47fec0942fa0
```

- [ ] **Step 5: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add app/build.gradle.kts app/src/main/AndroidManifest.xml app/src/main/res/values/strings.xml
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "chore(ads): release 빌드 — 실제 AdMob ID local.properties 주입

manifestPlaceholders로 debug(테스트 ID) / release(실 ID) 분리.
"
```

### Task 7.6: 개인정보 처리방침 페이지 (jewan100.github.io)

**Files:**
- Create: `~/Desktop/git/98_jewan/jewan100.github.io/privacy/zipkr/index.html`

- [ ] **Step 1: 정책 페이지 작성 (jewan100.github.io repo에서 별도 작업)**

기본 템플릿:

```html
<!DOCTYPE html>
<html lang="ko"><head><meta charset="UTF-8"><title>zipkr 개인정보 처리방침</title></head>
<body>
<h1>zipkr 개인정보 처리방침</h1>
<p>최종 수정일: 2026-04-26</p>
<h2>1. 수집 항목</h2>
<p>본 앱은 사용자가 입력한 검색어를 행정안전부 도로명주소 API에 전송하는 것 외 어떠한 개인정보도 수집·저장하지 않습니다.</p>
<h2>2. 광고</h2>
<p>본 앱은 Google AdMob을 사용해 배너 광고를 표시합니다. AdMob의 광고 ID 사용 정책은 Google 정책을 따릅니다.</p>
<h2>3. 크래시 리포팅</h2>
<p>안정성 향상을 위해 익명화된 크래시 정보를 Firebase Crashlytics로 전송합니다.</p>
<h2>4. 문의</h2>
<p>hawaii1468@gmail.com</p>
<p>EN: <a href="./en.html">English</a></p>
</body></html>
```

영문 버전(`en.html`)도 같이 작성.

- [ ] **Step 2: jewan100.github.io에서 commit + push (형 직접)**

URL: `https://jewan100.github.io/privacy/zipkr/`

### Task 7.7: README + CHANGELOG + ARCHITECTURE 보강

**Files:**
- Modify: `README.md`
- Create: `CHANGELOG.md`
- Create: `docs/ARCHITECTURE.md`

- [ ] **Step 1: README.md 작성**

```markdown
# zipkr — Postcode KR

빠르고 깔끔한 한국 우편번호 조회 안드로이드 앱.

- 도로명·지번·건물명 검색 (행정안전부 도로명주소 API)
- 영문 주소 동시 표시 (외국인 사용자 지원)
- 결과 카드에서 우편번호 1탭 복사

## Stack
Kotlin · Jetpack Compose · Hilt · Retrofit · Material3 · AdMob · Firebase Crashlytics

## Build
1. `local.properties`에 `juso.api.key`, `admob.app.id`, `admob.banner.unit.id` 설정.
2. `keystore.properties` 작성 (release 서명).
3. `./gradlew :app:installDevDebug` (개발) 또는 `:app:bundleProdRelease` (출시 AAB).

## License
TBD
```

- [ ] **Step 2: CHANGELOG.md 작성**

```markdown
# Changelog

## [Unreleased]

### Added
- 한국 우편번호 검색 (행정안전부 도로명주소 API)
- 영문 주소 동시 표시
- 즉시 복사 가능한 결과 카드 (1탭)
- KO/EN 다국어 지원
- AdMob 하단 적응형 배너
- Firebase Crashlytics 운영 관제
```

- [ ] **Step 3: docs/ARCHITECTURE.md 작성 (간략 — spec 참조)**

```markdown
# Architecture

상세는 [`docs/superpowers/specs/2026-04-25-postal-app-design.md`](superpowers/specs/2026-04-25-postal-app-design.md) 참조.

## 요약
- MVVM + Repository + 다중 Provider 추상화 + Hilt(DI).
- UI: Jetpack Compose + Material3 + Pretendard.
- Data: Retrofit + kotlinx.serialization → 행안부 API.
- Observability: Timber + Crashlytics.

## 패키지 경계
- `ui/` 화면·테마·컴포넌트
- `data/` 도메인 모델·Repository·Provider
- `ads/` AdMob 래퍼
- `util/` Clipboard·Share·Haptic
- `di/` Hilt 모듈
```

- [ ] **Step 4: commit**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr add README.md CHANGELOG.md docs/ARCHITECTURE.md
git -C /Users/jewan/Desktop/git/98_jewan/zipkr commit -m "docs: README·CHANGELOG·ARCHITECTURE 추가 (헌법 §7 강제 문서)"
```

### Task 7.8: PR + 병합 → main 동기화 (출시 직전 1회)

- [ ] **Step 1: feat 푸시 + PR → develop 병합**

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr push -u origin feat/release-prep-v1
```

PR 제목: `feat: zipkr 출시 준비 (Phase 7 — Crashlytics·서명·정책·문서)`. 병합.

- [ ] **Step 2: develop → main PR (출시 동기화)**

GitHub에서 develop → main PR 생성 → 병합. v1.0.0 태그:

```bash
git -C /Users/jewan/Desktop/git/98_jewan/zipkr checkout main
git -C /Users/jewan/Desktop/git/98_jewan/zipkr pull origin main
git -C /Users/jewan/Desktop/git/98_jewan/zipkr tag v1.0.0
git -C /Users/jewan/Desktop/git/98_jewan/zipkr push origin v1.0.0
```

---

## Phase 8 — Play Console 출시 (형 직접)

**브랜치 없음** — Google Play Console UI 작업.

### Task 8.1: Play Console 개발자 등록 (없으면)

- [ ] **Step 1: https://play.google.com/console 접속 → 개발자 계정 등록 (₩33,000 일회성, 본인 인증 며칠 소요)**

### Task 8.2: 앱 등록 + 메타 입력

- [ ] **Step 1: 앱 만들기**

- 이름: `우편번호 (zipkr)` / English: `Postcode KR`
- 기본 언어: 한국어
- 카테고리: Tools

- [ ] **Step 2: 메타 한·영 동시 입력**

- 짧은 설명 (한·영 각 80자)
- 자세한 설명 (한·영 각 4000자 이내) — spec의 "한 줄 소개" 확장
- 스크린샷 4~8장 (한·영, 1080×1920 권장) — 실기기/에뮬레이터 스크린샷 사용
- 앱 아이콘 512×512 + 피처 그래픽 1024×500
- 개인정보 처리방침 URL: `https://jewan100.github.io/privacy/zipkr/`

### Task 8.3: AAB 업로드 + 내부 테스트 → Closed Testing → Production

- [ ] **Step 1: 내부 테스트 트랙에 `app-release.aab` 업로드**
- [ ] **Step 2: 본인 + 1~2명 지인으로 내부 테스트 (정책 위반 사전 확인)**
- [ ] **Step 3: Closed Testing 5~10명 (옵션)**
- [ ] **Step 4: Production 출시 (심사 1~3일)**

### Task 8.4: 출시 후 자산 추출 회고

- [ ] **Step 1: 출시 직후 회고 1시간**

다음 항목 점검 후 양산 표준 메모리(`project_app_factory_standards.md`)에 새 표준 추가:
- 어떤 모듈이 다음 앱 그대로 쓰일까? (`ui/theme`, `ui/components`, `ads/`, `util/`, `data/provider/AddressProvider` 패턴 등)
- 어디서 시간을 가장 많이 썼는가? (다음 앱은 그 단계를 자동화 또는 템플릿화)
- 어디서 헌법·표준을 위반할 뻔했는가? (룰 강화)

- [ ] **Step 2: 두 번째 앱 시작 시 `core-ui` 모듈 추출 brainstorming 진입**

(별도 spec → plan 사이클로.)

---

## Self-Review

### Spec 커버리지 점검

| Spec 섹션 | 구현 task |
|---|---|
| 1. Context | (이번 plan의 Goal·Architecture에 반영) |
| 2. 앱 정체성 | Task 0.5 (applicationId, namespace), Task 8.2 (메타) |
| 3. MVP 범위 | Task 3.4(검색·결과), 4.3(상세), 5.1(i18n), 6.1(광고), 0.8(스플래시), 즉시성 패키지 = Task 3.1·3.4 |
| 4. 아키텍처(MVVM/Repository/다중Provider/Hilt) | Phase 2 + Task 2.8(DI) |
| 5. 화면 구성 + UX + 즉시성 KPI | Phase 3 + 4 |
| 6. API 통합 + 에러 처리 + 키 관리 | Task 2.4~2.6 + 2.9 |
| 7. AdMob 배너 | Phase 6 + Task 7.5(prod ID) |
| 8. i18n KO/EN | Phase 5 + Task 8.2(메타) |
| 9. 출시 체크리스트 | Phase 7 + 8 |
| 10. 테스트 전략 | Task 2.4·2.6·2.7·3.2 (단위), UI 테스트 1개는 향후 보강(현 plan 미포함 — 출시 후 추가 ADR 필요) |
| 11. 검증(KPI) | Task 8.4(출시 후 회고에서 측정 셋업) |
| 12. 위험·완화 | 각 Task에 분산 (키스토어 백업, 행안부 키 D-7, 광고 정책 등) |
| 13. 자산 추출 계획 | Task 8.4 회고로 명시화 |

### Placeholder 스캔 결과
- "TBD" 단어 1건 — README의 License 항목 (의도적 미정 → 출시 후 결정).
- "(향후 보강)", "(별도 spec 사이클로)" — 명시적 후속 작업 표시 (placeholder 아님).
- 모든 코드 step에 실제 코드 포함, 모든 명령에 실행 가능한 한 줄 명시.

### 타입 일관성 점검
- `Result<T>` (sealed) — Task 2.3에서 정의, 2.4·2.6·2.7·3.2에서 일관 사용.
- `AppError` (sealed) — Task 2.2에서 정의, 2.6에서 매핑, 3.2에서 친화 메시지로 변환.
- `Address` 도메인 모델 — Task 2.2 정의, 2.4·2.6·2.7·3.2·3.3·4.2·4.3에서 일관 사용.
- `SearchUiState.Phase` (sealed interface) — Task 3.2 정의, 3.4에서 when 분기로 사용.
- 메서드 시그니처: `repository.search(query)` — Task 2.3·2.7·3.2 모두 일치.

### 알려진 미완 항목 (의도적 후속)
- UI Instrumented 테스트 (SearchScreen 핵심 동선 1개) — 출시 후 별도 plan에서 추가.
- RUNBOOK.md (운영 가이드) — 출시 후 작성.
- App Shortcuts(E), 클립보드 모니터(C), 검색 히스토리·즐겨찾기·위젯 등 v1.1+ 항목 — 별도 spec 사이클.

---

## Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-04-26-zipkr-mvp-plan.md`.**

다음 두 가지 실행 옵션 중 선택:

### 1. Subagent-Driven (recommended)
- 매 Task마다 신선한 subagent를 보내 구현 → 두 단계 리뷰
- 빠른 반복, 컨텍스트 보호
- REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`

### 2. Inline Execution
- 이 세션에서 직접 batch 실행 + checkpoint 리뷰
- 컨텍스트 연속성 유지, 형이 매 단계 옆에서 보기 좋음
- REQUIRED SUB-SKILL: `superpowers:executing-plans`

**어느 쪽으로 가시겠어요, 형?**
