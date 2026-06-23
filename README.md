# zipkr — 한국 우편번호 찾기

> 도로명·지번·건물명·POI·우편번호 어떤 단편이든 5자 안에 정확한 한국 주소를 찾는다.
> 외국인 사용자를 위해 한·영 UI + 영문 keyword 검색을 1차 시민으로 박았다.

[![개인정보 처리방침](https://img.shields.io/badge/Privacy-Policy-blue)](https://jewan100.github.io/privacy/zipkr/)
[![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?logo=kotlin&logoColor=white)](#)
[![Jetpack Compose](https://img.shields.io/badge/Compose-Material3-4285F4?logo=jetpackcompose&logoColor=white)](#)

---

## 핵심 기능

| 기능 | 설명 | 관련 ADR |
|---|---|---|
| **한글 keyword 검색** | 도로명·지번·건물명을 행안부 API로 즉시 매칭 | — |
| **우편번호 5자리 역검색** | 5자리 숫자 입력 시 행안부 native 매칭으로 주소 역조회 | [ADR-0003](docs/decisions/0003-postal-reverse-search-native.md) |
| **검색 히스토리·즐겨찾기** | DataStore 영구 저장. 최근 5건 LRU + 무제한 즐겨찾기, 빈 화면 칩으로 즉시 재진입 | [ADR-0004](docs/decisions/0004-search-history-favorites-datastore.md) |
| **한·영 i18n + 앱별 언어 설정** | Android 13+ Per-app language 표준 패턴. 디바이스 언어 자동 분기 + OS 설정에서 zipkr만 한·영 토글 | [ADR-0005](docs/decisions/0005-i18n-per-app-language.md) |
| **영문 POI keyword 검색** | "Gangnam Finance Center" 같은 영문 입력을 Kakao keyword API → 한글 도로명 → 행안부 chain으로 매핑 | [ADR-0002](docs/decisions/0002-kakao-local-api-en-search.md) |
| **상세 시트** | 도로명·지번·영문주소·우편번호·KakaoMap 미니맵 + 외부 지도 앱 연동 | — |
| **검색어 하이라이트** | 인접 토큰 merge + 공백 정규화 fallback으로 "안현로서7길45" ↔ "안현로서7길 45" 시각적 동치 | — |

---

## 기술 스택

| 영역 | 선택 |
|---|---|
| 언어/UI | Kotlin · Jetpack Compose (Material 3) |
| minSdk / target | 24 (Android 7.0) / 36 |
| DI | Hilt |
| 네트워크 | Retrofit + OkHttp + kotlinx-serialization |
| 영속 저장 | Jetpack DataStore Preferences |
| 이미지 | Coil |
| 분석/안정성 | Firebase Crashlytics + Analytics, Timber |
| 광고 | Google Play Services Ads (AdMob 배너) |
| 외부 API | 행안부 도로명주소 검색 API · Kakao Local API · KakaoMap WebView |

라이브러리 스택의 전체 선정 근거는 [ADR-0001](docs/decisions/0001-android-library-stack.md) 참조.

---

## Provider 아키텍처

```
┌──────────────────┐    한글 keyword     ┌──────────────────────┐
│  SearchViewModel │ ──────────────────► │  JusoAddressProvider │
└────────┬─────────┘                     │  (행안부 API)         │
         │                               └──────────────────────┘
         │ 영문 keyword
         ▼
┌──────────────────────┐  road_address_name  ┌──────────────────────┐
│  KakaoLocalProvider  │ ──────────────────► │  JusoAddressProvider │
│  (POI 매핑)           │                     │  (한글 도로명 → 우편) │
└──────────────────────┘                     └──────────────────────┘
         ▲
         │ 5자리 숫자
         │ (sido 합성 skip)
         │
   raw query
```

- 모든 외부 API는 `Provider` interface로 추상화돼있다 (헌법 §2.2 기술 교체 가능성).
- 영문 검색은 Kakao → 행안부 2-step chain. 한글 입력과 우편번호는 행안부 단독 호출.
- Repository 계층(`SearchAddressUseCase`, `SearchHistoryRepository`)이 UI와 Provider 사이를 매개한다.

---

## 빌드

### 사전 준비

루트의 `local.properties`에 아래 키들을 채운다. 파일은 `.gitignore`에 등록돼있어 커밋되지 않는다.

```properties
# 필수 — 검색 동작
juso.api.key=YOUR_HAENGAN_KEY
kakao.rest.api.key=YOUR_KAKAO_REST_KEY
kakao.js.api.key=YOUR_KAKAO_JS_KEY

# release 빌드 시 권장 (debug는 Google 공식 테스트 ID로 빌드됨)
admob.app.id=ca-app-pub-...
admob.banner.unit.id=ca-app-pub-.../...
```

| 키 | 발급처 | 용도 |
|---|---|---|
| `juso.api.key` | [행정안전부 도로명주소 개발센터](https://business.juso.go.kr/) | 도로명·지번·우편번호 검색 |
| `kakao.rest.api.key` | [Kakao Developers](https://developers.kakao.com/) | 영문 POI keyword 검색 |
| `kakao.js.api.key` | Kakao Developers | 상세 시트 KakaoMap WebView |
| `admob.app.id` / `admob.banner.unit.id` | [AdMob](https://admob.google.com/) | release 배너 광고 |

### 출시 패키지

Play Console에 등록한 출시 패키지는 `com.inkcat.zipkr`이다. Kotlin 소스의 namespace는 기존 `com.jewan.zipkr`를 유지하지만, 스토어에 올라가는 `applicationId`는 Gradle 기본값으로 `com.inkcat.zipkr`를 사용한다.

Firebase/Crashlytics/Analytics를 쓰려면 Firebase 콘솔에서 Android 앱 패키지 `com.inkcat.zipkr`를 추가하고 새 `app/google-services.json`을 내려받아야 한다. 아직 기존 Firebase 설정으로 로컬 검증을 해야 할 때만 아래처럼 임시 override를 사용한다.

```bash
./gradlew :app:testDebugUnitTest -Pzipkr.applicationId=com.jewan.zipkr
```

### 실행

```bash
# debug 빌드 + 실기기 설치 (헌법 §13.1.5 강제 검증 단계)
./gradlew installDebug

# release 빌드 (서명 키 필요)
./gradlew assembleRelease

# 단위 테스트
./gradlew test
```

---

## 프로젝트 구조

```
app/src/main/kotlin/com/jewan/zipkr/
├── data/             # Address, Sido, Repository interface/구현, Provider
│   ├── DataStoreSearchHistoryRepository.kt
│   ├── SearchHistoryRepository.kt
│   └── ...
├── di/               # Hilt 모듈
│   ├── NetworkModule.kt
│   ├── DataModule.kt
│   └── PersistenceModule.kt
├── ui/
│   ├── search/       # SearchScreen, SearchViewModel, SearchUiState
│   ├── detail/       # DetailSheet, DetailHeader, DetailViewModel
│   └── components/   # 재사용 컴포넌트 (HistoryFavoritesPanel, SidoLabels, ...)
└── ...

docs/
├── decisions/        # ADR (의사결정 기록)
├── superpowers/      # 설계 spec + 구현 plan
└── release/          # 출시 준비물 (로컬 보관, gitignore)
```

---

## 문서

- [개발 헌법](../JEWAN_DEV_CONSTITUTION.md) — 코딩 스타일·아키텍처·문서화·테스트·Git/PR 룰
- [ADR (의사결정 기록)](docs/decisions/) — 0001~0005
- [개인정보 처리방침](https://jewan100.github.io/privacy/zipkr/) — Play Console 등록 URL

---

## 라이선스

이 저장소는 형 개인의 인디 앱 양산 공장 표준 적용 사례이며, 별도 오픈소스 라이선스를 부여하지 않는다. 코드 참고는 자유, 재배포 전 [이메일 문의](mailto:hawaii1468@gmail.com).
