# MeetingDoctors Integration Sample


This is a Sample for MeetingDoctors SDK's integration process(Chat & Videocall). When App is compiled and running at your device,
an initial screen with 2 options is show:

- Run standard Videocall Pool request process.
- Launch professional list chat screen, here you can chat with professionals of your company, and eventually, if your user has proper
  permissions, run a Videocall 1-to-1 request process with the professional you choose. You can choose this option from toolbar menu,
  located at right top corner inside chat screen.

You will see a bottom navigation bar, with two options:

- Medical History
- Logout action: This action will logout/deauthenticate your user from SDK's and will return to inital Screen to start again videocall pool request or enter again to professional list chat screen.

The only action need to do by developer side is to configure the user token and your company Api key. You can hardcode them directly
to code, but its recommended to set environment variables from Local.properties file created inside project. Environment variables
must have following names:

- API_KEY=<YOUR_API_KEY>
- USER_TOKEN=<YOUR_USER_TOKEN>

This way the project acces directly this properties & configure SDK's initialization automatically.

It is recommended too to configure an encryption password for SharedPreferences security, this variable must have the following name:

- sharedPreferencesEncryptionPass=<YOUR_ENCRYPTION_PASSWORD>

That's all the configuration needed by developer side.
