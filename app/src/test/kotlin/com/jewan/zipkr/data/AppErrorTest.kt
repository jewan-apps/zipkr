package com.jewan.zipkr.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * AppError.ApiBadResponse 헬퍼 7개의 정확한 errorCode 매핑을 검증한다.
 * 행안부 도로명주소 API errorCode 명세 기반이다.
 */
class AppErrorTest {
    // ── isAuthError (E0001 / E0002 / E0014) ────────────────────────────────

    @Test
    fun `E0001은 isAuthError가 true다`() {
        assertThat(AppError.ApiBadResponse("E0001").isAuthError).isTrue()
    }

    @Test
    fun `E0002는 isAuthError가 true다`() {
        assertThat(AppError.ApiBadResponse("E0002").isAuthError).isTrue()
    }

    @Test
    fun `E0014는 isAuthError가 true다`() {
        assertThat(AppError.ApiBadResponse("E0014").isAuthError).isTrue()
    }

    @Test
    fun `E0006은 isAuthError가 false다`() {
        // 기존 잘못된 매핑 정정 — E0006은 query too broad이지 auth 아니다.
        assertThat(AppError.ApiBadResponse("E0006").isAuthError).isFalse()
    }

    // ── isPathError (E0003) ─────────────────────────────────────────────────

    @Test
    fun `E0003은 isPathError가 true다`() {
        assertThat(AppError.ApiBadResponse("E0003").isPathError).isTrue()
    }

    @Test
    fun `E0001은 isPathError가 false다`() {
        assertThat(AppError.ApiBadResponse("E0001").isPathError).isFalse()
    }

    // ── isEmpty (E0005) ─────────────────────────────────────────────────────

    @Test
    fun `E0005는 isEmpty가 true다`() {
        assertThat(AppError.ApiBadResponse("E0005").isEmpty).isTrue()
    }

    @Test
    fun `E0001은 isEmpty가 false다`() {
        assertThat(AppError.ApiBadResponse("E0001").isEmpty).isFalse()
    }

    // ── isQueryTooBroad (E0006) ─────────────────────────────────────────────

    @Test
    fun `E0006은 isQueryTooBroad가 true다`() {
        assertThat(AppError.ApiBadResponse("E0006").isQueryTooBroad).isTrue()
    }

    @Test
    fun `E0001은 isQueryTooBroad가 false다`() {
        assertThat(AppError.ApiBadResponse("E0001").isQueryTooBroad).isFalse()
    }

    // ── isQueryTooShort (E0008) ─────────────────────────────────────────────

    @Test
    fun `E0008은 isQueryTooShort가 true다`() {
        assertThat(AppError.ApiBadResponse("E0008").isQueryTooShort).isTrue()
    }

    @Test
    fun `E0001은 isQueryTooShort가 false다`() {
        assertThat(AppError.ApiBadResponse("E0001").isQueryTooShort).isFalse()
    }

    // ── isNumericOnly (E0009) ───────────────────────────────────────────────

    @Test
    fun `E0009는 isNumericOnly가 true다`() {
        assertThat(AppError.ApiBadResponse("E0009").isNumericOnly).isTrue()
    }

    @Test
    fun `E0001은 isNumericOnly가 false다`() {
        assertThat(AppError.ApiBadResponse("E0001").isNumericOnly).isFalse()
    }

    // ── isInvalidQuery (E0010 / E0013) ──────────────────────────────────────

    @Test
    fun `E0010은 isInvalidQuery가 true다`() {
        assertThat(AppError.ApiBadResponse("E0010").isInvalidQuery).isTrue()
    }

    @Test
    fun `E0013은 isInvalidQuery가 true다`() {
        assertThat(AppError.ApiBadResponse("E0013").isInvalidQuery).isTrue()
    }

    @Test
    fun `E0001은 isInvalidQuery가 false다`() {
        assertThat(AppError.ApiBadResponse("E0001").isInvalidQuery).isFalse()
    }

    // ── 각 헬퍼는 해당 code에서만 true, 나머지 헬퍼는 false ─────────────────

    @Test
    fun `E0006은 isQueryTooBroad만 true이고 나머지 헬퍼는 false다`() {
        val error = AppError.ApiBadResponse("E0006")
        assertThat(error.isQueryTooBroad).isTrue()
        assertThat(error.isAuthError).isFalse()
        assertThat(error.isPathError).isFalse()
        assertThat(error.isEmpty).isFalse()
        assertThat(error.isQueryTooShort).isFalse()
        assertThat(error.isNumericOnly).isFalse()
        assertThat(error.isInvalidQuery).isFalse()
    }
}
