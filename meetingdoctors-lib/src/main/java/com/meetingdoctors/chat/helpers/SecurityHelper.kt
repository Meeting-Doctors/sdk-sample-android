package com.meetingdoctors.chat.helpers

import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Created by Héctor Manrique on 4/7/21.
 */

class SecurityHelper {
    companion object {
        fun md5base64(bytes: ByteArray?): String? {
            try {
                val digest = MessageDigest.getInstance("MD5")
                digest.update(bytes)
                val messageDigest = digest.digest()
                return Base64.encodeToString(messageDigest, Base64.NO_WRAP or Base64.NO_PADDING)
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return null
        }

        @Throws(Exception::class)
        private fun encrypt(key: ByteArray, decrypted: ByteArray): ByteArray {
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"))
            return cipher.doFinal(decrypted)
        }

        @Throws(Exception::class)
        private fun decrypt(key: ByteArray, encrypted: ByteArray): ByteArray {
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"))
            return cipher.doFinal(encrypted)
        }

        fun encrypt(decrypted: String, password: String): String? {
            var encrypted: String? = null
            try {
                val digest = MessageDigest.getInstance("MD5")
                digest.update(password.toByteArray())
                val passwordBytes = digest.digest()
                val decryptedBytes = decrypted.toByteArray()
                val encryptedBytes = encrypt(passwordBytes, decryptedBytes)
                encrypted = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return encrypted
        }

        fun decrypt(encrypted: String?, password: String): String? {
            var decrypted: String? = null
            try {
                val digest = MessageDigest.getInstance("MD5")
                digest.update(password.toByteArray())
                val passwordBytes = digest.digest()
                val encryptedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
                val decryptedBytes = decrypt(passwordBytes, encryptedBytes)
                decrypted = String(decryptedBytes, charset("UTF-8"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return decrypted
        }
    }
}
