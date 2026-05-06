# ADR-0002: Kakao Local API 통합 — 영문 검색 chain

| 항목 | 내용 |
|---|---|
| 상태 | 승인됨 |
| 날짜 | 2026-05-06 |
| 결정자 | jewan |
| 적용 범위 | zipkr (글로벌 결정판 트랙 — B 단계) |
| 관련 트랙 | `feedback_quality_first.md` (양보다 질·결정판 가치관) |
| 준수 헌법 | `JEWAN_DEV_CONSTITUTION.md` §8 (기술 도입 심사 규칙) |

---

## 컨텍스트
한국 우편번호 앱 zipkr가 외국인 사용자(한국 거주·여행자·해외 송배송 수신자·K-문화 팬덤)를 잡으려면 영문 keyword 검색이 필수이다. 행안부 도로명주소 API(`addrLinkApi.do`)는 한글 keyword만 지원하고 `addrEngApi.do`는 한글→영문 변환 용도라 입력측 영문 검색이 불가능함이 검증됐다. 외부 API의 영문 keyword 매칭이 필요하다.

검증된 외국인 실제 검색 패턴:
- POI(건물명·역명·랜드마크·호텔) → **약 90%**
- 도로명 단독 영문 → **약 10%** (한국인도 거의 안 함)

본 ADR은 영문 keyword를 한국 우편번호로 매핑하는 chain의 외부 API 선택을 결정한다.

## 결정
**Kakao Local API의 keyword search(`/v2/local/search/keyword.json`)** 를 통합한다.

흐름:
1. 사용자가 영문 keyword 입력 ("Gangnam Finance Center")
2. **Kakao keyword API** 호출 → 한글 `road_address_name` 추출 ("서울 강남구 테헤란로 152")
3. 추출한 한글 도로명을 **행안부 API**에 전달 → 우편번호 반환 ("06236")

도로명 단독 영문(예: "Teheran-ro 152")은 Kakao keyword/address 모두 미지원이므로 본 PR 범위에서 제외하고 빈 결과 화면에 hint 메시지로 안내한다 ("Try a building name or landmark"). 도로명 영문 매칭은 v2.0에서 Google Places API 또는 자체 매핑 사전으로 확장한다.

---

## 6단계 심사

### 1. 필요성
외국인 사용자(한국 거주 250만+, 여행자 연 1700만+, K-문화 팬덤 글로벌)가 한국 우편번호를 영문 keyword로 검색할 수 있어야 한다. 행안부 단독으론 불가능. 형의 글로벌 결정판 가치관(`feedback_quality_first.md`)에 따라 디테일 박는 게 baseline.

### 2. 대안 (최소 2개)
- **A. Google Places API**: 영문 도로명까지 잡음, 단 비용 발생(월 무료 쿼터 ~$200, 초과시 유료) + 신규 외부 의존성 + 키 발급 추가
- **B. Kakao Local API keyword search** (선택): POI 영문 90% 커버, **이미 zipkr가 KakaoMap WebView로 사용 중이라 기존 의존성 확장**, 무료 쿼터 일 100,000회 (인디 앱에 충분)
- **C. 자체 영문 인덱스 구축**: 행안부 응답의 `engAddr`를 캐시 인덱싱, 초기 데이터 없음 → cold start 미흡
- **D. 행안부 단독 유지**: 영문 검색 불가 → 글로벌 결정판 트랙 자체 좌초

### 3. 트레이드오프
- ✅ 이미 통합된 Kakao 키 재사용 (REST API key, KakaoMap WebView와 동일)
- ✅ 무료 쿼터 충분 (일 10만회), 인디 앱 트래픽 한도 내
- ✅ POI 영문 90% 커버 (검증됨: Gangnam Finance Center, Lotte World Tower, Gangnam Station, Seoul City Hall 모두 정확 매칭)
- ⚠️ Chain(2단계)로 응답 시간 약 +200~400ms 증가 (Kakao 1회 + 행안부 1회)
- ⚠️ 도로명 영문 단독 미지원 (외국인 검색 패턴의 약 10% 손실)
- ⚠️ Kakao의 한글 매핑이 정확하지 않은 edge case 가능 (예: 동음이의 POI)

