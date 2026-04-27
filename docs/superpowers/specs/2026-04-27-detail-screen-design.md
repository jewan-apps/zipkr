# zipkr 상세 화면 설계 (Phase 4 v1)

**작성일:** 2026-04-27
**브랜치(예정):** `feat/detail-screen-v1`
**관련 task:** #14 Phase 4 — 상세 화면 + 네비 + 스플래시

---

## 1. 목표

검색 결과 카드 탭 시 진입하는 상세 화면을 만들고, NavHost·Splash Screen API를 같이 통합해 출시 준비 상태를 끌어올린다. 상세 화면의 가치는 "**리스트 카드가 못 주는 시각·행동 정보**"로 정의한다.

## 2. 맥락

리스트 카드(`AddressResultCard`)는 v1.1b·v1.1c·v1.1h를 거쳐 이미 self-sufficient하다. 도로명·지번·영문·우편번호 4 segment 복사가 카드 위에서 끝난다. 따라서 상세 화면이 단지 "더 큰 화면에 같은 정보 + 같은 복사 버튼"이면 redundant 화면이 되고, **새 가치를 명확히 정의해야 한다**.

선택한 새 가치 두 축:
- **위치 시각화**: "이 주소가 어디지?" 1초 인지를 위한 정적 지도 미리보기
- **외부 앱 연계**: 카카오맵·네이버지도 deep link로 깊은 탐색을 자연스럽게 위임

## 3. 사용자 use case

| # | 시나리오 | 충족 영역 |
|---|---|---|
| U1 | "이 주소가 진짜 어디지" 위치 인지 | 정적 지도 미리보기 |
| U2 | "더 자세히·길찾기" 외부 앱 진입 | 카카오맵·네이버지도 deep link |
| U3 | 카드에서 못 본 추가 정보(건물명·시·구·동) 확인 | 확장된 Address 모델 표시 |
| U4 | 큰 화면에서 우편번호·풀 주소 한눈에 | 점보 우편번호 + 풀 주소 영역 |
| U5 | 상세에서 항목별 복사 (카드와 동일 동선) | CopyBar 재사용 |

## 4. 비목표 (out of scope)

- **인터랙티브 지도 SDK 통합** (카카오맵/네이버지도 SDK). v2(출시 후 사용자 피드백 기반)에 검토.
- **거리뷰·로드뷰** 임베드. v2 이후 옵션.
- **즐겨찾기·메모·히스토리** 등 personalization. 별도 PR(v1.2 등) 후보.
- **영문 건물명 표시**. 행안부 영문 API는 별도 키 승인 필요. 출시 시점 운영 키 신청 시 같이 검토.
- **주변 시설(편의점·우체국·지하철 등) 노출**. 외부 API 추가 부담 큼.
- **사용자 위치 권한 기반 거리 계산**. 권한 동의 흐름·정확도 부담.

## 5. 아키텍처 개요

```
SearchScreen (cardClick)
    │
    ▼ Navigation Compose (Address Parcelable 통째 전달)
DetailScreen (DetailViewModel + Hilt)
    │
    ├─ Address (이미 받은 데이터) → 즉시 표시
    │
    └─ lazy: KakaoLocalApi.geocode(roadAddress)
            ├─ 좌표 (Coordinate) → KakaoStaticMapUrlBuilder → MapPreview (Coil)
            └─ 좌표 → MapDeepLinkButtons (카카오맵·네이버지도)
```

좌표는 검색 시점이 아닌 **상세 진입 시 lazy fetch**한다. 검색 결과 N개 모두 좌표 호출하면 부담이 크고, 사용자는 한 세션 1-2개 카드만 탭한다는 가정.

## 6. 데이터 모델 변경

### 6.1 `Address` 확장 (+`@Parcelize`)

```kotlin
@Parcelize
data class Address(
    val zipCode: String,          // 기존
    val roadAddress: String,      // 기존
    val jibunAddress: String,     // 기존
    val englishAddress: String,   // 기존
    val buildingName: String,     // 신규 — bdNm (예: "안중읍행정복지센터", 없으면 빈 문자열)
    val sido: String,             // 신규 — siNm (예: "경기도")
    val sigungu: String,          // 신규 — sggNm (예: "평택시")
    val eupmyeondong: String,     // 신규 — emdNm (예: "안중읍")
) : Parcelable
```

행안부 raw 응답에 이미 다 있어 카카오 추가 호출 없이 받는다. 빈 문자열 허용(예: 건물명 없는 주소).

### 6.2 신규 도메인 모델 `Coordinate`

