# 상세 시트 (Phase 4 v2) 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 검색 결과 카드를 탭하면 ModalBottomSheet로 상세 시트가 올라와 풀 주소·건물명·인터랙티브 카카오맵·외부 앱 deep link를 한 화면에 보여준다.

**Architecture:** SearchScreen이 sheet open state(`detailSheetAddress: Address?`)를 보유한다. 사용자 카드 탭 시 state를 set → `DetailSheet` 컴포저블이 `ModalBottomSheet`를 띄운다. 시트 안에서 카카오 로컬 REST로 좌표를 lazy fetch + in-memory 캐시 후, `KakaoMapWebView`(WebView + 카카오맵 web JS SDK)와 deep link 버튼에 전달. NavHost는 사용하지 않는다 (시·도 시트와 동일 패턴).

**Tech Stack:** Kotlin 1.9.24, Compose Material3, Hilt, Retrofit + kotlinx-serialization, kotlin-parcelize, AndroidView{WebView}, core-splashscreen, mockk + truth + turbine

---

## File Structure

### 신규 파일

| 파일 | 책임 |
|---|---|
| `app/src/main/kotlin/com/jewan/zipkr/data/Coordinate.kt` | WGS84 위·경도 도메인 모델 (`@Parcelize`) |
| `app/src/main/kotlin/com/jewan/zipkr/data/CoordinateRepository.kt` | 좌표 fetch interface |
| `app/src/main/kotlin/com/jewan/zipkr/data/CoordinateRepositoryImpl.kt` | 카카오 API 호출 + in-memory 캐시 |
| `app/src/main/kotlin/com/jewan/zipkr/data/provider/kakao/KakaoLocalApi.kt` | Retrofit interface |
| `app/src/main/kotlin/com/jewan/zipkr/data/provider/kakao/KakaoModels.kt` | DTO + 도메인 변환 |
| `app/src/main/kotlin/com/jewan/zipkr/util/MapDeepLinks.kt` | 카카오맵·네이버지도 URL/Intent 빌더 |
| `app/src/main/kotlin/com/jewan/zipkr/util/KakaoMapHtmlBuilder.kt` | WebView 주입용 HTML 문자열 빌더 |
| `app/src/main/kotlin/com/jewan/zipkr/ui/components/CopyBar.kt` | AddressResultCard에서 분리한 4-segment CopyBar (재사용 가능) |
| `app/src/main/kotlin/com/jewan/zipkr/ui/components/KakaoMapWebView.kt` | AndroidView{WebView}로 카카오맵 web HTML 임베드 |
| `app/src/main/kotlin/com/jewan/zipkr/ui/components/MapDeepLinkButtons.kt` | 외부 앱 진입 2 버튼 |
| `app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailUiState.kt` | `CoordinatePhase` sealed |
| `app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailViewModel.kt` | Hilt + lazy fetch |
| `app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailHeader.kt` | 점보 zip + 풀 주소 영역 |
| `app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailSheet.kt` | ModalBottomSheet 조립 |
| `app/src/main/res/values/themes.xml` | `Theme.Zipkr.Splash` 정의 |

### 수정 파일

| 파일 | 변경 |
|---|---|
| `app/build.gradle.kts` | `kotlin-parcelize` plugin + 카카오 키 BuildConfig 2개 |
| `gradle/libs.versions.toml` | `kotlin-parcelize` plugin alias |
| `local.properties` | 카카오 키 2개 추가 |
| `app/src/main/kotlin/com/jewan/zipkr/data/Address.kt` | `@Parcelize` + 4 필드 추가 |
| `app/src/main/kotlin/com/jewan/zipkr/data/provider/juso/JusoModels.kt` | 4 필드 추가 + `toDomain` 매핑 |
| `app/src/main/kotlin/com/jewan/zipkr/di/DiQualifiers.kt` | `KAKAO_REST_API_KEY` qualifier |
| `app/src/main/kotlin/com/jewan/zipkr/di/NetworkModule.kt` | `KakaoLocalApi` Retrofit + 키 provide |
| `app/src/main/kotlin/com/jewan/zipkr/di/DataModule.kt` | `CoordinateRepository` bind |
| `app/src/main/kotlin/com/jewan/zipkr/ui/components/AddressResultCard.kt` | CopyBar 추출 → 신규 `ui/components/CopyBar.kt` import + 호출 |
| `app/src/main/kotlin/com/jewan/zipkr/ui/search/SearchScreen.kt` | `onCardClick: (Address) -> Unit` 시그니처 + DetailSheet 통합 |
| `app/src/main/kotlin/com/jewan/zipkr/MainActivity.kt` | `installSplashScreen()` |
| `app/src/main/AndroidManifest.xml` | activity theme = Splash |
| `app/src/test/kotlin/com/jewan/zipkr/data/provider/juso/JusoModelsTest.kt` | toDomain 매핑 신규 필드 검증 |
| `app/src/test/kotlin/com/jewan/zipkr/ui/search/SearchViewModelTest.kt` | 모델 변경 반영 (Address 생성자 인자 추가) |

### 신규 테스트 파일

| 파일 | 대상 |
|---|---|
| `app/src/test/kotlin/com/jewan/zipkr/data/provider/kakao/KakaoModelsTest.kt` | DTO → Coordinate 변환 |
| `app/src/test/kotlin/com/jewan/zipkr/data/CoordinateRepositoryImplTest.kt` | 캐시 hit, 실패 wrap |
| `app/src/test/kotlin/com/jewan/zipkr/util/KakaoMapHtmlBuilderTest.kt` | HTML substring assert |
| `app/src/test/kotlin/com/jewan/zipkr/util/MapDeepLinksTest.kt` | URL 빌더 |
| `app/src/test/kotlin/com/jewan/zipkr/ui/detail/DetailViewModelTest.kt` | lazy fetch / 캐시 hit / 실패 |

---

## Task 0: 분기 + worktree 준비

**Files:** 없음 (git operation만)

- [ ] **Step 1: 최신 develop pull**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr
git checkout develop
git pull origin develop
```

Expected: `Already up to date.` 또는 `Updating ... Fast-forward`

- [ ] **Step 2: feat 브랜치 + worktree 생성**

```bash
git worktree add -b feat/detail-sheet-v1 .worktrees/detail-sheet-v1 develop
```

Expected: `Preparing worktree (new branch 'feat/detail-sheet-v1')`

- [ ] **Step 3: local.properties 복사 (worktree 빌드용)**

```bash
cp local.properties .worktrees/detail-sheet-v1/local.properties
```

이후 모든 빌드·테스트는 `.worktrees/detail-sheet-v1/` 안에서 실행한다.

---

## Task 1: 카카오 키 BuildConfig 통합

**Files:**
- Modify: `local.properties`
- Modify: `app/build.gradle.kts:39-44`

- [ ] **Step 1: local.properties에 카카오 키 두 개 추가**

`/Users/jewan/Desktop/git/98_jewan/zipkr/.worktrees/detail-sheet-v1/local.properties` 파일 끝에 추가 (REST 키는 형이 발급한 dev 키, JS 키는 형이 발급해 채워넣음):

```properties
# 카카오 dev console 발급 키 (REST + JavaScript). 운영 키는 출시 직전 별도 발급.
kakao.rest.api.key=0c9ea6b74783941aee8bd997a1f74088
kakao.js.api.key=PLACEHOLDER_FORMAT_FORMA_KEY_FROM_FORMAT_KAKAO_DEV_CONSOLE
```

> JS 키가 빈 값이면 WebView 카카오맵이 로드 실패한다. 형이 카카오 dev console에서 "Web 플랫폼 등록 + 도메인 `https://localhost` 추가" 후 키를 발급해 위에 채워야 한다.

- [ ] **Step 2: build.gradle.kts에 buildConfigField 추가**

`app/build.gradle.kts` 파일에서 `defaultConfig` 블록 안 `val jusoKey = ...` 다음 줄에 추가 (line 39 근처):

```kotlin
val jusoKey = localProps.getProperty("juso.api.key", "")
buildConfigField("String", "JUSO_API_KEY", "\"$jusoKey\"")

val kakaoRestKey = localProps.getProperty("kakao.rest.api.key", "")
buildConfigField("String", "KAKAO_REST_API_KEY", "\"$kakaoRestKey\"")
val kakaoJsKey = localProps.getProperty("kakao.js.api.key", "")
buildConfigField("String", "KAKAO_JS_KEY", "\"$kakaoJsKey\"")
if (kakaoRestKey.isBlank() || kakaoJsKey.isBlank()) {
    println("⚠ kakao.rest.api.key / kakao.js.api.key가 local.properties에 없다. 상세 시트 지도가 동작하지 않는다.")
}
```

