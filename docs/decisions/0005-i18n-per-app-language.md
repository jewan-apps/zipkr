# ADR-0005: i18n 영문화 + 앱별 언어 설정 — Android 13+ localeConfig 표준 패턴

| 항목 | 내용 |
|---|---|
| 상태 | 승인됨 |
| 날짜 | 2026-05-05 |
| 결정자 | jewan |
| 적용 범위 | zipkr v1.1 i18n Phase 5 (글로벌 결정판 트랙 A 단계) |
| 관련 PR | #18 |
| 관련 ADR | [[0002-kakao-local-api-en-search]] — B 단계 영문 검색 chain |
| 관련 트랙 | `feedback_quality_first.md` (양보다 질·결정판) |
| 준수 헌법 | `JEWAN_DEV_CONSTITUTION.md` §2 (아키텍처 원칙) |

---

## 컨텍스트
zipkr를 외국인 사용자(한국 거주 250만+, 여행자 연 1700만+, K-문화 팬덤)에게 열려면 UI가 영문으로 분기돼야 한다. 글로벌 결정판 트랙의 단계 분리:

- **Phase 5-A (본 ADR)**: UI 텍스트 영문화 + 앱별 언어 설정 인프라
- **Phase 5-B**: 영문 keyword 검색 통합 (Kakao + 행안부 chain) — [[0002-kakao-local-api-en-search]]

A 단계는 디바이스 언어가 영어면 영문 UI, 한국어면 한글 UI로 자동 분기되고, 추가로 사용자가 디바이스 언어와 무관하게 zipkr만 한·영 선택할 수 있어야 한다. 후자는 외국인 한국 거주자가 디바이스 전체는 한글로 두고 zipkr만 영문으로 쓰는 케이스 + 한국인이 영어 UI를 선호하는 케이스를 동시에 잡는다.

기술 컨텍스트:
- minSdk는 Android 9 (API 28)
- targetSdk는 Android 14 (API 34)
- Android 13(API 33)부터 **Per-app language preferences** 표준 API가 도입됨

## 결정
**Android 13+ Per-app language preferences API**를 채택한다. `res/xml/locales_config.xml`에 지원 언어(ko, en)를 선언하고 `AndroidManifest.xml`의 `<application>`에 `android:localeConfig` 속성을 연결한다. 사용자는 OS 설정 → 일반 → 언어 → 앱 언어 → ZipKR 경로에서 zipkr 전용 언어를 토글한다.

번역 데이터:
- `res/values/strings.xml` — 한국어 (default)
- `res/values-en/strings.xml` — 영어 (약 80개 사용자 가시 문자열)

`Sido` enum의 한글 displayName은 데드 코드로 제거하고, UI 레이어에 `SidoLabels.kt` extension을 신설해 `stringResource(R.string.sido_seoul)` 같은 리소스 매핑을 담당시킨다. data 레이어의 androidx 무관성을 보존한다 (헌법 §2.3 레이어 분리).

Android 12 이하는 OS의 디바이스 언어를 그대로 사용한다 (앱별 언어 토글 UI 미노출). 12 이하 사용자가 전체 비중에서 작아 별도 In-app 토글 UI를 만드는 비용이 정당화되지 않는다.

---

## 6단계 심사

### 1. 필요성
글로벌 사용자 진입 자체의 전제 조건. 형의 `feedback_quality_first.md` 가치관에 따라 결정판 베이스라인. UI 영문화 없이 영문 검색만 추가하면 외국인 사용자가 검색 결과 화면에서 길을 잃는다.

추가로 외국인 한국 거주자의 실제 디바이스 사용 패턴이 "디바이스는 한글, 앱별로 영문"인 케이스가 적지 않다 (한국 카드사·은행 앱은 한글로 쓰되 IT 앱은 영문 선호). Per-app 토글이 이 패턴을 정확히 잡는다.

