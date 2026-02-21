# ============================================
# Debugging - preserve source file and line numbers
# ============================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================
# Kotlin
# ============================================
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod

# Kotlin Metadata (required by Koin for reflection)
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { *; }

# ============================================
# Kotlin Serialization
# ============================================
-keepattributes RuntimeVisibleAnnotations

# Keep @Serializable classes and their generated serializers
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static ** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serializer() on companion objects
-keepclasseswithmembers class **$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# ============================================
# Room
# ============================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers @androidx.room.Entity class * { *; }

# ============================================
# Google Play Services / Maps
# ============================================
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.android.gms.location.** { *; }
-keep interface com.google.android.gms.maps.** { *; }

# ============================================
# Coroutines
# ============================================
-dontwarn kotlinx.coroutines.**

# ============================================
# SQLCipher
# ============================================
-keep class net.zetetic.database.** { *; }

# ============================================
# Compose
# ============================================
-dontwarn androidx.compose.**
