# 우편번호 앱 (Postal-K) MVP — 설계 문서

**작성일**: 2026-04-25
**상태**: 설계 완료 (구현 계획 작성 대기)
**GitHub**: https://github.com/inkcat-apps/zipkr (Public)
**준수 표준**: 내부 개발 헌법(JEWAN_DEV_CONSTITUTION)
**관련 ADR**: [`docs/decisions/0001-android-library-stack.md`](../../decisions/0001-android-library-stack.md) (라이브러리 일괄 승인)

---

## 1. Context — 이 앱이 왜 만들어지는가

### 큰 그림
광고 수익형 안드로이드 앱 시리즈의 **첫 번째 앱**.
"한 개를 잘 만든다" + "**다음 앱에 무엇이 자산으로 재사용될 수 있는가**" 두 축이 모든 결정의 기준.

### 첫 앱으로 우편번호를 고른 이유
- **단일·명확한 기능**: 주소 → 우편번호 조회. MVP를 빠르게 띄우기 적합.
- **공공 데이터 풍부**: 행정안전부 도로명주소 API가 무료·표준·풍부 (`engAddr` 영문주소 포함).
- **백엔드 강점 활용**: API 통합·데이터 변환이 형(jewan)의 강점 영역과 일치.
- **시장 차별화 명확**: 기존 한국 우편번호 앱들의 디자인이 낙후 → "**압도적 디자인 + 즉시성**"으로 차별화.
- **블루오션 보너스**: 영문 주소·다국어 UI를 처음부터 지원해 **외국인·해외 송배** 시장도 포섭.

### 의도된 결과
- **출시**: D+10~17일 안에 Google Play Production 출시.
- **자산화**: 첫 앱 출시 직후 `core-ui`·`core-ads`·`core-network` 등 **공통 모듈**의 시드(seed) 추출.
- **수익 베이스라인**: 출시 후 1개월 내 DAU 100 / 월 수익 ~$30 진입 (KPI 목표).

---

## 2. 앱 정체성

| 항목 | 값 |
|---|---|
| 가칭 | 우편번호 (Postal-K) — 정식명 출시 직전 확정 |
| 한 줄 소개 | "예쁘고 빠른 한국 우편번호 검색 — 단 3초만에" |
| 패키지 | `com.jewan.zipkr` |
| 카테고리 | Tools |
| 타겟 사용자 | 한국 일반 사용자 + 한국에 사는·방문하는 외국인 |
| 타겟 시장 | 대한민국 (UI는 KO/EN 동시 지원) |
| 출시 OS | Android (minSdk 24, targetSdk 34) |
| iOS | v3+ 별도 트랙 |

---

## 3. MVP 범위 (v1.0)

### 들어가는 것
1. **주소 검색** — 도로명·지번·건물명 모두 지원 (행안부 API가 자동 처리)
2. **검색 결과 리스트** — 도로명주소·지번주소·우편번호·**영문주소(`engAddr`)**
3. **결과 카드 즉시 복사 액션** — Detail 진입 없이 카드에서 1탭으로 우편번호 클립보드 복사
4. **Detail 화면** — 우편번호 점보 표시 + 복사·공유 버튼
5. **상태 UI** — Idle / Loading(스켈레톤) / Success / Empty / Error
6. **AdMob 하단 고정 배너** (모든 화면)
7. **i18n 처음부터 셋업** — `strings.xml` 분리, KO/EN, 시스템 언어 자동 추종
8. **Play Console 메타 한·영 동시 등록** — 제목·설명·스크린샷 모두 두 언어
9. **즉시성 강화 패키지**:
   - 앱 진입 시 입력창 자동 포커스 + 키보드 자동 표시
   - Debounce 400ms 자동 검색 (엔터 X)
   - 복사 시 토스트 + 햅틱 피드백

### v2 이후로 미루는 것 (의도적 YAGNI)
- 검색 히스토리 / 즐겨찾기
- 홈스크린 위젯
- 카카오 지도 연동·길찾기
- 다크모드 커스텀 테마 (시스템 자동 추종은 무료)
- 클립보드 모니터 (다른 앱에서 복사된 주소 자동 제안) → v1.1
- App Shortcuts (홈 아이콘 롱프레스) → v1.2
- 전면 광고 (정책 학습 후 v1.1)

---

## 4. 아키텍처 — MVVM + Repository + 다중 Provider + Hilt