```kotlin
@Parcelize
data class Coordinate(
    val longitude: Double,  // 카카오 응답 x (WGS84)
    val latitude: Double,   // 카카오 응답 y (WGS84)
) : Parcelable
```

### 6.3 행안부 DTO 매핑 (`JusoAddressDto.toDomain`)

```kotlin
fun toDomain(): Address = Address(
    zipCode = zipNo,
    roadAddress = roadAddr,
    jibunAddress = jibunAddr,
    englishAddress = engAddr,
    buildingName = bdNm,
    sido = siNm,
    sigungu = sggNm,
    eupmyeondong = emdNm,
)
```

`JusoAddressDto`에 `bdNm/siNm/sggNm/emdNm` 4 필드 추가 (`@SerialName` 매핑).

## 7. 외부 의존성 — 카카오 로컬 API

### 7.1 endpoint

- **좌표 변환**: `GET https://dapi.kakao.com/v2/local/search/address.json?query={roadAddress}`
- **정적 지도**: `GET https://dapi.kakao.com/v2/maps/staticmap?...` (이미지, Coil이 직접 로드)
- 인증: HTTP header `Authorization: KakaoAK {REST_API_KEY}`

### 7.2 키 관리

- `local.properties` → `kakao.rest.api.key=...`
- `app/build.gradle.kts`에서 `Properties` 객체로 읽고 `buildConfigField("String", "KAKAO_REST_API_KEY", "...")`
- `NetworkModule`에서 `BuildConfig.KAKAO_REST_API_KEY` provide (`@Named(DiQualifiers.KAKAO_REST_API_KEY)`)
- 행안부 키와 동일 패턴 (헌법 §9 시크릿 관리)

### 7.3 응답 모델 (`KakaoModels.kt`)

```kotlin
@Serializable data class KakaoGeocodeResponse(val documents: List<KakaoDocument> = emptyList())
@Serializable data class KakaoDocument(val x: String, val y: String) {
    fun toCoordinate(): Coordinate = Coordinate(longitude = x.toDouble(), latitude = y.toDouble())
}
```

`x/y`가 String으로 오므로 toDouble 변환은 도메인 매핑 단에서.

### 7.4 Repository — `CoordinateRepository`

```kotlin
interface CoordinateRepository {
    suspend fun fetchCoordinate(roadAddress: String): Result<Coordinate>
}
```

- 단일 책임 (좌표만)
- in-memory 캐시: `MutableMap<String, Coordinate>` (key = roadAddress). 같은 카드 다시 진입 시 재호출 안 함.
- 캐시 영속화는 안 함 (앱 재시작 시 무효, MVP 단순함 우선)
- 네트워크 실패는 `Result.Failure(AppError)`로 wrap (기존 `AppError` enum 재사용)

### 7.5 정적 지도 URL (`KakaoStaticMapUrlBuilder`)

```kotlin
fun buildUrl(coord: Coordinate, width: Int, height: Int, level: Int = 3, marker: Boolean = true): String
```

- `level` 줌 단계 (3 = 동네 단위, 카카오 기준 기본값)
- `marker = true`이면 좌표에 핀 표시 (`markers=size:mid|...`)
- 인증: HTTP header `Authorization: KakaoAK {REST_KEY}` 필요 → Coil의 `ImageLoader`에 OkHttp 인터셉터로 카카오 도메인 요청에만 header 주입
- 카카오 dev console: "Android 플랫폼" 등록 + 패키지명 (`com.jewan.zipkr`, `com.jewan.zipkr.debug`) 등록 필수. REST 키는 그대로 사용 가능 (별도 key hash 인증은 SDK 사용 시에만 요구되며, REST API에는 불필요).

## 8. UI 구성

### 8.1 화면 레이아웃

```
┌────────────────────────────────────┐
│ ← 상세                              │  TopAppBar (back)
├────────────────────────────────────┤
│  17933                              │  점보 우편번호 (28sp, 브랜드, monospace)
│                                     │
│  경기도 평택시 안중읍 안현로 400      │  도로명 (titleLarge, SemiBold)
│  안중읍 안중리 445-16                │  지번 (bodyMedium, onSurfaceVariant)
│  안중읍행정복지센터                   │  건물명 (bodyMedium, primary, 빈 값이면 숨김)
│  400 Anhyeon-ro, Anjung-eup, ...    │  영문 (bodySmall, italic)
├────────────────────────────────────┤
│ ┌────────────────────────────────┐ │
│ │                                │ │
│ │       [ 정적 지도 이미지 ]       │ │  MapPreview (200dp, 16dp radius)
│ │           📍                    │ │
│ └────────────────────────────────┘ │
│                                     │  좌표 fetch 중: 회색 placeholder + spinner
│                                     │  실패: 회색 placeholder + "지도를 불러올 수 없어요"
├────────────────────────────────────┤
│ [ 카카오맵에서 보기 ]                │  MapDeepLinkButtons
│ [ 네이버지도에서 보기 ]              │  (좌표 fetch 실패 시 비활성)
├────────────────────────────────────┤
│  영문주소 │ 지번주소 │ 도로명 │ 우편 │  CopyBar 재사용 (v1.1b 컴포넌트)
└────────────────────────────────────┘
```