### 2. 대안 (최소 2개)
- **A. 앱 내부 언어 토글 UI 직접 구현**: 설정 화면에 토글 + `Locale.setDefault()` + `Configuration` 강제 갱신 + Activity recreate. 모든 SDK에서 동작하지만 OS와 분리된 별도 상태 관리(SharedPreferences 등) 필요, 앱 재시작 시 상태 복구 로직 필요, 시스템 다이얼로그·키보드 등은 여전히 OS 언어 따름 → 일관성 깨짐.
- **B. AppCompatDelegate.setApplicationLocales** (selected for Android 12-): AndroidX appcompat 1.6+의 백포팅 API. localeConfig와 조합해 Android 12 이하에서도 OS 설정 화면처럼 동작. 단 OS 설정 화면 진입은 불가능, 인앱 토글 UI 별도 필요.
- **C. Android 13+ Per-app language preferences + localeConfig 표준** (선택, Android 13+ 전용): OS 설정 화면에 자동 노출, 별도 인앱 UI 0, OS와 일관성 100%. Android 12 이하는 디바이스 언어로 fallback.
- **D. 자동 분기만(앱별 토글 없음)**: values + values-en/만 두고 디바이스 언어 그대로 따름. 구현 비용 ★ 최소지만 외국인 한국 거주자의 "디바이스 한글, zipkr 영문" use case 미충족.

### 3. 트레이드오프
**선택안 (C)의 명시적 트레이드오프**:
- ✅ 인앱 언어 설정 UI 0개 — 코드/QA 부담 없음
- ✅ OS 설정 화면 진입 (Android 13+) — 사용자가 다른 앱과 동일한 mental model로 토글
- ✅ OS와 100% 일관성 — 시스템 다이얼로그·키보드 등도 동일 언어
- ✅ 표준 패턴 — Google 권고 + 미래 보장 (deprecation 위험 0)
- ⚠️ Android 12 이하는 앱별 토글 UI 노출 안 됨 — 디바이스 언어에 종속
- ⚠️ Android 12 이하에서 디바이스 한글 + zipkr 영문 use case 미지원
- ⚠️ minSdk를 13으로 올리면 안 됨 (한국 사용자 일부가 12 이하 단말 사용 중)

**Android 12 이하 대응 결정**: 별도 인앱 토글 UI를 만들지 않는다. 이유:
1. 출시 직후 사용자 분포에서 13+ 비중이 압도적으로 클 것 (양산 베이스라인은 13+ 가정)
2. Android 12 사용자도 "디바이스 언어 = 앱 언어"라는 가장 흔한 패턴은 자동 분기로 잡힘
3. 인앱 토글 UI 추가 비용(설정 화면 신설 + 상태 영구 저장 + Activity recreate + QA) > Android 12 이하 토글 미지원으로 잃는 사용자 수

### 4. 동작 원리
- `localeConfig`는 앱이 지원하는 locale 목록을 OS에 선언한다.
- Android 13+ OS는 이 목록을 읽어 시스템 설정의 앱별 언어 화면에 zipkr를 노출하고, 사용자가 선택한 locale을 OS가 영구 저장한다.
- 앱 시작 시 OS가 자동으로 `Configuration.locale`을 사용자 선택 값으로 적용한다 — 앱 코드 변경 0.
- `Sido` 한글 displayName은 UI 레이어의 `SidoLabels.kt`에서 `stringResource()`로 대체 → 영어 빌드에선 자동으로 영문 라벨 매핑.

### 5. 원래 목적
Android Per-app language preferences API는 Google이 13에서 도입한 표준 i18n 기능으로, "디바이스 언어와 무관하게 앱별 언어를 OS가 관리한다"가 명시된 설계 목적. zipkr의 외국인 한국 거주자 use case는 이 API가 잡으려는 정확한 시나리오.

