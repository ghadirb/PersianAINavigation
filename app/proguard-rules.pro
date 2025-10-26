# Add project specific ProGuard rules here.
-keep class ir.navigation.persian.ai.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.api.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn com.google.android.gms.**
-dontwarn com.google.api.**
