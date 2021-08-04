# Videocall SDK - Integration Steps #

* Add repository. At Top Project's build.gradle file
``` gradle
repositories {
    maven {
                maven { url "https://meetingdoctors.jfrog.io/artifactory/Android/" }
                maven { url 'https://search.maven.org/artifact/com.opentok.android/opentok-android-sdk' }
    }
}
```

* Declare library in dependencies section. At app module gralde file
``` gradle
implementation 'com.meetingdoctors:videocall-sdk:1.9.1'
```

In order to check the last updates please check our changelog.

### Initialization ###

* Initialize library on your Application.onCreate(). apiKey is a constant string provided by MeetingDoctors.
Installation guid PENDING
``` java
        VideoCallClient.INSTANCE.initialize(this, <YOUR_API_KEY>, <YOUR_INSTALLATION_GUID>,
        new VideoCallClient.InitResponseListener() {

            @Override
            public void onInitSuccess() {
            	/* YOUR CODE HERE */
            }

            @Override
            public void onInitFailure(@Nullable String s) {
            	/* YOUR CODE HERE */
            }
        },
        //You must set a value from enum class VideoCallBuildMode
        <BUILD_MODE_ENVIRONMENT> VideoCallBuildMode.DEV/STAGING/PROD,
        // If you do not want to set specific locale just set <LOCALE> to null,
        //Locale configuration will be handled by Android system as usual
        //e.g. Locale locale = Locale("es")
        <LOCALE> "yourLocaleLanguage");

        Intent intent = new Intent(this, YourActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //OPTIONAL FLAG, CAN SET YOUR CUSTOM
        VideoCallClient.INSTANCE.setReturnIntent(intent);

```

* InstalllationGuid is an unique identifier  required to initialize, for a clean way to generate it, check snippet below:
``` java
    UUID.nameUUIDFromBytes((
                    Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID) +
                            Build.VERSION.RELEASE +
                            Build.MODEL +
                            getApiKey() +
                            Math.random()
            ).getBytes()).toString();
```

### User Login ###

* Before perform a video call , you must login. userToken is a constant string that will allow you to validate user from your own server.
``` java
VideoCallClient.INSTANCE.login(
                <YOUR_USER_TOKEN>,
                new VideoCallClient.LoginResponseListener() {
                    @Override
                    public void onLoginSuccess() {
                    	/* Usually you can start the video call from this point*/
                        VideoCallClient.INSTANCE.openCall(new VideoCallClient.RequestCallResponseListener() {

                            @Override
                            public void onRequestCallSuccess() {
                            	/* Your code here */
                            }
                            @Override
                            public void onRequestCallFailure(@Nullable String s) {
                                /* Your code here */
                            }
                        });
                    }

                    @Override
                    public void onLoginFailure(@Nullable String s) {
                        /* Your code here */
                    }
                }
        );
```

### Exposed Methods ###
* you can open a new video call just calling to 'openCall()' method:
```
	VideoCallClient.INSTANCE.openCall(new VideoCallClient.RequestCallResponseListener() {

                            @Override
                            public void onRequestCallSuccess() {
                            	/* Your code here */
                            }
                            @Override
                            public void onRequestCallFailure(@Nullable String s) {
                                /* Your code here */
                            }
                        });
```

* Also you can check for any current pending call in course, with 'checkPendingCall()':
```
	VideoCallClient.INSTANCE.checkPendingCall(new VideoCallClient.FetchCurrentCallListener() {
            @Override
            public void onSuccess(@Nullable CurrentConsultationResponse currentConsultationResponse) {
                if(currentConsultationResponse.getData() != null) {
                    resumeVideoCall(); /* from this point you can call to 'openCall()' */
                }
            }
            @Override
            public void onFailure(@Nullable String s) { }
        });
```

### 1to1 videocall implementation (from Videocall SDK v1.1.0) ###
* NOTE: to perform a successful 1to1 video call Customer Chat SDK v1.11.2 or newer IS REQUIRED

