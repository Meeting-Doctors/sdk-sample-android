CHANGELOG
------------------------
## 1.39.5 (03 Nov 2022)
### Added
    - Fix hide divider on Professional List

## 1.39.4 (25 Oct 2022)
### Added
    - Fix english literals Medical History

## 1.39.3 (18 Oct 2022)
### Added
    - Refactor for Medical History Reports
    - Fix: Click on disabled professional
    - Fix: Message design for offline room

## 1.39.0 (10 Oct 2022)
### Added
    - Android Api Migration to Api 31
    
## 1.38.1 (6 Oct 2022)
### Added
    - UI Fixes for Medical History

## 1.38.0 (28 Sep 2022)
### Added
    - UI restyling for Medical History

## 1.37.5 (27 Sep 2022)
### Added
    - Fix: Minor fix to Allergy severity

## 1.37.4 (19 Sep 2022)
### Added
    - Fix: Minor fixes to Referrals

## 1.37.3 (13 Sep 2022)
### Added
    - Fix: Minor fixes to Medical Prescriptions
    - Fix: Problem with socket on the room

## 1.37.1 (9 Sep 2022)
### Added
    - Fix: Add JWT to Medical Prescriptions
    
## 1.37.0 (6 Sep 2022)
### Added
    - New methods exposed to integrators

## 1.36.3 (2 Sep 2022)
### Added
    Fix on prescriptions service

## 1.36.2 (30 Ago 2022)
### Deprecated  --> Use 1.36.3 or above
### Added
    - Add new speciality (Ethology)
    - Fix: Pictures orientation with some devices on chat

## 1.36.0 (25 Ago 2022)
### Deprecated  --> Use 1.36.3 or above
### Added
    - Scroll to bottom feature
    - Fix: Update Date pattern for medical presciptions

## 1.35.3 (23 Ago 2022)
### Added
    - Refactor for Medical Prescriptions

## 1.35.2 (9 Ago 2022)
### Added
    - Fix deeplink open chat and VC 1to1

## 1.35.1 (1 Ago 2022)
### Added
    - Fix open prescription pdf viewer

## 1.35.0 (22 Jul 2022)
### Added
    - UI restyling for Professional Profile
    - Increase chat text size
    - Add loader when send images
    - BMI option for Medical History
    - Floating Menu Button for Chat screen
    - Refactor update profile info service
    - Fix offset for timezone
    - Fix professional list filter with more than one list
    - Fix open chat or not from integrator

## 1.32.4 (08 Jul 2022)
### Added
    - Fix professional item to overview and schedulings
    - Apply several fixes for Araboc languages localization

## 1.32.3 (01 Jun 2022)
### Added
    - Fix schedule not available for offline doctors
    
## 1.32.2 (19 May 2022)
### Added
    - Avoid nullability exception for synchronize call
    - Update Chat icon's colors for ColorPrimary

## 1.32.0 (11 May 2022)
### Added
    - UI restyling for ProfessionalList & Chat screen

## 1.31.0 (05 May 2022)
### Added
    - Add Vc 1to1 deeplink handling
    - BackTitleBar refactor

## 1.30.2 (11 Apr 2022)
### Added
    - feature: Add new specialities (animalNutrition, medicalManager & coachMental)
    - fix: update medical-derivations servive contract
    - refactor: add description field to UpdateProfileInfo service contract

## 1.30.0 (11 Apr 2022)
### Added
    - refactor: new Professional List HTTP based
    - fix: fix: allow permission to write on SHARED FOLDERS for Chat attaching files
    - fix: refresh JWT in order to do proper deauthenticate()

## 1.29.4 (21 Mar 2022)
### Added
    - fix: set MedicalHistoryFragment Clickable & Focusable

## 1.29.3 (10 Mar 2022)
### Added
    - fix: fix notification text language

## 1.29.2 (28 Feb 2022)
### Added
    - refactor: fix arabish dates parsing to post/put medical condition

## 1.29.1 (28 Feb 2022)
### Added
    - Update Data's Encryptation Protocol

## 1.29.0 (7 Feb 2022)
### Added
    - Create MedicalHistory Fragment
    - Add Arabic literals
    - Delete unused InstallReferrerReceiver

## 1.27.0 (30 Nov 2021)
### Added
    - Add welcome message feature
    - fixed bad url for Vc reports endpoint
    - fixed bad gradle environment variable VERSION_NAME --> SDK_CHAT_VERSION

## 1.26.1 (05 Nov 2021)
### Added
    - Refactor open_url pushes contract
    - Avoid null class for Intent for show message received notifications

## 1.26.0 (22 Oct 2021)
### Added
    - Added Screenshots capture feature

## 1.25.0 (20 Oct 2021)
### Added
    - Refactor for meetingdoctors-lib submodule at core level (.gradle files)
    - Fix for specialities filtering for including specialities.specialities.specialities
    - Fix for desynchronized messages for room with 100+ messages

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