```
┌─────────────────────────────────────────────────┐
│  UI (Compose)                                    │
│  └─ SearchScreen ──┐                             │
│       └─ ResultDetailScreen                      │
│                    │                             │
│                    ▼                             │
│  ViewModel (StateFlow)                           │
│  └─ SearchViewModel (uiState, onSearch, onCopy)  │
│                    │                             │
│                    ▼                             │
│  Repository (인터페이스)                          │
│  └─ AddressRepository                            │
│                    │                             │
│                    ▼                             │
│  Provider (구현체 — 미래에 늘어남)                 │
│  └─ JusoApiProvider (행안부)  ← v1                │
│  └─ KakaoApiProvider          ← v2 (확장점)       │
└─────────────────────────────────────────────────┘
        │
        ▼
   AdMob (별도 모듈처럼 분리)
```

### 패키지 구조 (single-module, 재사용 가능한 경계)

```
com.jewan.zipkr/
├─ ui/                     ← Compose 화면들
│   ├─ search/
│   │   ├─ SearchScreen.kt
│   │   └─ SearchViewModel.kt
│   ├─ detail/
│   │   ├─ DetailScreen.kt
│   │   └─ DetailViewModel.kt
│   ├─ theme/              ⭐ 공통 자산
│   └─ components/         ⭐ 공통 자산 (EmptyState, ErrorView, LoadingSkeleton, CopyableCard)
├─ data/
│   ├─ AddressRepository.kt              ⭐ 공통 자산 (인터페이스)
│   ├─ AddressRepositoryImpl.kt
│   └─ provider/
│       ├─ AddressProvider.kt            ← Provider 인터페이스
│       └─ juso/
│           ├─ JusoApiProvider.kt
│           ├─ JusoApi.kt (Retrofit)
│           └─ JusoModels.kt
├─ ads/                    ⭐ 공통 자산 (AdBanner Compose 컴포저블)
├─ util/                   ⭐ 공통 자산 (Clipboard, Share, Haptic, Toast)
├─ di/                     ⭐ 공통 자산 (Hilt 모듈)
│   ├─ NetworkModule.kt
│   ├─ DataModule.kt
│   └─ AppModule.kt
├─ PostalApp.kt            ← @HiltAndroidApp
└─ MainActivity.kt         ← @AndroidEntryPoint
```

⭐ 표시 = 두 번째 앱부터 `core-ui`, `core-ads`, `core-network`, `core-util`, `core-i18n` 등의 모듈로 추출 후보.

### 라이브러리 선택 (모두 공통 표준)

**언어/UI**
- Kotlin + Jetpack Compose, Material3
- Navigation Compose
- **Coil** (Compose 이미지 로딩 표준)
- **AndroidX Splash Screen API** (Android 12+ 표준 스플래시)
- **Accompanist** (권한·SystemUiController 등 Compose 보조)

**데이터/네트워크**
- Retrofit + OkHttp + kotlinx.serialization
- ViewModel + StateFlow

**DI**
- **Hilt** (첫 앱부터 도입 — 형의 "관심사 분리" 가치 + Spring DI 사고방식 매핑)
- `hilt-navigation-compose`로 Compose에서 ViewModel 주입

**빌드/품질**
- Gradle Kotlin DSL + **Version Catalog** (`libs.versions.toml`)
- **KSP** (Kotlin Symbol Processing — KAPT 대체, 빌드 속도 ~2배)
- **Detekt** (정적 분석)
- **Ktlint** (코드 포맷터)

**광고**
- Google Mobile Ads SDK (AdMob)

**운영 (장기 운영의 척추)**
- **Timber** (로깅 표준화, 환경별 자동 분기)
- **LeakCanary** (debug only — 메모리 누수 자동 탐지)
- **Firebase Crashlytics** ⭐ (크래시 자동 수집·알림 — 여러 앱을 한 콘솔에서 관제)

**테스트**
- JUnit + **MockK** (Kotlin 친화 mock)
- **Turbine** (Flow/StateFlow 테스트 표준)
- **Truth** (Google — 가독성 좋은 어설션)

**폰트**
- Pretendard Variable (한·영 모두 우아, 무료 상업)

---

## 5. 화면 구성 & UX

### SearchScreen (메인)