- [ ] **Step 3: 빌드해서 BuildConfig 클래스가 새 필드와 함께 생성되는지 확인**

```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr/.worktrees/detail-sheet-v1
./gradlew :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL` (경고 println은 JS 키가 빈 값이면 표시됨)

- [ ] **Step 4: commit**

```bash
git add app/build.gradle.kts
git commit -m "build: 카카오 REST/JS 키 BuildConfig 주입 추가"
```

(`local.properties`는 .gitignore라 staging 안 됨)

---

## Task 2: kotlin-parcelize plugin 추가

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts:4-8`

- [ ] **Step 1: libs.versions.toml에 plugin alias 추가**

`[plugins]` 섹션에 추가:

```toml
kotlin-parcelize = { id = "org.jetbrains.kotlin.plugin.parcelize", version.ref = "kotlin" }
```

(`kotlin` version은 이미 `[versions]`에 있음 — `kotlin = "1.9.24"`)

- [ ] **Step 2: build.gradle.kts plugins 블록에 적용**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)  // ← 추가
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}
```

- [ ] **Step 3: 빌드 확인**

```bash
./gradlew :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "build: kotlin-parcelize plugin 추가"
```

---

## Task 3: Address 모델 확장 + @Parcelize

**Files:**
- Modify: `app/src/main/kotlin/com/jewan/zipkr/data/Address.kt`

- [ ] **Step 1: Address.kt 전체 교체**

```kotlin
package com.jewan.zipkr.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 도메인 주소 모델이다.
 * 외부 API DTO(JusoModels)와 분리되며, UI는 항상 이 모델만 본다.
 *
 * v1.1c부터 buildingName/sido/sigungu/eupmyeondong이 행안부 raw 응답에서 같이 매핑된다.
 * 빈 문자열도 허용한다(예: 건물명이 없는 일반 주택).
 *
 * @Parcelize는 SearchScreen → DetailSheet에 rememberSaveable로 통째 전달하기 위함이다.
 */
@Parcelize
data class Address(
    val zipCode: String,
    val roadAddress: String,
    val jibunAddress: String,
    val englishAddress: String,
    val buildingName: String,
    val sido: String,
    val sigungu: String,
    val eupmyeondong: String,
) : Parcelable
```

- [ ] **Step 2: 기존 테스트 빌드 통과 확인 (Address 인자 추가로 깨질 수 있음)**

```bash
./gradlew :app:compileDebugUnitTestKotlin
```

Expected: 컴파일 에러 — 기존 `Address(...)` 호출부에 신규 인자가 빠짐. 다음 step에서 fix.

- [ ] **Step 3: SearchViewModelTest의 Address 생성 helper 추가 + 호출부 갱신**

`app/src/test/kotlin/com/jewan/zipkr/ui/search/SearchViewModelTest.kt`에서 `Address(...)` 호출하는 모든 줄을 찾아 신규 4 인자(빈 문자열)를 추가하거나, 파일 상단에 helper 추가:

```kotlin
private fun testAddress(
    zipCode: String,
    roadAddress: String = "도로명",
    jibunAddress: String = "지번",
    englishAddress: String = "Eng",
): Address = Address(
    zipCode = zipCode,
    roadAddress = roadAddress,
    jibunAddress = jibunAddress,
    englishAddress = englishAddress,
    buildingName = "",
    sido = "",
    sigungu = "",
    eupmyeondong = "",
)
```

기존 호출부 `Address(...)`을 모두 `testAddress(...)`로 바꾸기.

- [ ] **Step 4: 동일 helper로 다른 테스트 갱신** (`AddressRepositoryImplTest`, `JusoApiProviderTest`, `JusoModelsTest`도 마찬가지)

각 파일 상단에 위 testAddress helper 복붙 후 `Address(...)`을 다 교체. 또는 직접 인자 추가.

- [ ] **Step 5: 테스트 빌드 + 실행**

```bash
./gradlew :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL` (모든 기존 테스트 pass)

- [ ] **Step 6: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/data/Address.kt app/src/test
git commit -m "feat(data): Address에 buildingName·sido·sigungu·eupmyeondong 필드 추가 + Parcelize"
```

---

## Task 4: JusoModels 확장 + toDomain 매핑

**Files:**
- Modify: `app/src/main/kotlin/com/jewan/zipkr/data/provider/juso/JusoModels.kt`
- Modify: `app/src/test/kotlin/com/jewan/zipkr/data/provider/juso/JusoModelsTest.kt`

- [ ] **Step 1: JusoModelsTest에 신규 필드 매핑 검증 테스트 먼저 작성**

`app/src/test/kotlin/com/jewan/zipkr/data/provider/juso/JusoModelsTest.kt`에 추가:

```kotlin
@Test
fun `toDomain 매핑 시 행안부 raw 응답의 bdNm·siNm·sggNm·emdNm가 Address 신규 필드로 들어간다`() {
    val dto = JusoAddressDto(
        roadAddr = "경기도 평택시 안중읍 안현로 400",
        jibunAddr = "경기도 평택시 안중읍 안중리 445-16 안중읍행정복지센터",
        engAddr = "400 Anhyeon-ro, Anjung-eup, Pyeongtaek-si, Gyeonggi-do",
        zipNo = "17941",
        bdNm = "안중읍행정복지센터",
        siNm = "경기도",
        sggNm = "평택시",
        emdNm = "안중읍",
    )

    val domain = dto.toDomain()

    assertThat(domain.zipCode).isEqualTo("17941")
    assertThat(domain.buildingName).isEqualTo("안중읍행정복지센터")
    assertThat(domain.sido).isEqualTo("경기도")
    assertThat(domain.sigungu).isEqualTo("평택시")
    assertThat(domain.eupmyeondong).isEqualTo("안중읍")
}

@Test
fun `bdNm이 빈 문자열이면 Address buildingName도 빈 문자열이다`() {
    val dto = JusoAddressDto(
        roadAddr = "도로명", jibunAddr = "지번", engAddr = "Eng", zipNo = "12345",
        bdNm = "", siNm = "서울", sggNm = "강남구", emdNm = "역삼동",
    )

    assertThat(dto.toDomain().buildingName).isEqualTo("")
}
```

- [ ] **Step 2: 테스트 컴파일 fail 확인**

```bash
./gradlew :app:compileDebugUnitTestKotlin
```

Expected: 컴파일 에러 — `JusoAddressDto`에 신규 필드 없음.

- [ ] **Step 3: JusoModels.kt에 신규 필드 추가 + toDomain 매핑**

```kotlin
@Serializable
data class JusoAddressDto(
    @SerialName("roadAddr") val roadAddr: String,
    @SerialName("jibunAddr") val jibunAddr: String,
    @SerialName("engAddr") val engAddr: String,
    @SerialName("zipNo") val zipNo: String,
    @SerialName("bdNm") val bdNm: String = "",
    @SerialName("siNm") val siNm: String = "",
    @SerialName("sggNm") val sggNm: String = "",
    @SerialName("emdNm") val emdNm: String = "",
) {
    fun toDomain(): Address =
        Address(
            zipCode = zipNo,
            roadAddress = roadAddr,
            jibunAddress = jibunAddr,
            englishAddress = engAddr,
            buildingName = bdNm,
            sido = siNm,
            sigungu = sggNm,
            eupmyeondong = emdNm,
        )
}
```

(default = "" 이유: 미래 행안부 응답에서 필드가 빠지더라도 deserialization 실패 안 함)

- [ ] **Step 4: 전체 테스트 실행 → pass 확인**

```bash
./gradlew :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL` — 신규 테스트 2건 + 기존 테스트 모두 pass

- [ ] **Step 5: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/data/provider/juso/JusoModels.kt \
        app/src/test/kotlin/com/jewan/zipkr/data/provider/juso/JusoModelsTest.kt
git commit -m "feat(data): JusoAddressDto에 bdNm·siNm·sggNm·emdNm 필드 추가 + Address 매핑 확장"
```

---

## Task 5: Coordinate 도메인 모델

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/data/Coordinate.kt`

- [ ] **Step 1: Coordinate.kt 작성**

```kotlin
package com.jewan.zipkr.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * WGS84 위·경도 좌표이다.
 * 카카오 응답의 x = longitude, y = latitude로 매핑된다.
 * @Parcelize는 시트 state에 들어갈 가능성을 위해 둔다 (현재는 ViewModel state라 직접 필요 없지만 확장 대비).
 */
