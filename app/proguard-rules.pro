# HearBoost ProGuard Rules
-keep class com.hearboost.audio.** { *; }
-keep class com.hearboost.bluetooth.** { *; }
-keep class com.hearboost.service.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel { <init>(...); }
