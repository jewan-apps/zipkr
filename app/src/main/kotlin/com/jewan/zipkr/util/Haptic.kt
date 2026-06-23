package com.jewan.zipkr.util

import android.view.HapticFeedbackConstants
import android.view.View

/**
 * 짧은 햅틱 피드백을 발생시킨다.
 * 즉시성 신호의 일부이며, 시스템 햅틱 비활성 사용자에겐 자동 무시된다.
 */
fun View.lightHaptic() {
    performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
}
