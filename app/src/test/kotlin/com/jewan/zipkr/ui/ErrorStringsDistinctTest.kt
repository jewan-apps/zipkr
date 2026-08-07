package com.jewan.zipkr.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * `error_*` 사용자 메시지가 서로 구별되는지 검증한다.
 *
 * 2026-08-01 Play 심사 반려의 직접 원인이 이 중복이었다. api/unknown/auth/path 네 키가
 * 모두 "잠시 후 다시 시도해주세요"로 같았고, 행안부 개발승인키 만료(E0014)로 검색이
 * 100% 실패했을 때 화면에는 재시도 문구만 보였다. 심사자는 "눌러도 반응 없는 UI 요소"로
 * 판정했고, 우리도 스크린샷만으로는 원인을 특정할 수 없었다.
 *
 * strings.xml을 직접 파싱한다 — Android 리소스 로더 없이 순수 JVM에서 돌기 위함이며,
 * 실제 배포되는 파일 그대로를 읽으므로 리소스와 테스트가 어긋날 수 없다.
 */
class ErrorStringsDistinctTest {
    @Test
    fun `한국어 error 메시지는 서로 중복되지 않는다`() {
        assertNoDuplicateErrorMessages("src/main/res/values/strings.xml")
    }

    @Test
    fun `영어 error 메시지는 서로 중복되지 않는다`() {
        assertNoDuplicateErrorMessages("src/main/res/values-en/strings.xml")
    }

    @Test
    fun `한국어와 영어의 error 키 집합이 같다`() {
        val ko = parseErrorStrings("src/main/res/values/strings.xml").keys
        val en = parseErrorStrings("src/main/res/values-en/strings.xml").keys
        assertThat(en).containsExactlyElementsIn(ko)
    }

    private fun assertNoDuplicateErrorMessages(relativePath: String) {
        val messages = parseErrorStrings(relativePath)
        assertThat(messages).isNotEmpty()

        // 같은 문구를 쓰는 키들을 모아 보고한다 — 어떤 키끼리 겹쳤는지 바로 보여야 고칠 수 있다.
        val duplicated =
            messages.entries
                .groupBy({ it.value }, { it.key })
                .filterValues { it.size > 1 }

        assertThat(duplicated).isEmpty()
    }

    /** `name="error_..."` string 항목만 뽑아 key to value 로 반환한다. */
    private fun parseErrorStrings(relativePath: String): Map<String, String> {
        val file = File(relativePath)
        assertThat(file.exists()).isTrue()
        return STRING_ENTRY
            .findAll(file.readText())
            .associate { it.groupValues[1] to it.groupValues[2].trim() }
            .filterKeys { it.startsWith(ERROR_KEY_PREFIX) }
    }

    private companion object {
        // <string name="키">값</string> 한 줄 형태만 매칭한다. 현재 strings.xml은 전부 이 형태다.
        val STRING_ENTRY = Regex("""<string name="([^"]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
        const val ERROR_KEY_PREFIX = "error_"
    }
}
