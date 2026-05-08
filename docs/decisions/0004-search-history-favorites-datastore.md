# ADR-0004: 검색 히스토리·즐겨찾기 저장소 — DataStore Preferences

| 항목 | 내용 |
|---|---|
| 상태 | 승인됨 |
| 날짜 | 2026-05-04 |
| 결정자 | jewan |
| 적용 범위 | zipkr v1.1k |
| 관련 PR | #16 |
| 준수 헌법 | `JEWAN_DEV_CONSTITUTION.md` §2 (아키텍처 원칙), §8 (기술 도입 심사) |

---

## 컨텍스트
zipkr v1.0~v1.1 단계에서 사용자가 동일 주소를 반복 검색하는 패턴이 식별됐다. 자주 쓰는 집·회사 주소를 매번 다시 타이핑하는 마찰이 컸고, 검색 직후 다시 같은 주소를 보기 위해 추가 입력이 필요한 상태였다.

요구사항:
- **최근 검색** — 검색→상세 진입 시 자동 저장, 최대 5건 (LRU)
- **즐겨찾기** — 사용자 명시적 토글 (★), 개수 무제한
- 빈 검색 상태에서 즉시 노출 (칩 형태 → 탭으로 즉시 detail sheet 오픈, 재검색 없이)
- 영구 저장 (앱 재시작 후에도 유지)

저장할 데이터 단위는 `Address` 도메인 객체 한 건이고, 고유 식별자는 `zipCode|roadAddress|buildingName` 조합 키(`stableKey`)다. 검색 대상이 되는 필드는 없고, 순차 조회·LRU 갱신·토글이면 충분.

## 결정
**Jetpack DataStore Preferences**를 채택해 검색 히스토리·즐겨찾기를 영구 저장한다. `Address`는 `@Serializable` 어노테이션 + `stableKey` 프로퍼티를 추가해 JSON 문자열 리스트로 직렬화한다.

구조:
- `recents: List<Address>` — max 5 LRU, 새 항목 head 추가 + 중복 stableKey 제거 + tail trim
- `favorites: List<Address>` — 무제한, stableKey 기준 토글
- `Flow<...>` 기반 reactive 노출 — UI는 `collectAsState`로 자동 구독

레이어:
- `data/SearchHistoryRepository` (interface)
- `data/DataStoreSearchHistoryRepository` (구현)
- `di/PersistenceModule` — `DataStore<Preferences>` Hilt 바인딩
- `di/DataModule` — Repository 바인딩

---

## 6단계 심사

### 1. 필요성
반복 사용 패턴 해소 + 빠른 재진입 흐름은 우편번호 앱의 핵심 UX. 형의 `feedback_quality_first.md`(양보다 질, 결정판) 가치관에 따라 v1.1 단계에서 박아두는 게 baseline이다. 사용자가 영구 저장된 즐겨찾기·최근 목록을 갖는 것은 "이 앱을 다시 연다"는 retention의 기반.

### 2. 대안 (최소 2개)
- **A. SharedPreferences** (레거시): 동기 API, ANR 위험, Flow 없음, Google이 deprecate 권고 중.
- **B. DataStore Preferences** (선택): 비동기 Flow 기반, 타입 안전, Google 공식 권고, Jetpack 1차 시민. Address를 JSON 직렬화해 `stringPreferencesKey`에 저장.
- **C. DataStore Proto**: Protobuf schema 정의 필요, 컴파일러 의존성 추가, 강타입이지만 `Address` 한 종류 단순 데이터에 over-engineering.
- **D. Room**: 풀 SQL DB. 인덱스/쿼리 필요 없는 단순 LRU 5건 + 무제한 리스트에 과함. 헌법 §3.2(과한 추상화) 위반.
- **E. 메모리만 유지**: 앱 재시작 시 휘발, 사용자가 "어제 찾은 주소"를 다시 입력해야 하므로 요구 미충족.

