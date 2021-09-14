package com.meetingdoctors.chat.data


import android.content.Context
import com.google.gson.Gson
import com.meetingdoctors.chat.BuildConfig
import com.meetingdoctors.chat.domain.entities.Doctor
import com.meetingdoctors.chat.domain.entities.Setup
import com.meetingdoctors.chat.domain.entities.UserData
import com.meetingdoctors.chat.helpers.SecurityHelper.Companion.decrypt
import com.meetingdoctors.chat.helpers.SecurityHelper.Companion.encrypt
import com.meetingdoctors.chat.helpers.SystemHelper.Companion.getVersionCode


/**
 * Created by Héctor Manrique on 4/9/21.
 */

class Cache {

    companion object {

        private const val DOCTORS_KEY = "doctors"
        private const val USER_DATA_KEY = "userData"
        private const val ROOM = "room"
        private const val SETUP = "setup"
        private const val CACHE_VERSION = "cacheVersion"
        fun clear(context: Context) {
            clear(context, DOCTORS_KEY)
            clear(context, USER_DATA_KEY)
        }

        /**/
        fun putDoctors(context: Context, doctors: List<Doctor>?) {
            val doctorsContainer = DoctorsContainer()
            doctorsContainer.doctors = doctors
            put(context, DOCTORS_KEY, Gson().toJson(doctorsContainer))
        }

        fun getDoctors(context: Context): List<Doctor>? {
            val doctorsContainer = Gson().fromJson(get(context, DOCTORS_KEY), DoctorsContainer::class.java)
            return doctorsContainer?.doctors
        }

        /**/
        fun putUserData(context: Context, userData: UserData?) {
            val userDataContainer = UserDataContainer()
            userDataContainer.userData = userData
            put(context, USER_DATA_KEY, Gson().toJson(userDataContainer))
        }

        fun getUserData(context: Context): UserData? {
            val userDataContainer = Gson().fromJson(get(context, USER_DATA_KEY), UserDataContainer::class.java)
            return userDataContainer?.userData
        }

        /**/
        fun putMessages(context: Context, roomId: Int, messages: List<Message>?) {
            val messagesContainer = MessagesContainer()
            messagesContainer.messages = messages
            put(context, ROOM + roomId, Gson().toJson(messagesContainer))
        }

        fun getMessages(context: Context, roomId: Int): List<Message>? {
            val messagesContainer = Gson().fromJson(get(context, ROOM + roomId), MessagesContainer::class.java)
            return messagesContainer?.messages
        }

        /**/
        fun putSetup(context: Context, setup: Setup?) {
            val setupContainer = SetupContainer()
            setupContainer.setup = setup
            put(context, SETUP, Gson().toJson(setupContainer))
        }

        fun getSetup(context: Context): Setup? {
            val setupContainer = Gson().fromJson(get(context, SETUP), SetupContainer::class.java)
            return setupContainer?.setup
        }

        /**/
        private fun put(context: Context, name: String, data: String) {
            val installationGuid = Repository.instance?.getInstallationGuid()
            installationGuid?.let {
                val editor = context.getSharedPreferences(BuildConfig.FLAVOR + "_" + name, Context.MODE_PRIVATE).edit()
                editor.putInt(CACHE_VERSION, getVersionCode(context)!!)
                editor.putString(name, encrypt(data, it))
                editor.commit()
            }
        }

        private operator fun get(context: Context, name: String): String? {
            var decrypted: String? = null
            val sharedPreferences = context.getSharedPreferences(BuildConfig.FLAVOR + "_" + name, Context.MODE_PRIVATE)
            if (sharedPreferences.getInt(CACHE_VERSION, 0) == getVersionCode(context)) {
                val encrypted = sharedPreferences.getString(name, null)
                val installationGuid = Repository.instance?.getInstallationGuid()
                if (encrypted != null && installationGuid != null) {
                    decrypted = decrypt(encrypted, installationGuid)
                }
            }
            return decrypted
        }

        private fun clear(context: Context, name: String) {
            val editor = context.getSharedPreferences(BuildConfig.FLAVOR + "_" + name, Context.MODE_PRIVATE).edit()
            editor.remove(name)
            editor.commit()
        }

        /**/
        private class DoctorsContainer {
            var doctors: List<Doctor>? = null
        }

        private class UserDataContainer {
            var userData: UserData? = null
        }

        private class MessagesContainer {
            var messages: List<Message>? = null
        }

        private class SetupContainer {
            var setup: Setup? = null
        }
    }
}