### 6. 유사 기술 비교
| 측면 | localeConfig (선택) | AppCompatDelegate | 자체 토글 + Locale.setDefault |
|---|---|---|---|
| Android 13+ 지원 | ✅ 표준 | ✅ | ✅ |
| Android 12- 지원 | ❌ (디바이스 언어 fallback) | ✅ (appcompat 1.6+) | ✅ |
| OS 설정 화면 진입 | ✅ 자동 | ⚠️ (선택적, 13+만) | ❌ |
| 인앱 토글 UI 필요 | ❌ | ✅ | ✅ |
| 상태 영구 저장 책임 | OS | OS (13+) / 앱 (12-) | 앱 |
| 미래 보장 | ✅ 표준 | ✅ AndroidX | ⚠️ |
| QA 부담 | ★ | ★★ | ★★★ |
| 코드 변경량 | ★ (XML 2개 + Manifest 1줄) | ★★ | ★★★★ |

→ zipkr가 인디 1인 양산 단계인 만큼 **OS 표준에 위임 + 인앱 UI 0**이 가장 합리적. v2.0+에서 Android 12 이하 사용자 비중이 무의미해질수록 본 결정의 가성비가 더 커진다.

---

## 영향
- **신규 의존성**: 없음 (AndroidManifest + res XML만)
- **신규 리소스**: `res/values-en/strings.xml` (~80개 문자열), `res/xml/locales_config.xml`
- **신규 코드**: `ui/components/SidoLabels.kt` (Sido enum → R.string 매핑 extension)
- **데드 코드 제거**: `data/Sido.kt` 한글 `displayName` 필드 (UI 레이어로 책임 이동, `feedback_dead_code_removal` 룰)
- **레이아웃 영향**: 영문 시·도 라벨이 한글(2자) 대비 4~5배 길어 칩 너비 초과 — `fontSize = 12sp`, `maxLines = 1`, `softWrap = false`, `ellipsis`로 해결. 영문은 표준 음역(Chungbuk, Gyeongnam 등)으로 단축한다.
- **사용자 가시 변화**: 디바이스 영어 → UI 영문 자동 분기. Android 13+에서 OS 설정 → 앱 언어 → ZipKR에서 한·영 자유 토글.

## 후속 작업
- ✅ `values-en/strings.xml` 작성
- ✅ `locales_config.xml` + Manifest `android:localeConfig`
- ✅ `SidoLabels.kt` extension 신설
- ✅ `Sido.displayName` 데드 필드 제거
- ✅ `ErrorView.retryLabel` 기본값을 `stringResource(R.string.action_retry)`로 전환
- ✅ `SidoSelectorSheet` 영문 칩 너비 해결 (fontSize/maxLines/ellipsis)
- ✅ SM-S938N installDebug 통과, OS 설정에서 한·영 토글 동작 폰 검증
- Phase 5-B (별도 PR/ADR): 영문 검색 chain → [[0002-kakao-local-api-en-search]]
- v2.0 확장 후보: 추가 언어(일본어·중국어 간체) — 일본인·중국인 K-문화 팬덤 비중에 따라 우선순위 결정

## 결정 근거 메모
- 헌법 §2.3(레이어 분리) — `Sido` 데이터 모델이 한글 표시 책임을 들고 있던 게 SRP 위반이었음을 i18n이 드러냈다. UI 레이어로 책임 이동은 i18n 자체와 무관하게 옳은 정리.
- Android 12 이하 인앱 토글을 만들지 않은 결정은 "양산 베이스라인의 단순함"을 우선한 형의 판단. 외국인 한국 거주자 중 Android 12 사용자 비중이 실측 데이터에서 의미있게 잡히면 v1.2에서 AppCompatDelegate 경유 인앱 토글로 보강 가능.
- 형의 글로벌 결정판 가치관 (`feedback_quality_first.md`)에 따라 첫 외국어 진입을 영어로 잡되, 잡을 거면 OS 표준 패턴까지 박는 게 "디테일 박힌 결정판"의 본보기.