```
┌─────────────────────────────┐
│  우편번호                    │  ← 상단 바 (앱 이름)
├─────────────────────────────┤
│ ┌─────────────────────────┐ │
│ │ 🔍 주소를 입력하세요     │ │  ← 진입 시 자동 포커스 + 키보드 자동 표시
│ └─────────────────────────┘ │
│                             │
│   ┌─ 결과 카드 ──── 📋 ─┐    │  ← 우측 즉시 복사 버튼
│   │ 📍 도로명           │    │
│   │   서울 강남구 테헤란로 │    │
│   │ 📮 06234            │    │
│   │   English address.. │    │
│   └─────────────────────┘    │
│                             │
├─────────────────────────────┤
│  [ AdMob 배너 — 320×50 ]    │  ← 하단 고정
└─────────────────────────────┘
```

### DetailScreen (상세)
```
┌─────────────────────────────┐
│  ← 뒤로                      │
├─────────────────────────────┤
│  📮 06234                   │  ← 우편번호 점보 (가장 큰 글씨)
│                             │
│  도로명 / 지번 / English      │  ← 세 줄 표시
│                             │
│  [ 📋 복사 ]  [ 📤 공유 ]    │
├─────────────────────────────┤
│  [ AdMob 배너 ]              │
└─────────────────────────────┘
```

### 즉시성 동선 (UX 핵심)
```
앱 켜기 → 키보드 자동↑ → 주소 입력 → 자동 검색 (debounce 400ms)
                                  → 결과 카드 → [📋 1탭] → 토스트+진동 → 끝
                                                       (3초, 2탭)
```
**KPI**: 최초 결과 복사까지 **평균 3초 이내, 2탭 이하**.

### 디자인 시스템 (압도 차별화)
- **컬러**: Material3 Dynamic Color + 자체 액센트 1색
- **타이포**: Pretendard Variable
- **간격·라운딩**: 4dp 그리드, 카드 라운딩 16dp
- **다크모드**: 시스템 자동 추종
- **모션**: 절제된 easing, 200~300ms

---

## 6. API 통합

### 행안부 도로명주소 API
- Endpoint: `https://business.juso.go.kr/addrlink/addrLinkApi.do`
- 인증: `confmKey` (발급 후 `local.properties`에 저장, `BuildConfig.JUSO_API_KEY`로 노출)
- 응답: JSON, `roadAddr` / `jibunAddr` / `engAddr` / `zipNo` 포함
- 페이징: `currentPage`, `countPerPage` (MVP는 첫 10개만)
- 영문 주소: 같은 응답에 `engAddr` 포함 — 별도 호출 불필요

### Repository / Provider 추상화 (공통 자산)

```kotlin
interface AddressProvider {
    suspend fun search(query: String): Result<List<Address>>
}

class JusoApiProvider @Inject constructor(...) : AddressProvider { ... }

interface AddressRepository {
    suspend fun search(query: String): Result<List<Address>>
}
class AddressRepositoryImpl @Inject constructor(
    private val provider: AddressProvider
) : AddressRepository { ... }
```
- v2에서 `KakaoApiProvider` 추가 시 `DataModule.kt`만 수정.
- 다른 앱에서는 동일한 패턴으로 `WeatherRepository`·`QrRepository` 등을 정의 — 패턴 자체가 자산.

### 에러 처리
- 네트워크 오류 → 재시도 가능한 메시지
- API 결과 코드 비정상 → 사용자 친화 메시지 매핑
- 빈 결과 → Empty 상태 UI

### 키 관리 표준 (공통 자산)
- `local.properties`에 `JUSO_API_KEY=...` 저장 → `.gitignore` 처리
- `build.gradle.kts`에서 `BuildConfig.JUSO_API_KEY`로 노출
- 모든 후속 프로젝트가 동일 패턴

---

## 7. 광고 통합 (AdMob)

- **MVP**: 하단 적응형 배너 1개 (모든 화면 공통)
- 개발 중 — 테스트 광고 ID(debug 빌드), 출시 직전 — 실 광고 ID(release 빌드)로 buildTypes 분기 (단순 앱이라 flavor 미사용)
- `ads/AdBanner.kt` Compose 컴포저블로 래핑 → 다음 앱 그대로 재사용 (공통 자산)
- 전면 광고는 **v1.1**로 분리 (정책 위반 리스크 사전 방지)

---

## 8. 다국어 (i18n) — 재사용 자산화 핵심

- 기본 언어: `ko`
- 추가 언어: `en` (외국인 타겟)
- 모든 사용자 가시 문자열은 `res/values/strings.xml` (KO) + `res/values-en/strings.xml` (EN)
- **하드코딩 금지** — 프로젝트 표준
- Play Console 메타도 한·영 동시 등록 (제목·설명·스크린샷)
- 시스템 언어 자동 추종 (`compileSdk` 기본 동작)

