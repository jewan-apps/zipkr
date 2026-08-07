#!/usr/bin/env bash
# 외부 API 키가 지금 실제로 동작하는지 확인한다.
#
# 왜 필요한가 — 2026-08-01 Play 심사 반려의 원인이 행안부 개발승인키 만료였다.
# 빌드도 통과했고 정적분석도 통과했고 앱도 켜졌다. 키가 죽었다는 건 실제로 호출해야만
# 알 수 있었다. 이 스크립트 한 번이면 잡혔을 문제다.
#
# 사용법:
#   ./scripts/verify-api-keys.sh
#
# 종료 코드: 0 = 전부 정상, 1 = 하나라도 실패
#
# 키 값은 절대 출력하지 않는다 (헌법 §9). 존재 여부와 호출 결과만 보고한다.

set -uo pipefail
cd "$(dirname "$0")/.."

PROPS="local.properties"
FAILED=0

red()   { printf '\033[31m%s\033[0m\n' "$1"; }
green() { printf '\033[32m%s\033[0m\n' "$1"; }

prop() {
  # local.properties 에서 키 하나를 읽는다. 없으면 빈 문자열.
  [ -f "$PROPS" ] || return 0
  grep "^$1=" "$PROPS" 2>/dev/null | head -1 | cut -d= -f2-
}

check() {
  # $1 = 표시 이름, $2 = 성공 여부(0/1), $3 = 상세
  if [ "$2" -eq 0 ]; then
    green "  ✅ $1 — $3"
  else
    red "  ❌ $1 — $3"
    FAILED=1
  fi
}

echo "외부 API 키 검증 — $(date '+%Y-%m-%d %H:%M')"
echo

if [ ! -f "$PROPS" ]; then
  red "❌ $PROPS 가 없다. 키를 채운 뒤 다시 실행한다."
  exit 1
fi

# ── 행안부 도로명주소 API ──────────────────────────────────────────────────
# 한글 검색과 영문 검색(Kakao chain의 2단계) 양쪽이 이 키를 쓴다.
# 키가 죽으면 앱의 검색 기능 전체가 죽는다.
echo "행안부 도로명주소 API"
JUSO_KEY="$(prop 'juso.api.key')"
if [ -z "$JUSO_KEY" ]; then
  check "승인키" 1 "local.properties 에 juso.api.key 가 없다"
else
  JUSO_BODY="$(curl -s -m 20 \
    "https://business.juso.go.kr/addrlink/addrLinkApi.do?resultType=json&confmKey=${JUSO_KEY}&currentPage=1&countPerPage=1&keyword=%EA%B0%95%EB%82%A8" \
    2>/dev/null)"
  JUSO_CODE="$(printf '%s' "$JUSO_BODY" | sed -n 's/.*"errorCode":"\([^"]*\)".*/\1/p')"
  JUSO_MSG="$(printf '%s' "$JUSO_BODY" | sed -n 's/.*"errorMessage":"\([^"]*\)".*/\1/p')"
  if [ "$JUSO_CODE" = "0" ]; then
    check "승인키" 0 "정상 (errorCode=0)"
  else
    # E0001/E0002/E0014 = 인증 계열. E0014 가 "개발승인키 기간 만료"다.
    check "승인키" 1 "errorCode=${JUSO_CODE:-응답없음} · ${JUSO_MSG:-메시지없음}"
  fi
fi
echo

# ── Kakao Local API ───────────────────────────────────────────────────────
# 영문 검색의 1단계(키워드 → 한글 도로명)와 상세 시트 지도에 쓴다.
echo "Kakao Local API"
KAKAO_REST="$(prop 'kakao.rest.api.key')"
if [ -z "$KAKAO_REST" ]; then
  check "REST 키" 1 "local.properties 에 kakao.rest.api.key 가 없다"
else
  KAKAO_STATUS="$(curl -s -m 20 -o /dev/null -w '%{http_code}' \
    -H "Authorization: KakaoAK ${KAKAO_REST}" \
    "https://dapi.kakao.com/v2/local/search/keyword.json?query=Gangnam&size=1" 2>/dev/null)"
  if [ "$KAKAO_STATUS" = "200" ]; then
    check "REST 키" 0 "정상 (HTTP 200)"
  else
    check "REST 키" 1 "HTTP ${KAKAO_STATUS:-응답없음}"
  fi
fi

KAKAO_JS="$(prop 'kakao.js.api.key')"
if [ -z "$KAKAO_JS" ]; then
  check "JS 키" 1 "local.properties 에 kakao.js.api.key 가 없다 (상세 시트 지도가 안 뜬다)"
else
  # JS 키는 WebView 안에서만 유효해 서버 호출로 검증할 수 없다. 존재만 확인한다.
  check "JS 키" 0 "존재함 (실동작은 상세 시트에서 육안 확인)"
fi
echo

# ── AdMob 실 ID ───────────────────────────────────────────────────────────
# 값이 없으면 release 가 테스트 ID로 빌드된다 — 출시해도 수익이 0이다.
echo "AdMob release ID"
for k in admob.app.id admob.banner.unit.id; do
  if [ -n "$(prop "$k")" ]; then
    check "$k" 0 "존재함"
  else
    check "$k" 1 "없음 — release 가 테스트 ID로 빌드된다"
  fi
done
echo

if [ "$FAILED" -eq 0 ]; then
  green "전부 정상. 출시 진행 가능."
else
  red "실패 항목이 있다. 고치기 전에는 제출하지 않는다."
fi
exit "$FAILED"