* First step is to implement OnVideoCallRequest interface in your current activity (e.g. MainActivity
```
	public class <YOUR_ACTIVITY> extends Activity implements MeetingDoctorsClient.OnVideoCallRequest {
    	....
        MeetingDoctorsClient.Companion.getInstance().setVideoCallRequestListener(this);
        ....
    }
```

* Next step is to apply required logic to OnVideoCallRequest to perfor a 1to1 videocall successfully
```
	@Override
    public void perform1to1VideoCall(String professionalHash,
                                     String doctorName, Context context,
                                     OnVideoCall1to1RequestDone videoCallRequestDoneListener) {
        VideoCallClient.INSTANCE.login(
                <YOUR_USER_TOKEN>,
                new VideoCallClient.LoginResponseListener() {
                    @Override
                    public void onLoginSuccess() {

                        VideoCallClient.INSTANCE.requestOneToOneCall(context, professionalHash, doctorName, new VideoCallClient.RequestOneToOneCallListener() {
                            @Override
                            public void onRequestOneToOneCallSuccess(@NotNull String roomId) {
                                if (videoCallRequestDoneListener != null) {
                                    videoCallRequestDoneListener.performRequestDoneAction(roomId);
                                }
                            }

                            @Override
                            public void onRequestOneToOneCallCancelledPrevious(@NotNull String roomId) {
                                if (videoCallRequestDoneListener != null) {
                                    videoCallRequestDoneListener.performSendCancelledCallMessage(roomId, null);
                                }
                            }

                            @Override
                            public void onRequestOneToOneCallFailure(@Nullable String message) {}
                        });
                    }

                    @Override
                    public void onLoginFailure(@Nullable String s) {}
                }
        );
    }

    @Override
    public void performCancelVideoCall(Context context, String message, OnVideoCall1to1RequestDone videoCallRequestDoneListener, String doctorName) {
        VideoCallClient.INSTANCE.requestCancelCallCustomer(context, doctorName, new VideoCallClient.CancelCallResponseListener() {
            @Override
            public void onCancelCallSuccess() {
                if (videoCallRequestDoneListener != null) {
                    videoCallRequestDoneListener.performRequestCancelledAction();
                }
            }

            @Override
            public void onCancelCallCancelledPrevious(@NotNull String roomId, String hash) {
                if (videoCallRequestDoneListener != null) {
                    videoCallRequestDoneListener.performSendCancelledCallMessage(roomId, hash);
                }
            }

            @Override
            public void onCancelCallFailure(@Nullable String message) {}
        });
    }

    @Override
    public boolean hasProfessionalAssignedVideoCall(String professionalHash) {
        return VideoCallClient.INSTANCE.hasAssignedCall() && VideoCallClient.INSTANCE.hasProfessionalAssignedCall(professionalHash);
    }
```

### Push Notifications for Apps with Firebase ###
* If your project has not Firebase dependency implemented, you need to declare it inside your app Build.gradle file:
``` gradle
implementation "com.google.firebase:firebase-core:${FIREBASE_CORE_VERSION}"
```

* Add the following code in your FirebaseMessagingService class:
``` java
public class YourFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        MDVideoCallFirebaseMessagingService.Companion.onMessageReceived(remoteMessage);

        /* your code here */
    }
}
```

* If your App update your Firebase Token, you must update to our SDK, you can do this the following way, e.g. in your FirebaseMessagingService class:
``` java
public class YourFirebaseMessagingService extends FirebaseMessagingService {
    ....
        @Override
    public void onNewToken(String token) {
        Log.i("FCM", "onNewToken() New token received on app");
        super.onNewToken(token);
        VideoCallClient.INSTANCE.onNewTokenReceived(token);
    }
}
```

### Customization ###
* PreCallActivity styles canbe customized with your app's color styles, its customization is based on following color resources:
``` xml
    <color name="colorPrimary" tools:ignore="ResourceName"><YOUR_PRIMARY_COLOR></color>
    <color name="colorPrimaryLight" tools:ignore="ResourceName"><YOUR_PRIMARY_LIGHT_COLOR></color>
```

![divider](https://drive.google.com/open?id=1CAOsO0Jh00jhaih3tsrEUxNXlOwIlYFb)

### ProGuard settings ###
* If you're using version 1.2.0 or previous,add them in your ProGuard settings. **Only required if you're obfuscating your app, ignore otherwise**. Version 1.3.0 & above not need to add proguard rules to your project because are embedded inside SDK.

```
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keep class com.opentok.** { *; }
-dontwarn com.opentok.**
-keep class org.webrtc.** { *; }

-keep class com.meetingdoctors.** { *; }
-dontwarn com.meetingdoctors.**
-keepnames @kotlin.Metadata class com.meetingdoctors.**
-keepclassmembers class com.meetingdoctors.** { *; }

##---------------Begin: OkHttp, Retrofit and Moshi
-dontwarn okhttp3.**
-dontwarn retrofit2.Platform$Java8
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *
-dontwarn org.jetbrains.annotations.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-keep public class kotlin.reflect.jvm.internal.impl.** { public *; }
##---------------End: OkHttp, Retrofit and Moshi
```