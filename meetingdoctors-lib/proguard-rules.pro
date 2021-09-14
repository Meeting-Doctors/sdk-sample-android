-obfuscationdictionary proguard-dictionary-keywords.txt
-classobfuscationdictionary proguard-dictionary-keywords.txt
-packageobfuscationdictionary proguard-dictionary-keywords.txt
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-dontskipnonpubliclibraryclasses
-dontusemixedcaseclassnames
-repackageclasses '$'
-allowaccessmodification
-optimizationpasses 5
-verbose

# Keep the BuildConfig
#-keep class com.meetingdoctors.chat.BuildConfig { *; }

# Backward StackTraces
-keepattributes SourceFile, LineNumberTable

# Preserve some attributes that may be required for reflection.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService
-keep public class com.google.android.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService
-dontnote com.google.vending.licensing.ILicensingService
-dontnote com.google.android.vending.licensing.ILicensingService

-keepclassmembers class **.R$* {
    public static <fields>;
}

# The support libraries contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version. We know about them, and they are safe.
-dontnote android.support.**
-dontnote androidx.**
-dontwarn android.support.**
-dontwarn androidx.**

# Keep setters in Views so that animations can still work.
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# We want to keep methods in Activity that could be used in the XML attribute onClick.
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# This class is deprecated, but remains for backward compatibility.
-dontwarn android.util.FloatMath

##---------------Begin: Kotlin and JetBrains classes  ----------

-keep class kotlin.** { *; }
-keep class org.jetbrains.** { *; }

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
  static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
  static void checkFieldIsNotNull(java.lang.Object, java.lang.String);
  static void checkFieldIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
  static void checkNotNull(java.lang.Object);
  static void checkNotNull(java.lang.Object, java.lang.String);
  static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
  static void checkNotNullParameter(java.lang.Object, java.lang.String);
  static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
  static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String);
  static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
  static void throwUninitializedPropertyAccessException(java.lang.String);
}

##---------------Begin: Kotlin and JetBrains classes  ----------

##---------------Begin: Dynamic classes  ----------

#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgentHelper
#-keep public class * extends android.preference.Preference
#-keep public class com.android.vending.licensing.ILicensingService

##---------------End: Dynamic classes  ----------


##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
##---------------End: proguard configuration for Gson  ----------

##---------------Begin: proguard configuration for squareup libs  ----------
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod, Exceptions

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Retrofit annotations.
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8

# Disable affected warnings.
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn javax.annotation.concurrent.GuardedBy

##---------------End: proguard configuration for squareup libs  ----------

##---------------Begin: proguard configuration for RX  ----------

# Keep RX indexes.
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
     long producerIndex;
     long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
     long producerNode;
     long consumerNode;
}

-dontwarn java.util.concurrent.Flow*
##---------------End: proguard configuration for RX  ----------

##---------------Begin: proguard configuration for Glide libs  ----------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * implements com.bumptech.glide.request.target.Target
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
-dontwarn com.bumptech.glide.RequestBuilder
-keep public class com.bumptech.glide.RequestBuilder

# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
##---------------End: proguard configuration for Glide libs  ----------

##---------------Begin: proguard configuration for ocpsoft-pretty-time  ----------
-keep class org.ocpsoft.* { *; }
-dontwarn org.ocpsoft.**
-keep class org.ocpsoft.prettytime.i18n**
##---------------End: proguard configuration for ocpsoft-pretty-time  ----------

##---------------Begin: proguard configuration for @Keep support annotation  ----------
-keep class androidx.annotation.Keep

-keep @androidx.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}
##---------------End: proguard configuration for @Keep support annotation  ----------

##---------------Begin: hide logs  ----------
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** d(...);
    public static *** e(...);
}
##---------------End: hide logs  ----------