---

## 9. 출시 체크리스트

### 코드/빌드
- [ ] `buildTypes`: `debug`(테스트 광고 ID) / `release`(실 광고 ID via local.properties 주입)
- [ ] `local.properties`에 `JUSO_API_KEY` 저장 + `.gitignore` 확인
- [ ] 키스토어 생성 + **안전한 백업 위치 표준화** (분실 = 앱 영구 업데이트 불가)
- [ ] `proguard-rules.pro` — Retrofit·Hilt·kotlinx-serialization 룰
- [ ] minSdk 24 / targetSdk 34
- [ ] AAB(Android App Bundle) 빌드 확인

### 외부 등록
- [ ] **Google Play Console 개발자 등록** (₩33,000, 본인 인증 며칠)
- [ ] **행안부 API 인증키** 발급 (회원가입 → 신청 → 승인 하루 내)
- [ ] AdMob 앱 등록 + 배너 광고 단위 발급
- [ ] `app-ads.txt` 갱신 (`jewan100.github.io/app-ads.txt`)
- [ ] **Firebase 프로젝트 생성** + `google-services.json` 추가 + Crashlytics 활성화

### 정책/메타
- [ ] **개인정보 처리방침** → `jewan100.github.io/privacy/postal/` 호스팅
- [ ] Play Console 메타 (한·영 동시): 제목·짧은 설명·자세한 설명
- [ ] 스크린샷 (한·영 각 4~8장, 1080×1920)
- [ ] 앱 아이콘 512×512 + 피처 그래픽 1024×500

### 출시 단계
- [ ] **내부 테스트** → 형 본인 + 1~2명 지인
- [ ] **Closed Testing** → 5~10명 (옵션)
- [ ] **Production** 출시 (심사 1~3일)

---

## 10. 테스트 전략

| 종류 | 범위 | MVP 포함 |
|---|---|---|
| 단위 테스트 | Repository, Provider, 에러 매핑 | ✅ |
| UI 테스트 | SearchScreen 핵심 동선 1개 (검색→결과→복사) | ✅ |
| 수동 시나리오 | 한글·영문·지번·도로명·빈 결과·네트워크 단절 | ✅ |
| 광고 표시 확인 | 테스트 광고 노출 검증 | ✅ |

---

## 11. 검증 (출시 후 KPI)

- **즉시성**: 최초 결과 복사까지 평균 3초 이내, 2탭 이하 (자체 로깅)
- **수익** (출시 후 1개월): DAU 100 / MAU 500 / 월 수익 ~$30 시드
- **품질**: ANR/Crash rate < 0.5% (Play Console Vitals)
- **리뷰**: ★4.0 이상 유지

---

## 12. 위험 요소 + 완화

| 위험 | 완화 |
|---|---|
| 행안부 API 인증키 발급 지연 | D-7에 미리 신청 |
| Play Console 본인 인증 지연 | 코딩 시작 시점에 동시 진행 |
| AdMob 정책 위반 (광고 위치) | 첫 출시 = 하단 배너만, 전면은 v1.1 |
| 디자인이 기대만큼 안 나옴 | Pretendard + Material3 표준으로 베이스라인 보장 |
| 개인정보 처리방침 누락 | Play Console 거절 사유 1위 — 출시 전 호스팅 확인 필수 |
| 키스토어 분실 | 첫 앱 키스토어 백업 위치 즉시 표준화 |

---

## 13. 자산 추출 계획 (두 번째 앱으로)

첫 앱 출시 직후 **추출 회고**를 거쳐 분리:

1. `core-ui`: `theme/` + 공통 컴포넌트 (EmptyState, ErrorView, LoadingSkeleton, CopyableCard)
2. `core-ads`: `AdBanner` 컴포저블 + 광고 ID 주입 패턴
3. `core-network`: Retrofit·OkHttp 셋업 + BuildConfig 키 주입 패턴
4. `core-util`: Clipboard·Share·Haptic·Toast
5. `core-i18n`: 다국어 셋업 보일러플레이트
6. **공통 표준 문서 갱신**: 내부 프로젝트 표준 메모리에 새 표준 즉시 반영

---

## 14. 다음 단계

1. **이 spec 형 검토** → 수정 또는 승인
2. **`writing-plans` 스킬**로 구현 계획서(implementation plan) 작성 → step-by-step 작업 분해
3. 외부 준비(Play Console 등록·행안부 API 키)는 코딩과 병렬 진행
