# ADR-0001: 안드로이드 라이브러리 스택 일괄 승인

| 항목 | 내용 |
|---|---|
| 상태 | 승인됨 |
| 날짜 | 2026-04-25 |
| 결정자 | jewan |
| 적용 범위 | zipkr 및 후속 프로젝트 (`inkcat-apps/*`) |
| 관련 spec | `docs/superpowers/specs/2026-04-25-postal-app-design.md` |
| 준수 헌법 | `JEWAN_DEV_CONSTITUTION.md` §8 (기술 도입 심사 규칙) |

---

## 컨텍스트
zipkr와 후속 프로젝트들이 공통으로 사용할 라이브러리 스택을 결정한다.
헌법 §8은 "신규 의존성 도입 시 ① 필요성 ② 대안 2개 ③ 트레이드오프 ④ 동작 원리 ⑤ 원래 목적 ⑥ 유사 기술 비교"의 6단계 문서화를 강제한다.
12개 라이브러리를 개별 ADR로 관리하면 MVP 일정이 무너지므로, **본 ADR 1건으로 일괄 심사·승인**하고 향후 신규 의존성만 별도 ADR을 작성한다.

## 결정
아래 12개 라이브러리를 **공통 표준 의존성**으로 채택한다. 모든 프로젝트는 본 스택을 기본값으로 시작하며, 추가 의존성은 별도 ADR + 형의 명시 승인 후에만 도입한다. 모든 버전은 Version Catalog(`libs.versions.toml`)로 명시 고정하며 `latest` 사용을 금지한다.

---

## 6단계 심사 — 카테고리별

### A. 빌드/품질 (4종)

| 항목 | 1. 필요성 | 2. 대안 | 3. 트레이드오프 | 4. 동작 원리 | 5. 원래 목적 | 6. 유사 기술 비교 |
|---|---|---|---|---|---|---|
| **KSP** | KAPT의 빌드 속도 병목 해소. Hilt·Room이 KSP 지원하므로 채택 즉시 효과 | KAPT 유지 / Annotation 기반 도구 제거 | 일부 라이브러리는 KSP 미지원 (대부분 마이그레이션 완료) | Kotlin 컴파일러 플러그인으로 어노테이션 처리, Java AST 우회 | KAPT의 Java 의존·속도 한계를 해소하기 위한 JetBrains 공식 후속 | KAPT는 느림·deprecated 추세 |
| **Detekt** | 코드 품질 정적 분석 자동화 (공통 표준 일관성 가드) | ktlint 단독 / Android Lint 단독 | 룰 셋 학습/조정 비용 | Kotlin AST 분석으로 안티패턴 탐지 | Kotlin 코드 품질 자동 검사 | Android Lint는 안드로이드 특화·문법 분석 약함, ktlint는 포맷 위주 |
| **Ktlint** | 코드 포맷 일관성 자동 보장 (PR 리뷰 부담↓) | spotless / 수동 포맷 | 일부 룰이 의견적 | Kotlin coding convention 기반 포맷·검사 | Kotlin 공식 스타일 가이드 강제 | spotless는 다언어 통합 도구 — Kotlin 단일이면 ktlint가 더 가벼움 |
| **Version Catalog** | 여러 앱 의존성 버전 단일 관리 | buildSrc / 직접 명시 / Gradle 플러그인 | TOML 문법 학습 | Gradle 7.4+ 공식 기능, `libs.versions.toml`로 중앙 집중 | 멀티모듈/멀티프로젝트 의존성 동기화 | buildSrc는 Kotlin 코드, 빌드 캐시 친화도↓ |

### B. UI 보조 (3종)