@Parcelize
data class Coordinate(
    val longitude: Double,
    val latitude: Double,
) : Parcelable
```

- [ ] **Step 2: 빌드 확인**

```bash
./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/data/Coordinate.kt
git commit -m "feat(data): WGS84 좌표 도메인 모델 Coordinate 추가"
```

---

## Task 6: 카카오 DTO + toCoordinate 변환 + 테스트

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/data/provider/kakao/KakaoModels.kt`
- Create: `app/src/test/kotlin/com/jewan/zipkr/data/provider/kakao/KakaoModelsTest.kt`

- [ ] **Step 1: 테스트 먼저 작성**

`app/src/test/kotlin/com/jewan/zipkr/data/provider/kakao/KakaoModelsTest.kt`:

```kotlin
package com.jewan.zipkr.data.provider.kakao

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KakaoModelsTest {
    @Test
    fun `KakaoDocument의 x·y 문자열을 Coordinate WGS84로 변환한다`() {
        val doc = KakaoDocument(x = "126.931266170341", y = "36.9860624565805")

        val coord = doc.toCoordinate()

        assertThat(coord.longitude).isEqualTo(126.931266170341)
        assertThat(coord.latitude).isEqualTo(36.9860624565805)
    }

    @Test
    fun `KakaoDocument에 잘못된 숫자 문자열이 들어오면 NumberFormatException을 던진다`() {
        val doc = KakaoDocument(x = "abc", y = "36.98")

        try {
            doc.toCoordinate()
            assert(false) { "예외가 발생해야 한다" }
        } catch (_: NumberFormatException) {
            // 기대된 예외
        }
    }
}
```

- [ ] **Step 2: KakaoModels.kt 작성**

```kotlin
package com.jewan.zipkr.data.provider.kakao

import com.jewan.zipkr.data.Coordinate
import kotlinx.serialization.Serializable

/**
 * 카카오 로컬 REST geocoding API 응답 DTO이다.
 * 응답 필드는 풍부하지만 좌표 변환에 필요한 documents.x/y만 추출한다.
 * @see https://developers.kakao.com/docs/latest/ko/local/dev-guide#search-by-address
 */
@Serializable
data class KakaoGeocodeResponse(
    val documents: List<KakaoDocument> = emptyList(),
)

@Serializable
data class KakaoDocument(
    val x: String,
    val y: String,
) {
    /**
     * String으로 오는 좌표를 Double로 파싱한다.
     * 잘못된 숫자면 NumberFormatException — 호출자(Repository)에서 catch하여 AppError로 wrap한다.
     */
    fun toCoordinate(): Coordinate =
        Coordinate(longitude = x.toDouble(), latitude = y.toDouble())
}
```

- [ ] **Step 3: 테스트 실행 → pass 확인**

```bash
./gradlew :app:testDebugUnitTest --tests "com.jewan.zipkr.data.provider.kakao.KakaoModelsTest"
```

Expected: `BUILD SUCCESSFUL`, 2 tests passed

- [ ] **Step 4: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/data/provider/kakao/KakaoModels.kt \
        app/src/test/kotlin/com/jewan/zipkr/data/provider/kakao/KakaoModelsTest.kt
git commit -m "feat(data): 카카오 geocoding DTO + Coordinate 변환 추가"
```

---

## Task 7: KakaoLocalApi Retrofit interface

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/data/provider/kakao/KakaoLocalApi.kt`

- [ ] **Step 1: KakaoLocalApi.kt 작성**

```kotlin
package com.jewan.zipkr.data.provider.kakao

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 카카오 로컬 REST API의 도로명주소 검색 endpoint이다.
 * Endpoint: https://dapi.kakao.com/v2/local/search/address.json
 * 인증: Authorization header (NetworkModule의 OkHttp 인터셉터에서 주입)
 */
interface KakaoLocalApi {
    @GET("v2/local/search/address.json")
    suspend fun geocode(
        @Query("query") query: String,
    ): KakaoGeocodeResponse
}
```

- [ ] **Step 2: 컴파일 확인**

```bash
./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/data/provider/kakao/KakaoLocalApi.kt
git commit -m "feat(data): 카카오 로컬 geocoding Retrofit interface 추가"
```

---

## Task 8: DiQualifiers + NetworkModule에 카카오 통합

**Files:**
- Modify: `app/src/main/kotlin/com/jewan/zipkr/di/DiQualifiers.kt`
- Modify: `app/src/main/kotlin/com/jewan/zipkr/di/NetworkModule.kt`

- [ ] **Step 1: DiQualifiers.kt에 KAKAO_REST_API_KEY 추가**

```kotlin
package com.jewan.zipkr.di

object DiQualifiers {
    const val JUSO_API_KEY = "juso_api_key"
    const val KAKAO_REST_API_KEY = "kakao_rest_api_key"
}
```

- [ ] **Step 2: NetworkModule.kt에 카카오 Retrofit instance 추가**

`app/src/main/kotlin/com/jewan/zipkr/di/NetworkModule.kt` — 기존 Juso provide 다음에 추가:

```kotlin
import com.jewan.zipkr.data.provider.kakao.KakaoLocalApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient

private const val KAKAO_BASE_URL = "https://dapi.kakao.com/"

@Provides
@Singleton
@Named(DiQualifiers.KAKAO_REST_API_KEY)
fun provideKakaoRestApiKey(): String = BuildConfig.KAKAO_REST_API_KEY

@Provides
@Singleton
@Named("kakao_okhttp")
fun provideKakaoOkHttpClient(
    @Named(DiQualifiers.KAKAO_REST_API_KEY) apiKey: String,
    loggingInterceptor: HttpLoggingInterceptor,
): OkHttpClient {
    val authInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("Authorization", "KakaoAK $apiKey")
            .build()
        chain.proceed(req)
    }
    return OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()
}

@Provides
@Singleton
fun provideKakaoLocalApi(
    @Named("kakao_okhttp") client: OkHttpClient,
    json: Json,
): KakaoLocalApi {
    val contentType = "application/json".toMediaType()
    return Retrofit.Builder()
        .baseUrl(KAKAO_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
        .create(KakaoLocalApi::class.java)
}
```

(필요 import: `okhttp3.Interceptor`, `okhttp3.OkHttpClient`, `okhttp3.MediaType.Companion.toMediaType`, `kotlinx.serialization.json.Json`, `retrofit2.Retrofit`, `retrofit2.converter.kotlinx.serialization.asConverterFactory`)

기존 `HttpLoggingInterceptor`·`Json` provide가 이미 있는지 NetworkModule에서 확인. 없으면 같이 추가.

- [ ] **Step 3: 빌드 확인**

```bash
./gradlew :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/di
git commit -m "feat(di): 카카오 로컬 API용 OkHttp 인증 인터셉터 + Retrofit instance 추가"
```

---

## Task 9: CoordinateRepository (interface + impl + 캐시) + 테스트

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/data/CoordinateRepository.kt`
- Create: `app/src/main/kotlin/com/jewan/zipkr/data/CoordinateRepositoryImpl.kt`
- Create: `app/src/test/kotlin/com/jewan/zipkr/data/CoordinateRepositoryImplTest.kt`

- [ ] **Step 1: CoordinateRepository.kt 작성 (interface)**

```kotlin
package com.jewan.zipkr.data

interface CoordinateRepository {
    /**
     * 도로명주소로 좌표를 조회한다.
     * 같은 주소 재호출은 in-memory 캐시 hit으로 즉시 반환한다.
     */
    suspend fun fetchCoordinate(roadAddress: String): Result<Coordinate>
}
```

- [ ] **Step 2: 테스트 먼저 작성 (TDD)**

`app/src/test/kotlin/com/jewan/zipkr/data/CoordinateRepositoryImplTest.kt`:

```kotlin
package com.jewan.zipkr.data

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.provider.kakao.KakaoDocument
import com.jewan.zipkr.data.provider.kakao.KakaoGeocodeResponse
import com.jewan.zipkr.data.provider.kakao.KakaoLocalApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class CoordinateRepositoryImplTest {
    private val api: KakaoLocalApi = mockk()
    private val repo = CoordinateRepositoryImpl(api)

    @Test
    fun `정상 응답 시 첫 document를 Coordinate로 반환한다`() = runTest {
        coEvery { api.geocode("경기도 평택시 안현로 400") } returns
            KakaoGeocodeResponse(documents = listOf(KakaoDocument(x = "126.93", y = "36.98")))

        val result = repo.fetchCoordinate("경기도 평택시 안현로 400")

        val coord = (result as Result.Success).value
        assertThat(coord.longitude).isEqualTo(126.93)
        assertThat(coord.latitude).isEqualTo(36.98)
    }

    @Test
    fun `documents 빈 배열이면 AppError ApiBadResponse 실패를 반환한다`() = runTest {
        coEvery { api.geocode(any()) } returns KakaoGeocodeResponse(documents = emptyList())

        val result = repo.fetchCoordinate("없는 주소")

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        val error = (result as Result.Failure).error
        assertThat(error).isInstanceOf(AppError.ApiBadResponse::class.java)
    }

    @Test
    fun `네트워크 IOException은 AppError Network로 wrap된다`() = runTest {
        coEvery { api.geocode(any()) } throws IOException("network down")

        val result = repo.fetchCoordinate("주소")

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        assertThat((result as Result.Failure).error).isInstanceOf(AppError.Network::class.java)
    }

    @Test
    fun `같은 주소를 두 번 호출하면 두 번째는 캐시 hit으로 API 호출 안 한다`() = runTest {
        coEvery { api.geocode("주소") } returns
            KakaoGeocodeResponse(documents = listOf(KakaoDocument(x = "1.0", y = "2.0")))

        repo.fetchCoordinate("주소")
        repo.fetchCoordinate("주소")

        coVerify(exactly = 1) { api.geocode("주소") }
    }
}
```

- [ ] **Step 3: 테스트 fail 확인**

```bash
./gradlew :app:testDebugUnitTest --tests "com.jewan.zipkr.data.CoordinateRepositoryImplTest"
```

Expected: 컴파일 에러 — `CoordinateRepositoryImpl` 없음

- [ ] **Step 4: CoordinateRepositoryImpl.kt 작성**

```kotlin
package com.jewan.zipkr.data

