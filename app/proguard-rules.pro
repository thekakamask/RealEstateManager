# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep classes with Firestore annotations
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault

# Prevent obfuscation of classes used with Firebase Firestore
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}

# Keep Firestore model classes
-keepclassmembers class * {
    public <init>(...);
}

# Keep all @Keep-annotated classes and members
-keep @androidx.annotation.Keep class * {*;}
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Keep Google Maps
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }

# Keep Firebase
-keep class com.google.firebase.** { *; }

# Keep Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.** { *; }
-keep class hilt_aggregated_deps.** { *; }

# Keep Room entities and DAO
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Keep Retrofit models
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }

# Keep Kotlin serialization
-keep class kotlinx.serialization.** { *; }
-keep class kotlinx.serialization.json.** { *; }
-keep class **$$serializer { *; }

# Keep parcelable / reflection
-keepclassmembers class * {
    @android.os.Parcelable$Creator *;
}