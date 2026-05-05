package com.jewan.zipkr.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jewan.zipkr.R
import com.jewan.zipkr.data.Sido

/**
 * Sido enum 값을 i18n string resource로 매핑한다.
 * data 레이어(Sido)는 androidx 의존성을 가지지 않도록 깨끗하게 유지하고,
 * UI 레이어에서 라벨 변환을 책임진다.
 *
 * Map으로 정의해 17개 분기 cyclomatic complexity 누적을 회피한다 (detekt 임계 10).
 * Sido는 enum이라 모든 키가 존재함이 컴파일 타임에 보장되므로 getValue 사용해도 안전하다.
 */
private val SIDO_LABEL_RES: Map<Sido, Int> =
    mapOf(
        Sido.SEOUL to R.string.sido_seoul,
        Sido.GYEONGGI to R.string.sido_gyeonggi,
        Sido.INCHEON to R.string.sido_incheon,
        Sido.BUSAN to R.string.sido_busan,
        Sido.DAEGU to R.string.sido_daegu,
        Sido.GWANGJU to R.string.sido_gwangju,
        Sido.DAEJEON to R.string.sido_daejeon,
        Sido.ULSAN to R.string.sido_ulsan,
        Sido.SEJONG to R.string.sido_sejong,
        Sido.GANGWON to R.string.sido_gangwon,
        Sido.CHUNGBUK to R.string.sido_chungbuk,
        Sido.CHUNGNAM to R.string.sido_chungnam,
        Sido.JEONBUK to R.string.sido_jeonbuk,
        Sido.JEONNAM to R.string.sido_jeonnam,
        Sido.GYEONGBUK to R.string.sido_gyeongbuk,
        Sido.GYEONGNAM to R.string.sido_gyeongnam,
        Sido.JEJU to R.string.sido_jeju,
    )

/**
 * Composable에서 시·도의 짧은 라벨을 가져온다 (UI 칩·앵커용).
 * 디바이스 언어에 따라 ko/en 자동 분기된다.
 */
@Composable
fun Sido.label(): String = stringResource(SIDO_LABEL_RES.getValue(this))