import com.jewan.zipkr.data.provider.kakao.KakaoLocalApi
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 카카오 로컬 geocoding을 호출해 좌표를 조회한다.
 * 같은 도로명주소 재호출은 ConcurrentHashMap 캐시로 즉시 반환 (앱 재시작 시 무효).
 *
 * 실패 모드:
 * - documents 빈 배열 → ApiBadResponse("EMPTY_DOCUMENTS")
 * - IOException → Network
 * - 그 외 → Unknown
 */
@Singleton
class CoordinateRepositoryImpl
    @Inject
    constructor(
        private val api: KakaoLocalApi,
    ) : CoordinateRepository {
        private val cache = ConcurrentHashMap<String, Coordinate>()

        override suspend fun fetchCoordinate(roadAddress: String): Result<Coordinate> {
            cache[roadAddress]?.let { return Result.Success(it) }
            return runCatching { api.geocode(roadAddress) }
                .fold(
                    onSuccess = { response ->
                        val first = response.documents.firstOrNull()
                            ?: return Result.Failure(AppError.ApiBadResponse(EMPTY_DOCUMENTS_CODE))
                        val coord = first.toCoordinate()
                        cache[roadAddress] = coord
                        Result.Success(coord)
                    },
                    onFailure = { throwable ->
                        when (throwable) {
                            is IOException -> Result.Failure(AppError.Network(throwable))
                            else -> Result.Failure(AppError.Unknown(throwable))
                        }
                    },
                )
        }

        private companion object {
            // 카카오 geocoding이 빈 documents를 반환한 케이스이다 (행안부 주소가 카카오에 없음).
            const val EMPTY_DOCUMENTS_CODE = "KAKAO_EMPTY"
        }
    }
```

> 만약 기존 `AppError.Network` / `AppError.Unknown` 시그니처가 throwable 인자를 받지 않으면, 인자 없는 형태로 호출 (`AppError.Network()`). `app/src/main/kotlin/com/jewan/zipkr/data/AppError.kt`를 먼저 봐서 시그니처 맞추기.

- [ ] **Step 5: 테스트 pass 확인**

```bash
./gradlew :app:testDebugUnitTest --tests "com.jewan.zipkr.data.CoordinateRepositoryImplTest"
```

Expected: 4 tests passed

- [ ] **Step 6: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/data/CoordinateRepository.kt \
        app/src/main/kotlin/com/jewan/zipkr/data/CoordinateRepositoryImpl.kt \
        app/src/test/kotlin/com/jewan/zipkr/data/CoordinateRepositoryImplTest.kt
git commit -m "feat(data): CoordinateRepository + ConcurrentHashMap 캐시 구현"
```

---

## Task 10: DataModule에 CoordinateRepository bind

**Files:**
- Modify: `app/src/main/kotlin/com/jewan/zipkr/di/DataModule.kt`

- [ ] **Step 1: DataModule.kt에 bind 추가**

기존 `bindAddressRepository` 다음에 추가:

```kotlin
@Binds
@Singleton
abstract fun bindCoordinateRepository(impl: CoordinateRepositoryImpl): CoordinateRepository
```

(필요 import: `com.jewan.zipkr.data.CoordinateRepository`, `com.jewan.zipkr.data.CoordinateRepositoryImpl`)

- [ ] **Step 2: Hilt 빌드 확인**

```bash
./gradlew :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/di/DataModule.kt
git commit -m "feat(di): CoordinateRepository bind 추가"
```

---

## Task 11: KakaoMapHtmlBuilder util + 테스트

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/util/KakaoMapHtmlBuilder.kt`
- Create: `app/src/test/kotlin/com/jewan/zipkr/util/KakaoMapHtmlBuilderTest.kt`

- [ ] **Step 1: 테스트 먼저**

```kotlin
package com.jewan.zipkr.util

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.Coordinate
import org.junit.Test

class KakaoMapHtmlBuilderTest {
    private val coord = Coordinate(longitude = 126.93, latitude = 36.98)

    @Test
    fun `HTML에 카카오맵 SDK script 태그가 들어가고 jsKey가 주입된다`() {
        val html = buildKakaoMapHtml(coord, jsKey = "TEST_KEY", level = 3)

        assertThat(html).contains("dapi.kakao.com/v2/maps/sdk.js")
        assertThat(html).contains("appkey=TEST_KEY")
        assertThat(html).contains("autoload=false")
    }

    @Test
    fun `HTML에 위·경도가 그대로 들어가고 마커가 추가된다`() {
        val html = buildKakaoMapHtml(coord, jsKey = "K", level = 3)

        // kakao.maps.LatLng(36.98, 126.93) 형식
        assertThat(html).contains("36.98")
        assertThat(html).contains("126.93")
        assertThat(html).contains("Marker")
    }

    @Test
    fun `level 인자가 setLevel 호출에 반영된다`() {
        val html = buildKakaoMapHtml(coord, jsKey = "K", level = 5)

        assertThat(html).contains("level: 5")
    }
}
```

- [ ] **Step 2: KakaoMapHtmlBuilder.kt 작성**

```kotlin
package com.jewan.zipkr.util

import com.jewan.zipkr.data.Coordinate

private const val DEFAULT_ZOOM_LEVEL = 3

/**
 * WebView에 주입할 카카오맵 web SDK HTML 문자열을 만든다.
 * baseUrl은 호출자(`WebView.loadDataWithBaseURL`)에서 https://localhost를 사용해야 한다 — 카카오 도메인 검사 통과.
 *
 * @param coord 마커가 찍힐 좌표 (WGS84)
 * @param jsKey 카카오 JavaScript 키 (BuildConfig.KAKAO_JS_KEY)
 * @param level 카카오맵 줌 레벨 (1=가장 가까움, 14=가장 멈). 3 = 동네 단위 default.
 */
fun buildKakaoMapHtml(
    coord: Coordinate,
    jsKey: String,
    level: Int = DEFAULT_ZOOM_LEVEL,
): String =
    """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
      <style>html, body, #map { margin: 0; padding: 0; width: 100%; height: 100%; }</style>
      <script src="//dapi.kakao.com/v2/maps/sdk.js?appkey=$jsKey&autoload=false"></script>
    </head>
    <body>
      <div id="map"></div>
      <script>
        kakao.maps.load(function() {
          var center = new kakao.maps.LatLng(${coord.latitude}, ${coord.longitude});
          var map = new kakao.maps.Map(document.getElementById('map'), {
            center: center,
            level: $level
          });
          new kakao.maps.Marker({ position: center, map: map });
          map.addControl(new kakao.maps.ZoomControl(), kakao.maps.ControlPosition.RIGHT);
        });
      </script>
    </body>
    </html>
    """.trimIndent()
```

- [ ] **Step 3: 테스트 pass**

```bash
./gradlew :app:testDebugUnitTest --tests "com.jewan.zipkr.util.KakaoMapHtmlBuilderTest"
```

Expected: 3 tests passed

- [ ] **Step 4: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/util/KakaoMapHtmlBuilder.kt \
        app/src/test/kotlin/com/jewan/zipkr/util/KakaoMapHtmlBuilderTest.kt
git commit -m "feat(util): 카카오맵 web HTML 빌더 + 테스트"
```

---

