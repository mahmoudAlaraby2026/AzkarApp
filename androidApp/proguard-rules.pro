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