### 3. 트레이드오프
- ✅ 비동기 Flow → UI `collectAsState`로 자연스러운 reactive 갱신
- ✅ ANR 안전 (DataStore는 IO 디스패처에서 처리)
- ✅ 신규 의존성 1개(`androidx.datastore:datastore-preferences`)만 추가, Jetpack 표준
- ✅ JSON 직렬화로 `Address` 스키마 진화 시 마이그레이션 유연 (필드 추가는 무손실)
- ⚠️ Address JSON 직렬화 코스트 — 항목당 수백 바이트, 최대 ~100건 가정해도 무시 가능
- ⚠️ 즐겨찾기가 수천 건으로 폭증하면 부적합 (Room으로 전환 필요) — 현재 사용자 패턴에선 비현실적
- ⚠️ `Address`에 `@Serializable` 추가 → kotlinx-serialization 의존성 필요 (이미 네트워크 응답 파싱에 사용 중이라 incremental 0)

### 4. 동작 원리
- DataStore는 내부적으로 단일 파일에 비동기적으로 직렬화된 데이터를 쓰고 읽는다.
- `Flow<Preferences>`로 변경 사항이 즉시 emit되며, UI 콜렉터는 자동 재컴포지션된다.
- 동시 쓰기는 mutex로 직렬화돼 race 없음.
- `edit { prefs -> ... }` 블록 안에서 read-modify-write가 원자적으로 실행된다 (LRU 갱신·토글에 안전).

### 5. 원래 목적
DataStore는 SharedPreferences의 후속 API로, Google이 명시적으로 "key-value, small data, async, type-safe"를 목적으로 설계했다. 검색 히스토리·즐겨찾기는 정확히 이 범주 — key-value 두 묶음(recents/favorites), JSON 문자열 직렬화로 small data 유지.

### 6. 유사 기술 비교
| 측면 | DataStore Pref | SharedPref | Room | 메모리만 |
|---|---|---|---|---|
| 비동기 API | ✅ | ❌ | ✅ | N/A |
| Flow 지원 | ✅ | ❌ | ✅ | (StateFlow 가능) |
| 타입 안정성 | ✅ (Proto면 더 강함) | ⚠️ | ✅ | ✅ |
| 구현 복잡도 | ★★ | ★ | ★★★★ | ★ |
| 영구 저장 | ✅ | ✅ | ✅ | ❌ |
| 인디 단계 적합도 | ✅ 최적 | ⚠️ 레거시 | ❌ 과함 | ❌ |
| zipkr 사용 패턴 적합 | ✅ | △ | ❌ | ❌ |

→ **DataStore Preferences가 단순 key-value 영구 저장 use case의 표준 답**. Room은 인덱스 쿼리가 필요해질 때 (예: 즐겨찾기 폴더·태그 등) 전환 트리거.

---

## 영향
- **신규 의존성**: `androidx.datastore:datastore-preferences` (Jetpack 표준)
- **신규 클래스**: `SearchHistoryRepository` (interface), `DataStoreSearchHistoryRepository`, `PersistenceModule`
- **데이터 모델 변경**: `Address`에 `@Serializable` + `stableKey` 프로퍼티 추가
- **UI 변경**: 빈 검색 상태에 `HistoryFavoritesPanel` 칩 행 노출 (둘 다 비면 기존 EmptyState 유지), DetailSheet 헤더 우측에 ★ 토글
- **자동 동작**: detail sheet 오픈 시 `addRecent()` 자동 호출 — 사용자 인지 부담 0

## 후속 작업
- ✅ `DataStoreSearchHistoryRepository` 구현 (max 5 LRU, favorite 무제한)
- ✅ `PersistenceModule` Hilt 바인딩
- ✅ `Address.stableKey` 도입
- ✅ `HistoryFavoritesPanel` 컴포넌트
- ✅ `DetailSheet` ★ 토글 + 자동 addRecent
- v2.0 트리거: 즐겨찾기 폴더/태그 요구 발생 시 Room 전환 검토

## 결정 근거 메모
- 단순한 use case에 강타입 schema(Proto)나 RDBMS(Room)는 과한 도구. JSON Preferences는 "충분히 단순, 충분히 안전"의 균형점.
- 헌법 §2.2(기술 교체 가능성) — Repository interface로 분리해 미래에 Room으로 갈아끼울 때 ViewModel/UI 영향 0이 되도록 했다. interface-first 설계의 가치 명시.
- `feedback_dead_code_removal` 룰에 따라 메모리 전용 임시 구현은 두지 않았다 — 영구 저장이 baseline.
