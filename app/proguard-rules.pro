# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# kotlinx-serialization — Companion·serializer 함수가 reflection으로 호출되므로 보존한다.
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.jewan.zipkr.**$$serializer { *; }
-keepclassmembers class com.jewan.zipkr.** {
    *** Companion;
}
-keepclasseswithmembers class com.jewan.zipkr.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit — interface 메서드·어노테이션이 reflection으로 호출된다.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Hilt — generated component·ViewModel은 보존해야 DI graph가 동작한다.
-keep class dagger.hilt.android.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Timber — annotation 누락 경고를 막는다.
-dontwarn org.jetbrains.annotations.**