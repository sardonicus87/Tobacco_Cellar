# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

-keep class com.sardonicus.tobaccocellar.data.** { *; }
-keep class com.sardonicus.tobaccocellar.ui.settings.SettingsViewModelKt { *; }
-keep class com.sardonicus.tobaccocellar.data.TobaccoDatabaseKt { *; }
-keep class com.sardonicus.tobaccocellar.data.PreferencesRepo { *; }
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings
#-keepnames class kotlinx.serialization.SerializersKt
#-keep class *$$serializer { *; }
#-keep class * { @kotlinx.serialization.Serializable <fields>; }
#-keepclassmembers class * { @kotlinx.serialization.Serializable <fields>; }

#-keepclassmembers class ** {
#    @kotlinx.serialization.descriptors.SerialDescriptor Companion;
#}

#-keep class edu.umd.cs.findbugs.annotations.** { *; }
#-keep interface edu.umd.cs.findbugs.annotations.** { *; }

#-keep public class com.sardonicus.tobaccocellar.ui.settings.** { public *; }
#-keep public class com.sardonicus.tobaccocellar.ui.settings.SettingsViewModel { public *; }