## Task 12: MapDeepLinks util + 테스트

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/util/MapDeepLinks.kt`
- Create: `app/src/test/kotlin/com/jewan/zipkr/util/MapDeepLinksTest.kt`

- [ ] **Step 1: 테스트 먼저**

```kotlin
package com.jewan.zipkr.util

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.Coordinate
import org.junit.Test

class MapDeepLinksTest {
    private val coord = Coordinate(longitude = 126.93, latitude = 36.98)
    private val address = "경기도 평택시 안현로 400"

    @Test
    fun `좌표가 있으면 카카오맵 deep link는 lat lng를 사용한다`() {
        val link = kakaoMapDeepLink(coord = coord, fallbackAddress = address)
        assertThat(link).startsWith("kakaomap://look?p=")
        assertThat(link).contains("36.98,126.93")
    }

    @Test
    fun `좌표가 null이면 카카오맵 deep link는 search query로 fallback한다`() {
        val link = kakaoMapDeepLink(coord = null, fallbackAddress = address)
        assertThat(link).startsWith("kakaomap://search?q=")
        // URL 인코딩된 한글 주소
        assertThat(link).contains("%EA%B2%BD%EA%B8%B0")
    }

    @Test
    fun `좌표가 있으면 네이버지도 deep link는 lat lng + name을 사용한다`() {
        val link = naverMapDeepLink(coord = coord, name = address)
        assertThat(link).startsWith("nmap://place?")
        assertThat(link).contains("lat=36.98")
        assertThat(link).contains("lng=126.93")
    }

    @Test
    fun `좌표가 null이면 네이버지도 deep link는 query로 fallback한다`() {
        val link = naverMapDeepLink(coord = null, name = address)
        assertThat(link).startsWith("nmap://search?")
        assertThat(link).contains("query=")
    }

    @Test
    fun `web fallback URL은 좌표 없이도 카카오맵 검색을 연다`() {
        val link = kakaoMapWebFallback(address)
        assertThat(link).startsWith("https://map.kakao.com/")
    }
}
```

- [ ] **Step 2: MapDeepLinks.kt 작성**

```kotlin
package com.jewan.zipkr.util

import com.jewan.zipkr.data.Coordinate
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * 외부 지도 앱 deep link / web fallback URL을 만든다.
 *
 * - 좌표가 있으면 정확한 핀 URL로, 없으면 주소 텍스트 query URL로 fallback한다.
 * - 카카오맵·네이버지도 두 앱 모두 같은 패턴이다.
 * - 앱 미설치 시 ActivityNotFoundException → 호출자에서 web fallback URL을 ACTION_VIEW로 처리.
 */
private fun encode(text: String): String =
    URLEncoder.encode(text, StandardCharsets.UTF_8.name())

fun kakaoMapDeepLink(coord: Coordinate?, fallbackAddress: String): String =
    if (coord != null) {
        "kakaomap://look?p=${coord.latitude},${coord.longitude}"
    } else {
        "kakaomap://search?q=${encode(fallbackAddress)}"
    }

fun naverMapDeepLink(coord: Coordinate?, name: String): String =
    if (coord != null) {
        "nmap://place?lat=${coord.latitude}&lng=${coord.longitude}&name=${encode(name)}"
    } else {
        "nmap://search?query=${encode(name)}"
    }

fun kakaoMapWebFallback(address: String): String =
    "https://map.kakao.com/?q=${encode(address)}"

fun naverMapWebFallback(address: String): String =
    "https://map.naver.com/?query=${encode(address)}"
```

- [ ] **Step 3: 테스트 pass**

```bash
./gradlew :app:testDebugUnitTest --tests "com.jewan.zipkr.util.MapDeepLinksTest"
```

Expected: 5 tests passed

- [ ] **Step 4: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/util/MapDeepLinks.kt \
        app/src/test/kotlin/com/jewan/zipkr/util/MapDeepLinksTest.kt
git commit -m "feat(util): 카카오맵·네이버지도 deep link + web fallback URL 빌더"
```

---

## Task 13: KakaoMapWebView Compose 컴포넌트

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/components/KakaoMapWebView.kt`

- [ ] **Step 1: KakaoMapWebView.kt 작성**

```kotlin
package com.jewan.zipkr.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.viewinterop.AndroidView
import com.jewan.zipkr.data.Coordinate
import com.jewan.zipkr.util.buildKakaoMapHtml

private const val MAP_BASE_URL = "https://localhost"
private const val MAP_MIME_TYPE = "text/html"
private const val MAP_ENCODING = "UTF-8"
private val MAP_CORNER_RADIUS = androidx.compose.ui.unit.dp(16f)

/**
 * WebView로 카카오맵 web SDK를 임베드한다.
 * @param coord 마커 좌표
 * @param jsKey BuildConfig.KAKAO_JS_KEY
 * @param level 줌 레벨 (default 3 = 동네)
 *
 * baseUrl을 https://localhost로 둬야 카카오 web 플랫폼 도메인 검사 통과.
 * 보안 노트: setJavaScriptEnabled(true)는 카카오 SDK 로딩에 필요하다 — 우리 자체 JS는 주입 안 함.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun KakaoMapWebView(
    coord: Coordinate,
    jsKey: String,
    modifier: Modifier = Modifier,
    level: Int = 3,
) {
    val html = remember(coord, jsKey, level) { buildKakaoMapHtml(coord, jsKey, level) }
    AndroidView(
        modifier = modifier.clip(RoundedCornerShape(MAP_CORNER_RADIUS)),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                loadDataWithBaseURL(MAP_BASE_URL, html, MAP_MIME_TYPE, MAP_ENCODING, null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(MAP_BASE_URL, html, MAP_MIME_TYPE, MAP_ENCODING, null)
        },
    )
}
```

> **주의**: `dp(16f)` 표현식은 잘못됨 — 정확한 표현은 `16.dp`. Step 1을 수정해 `import androidx.compose.ui.unit.dp` 추가하고 `private val MAP_CORNER_RADIUS = 16.dp`로 작성.

- [ ] **Step 2: 빌드 확인**

```bash
./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`. dp import 누락 시 fix.

- [ ] **Step 3: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/ui/components/KakaoMapWebView.kt
git commit -m "feat(ui): KakaoMapWebView — AndroidView로 카카오맵 web SDK 임베드"
```

---

## Task 14: MapDeepLinkButtons Compose

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/components/MapDeepLinkButtons.kt`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: strings.xml에 라벨 추가**

```xml
<string name="map_open_kakao">카카오맵</string>
<string name="map_open_naver">네이버지도</string>
<string name="map_load_failed">지도를 불러올 수 없어요</string>
<string name="map_load_retry">다시 시도</string>
```

- [ ] **Step 2: MapDeepLinkButtons.kt 작성**

```kotlin
package com.jewan.zipkr.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.jewan.zipkr.R
import com.jewan.zipkr.data.Coordinate
import com.jewan.zipkr.ui.theme.ZipkrSpacing
import com.jewan.zipkr.util.kakaoMapDeepLink
import com.jewan.zipkr.util.kakaoMapWebFallback
import com.jewan.zipkr.util.naverMapDeepLink
import com.jewan.zipkr.util.naverMapWebFallback

/**
 * 외부 지도 앱 진입 2 버튼이다.
 * - 카카오맵·네이버지도 deep link 시도 → 미설치 시 web fallback URL을 ACTION_VIEW로 연다.
 * - 좌표가 null이어도 주소 query 기반 fallback URL로 동작 (좌표 fetch 실패 시 graceful degradation).
 */
@Composable
fun MapDeepLinkButtons(
    coord: Coordinate?,
    address: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
    ) {
        FilledTonalButton(
            onClick = {
                openOrFallback(
                    context = context,
                    deepLink = kakaoMapDeepLink(coord, address),
                    webFallback = kakaoMapWebFallback(address),
                )
            },
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.map_open_kakao))
        }
        FilledTonalButton(
            onClick = {
                openOrFallback(
                    context = context,
                    deepLink = naverMapDeepLink(coord, address),
                    webFallback = naverMapWebFallback(address),
                )
            },
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.map_open_naver))
        }
    }
}

private fun openOrFallback(
    context: Context,
    deepLink: String,
    webFallback: String,
) {
    val deepIntent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
    try {
        context.startActivity(deepIntent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webFallback)))
    }
}
```

- [ ] **Step 3: 빌드 확인**

```bash
./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/ui/components/MapDeepLinkButtons.kt \
        app/src/main/res/values/strings.xml
git commit -m "feat(ui): MapDeepLinkButtons — 카카오맵·네이버지도 진입 + web fallback"
```

---

## Task 15: AddressResultCard에서 CopyBar 분리 (재사용 가능하게)

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/components/CopyBar.kt`
- Modify: `app/src/main/kotlin/com/jewan/zipkr/ui/components/AddressResultCard.kt`

