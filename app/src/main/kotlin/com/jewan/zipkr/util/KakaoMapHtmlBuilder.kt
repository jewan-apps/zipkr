package com.jewan.zipkr.util

import com.jewan.zipkr.data.Coordinate

private const val DEFAULT_ZOOM_LEVEL = 3

/**
 * WebView에 주입할 카카오맵 web SDK HTML 문자열을 만든다.
 * baseUrl은 호출자(`WebView.loadDataWithBaseURL`)에서 https://localhost를 사용해야 한다 — 카카오 도메인 검사 통과.
 *
 * @param coord 마커가 찍힐 좌표 (WGS84)
 * @param jsKey 카카오 JavaScript 키 (BuildConfig.KAKAO_JS_KEY)
 * @param level 카카오맵 줌 레벨 (1=가장 가까움, 14=가장 멈). 3 = 동네 단위 default.
 */
fun buildKakaoMapHtml(
    coord: Coordinate,
    jsKey: String,
    level: Int = DEFAULT_ZOOM_LEVEL,
): String =
    """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
      <style>
        html, body { margin: 0; padding: 0; width: 100vw; height: 100vh; }
        #map { width: 100vw; height: 100vh; }
      </style>
      <script src="//dapi.kakao.com/v2/maps/sdk.js?appkey=$jsKey&autoload=false"></script>
    </head>
    <body>
      <div id="map"></div>
      <script>
        kakao.maps.load(function() {
          var center = new kakao.maps.LatLng(${coord.latitude}, ${coord.longitude});
          var map = new kakao.maps.Map(document.getElementById('map'), {
            center: center,
            level: $level
          });
          new kakao.maps.Marker({ position: center, map: map });
          map.addControl(new kakao.maps.ZoomControl(), kakao.maps.ControlPosition.RIGHT);
        });
      </script>
    </body>
    </html>
    """.trimIndent()
