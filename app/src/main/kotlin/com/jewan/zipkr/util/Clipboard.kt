package com.jewan.zipkr.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.getSystemService

/**
 * 클립보드 복사 헬퍼이다.
 * Android 13+는 시스템이 자동 토스트를 띄우므로 별도 안내가 중복되지 않게 호출부에서 분기 가능하다.
 */
fun Context.copyToClipboard(
    label: String,
    text: String,
) {
    val manager = getSystemService<ClipboardManager>() ?: return
    manager.setPrimaryClip(ClipData.newPlainText(label, text))
}
