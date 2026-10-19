# Optimization and Shrinking rules
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Preserve Compose-related attributes
-keepattributes Signature,Annotation*,InnerClasses,EnclosingMethod

# Keep Compose view models and states
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep coil resources
-keep class coil3.** { *; }
-dontwarn coil3.**

# Keep WorkManager classes (Fixes crash in Full Mode)
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# Keep generated resources
-keep class azkarapp.shared.generated.resources.** { *; }

# Optimization for faster startup
-repackageclasses ''
-allowaccessmodification
-overloadaggressively

# Standard Android keep rules are already included by proguard-android-optimize.txt

# Keep Kotlin Serialization
-keepattributes *Annotation*, ElementValuePairs
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-dontwarn kotlinx.serialization.**
-keepclassmembers class * {
    kotlinx.serialization.KSerializer *;
}

# Keep data models and app classes
-keep class com.mahmoud.azkarapp.Zekr { *; }
-keep class com.mahmoud.azkarapp.Hadith { *; }
-keep class com.mahmoud.azkarapp.** { *; }