- [ ] **Step 1: AddressResultCard.kt에서 `private fun CopyBar(...)` 와 그 하위 (`ZipSegment`, `RoadSegment`, `SecondarySegment`, `SegmentDivider`) + 시각 토큰 const 들을 새 파일로 옮김**

`app/src/main/kotlin/com/jewan/zipkr/ui/components/CopyBar.kt`:

```kotlin
package com.jewan.zipkr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewan.zipkr.R

private const val ZIP_SEGMENT_WEIGHT = 1.4f
private const val ROAD_SEGMENT_WEIGHT = 1.4f
private const val SECONDARY_SEGMENT_WEIGHT = 1f
private const val ROAD_TINT_ALPHA = 0.10f
private val SEGMENT_LABEL_SIZE = 12.sp
private val SEGMENT_VPAD = 14.dp
private val SEGMENT_ICON_SIZE = 14.dp
private val SEGMENT_ICON_GAP = 6.dp
private val SEGMENT_DIVIDER_WIDTH = 1.dp

/**
 * 4 segment 통일 복사 바. AddressResultCard와 DetailSheet가 공유한다.
 * 우→좌 위계: 우편번호(brand fill) | 도로명(brand tint) | 지번 plain | 영문 plain.
 */
@Composable
fun CopyBar(
    onCopyZip: () -> Unit,
    onCopyRoad: () -> Unit,
    onCopyJibun: () -> Unit,
    onCopyEnglish: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
    ) {
        SecondarySegment(stringResource(R.string.copy_english_short), onCopyEnglish, Modifier.weight(SECONDARY_SEGMENT_WEIGHT))
        SegmentDivider()
        SecondarySegment(stringResource(R.string.copy_jibun_short), onCopyJibun, Modifier.weight(SECONDARY_SEGMENT_WEIGHT))
        SegmentDivider()
        RoadSegment(onCopyRoad, Modifier.weight(ROAD_SEGMENT_WEIGHT))
        ZipSegment(onCopyZip, Modifier.weight(ZIP_SEGMENT_WEIGHT))
    }
}

@Composable
private fun ZipSegment(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RectangleShape,
        contentPadding = PaddingValues(vertical = SEGMENT_VPAD),
    ) {
        Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(SEGMENT_ICON_SIZE))
        Spacer(Modifier.width(SEGMENT_ICON_GAP))
        Text(stringResource(R.string.copy_zip_short), fontSize = SEGMENT_LABEL_SIZE, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RoadSegment(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RectangleShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = ROAD_TINT_ALPHA),
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        contentPadding = PaddingValues(vertical = SEGMENT_VPAD),
    ) {
        Text(stringResource(R.string.copy_road_short), fontSize = SEGMENT_LABEL_SIZE, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SecondarySegment(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RectangleShape,
        contentPadding = PaddingValues(vertical = SEGMENT_VPAD),
    ) {
        Text(label, fontSize = SEGMENT_LABEL_SIZE, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SegmentDivider() {
    Box(
        modifier = Modifier
            .width(SEGMENT_DIVIDER_WIDTH)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}
```

- [ ] **Step 2: AddressResultCard.kt에서 CopyBar·ZipSegment·RoadSegment·SecondarySegment·SegmentDivider·관련 const 삭제**

남는 코드는 `AddressResultCard` + `CardBody` 둘만. CopyBar 호출은 import 추가로 그대로 작동:

```kotlin
import com.jewan.zipkr.ui.components.CopyBar  // 같은 패키지라 import 생략 가능
```

- [ ] **Step 3: 빌드 + 기존 테스트 pass 확인**

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Expected: 둘 다 SUCCESSFUL

- [ ] **Step 4: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/ui/components/CopyBar.kt \
        app/src/main/kotlin/com/jewan/zipkr/ui/components/AddressResultCard.kt
git commit -m "refactor(ui): CopyBar를 별도 컴포저블로 분리 (DetailSheet 재사용 위함)"
```

---

## Task 16: DetailUiState

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailUiState.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.jewan.zipkr.ui.detail

import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.data.Coordinate

/**
 * 상세 시트의 좌표 fetch 상태이다. Address 자체는 컴포저블 props로 받으므로 여기에 포함 안 한다.
 */
sealed interface CoordinatePhase {
    data object Loading : CoordinatePhase
    data class Success(val coordinate: Coordinate) : CoordinatePhase
    data class Failure(val error: AppError) : CoordinatePhase
}
```

- [ ] **Step 2: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailUiState.kt
git commit -m "feat(ui): DetailUiState — 좌표 fetch CoordinatePhase sealed"
```

---

## Task 17: DetailViewModel + 테스트

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailViewModel.kt`
- Create: `app/src/test/kotlin/com/jewan/zipkr/ui/detail/DetailViewModelTest.kt`

- [ ] **Step 1: 테스트 먼저**

```kotlin
package com.jewan.zipkr.ui.detail

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.data.Coordinate
import com.jewan.zipkr.data.CoordinateRepository
import com.jewan.zipkr.data.Result
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
class DetailViewModelTest {
    private val repo: CoordinateRepository = mockk()
    private lateinit var viewModel: DetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = DetailViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `초기 상태는 Loading이다`() = runTest {
        assertThat(viewModel.coordinate.value).isEqualTo(CoordinatePhase.Loading)
    }

    @Test
    fun `fetchCoordinate 성공 시 Success로 전환된다`() = runTest {
        coEvery { repo.fetchCoordinate("주소") } returns
            Result.Success(Coordinate(longitude = 126.0, latitude = 36.0))

        viewModel.fetchCoordinate("주소")

        val phase = viewModel.coordinate.value as CoordinatePhase.Success
        assertThat(phase.coordinate.longitude).isEqualTo(126.0)
    }

    @Test
    fun `fetchCoordinate 실패 시 Failure에 AppError가 보존된다`() = runTest {
        coEvery { repo.fetchCoordinate("주소") } returns Result.Failure(AppError.Network())

        viewModel.fetchCoordinate("주소")

        val phase = viewModel.coordinate.value as CoordinatePhase.Failure
        assertThat(phase.error).isInstanceOf(AppError.Network::class.java)
    }
}
```

- [ ] **Step 2: DetailViewModel.kt 작성**

```kotlin
package com.jewan.zipkr.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewan.zipkr.data.CoordinateRepository
import com.jewan.zipkr.data.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 상세 시트의 ViewModel이다.
 * Address는 컴포저블 props로 받고, 좌표만 lazy fetch한다.
 * 같은 주소 재호출은 Repository의 in-memory 캐시가 흡수한다.
 */
@HiltViewModel
class DetailViewModel
    @Inject
    constructor(
        private val coordinateRepository: CoordinateRepository,
    ) : ViewModel() {
        private val _coordinate = MutableStateFlow<CoordinatePhase>(CoordinatePhase.Loading)
        val coordinate: StateFlow<CoordinatePhase> = _coordinate.asStateFlow()

        fun fetchCoordinate(roadAddress: String) {
            _coordinate.value = CoordinatePhase.Loading
            viewModelScope.launch {
                _coordinate.value =
                    when (val result = coordinateRepository.fetchCoordinate(roadAddress)) {
                        is Result.Success -> CoordinatePhase.Success(result.value)
                        is Result.Failure -> CoordinatePhase.Failure(result.error)
                    }
            }
        }
    }
```

- [ ] **Step 3: 테스트 pass**

```bash
./gradlew :app:testDebugUnitTest --tests "com.jewan.zipkr.ui.detail.DetailViewModelTest"
```

Expected: 3 tests passed

- [ ] **Step 4: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailViewModel.kt \
        app/src/test/kotlin/com/jewan/zipkr/ui/detail/DetailViewModelTest.kt
git commit -m "feat(ui): DetailViewModel — 좌표 lazy fetch + Phase 매핑"
```

---

## Task 18: DetailHeader Compose

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailHeader.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.jewan.zipkr.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.ui.theme.ZipkrSpacing

private val ZIP_JUMBO_SIZE = 32.sp
private val LABEL_BODY_SIZE = 13.sp
private val SUBTLE_BODY_SIZE = 12.sp

/**
 * 상세 시트 상단의 점보 우편번호 + 풀 주소 영역이다.
 * 건물명은 빈 문자열일 때 숨긴다.
 */
@Composable
fun DetailHeader(
    address: Address,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = ZipkrSpacing.md, vertical = ZipkrSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.xs),
    ) {
        Text(
            text = address.zipCode,
            fontSize = ZIP_JUMBO_SIZE,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = address.roadAddress,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = address.jibunAddress,
            fontSize = LABEL_BODY_SIZE,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (address.buildingName.isNotEmpty()) {
            Text(
                text = address.buildingName,
                fontSize = LABEL_BODY_SIZE,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            text = address.englishAddress,
            fontSize = SUBTLE_BODY_SIZE,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
```

