# ── Cosmica ProGuard rules ────────────────────────────────────────────────────

# Preserve stack traces for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Kotlin ───────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }

# ── Retrofit + OkHttp ────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keepattributes Signature, Exceptions, InnerClasses, EnclosingMethod

# ── Gson ─────────────────────────────────────────────────────────────────────
# Keep all @SerializedName-annotated fields so Gson can (de)serialize DTOs
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.cosmica.app.data.remote.dto.** { *; }

# ── Room ─────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ── Hilt / Dagger ────────────────────────────────────────────────────────────
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* <fields>;
    @dagger.* <fields>;
}
-dontwarn dagger.hilt.**

# ── Coil ─────────────────────────────────────────────────────────────────────
-dontwarn coil.**

# ── Coroutines ───────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**