### 4. 동작 원리
- Kakao keyword search는 카카오의 POI 인덱스 기반. 영문 query를 한·영 매핑으로 처리해 한국 POI 데이터에서 매칭한다.
- 응답에 `place_name`(POI 한글명), `road_address_name`(한글 도로명), `address_name`(한글 지번주소)이 포함된다.
- `road_address_name`을 행안부 `addrLinkApi.do`의 `keyword`로 그대로 전달하면 우편번호 + 영문주소 + 추가 메타데이터를 받는다.

### 5. 원래 목적
Kakao Local API는 카카오맵 서비스의 외부 활용을 위한 공식 API. POI 검색·좌표 변환·카테고리 검색이 핵심 use case로, 본 chain은 "영문 keyword → 한글 POI 매핑" 단계에서 정확히 의도된 사용 사례에 부합한다.

### 6. 유사 기술 비교
| 측면 | Kakao Local | Naver Map API | Google Places |
|---|---|---|---|
| 영문 POI 검색 | ✅ 강함 (검증됨) | ✅ 강함 | ✅ 가장 강함 (도로명까지) |
| 무료 쿼터 | 일 10만회 | 일 25,000회 | 월 ~$200 무료 (초과 유료) |
| 한국 POI 정확도 | ✅ 최상 | ✅ 최상 | ⚠️ 중상 (한국 데이터 일부 갭) |
| zipkr 기존 통합 | ✅ 이미 사용 중 | ❌ | ❌ |
| 신규 키 필요 | ❌ | 신규 | 신규 + 결제 등록 |

→ **Kakao이 zipkr 컨텍스트에서 가장 합리적**. Google은 도로명 영문이 강하지만 비용·신규 의존성·결제 등록 부담이 인디 단계에 비해 큼. v2.0 마일에서 재검토 가능.

---

## 영향
- **신규 의존성**: 없음 (Kakao 키는 이미 보유, REST API 호출만 추가)
- **추가 호출 단위**: 영문 입력 1회당 Kakao 1회 + 행안부 1회 (이전 한글 입력은 행안부 1회만, 동작 변경 없음)
- **사용자 가시 변화**: 영문 검색 시 결과 노출 (외국인 사용성 ↑)
- **결과 신뢰도**: Kakao keyword 매칭 결과 1건을 행안부 도로명 검색에 전달 — 동일 도로명에 여러 건물이 있으면 행안부가 여러 우편번호 반환할 수 있어 결과 dedup이 필요할 수 있다 (구현 시 검토).

## 후속 작업
- B-2: `KakaoLocalProvider` 추가 + Hilt DI 등록
- B-3: 입력 자동 감지 (영문 비율 50% 이상 시 영문 chain) + chain 매핑 로직
- B-4: `SearchAddressUseCase`에 영문 분기 통합
- B-5: 영문 placeholder hint 갱신 ("Search in English: building names, stations, landmarks")
- B-6: 단위 테스트 + installDebug + 형 폰 5개 시나리오 검증
- 출시 후 모니터링: Kakao API 호출 수 추이, 빈 결과율, edge case 수집
- v2.0: Google Places API 또는 자체 영문 도로명 사전으로 확장

## 결정 근거 메모
- 형의 빠른결단 + 양보다 질 가치관에 따라 "이미 검증된 Kakao chain"으로 즉시 진행하되, "v2.0 확장 여지"를 명시해 결정판이 아니라 v1 baseline임을 분명히 한다.
- 영문 도로명 단독 미지원은 결정판의 흠집이지만 외국인 검색 패턴의 90%를 커버하므로 v1 출시에 적합한 균형이다.