- [ ] **Step 2: 빌드**

```bash
./gradlew :app:compileDebugKotlin
```

- [ ] **Step 3: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailHeader.kt
git commit -m "feat(ui): DetailHeader — 점보 우편번호 + 풀 주소 영역"
```

---

## Task 19: DetailSheet 조립

**Files:**
- Create: `app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailSheet.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.jewan.zipkr.ui.detail

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jewan.zipkr.BuildConfig
import com.jewan.zipkr.R
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.ui.components.CopyBar
import com.jewan.zipkr.ui.components.KakaoMapWebView
import com.jewan.zipkr.ui.components.MapDeepLinkButtons
import com.jewan.zipkr.ui.theme.ZipkrSpacing
import com.jewan.zipkr.util.copyToClipboard
import com.jewan.zipkr.util.lightHaptic

private val SHEET_RADIUS = 24.dp
private val MAP_HEIGHT = 220.dp

/**
 * 검색 결과 카드 탭 시 노출되는 상세 모달 시트이다.
 * skipPartiallyExpanded로 풀 높이까지 즉시 확장한다 (시·도 시트와 동일 패턴).
 *
 * 좌표는 시트가 처음 composition될 때 LaunchedEffect로 lazy fetch.
 * 카카오맵 WebView는 좌표 Success일 때만 그린다 — Loading/Failure는 placeholder 표시.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailSheet(
    address: Address,
    onDismiss: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coordinatePhase by viewModel.coordinate.collectAsState()
    val context = LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    val toastTpl = stringResource(R.string.copy_toast)

    LaunchedEffect(address.roadAddress) {
        viewModel.fetchCoordinate(address.roadAddress)
    }

    val onCopy: (String, String) -> Unit = { label, text ->
        context.copyToClipboard(label, text)
        view.lightHaptic()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, toastTpl.format(text), Toast.LENGTH_SHORT).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = SHEET_RADIUS, topEnd = SHEET_RADIUS),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = ZipkrSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
        ) {
            DetailHeader(address)
            MapArea(phase = coordinatePhase, onRetry = { viewModel.fetchCoordinate(address.roadAddress) })
            MapDeepLinkButtons(
                coord = (coordinatePhase as? CoordinatePhase.Success)?.coordinate,
                address = address.roadAddress,
                modifier = Modifier.padding(horizontal = ZipkrSpacing.md),
            )
            // CopyBar는 컴포넌트 내부 라벨 stringResource 사용 — 라벨 인자 통일을 위해 helper로
            CopyBar(
                onCopyZip = { onCopy(stringResource(R.string.copy_zip_label), address.zipCode) },
                onCopyRoad = { onCopy(stringResource(R.string.copy_road_label), address.roadAddress) },
                onCopyJibun = { onCopy(stringResource(R.string.copy_jibun_label), address.jibunAddress) },
                onCopyEnglish = { onCopy(stringResource(R.string.copy_english_label), address.englishAddress) },
            )
        }
    }
}

@Composable
private fun MapArea(
    phase: CoordinatePhase,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(MAP_HEIGHT)
            .padding(horizontal = ZipkrSpacing.md),
    ) {
        when (phase) {
            CoordinatePhase.Loading -> MapPlaceholder { CircularProgressIndicator() }
            is CoordinatePhase.Success ->
                KakaoMapWebView(
                    coord = phase.coordinate,
                    jsKey = BuildConfig.KAKAO_JS_KEY,
                    modifier = Modifier.fillMaxSize(),
                )
            is CoordinatePhase.Failure ->
                MapPlaceholder {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.map_load_failed), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = onRetry) { Text(stringResource(R.string.map_load_retry)) }
                    }
                }
        }
    }
}

@Composable
private fun MapPlaceholder(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
```

> CopyBar 호출의 `stringResource(...)` 인자는 Composable 내에서만 호출 가능하므로 Lambda 안에서는 사용 불가. **수정 필요**: 위 코드의 `stringResource(R.string.copy_zip_label)` 같은 호출을 lambda 밖에서 미리 변수로 받아와야 함. 다음 step에서 fix.

- [ ] **Step 2: stringResource를 Composable 본문으로 끌어올림**

`onCopy` 정의 위에 4개 라벨 변수를 미리 받기:

```kotlin
val zipLabel = stringResource(R.string.copy_zip_label)
val roadLabel = stringResource(R.string.copy_road_label)
val jibunLabel = stringResource(R.string.copy_jibun_label)
val englishLabel = stringResource(R.string.copy_english_label)
```

그리고 CopyBar 호출에서:
```kotlin
CopyBar(
    onCopyZip = { onCopy(zipLabel, address.zipCode) },
    onCopyRoad = { onCopy(roadLabel, address.roadAddress) },
    onCopyJibun = { onCopy(jibunLabel, address.jibunAddress) },
    onCopyEnglish = { onCopy(englishLabel, address.englishAddress) },
)
```

- [ ] **Step 3: 빌드 + ktlint/detekt pass**

```bash
./gradlew :app:assembleDebug :app:detekt :app:ktlintCheck
```

문제 발생 시 fix. (`LongMethod` 경고 시 helper 함수로 잘게 쪼개기)

- [ ] **Step 4: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/ui/detail/DetailSheet.kt
git commit -m "feat(ui): DetailSheet 조립 — header + map + deep links + CopyBar"
```

---

## Task 20: SearchScreen에 DetailSheet 통합

**Files:**
- Modify: `app/src/main/kotlin/com/jewan/zipkr/ui/search/SearchScreen.kt`
- Modify: `app/src/main/kotlin/com/jewan/zipkr/MainActivity.kt`

- [ ] **Step 1: SearchScreen.kt — onCardClick 시그니처 변경**

기존 `onCardClick: (zip: String) -> Unit` → `onCardClick: (Address) -> Unit`. 호출부도 같이 변경 (`AddressResultCard`로 전달하는 람다, `SearchCallbacks` data class 등).

- [ ] **Step 2: SearchScreen 안에 detailSheetAddress 상태 추가**

`Scaffold` 외부 `var sheetOpen by rememberSaveable { mutableStateOf(false) }` 옆에 추가:

```kotlin
var detailSheetAddress by rememberSaveable { mutableStateOf<Address?>(null) }
```

- [ ] **Step 3: SearchCallbacks의 onCardClick을 detailSheetAddress 세터로 연결**

`rememberSearchCallbacks`의 `onCardClick = onCardClick` → 이 외부 콜백을 `{ detailSheetAddress = it }`로 바꾸기. 또는 `MainActivity`에서 `onCardClick = { /* no-op */ }` 그대로 두고 SearchScreen 안에서 detailSheetAddress 직접 set.

가장 깔끔: `MainActivity`의 `onCardClick`은 미래 navigation 위해 남겨두지만, 이번엔 SearchScreen 안에서 직접 detailSheetAddress 처리. `MainActivity`에서 `onCardClick = { /* DetailSheet에서 처리 */ }`.

- [ ] **Step 4: SearchScreen 본문 끝에 DetailSheet 호출 추가**

기존 시·도 시트 호출 옆에:

```kotlin
detailSheetAddress?.let { addr ->
    DetailSheet(
        address = addr,
        onDismiss = { detailSheetAddress = null },
    )
}
```

- [ ] **Step 5: MainActivity의 onCardClick 콜백 시그니처 변경 (Address 받음)**

```kotlin
SearchScreen(
    onCardClick = { /* no-op — DetailSheet가 SearchScreen 내부에서 처리 */ },
)
```

또는 더 깔끔하게 onCardClick prop을 SearchScreen에서 제거 (필요 없어졌음).

- [ ] **Step 6: 빌드 + 기존 테스트 pass**

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Expected: SUCCESSFUL. SearchViewModelTest의 onCardClick 시그니처도 자동 호환 (콜백이라 type-erased).

- [ ] **Step 7: commit**

```bash
git add app/src/main/kotlin/com/jewan/zipkr/ui/search/SearchScreen.kt \
        app/src/main/kotlin/com/jewan/zipkr/MainActivity.kt
