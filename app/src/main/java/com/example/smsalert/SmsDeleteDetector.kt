package com.example.smsalert

import android.content.Context
import android.provider.Telephony
import androidx.core.database.getStringOrNull
import java.util.*

class SmsDeleteDetector(private val context: Context) {

    private var smsConversations = emptyMap<Int, Int>()
    private var mmsConversations = emptyMap<Int, Int>()
    private var allSms = emptyMap<Int, SmsObject>()

    /**
     * conversations map: < thread id, count of msgs >
     * allSms map : <id, smsobject> -
     * */

    private fun getSmsConversations(): Map<Int, Int> {
        val allSmsConversations = mutableMapOf<Int, Int>()
        context.contentResolver.query(
            Telephony.Sms.Conversations.CONTENT_URI,
            null,
            null,
            null,
            Telephony.Sms.Conversations.DEFAULT_SORT_ORDER
        ).use { cursor ->
            if (cursor == null || !cursor.moveToFirst()) {
                return allSmsConversations
            }
            do {
                val threadId = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.Conversations.THREAD_ID))
                val msgCount = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.Conversations.MESSAGE_COUNT))
                allSmsConversations[threadId] = msgCount
            } while (cursor.moveToNext())
        }

        return allSmsConversations
    }

    private fun getMmsConversations(): Map<Int, Int> {
        val allMmsConversations = mutableMapOf<Int, Int>()
        context.contentResolver.query(
            Telephony.Mms.CONTENT_URI,
            arrayOf("COUNT(thread_id)", "thread_id"),
            "thread_id IS NOT NULL) GROUP BY (thread_id",
            null,
            null
        ).use { cursor ->
            if ( cursor == null || !cursor.moveToFirst() ) {
                return allMmsConversations
            }

            do {
                val msgCount = cursor.getInt(cursor.getColumnIndexOrThrow(cursor.columnNames[0]))
                val threadId = cursor.getInt(cursor.getColumnIndexOrThrow(cursor.columnNames[1]))
                allMmsConversations[threadId] = msgCount
            } while (cursor.moveToNext())
        }

        return allMmsConversations
    }


    fun getDeletedConversationsId(): Set<Int> {
        return deleteSmsConversations() + deleteMmsConversations()
    }

    private fun deleteMmsConversations(): Set<Int> {
        val currentSmsConversation = getSmsConversations()
        val deletedSmsConversations = smsConversations.filter {
            (!currentSmsConversation.containsKey(it.key)) || ((currentSmsConversation[it.key] ?: 0) < it.value)
        }
        smsConversations = currentSmsConversation
        return deletedSmsConversations.keys
    }

    private fun deleteSmsConversations(): Set<Int> {
        val currentMmsConversation = getMmsConversations()
        val deletedMmsConversations = mmsConversations.filter {
            (!currentMmsConversation.containsKey(it.key)) || ((currentMmsConversation[it.key] ?: 0) < it.value)
        }
        mmsConversations = currentMmsConversation
        return deletedMmsConversations.keys
    }


    /**BY SMS*/
    private fun getAllSms(): Map<Int, SmsObject> {
        val allSms = mutableMapOf<Int, SmsObject>()
        var id: Int
        var threadId: Int
        var address: String
        var body: String
        var date: Long
        var type: String
        var readState: String
        var smsObject: SmsObject
        val calendar = Calendar.getInstance()
        val smsCursor = context.contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, Telephony.Sms.DEFAULT_SORT_ORDER)
        smsCursor?.let {
            if (it.moveToFirst()) {
                do {
                    id = it.getInt(it.getColumnIndexOrThrow(Telephony.Sms._ID))
                    threadId = it.getInt(it.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
                    address = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                    body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
                    date = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE))
                    calendar.timeInMillis = date
                    val formatted: String = calendar.time.toString()
                    type = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.TYPE))
                    readState = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.READ))
                    smsObject = SmsObject(id, threadId, address, body, formatted, type, readState)
                    allSms[id] = smsObject
                } while (it.moveToNext())
            }
            it.close()
        }

        val mmsCursor = context.contentResolver.query(Telephony.Mms.CONTENT_URI, null, null, null, Telephony.Sms.DEFAULT_SORT_ORDER)
        mmsCursor?.let {
            if (it.moveToFirst()) {
                do {
                    id = it.getInt(it.getColumnIndexOrThrow(Telephony.Mms._ID))
                    threadId = it.getInt(it.getColumnIndexOrThrow(Telephony.Mms.THREAD_ID))
                    address = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Mms.CREATOR)).toString()
                    body = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Mms.RETRIEVE_TEXT)).toString()
                    date = it.getLong(it.getColumnIndexOrThrow(Telephony.Mms.DATE))
                    calendar.timeInMillis = date
                    val formatted: String = calendar.time.toString()
                    type = it.getString(it.getColumnIndexOrThrow(Telephony.Mms.MESSAGE_BOX))
                    readState = it.getString(it.getColumnIndexOrThrow(Telephony.Mms.READ))
                    smsObject = SmsObject(id, threadId, address, body, formatted, type, readState)
                    allSms[id] = smsObject
                } while (it.moveToNext())
            }
            it.close()
        }

        /**Print For Test*/
//        logToastHelper.showLogMsg(context, "$tag", Telephony.Sms.CONTENT_URI.toString())
//        logToastHelper.showLogMsg(context, "$tag size allSms= ", allSms.size.toString())
//        for (i in allSms) {
//            logToastHelper.showLogMsg(context, "$tag ${i.key}", i.value.toString())
//        }
        return allSms
    }

    fun newSms(): Map<Int, SmsObject> {
        val allSmsCurrent = getAllSms()
        val newSms = allSmsCurrent.filter {
            (!allSms.containsKey(it.key))
        }
        allSms = allSmsCurrent
        return newSms
    }

    fun deleteSms(): Map<Int, SmsObject> {
        val allSmsCurrent = getAllSms()
        val deleteSms = allSms.filter {
            (!allSmsCurrent.containsKey(it.key))
        }
        allSms = allSmsCurrent
        return deleteSms
    }


}