# kotlinx-serialization
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep,includedescriptorclasses class com.koflox.strava.impl.**$$serializer { *; }
-keepclassmembers class com.koflox.strava.impl.** {
    *** Companion;
}
-keepclasseswithmembers class com.koflox.strava.impl.** {
    kotlinx.serialization.KSerializer serializer(...);
}