git commit -m "feat(ui): SearchScreen에 DetailSheet 통합 — 카드 탭 → 모달 시트"
```

---

## Task 21: Splash Screen API 통합

**Files:**
- Create: `app/src/main/res/values/themes.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/kotlin/com/jewan/zipkr/MainActivity.kt`

- [ ] **Step 1: themes.xml 작성**

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Theme.Zipkr" parent="android:Theme.Material.Light.NoActionBar">
        <!-- Compose가 모든 색·폰트를 그리므로 base는 NoActionBar만 충족 -->
    </style>
    <style name="Theme.Zipkr.Splash" parent="Theme.SplashScreen">
        <item name="windowSplashScreenBackground">@color/splash_background</item>
        <item name="windowSplashScreenAnimatedIcon">@mipmap/ic_launcher_foreground</item>
        <item name="postSplashScreenTheme">@style/Theme.Zipkr</item>
    </style>
</resources>
```

> `@color/splash_background`은 `colors.xml`에 추가해야 함. 다음 step에서 같이.

- [ ] **Step 2: colors.xml에 splash 색 추가**

`app/src/main/res/values/colors.xml` (없으면 생성):

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="splash_background">#0A0F1F</color>
</resources>
```

(다크 네이비 — zipkr 앱 배경 톤과 일관)

- [ ] **Step 3: AndroidManifest.xml에서 activity theme 변경**

```xml
<activity
    android:name=".MainActivity"
    android:theme="@style/Theme.Zipkr.Splash"
    android:exported="true">
```

- [ ] **Step 4: MainActivity에서 installSplashScreen() 호출**

```kotlin
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { /* ... */ }
    }
}
```

- [ ] **Step 5: 빌드 + 실기 install + 진입 애니메이션 확인**

```bash
./gradlew :app:installDebug
SDK_DIR=$(grep '^sdk.dir=' local.properties | cut -d'=' -f2)
"$SDK_DIR/platform-tools/adb" shell monkey -p com.jewan.zipkr.debug -c android.intent.category.LAUNCHER 1
```

Expected: 앱 실행 시 다크 배경 + 앱 아이콘 splash 잠깐 → 검색 화면

- [ ] **Step 6: commit**

```bash
git add app/src/main/res/values/themes.xml \
        app/src/main/res/values/colors.xml \
        app/src/main/AndroidManifest.xml \
        app/src/main/kotlin/com/jewan/zipkr/MainActivity.kt
git commit -m "feat(splash): Splash Screen API 통합 — 다크 배경 + 앱 아이콘"
```

---

## Task 22: 정적 분석 + 실기 검증

**Files:** 없음 (검증 단계)

- [ ] **Step 1: detekt + ktlint pass**

```bash
./gradlew :app:detekt :app:ktlintCheck
```

위반 발생 시 fix:
- LongMethod (50줄 초과) → helper 함수로 분리
- LongParameterList (4개 초과) → data class로 묶음
- MagicNumber → file-private const val 추출
- ktlint 자동 fix 가능: `./gradlew :app:ktlintFormat`

- [ ] **Step 2: 전체 unit test pass**

```bash
./gradlew :app:testDebugUnitTest
```

Expected: SUCCESSFUL

- [ ] **Step 3: 실기 install + 검증**

```bash
./gradlew :app:installDebug
SDK_DIR=$(grep '^sdk.dir=' local.properties | cut -d'=' -f2)
"$SDK_DIR/platform-tools/adb" shell monkey -p com.jewan.zipkr.debug -c android.intent.category.LAUNCHER 1
```

수동 시나리오:
1. ✅ 앱 실행 → splash 잠깐 → 검색 화면
2. ✅ "안중" 검색 → 결과 카드 표시 + 하이라이트 정상 동작
3. ✅ 카드 탭 → 상세 모달 시트가 슬라이드업
4. ✅ 시트 안에 점보 우편번호·도로명·지번·건물명·영문 표시
5. ✅ 카카오맵 WebView 로드 (1-2초 latency 후 지도·핀 표시) → 드래그·줌 동작
6. ✅ "카카오맵" 버튼 → 카카오맵 앱 또는 web fallback
7. ✅ "네이버지도" 버튼 → 네이버지도 앱 또는 web fallback
8. ✅ CopyBar 4 segment 모두 클립보드 복사 + 토스트
9. ✅ swipe down 또는 scrim tap으로 시트 닫힘
10. ✅ 다른 카드 탭 → 새 시트 (이전 시트 캐시된 좌표 영향 없음 확인)

문제 발생 시 spec §16 "미해결 항목" 점검 후 inline fix.

- [ ] **Step 4: 모든 변경 review (`git status`, `git diff`)**

문제 없으면 다음 task.

---

## Task 23: PR 생성

**Files:** 없음 (git operation만)

- [ ] **Step 1: push**

```bash
GH_TOKEN=$(gh auth token --user jewan100) git push -u origin feat/detail-sheet-v1
```

- [ ] **Step 2: PR 생성**

```bash
GH_TOKEN=$(gh auth token --user jewan100) gh pr create -R jewan-apps/zipkr \
  --base develop --head feat/detail-sheet-v1 \
  --title "[FEAT] zipkr 상세 모달 시트 + 카카오맵 WebView (Phase 4 v2)" \
  --body "$(cat <<'EOF'
## 무엇을

검색 결과 카드 탭 시 ModalBottomSheet로 상세 시트가 올라와 풀 주소·건물명·인터랙티브 카카오맵·외부 앱 deep link를 한 화면에 보여준다.

## 왜

리스트 카드(v1.1b·v1.1c·v1.1h)가 이미 self-sufficient(우편번호·한글·영문·지번 4 segment 복사)라 상세 화면이 단순한 정보 복제면 redundant. 따라서 새 가치 두 축으로 정의:
- **위치 시각화**: 인터랙티브 카카오맵 (드래그·줌)
- **외부 앱 연계**: 카카오맵·네이버지도 deep link로 깊은 탐색 위임

## 어떻게

### 데이터
- `Address` 모델에 buildingName/sido/sigungu/eupmyeondong 4 필드 추가 + @Parcelize
- `JusoAddressDto`에 같은 4 필드 추가 (행안부 raw 응답에 이미 있음 — 추가 호출 없음)
- 신규 `Coordinate` 도메인 모델 (WGS84)

### 외부 의존성
- **카카오 로컬 REST geocoding**: 도로명주소 → 좌표
- **카카오맵 web JavaScript SDK**: WebView로 임베드 (인터랙티브 지도)
- 키 두 개 관리: REST 키 + JavaScript 키 (`local.properties` → BuildConfig)

### UI
- **DetailSheet** ModalBottomSheet — skipPartiallyExpanded
- 시·도 필터 시트와 동일 패턴 (NavHost 사용 안 함)
- DetailHeader (점보 zip + 풀 주소) / KakaoMapWebView (220dp) / MapDeepLinkButtons / CopyBar 재사용
- CopyBar는 AddressResultCard에서 별도 파일로 분리해 재사용

### 기타
- Splash Screen API 통합 (다크 배경 + 앱 아이콘)
- in-memory 캐시 (ConcurrentHashMap, 같은 카드 재진입 시 좌표 재호출 X)
- 좌표 fetch 실패 시 placeholder + 재시도 + deep link는 주소 query fallback

## 테스트

- [x] KakaoModelsTest, CoordinateRepositoryImplTest, KakaoMapHtmlBuilderTest, MapDeepLinksTest, DetailViewModelTest 신규 작성
- [x] JusoModelsTest 갱신 (신규 4 필드 매핑 검증)
- [x] SearchViewModelTest 갱신 (Address 생성자 변경 반영)
- [x] detekt + ktlint pass
- [x] 실기 검증 (Galaxy S25 Ultra) — 검색 → 카드 탭 → 시트 → 지도 인터랙티브 → deep link → 복사 → 닫기 시나리오 모두 동작

## 다음

머지 후 Phase 5 (i18n) 또는 Phase 6 (AdMob) 형 결정.
EOF
)"
```

- [ ] **Step 2: PR URL 형에게 알리고 머지 컨펌 받기**

머지 후 worktree 정리:
```bash
cd /Users/jewan/Desktop/git/98_jewan/zipkr
git checkout develop
git pull origin develop
git worktree remove .worktrees/detail-sheet-v1
git branch -D feat/detail-sheet-v1
```

---

## Self-review 메모

- ✅ Spec §1~18 모든 섹션이 task로 매핑됨 (Splash §12 → Task 21, 키 §7.3 → Task 1, 모달 §9 → Task 20 등)
- ✅ "TBD" 같은 placeholder 없음 — 모든 step에 실제 코드/명령어 포함
- ✅ 타입 일관성: `Coordinate(longitude, latitude)`가 모든 task에서 동일 시그니처
- ✅ TDD 흐름: failing test → 구현 → pass → commit
- ⚠ Task 19 Step 1의 `dp(16f)` 표현 잘못 — Step 1 안에 fix 명시 (`16.dp` 사용)
- ⚠ Task 20에서 SearchViewModelTest 갱신 자동화 보장 안 됨 — implementation 시 검증 필요