### 8.2 컴포넌트 분리

- `DetailScreen.kt` — 화면 컴포저블 (top bar + Column 조립)
- `DetailHeader.kt` — 점보 우편번호 + 풀 주소 영역
- `MapPreview.kt` — Coil 이미지 + placeholder + error state 한 컴포저블
- `MapDeepLinkButtons.kt` — 외부 앱 deep link 2 버튼
- `util/MapDeepLinks.kt` — URL 빌더 + Intent 처리 (카카오맵·네이버지도 + web fallback)

`AddressResultCard`의 CopyBar 부분은 별도 함수(`CopyBar`)로 추출되어 있으므로 그대로 재사용 가능.

### 8.3 색·폰트

- 기존 `ZipkrTheme` 토큰 재사용
- 점보 우편번호: 카드와 동일 (28sp, monospace, primary)
- 지도 영역 코너: 16dp radius
- 외부 앱 버튼: `FilledTonalButton` (브랜드 tint, 카드의 RoadSegment 톤과 일관)

## 9. 라우팅 (Navigation Compose)

### 9.1 새 의존성

`gradle/libs.versions.toml`에 `androidx-navigation-compose` 추가.

### 9.2 Route 정의

```kotlin
sealed interface ZipkrRoute {
    @Serializable data object Search : ZipkrRoute
    @Serializable data class Detail(val address: Address) : ZipkrRoute
}
```

Navigation Compose 2.8+ type-safe routing 사용. `Address`가 `@Parcelize`+`@Serializable`이면 `navArgument` 자동 처리.

`@Serializable`이 어려운 경우 fallback: `navType = NavType.ParcelableType(Address::class.java)`로 수동 처리.

### 9.3 NavHost 위치

- **`ui/Navigation.kt` 단일 파일** (Search·Detail 2 화면뿐). 5 화면 넘으면 그때 디렉토리 분리.
- `MainActivity`의 `setContent`에서 `NavHost(navController, startDestination = ZipkrRoute.Search)` 호출.

### 9.4 SearchScreen 변경

- `onCardClick: (zipCode) -> Unit` → `onCardClick: (Address) -> Unit`로 시그니처 변경
- 호출부에서 `navController.navigate(ZipkrRoute.Detail(address))`

### 9.5 DetailViewModel — savedStateHandle

```kotlin
@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val coordinateRepository: CoordinateRepository,
) : ViewModel() {
    private val address: Address = savedStateHandle.toRoute<ZipkrRoute.Detail>().address
    // ...
}
```

`SavedStateHandle.toRoute<T>()`로 type-safe하게 Address 복원.

## 10. 에러 처리

| 시나리오 | 처리 |
|---|---|
| 좌표 fetch 네트워크 실패 | 지도 영역 회색 placeholder + "지도를 불러올 수 없어요" + 재시도 버튼. 외부 앱 deep link 버튼은 **주소 텍스트 기반으로 fallback 동작** (좌표 없이도 카카오맵에 검색 query로 진입 가능: `kakaomap://search?q=...`) |
| 좌표 응답에 documents 빈 배열 | 동일하게 placeholder + 메시지 (지도 못 보여줌). deep link는 주소 query로 fallback. |
| 카카오 API 키 누락 (BuildConfig 빈 문자열) | 빌드 시점에 `local.properties` 누락 경고 (build.gradle.kts에서 빈 키면 println 경고). 런타임에는 placeholder. |
| 외부 앱 미설치 | Intent resolveActivity == null이면 web fallback URL을 ACTION_VIEW로 (`https://map.kakao.com/...`, `https://map.naver.com/...`) |

## 11. 캐시 정책

- `CoordinateRepositoryImpl` 내 `MutableMap<String, Coordinate>` (key = roadAddress)
- thread-safety: ViewModel에서만 호출되고 `Mutex` 또는 `ConcurrentHashMap` 중 단순함 우선해 `ConcurrentHashMap` 사용
- 영속화 X (앱 재시작 시 초기화). MVP는 메모리 캐시면 충분.
- 캐시 만료 X (좌표는 거의 변하지 않음).

