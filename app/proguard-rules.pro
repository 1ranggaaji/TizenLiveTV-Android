# Proguard rules for Tizen Live TV
-keep class com.tizen.livetv.model.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
