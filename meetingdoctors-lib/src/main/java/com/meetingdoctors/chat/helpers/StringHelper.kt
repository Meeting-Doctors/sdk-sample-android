package com.meetingdoctors.chat.helpers

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

/**
 * Created by Héctor Manrique on 4/7/21.
 */

class StringHelper {
    companion object {

        fun urlEncode(string: String?): String? {
            var string = string
            try {
                string = URLEncoder.encode(string, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            return string
        }

        fun parseLongArray(list: String?): List<Long> {
            val longArray = ArrayList<Long>()
            if (list != null) {
                val numbers = list.split(",".toRegex()).toTypedArray()
                for (i in numbers.indices) {
                    if (numbers[i].length > 0) {
                        longArray.add(numbers[i].toLong())
                    }
                }
            }
            return longArray
        }
    }
}
