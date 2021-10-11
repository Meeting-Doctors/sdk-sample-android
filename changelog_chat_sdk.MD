CHANGELOG
------------------------
## 1.24.0 (5 Oct 2021)
### Added
    - Add session expired
    
## 1.23.3 (9 Sep 2021)
### Added
    - Add new veterinary speciality

## 1.23.2 (19 Aug 2021)
### Added
    - Fix on push notifications

## 1.23.0 (4 Aug 2021)
#### Added
    - Update firebase dependency to Bom artifact

## 1.22.0 (28 Jul 2021)
#### Added
    - Include new specialties (DOCTOR_GO_HEALTH_ADVISOR, FITNESS COACHING & NUTRITIONAL COACHING)
    - Use proper constant ROOM instead of USER_DATA_KEY for putMessages() method's Cache class


## 1.21.0 (21 Jul 2021)
#### Added
    - Remove firesbase push token at call deauthenticate method
    - Fix push notification when app is closed to open Home Activity and then navigate to chat activity

## 1.20.0 (13 Jul 2021)
#### Added
    - Update Android Api Target to 30(Android 11)
    - Update Okhttp version from 3.14.1 to 4.9.1

## 1.19.2 (1 Jul 2021)
#### Added
    - Fix filter professional list for exclude the last speciality added

## 1.19.1 (21 Jun 2021)
#### Added
    - Fix MH DiseaseDetailActivity's nullability issue

## 1.19.0 (10 Jun 2021)
#### Added
    - SDK's migration from Java to Kotlin

## 1.18.4 (26 May 2021)
#### Added
    - add @keep annotation for Professional entity
    - get "body" string from remoteMeesage in order to show only message text(not full json content)

## 1.18.2 (05 May 2021)
#### Added
    - Add Locale selector from MeetingDoctorsClient newInstance() method

## 1.18.1 (21 April 2021)
#### Added
    - Chatsocket options update: transporOptions setted up to "websocket" mode.

## 1.18.0 (20 April 2021)
#### Added
    - Migrate artifactory publishing process from Bintray ---> Jfrog Artifactory
    - Implement staging build mode

## 1.16.3 (31 March 2021)
#### Added
    No info available.

## 1.16.2 (08 March 2021)
#### Added
    No info available.

## 1.16.1 (03 March 2021)
#### Added
    No info available.

## 1.16.0 (01 March 2021)
#### Added
    - Inject custom navigationView Menu for Medical History main Screen
    - Solved minor bugs
    - Solve duplicate Chat Screen when resuming App from push notification whent it's at top's activity stack

## 1.15.1 (04 January 2021)
#### Added
    - filtering professional categories programmatically works fine now
    - several string resources updated

## 1.15.0 (18 December 2020)
#### Added
    No info available.

## 1.14.0-consumer-proguard (09 December 2020)
#### Added
    - Add MH Flags for visibility option
    - Add new specialities for Professionals

## 1.13.5-consumer_proguard (01 December 2020)
#### Added
    No info available

## 1.13.4 (21 October 2020)
#### Added
    No info available.

## 1.13.3 (20 October 2020)
#### Added
    No info available.

## 1.13.2 (02 October 2020)
#### Added
    No info available.

## 1.12.1 (18 August 2020)
#### Added
    No info available.

## 1.12.0 (10 August 2020)
#### Added
    - Add deauthenticate(OnResetDataListener resetDataListener) method to MeetingDoctorsClient in order to perform proper logout session

## 1.11.2 (05 August 2020)
#### Added
    - fix: bug causes App's crash with bad date formatting at Disease edit screen
    - Add referrals section to Medical History
    - Add 1to1 Videocall option to Chat screen (not available for all)
    - Add invitation code feature
    - Add update user info feature
    - Security improvements
    - Update several dependency version

## 1.6.1 (22 April 2020)
#### Added
    - Fix for NPSRequestActions missing declaration with @Keep annotation

## 1.5.1 (05 February 2020)
#### Added
    - Implemented logout() method to MeetingDoctorsClient class

## 1.4.1 (30 January 2020)
#### Added
    No info available.

## 1.4.0-stable (23 January 2020)
#### Added
    No info available.

## 1.4.0-beta2 (13 January 2020)
#### Added
    No info available.

## 1.4.0-beta (09 January 2020)
#### Added
    No info available.

## 1.3.27 (18 December 2019)
#### Added
    No info available.

## 1.3.26 (04 December 2019)
#### Added
    No info available.
