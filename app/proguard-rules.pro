# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class com.workshop.mat.data.model.** { *; }
-keepclassmembers class com.workshop.mat.data.model.** { *; }

# Gson
-keep class com.google.gson.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