## 12. Splash Screen

### 12.1 라이브러리

`gradle/libs.versions.toml`에 `androidx.core:core-splashscreen` 추가.

### 12.2 theme

`res/values/themes.xml`에 `Theme.Zipkr.Splash` 정의 — `windowSplashScreenBackground` + `windowSplashScreenAnimatedIcon`(앱 아이콘 자동) + `postSplashScreenTheme`(메인 테마 지정).

### 12.3 MainActivity

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()  // super.onCreate 전에 호출
    super.onCreate(savedInstanceState)
    // ...
}
```

### 12.4 manifest

`<activity android:theme="@style/Theme.Zipkr.Splash">`로 변경.

작업량: ~5분. 출시 표준 UX라 이번 PR에 같이 포함.

## 13. 테스트 전략

| 대상 | 테스트 종류 | 커버리지 |
|---|---|---|
| `KakaoLocalApiProvider` | API mock 응답 → Coordinate 변환 | 정상 / 빈 documents / x/y NaN |
| `CoordinateRepositoryImpl` | 캐시 동작 | 첫 호출 fetch, 재호출 캐시 hit |
| `KakaoStaticMapUrlBuilder` | URL 조립 | 좌표 + 마커 + 줌 파라미터 |
| `MapDeepLinks` | URL/Intent 빌더 | 카카오맵·네이버지도·web fallback |
| `DetailViewModel` | savedStateHandle Address 복원 + lazy fetch | 정상 / 실패 / 캐시 hit |
| 통합 테스트 | 진입 → 지도 표시 → deep link 클릭 | 수동 (실기) |

기존 `SearchViewModelTest`는 `onCardClick` 시그니처 변경 (`zipCode` → `Address`)에 맞춰 갱신.

## 14. 단계 구분 (v1 / v2)

| 단계 | 범위 | 시점 |
|---|---|---|
| **v1 (이번 PR)** | 정적 지도 + deep link + Address 확장 + Splash + NavHost | 지금 |
| **v2 (출시 후 검토)** | 카카오맵 SDK 인터랙티브 임베드 | 사용자 피드백 데이터 기반 결정 |

## 15. 릴리즈 영향

- 새 의존성: `androidx.navigation:navigation-compose`, `androidx.core:core-splashscreen`, `coil-compose` (이미 있을 수 있음 — 확인)
- 새 권한: **없음** (인터넷만, 이미 선언)
- 새 키: 카카오 REST API 키 1개 (운영 키 출시 시 별도 발급 권장 — 형 작업)
- 앱 크기 증가: ~수백 KB (Navigation Compose + Coil)

## 16. 미해결 항목 (구현 단계 결정)

- 카카오 정적 지도 줌 level 기본값 (3 vs 4) — 실기 보고 조정
- DetailHeader 우편번호 점보 폰트 size — 카드 28sp 기준에서 더 키울지 (32sp?)
- 외부 앱 버튼 라벨 — "카카오맵에서 보기" vs "카카오맵 열기" 미세 wording
- 좌표 fetch 실패 시 재시도 버튼 vs 자동 재시도 — UX 결정
- DetailScreen 진입 transition (slide vs fade vs default)

위 항목은 implementation plan 단계 또는 구현 중 결정.

## 17. 헌법 준수

- 함수 ≤ 50 라인 (DetailScreen, DetailHeader, MapPreview 등 적절히 분리)
- 인자 ≤ 4 (callbacks data class 활용)
- 한국어 `~다` 종결 주석
- 매직 넘버 → file-private const
- 모든 새 외부 호출은 `Result<T, AppError>` wrap
- `local.properties` → `BuildConfig` 키 관리 (§9)
- 실기기 install 검증 강제 (§13.1.5)
- detekt + ktlint 통과 후 PR

## 18. 참고

- 행안부 raw 응답 검증: `business.juso.go.kr/addrlink/addrLinkApi.do` (확인 완료, 좌표 없음)
- 카카오 geocoding 검증: `dapi.kakao.com/v2/local/search/address.json` (확인 완료, 좌표·건물명 모두 있음)
- 카카오 Static Map: `dapi.kakao.com/v2/maps/staticmap`
- Navigation Compose 2.8+ type-safe routing: https://developer.android.com/jetpack/compose/navigation
- Splash Screen API: https://developer.android.com/develop/ui/views/launch/splash-screen
