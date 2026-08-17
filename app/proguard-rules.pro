# ── Kotlin Serialization ──────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-keep,allowobfuscation @kotlinx.serialization.Serializable class ** { *; }
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep class kotlinx.serialization.** { *; }

# ── Retrofit ─────────────────────────────────────────────────────────
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }

# ── OkHttp ────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**

# ── Room ─────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class **
-dontwarn androidx.room.paging.**

# ── Moshi (test-only, but needed for ProGuard consistency) ───────────
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }

# ── Coroutines ────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ── App-specific ─────────────────────────────────────────────────────
-keep class de.kikompetenz.app.data.api.** { *; }
-keep class de.kikompetenz.app.data.db.** { *; }
-keep class de.kikompetenz.app.BuildConfig { *; }

# ── Security Crypto (Tink dependency) ────────────────────────────────
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.crypto.tink.**
-keep class com.google.crypto.tink.** { *; }

# ── Misc ──────────────────────────────────────────────────────────────
-dontwarn org.jetbrains.annotations.**
-dontwarn javax.annotation.**
-keep,allowoptimization,allowshrinking class kotlin.Metadata { *; }

# ── Remove logging in release ─────────────────────────────────────────
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