| 항목 | 1. 필요성 | 2. 대안 | 3. 트레이드오프 | 4. 동작 원리 | 5. 원래 목적 | 6. 유사 기술 비교 |
|---|---|---|---|---|---|---|
| **Coil** | Compose 친화 이미지 로딩 표준 (공통 자산) | Glide / Picasso / 직접 구현 | Glide 대비 일부 고급 캐시 기능 부족 | Kotlin 코루틴 + Compose 친화 API | Compose 시대 이미지 로딩 표준화 | Glide는 자바 기반·Compose 통합 약함, Picasso는 비활성 |
| **AndroidX Splash Screen API** | Android 12+ 표준 스플래시 (공통 진입 표준) | Theme 기반 수동 / 라이브러리 자체 구현 | API 사용 시 minSdk 12 미만 fallback 필요 (지원함) | 시스템 윈도우로 스플래시 그리기, 앱 초기화 시 자동 dismiss | Android 12부터 표준화된 스플래시 동작 통합 | 직접 구현은 OS별 동작 불일치 |
| **Accompanist** | Permissions·SystemUiController 등 Compose 보조 (Compose 본체 미지원 영역 보조) | 직접 구현 / 다른 헬퍼 라이브러리 | Google 실험적 → 일부 모듈은 Compose 본체로 흡수되며 deprecated | Compose 위 추가 컴포넌트·헬퍼 | Jetpack Compose 보조 (Google 공식 실험소) | 직접 구현은 보일러플레이트↑ |

### C. 운영 (3종)

| 항목 | 1. 필요성 | 2. 대안 | 3. 트레이드오프 | 4. 동작 원리 | 5. 원래 목적 | 6. 유사 기술 비교 |
|---|---|---|---|---|---|---|
| **Timber** | 환경별 로깅 자동 분기 (debug = Logcat, release = no-op 또는 Crashlytics 연동) | `android.util.Log` 직접 / 자체 wrapper | 라이브러리 종속 | `Tree` 인터페이스를 통한 로그 라우팅 | Logcat 보일러플레이트 제거 + 환경별 분기 | 자체 wrapper 구현은 여러 앱 표준화 부담↑ |
| **LeakCanary** | Debug 빌드에서 메모리 누수 자동 감지 (앱 안정성 보장) | 수동 메모리 프로파일링 / 다른 누수 도구 | Debug 빌드 크기·성능 영향 (release 자동 제외) | RefWatcher가 객체 GC 후 누수 탐지 → 알림 | 안드로이드 메모리 누수 자동 진단 | 수동 프로파일링은 발견 비용 매우 높음 |
| **Firebase Crashlytics** ⭐ | 여러 앱의 크래시·ANR을 한 콘솔에서 관제 (장기 운영 척추) | Sentry / Bugsnag / 자체 수집 | Firebase 셋업 +1일·구글 의존 | 네이티브·자바 크래시 캡처 → Firebase 전송 → 그룹화 | 모바일 크래시 자동 수집·분류 | Sentry는 좋지만 무료 한도 작음, 자체 수집은 장기 운영 부담↑ |

### D. 테스트 (3종)

| 항목 | 1. 필요성 | 2. 대안 | 3. 트레이드오프 | 4. 동작 원리 | 5. 원래 목적 | 6. 유사 기술 비교 |
|---|---|---|---|---|---|---|
| **MockK** | Kotlin 친화 mock (sealed/object/coroutine 지원) | Mockito / Mockito-Kotlin / 수동 fake | DSL 학습 | 바이트코드 조작으로 mock 생성, Kotlin 친화 DSL | Kotlin 환경 mock 표준화 | Mockito는 자바 기반·Kotlin 한정 케이스 약함 |
| **Turbine** | Flow/StateFlow 테스트 표준 (timing 안정) | 수동 collect + assert / runTest 직접 사용 | 라이브러리 종속 | TestScope에서 Flow를 채널처럼 검증 | Kotlin Flow 테스트 가독성·안정성 | 수동 collect는 timing 이슈·assert 부담 |
| **Truth** (Google) | 가독성 좋은 어설션 (`assertThat(x).isEqualTo(y)`) | JUnit `assertEquals` / Hamcrest / AssertJ | 라이브러리 종속 | Subject 기반 assertion DSL | Java/Kotlin 어설션 가독성 향상 | AssertJ도 좋으나 Truth는 Google 공식·Compose 친화 |

---

## 결과 / 영향
- 본 12개는 모든 프로젝트가 default로 채택. `libs.versions.toml`에 명시 버전으로 고정.
- 신규 의존성 추가 시 별도 ADR(`docs/decisions/000N-...md`) 작성 + 형 명시 승인 필수.
- 라이브러리 deprecation 또는 더 나은 대안 발견 시 별도 ADR로 교체 결정.
- 내부 공통 표준 메모리와 본 ADR은 항상 정합 유지.

## 승인
- jewan, 2026-04-25 — 공통 표준 의존성으로 일괄 채택.
