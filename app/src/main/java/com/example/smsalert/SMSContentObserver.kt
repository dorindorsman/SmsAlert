package com.example.smsalert

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.Telephony
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*


class SMSContentObserver(context: Context, handler: Handler?) : ContentObserver(handler) {
    private val context: Context = context
    private val logToastHelper: LogToastHelper = LogToastHelper()
    private val TAG: String = javaClass.kotlin.simpleName.toString()
    private var conversations = emptyMap<Int, Int>().toMutableMap()

    private var allSms = emptyMap<Int, SmsObject>().toMutableMap()
    private var isPrint = false

    /**
     * <thread id, num of msgs> count of msgs
     * <id, smsobject> - all the msgs
     *
     *
     * */

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        /**BY Conversations*/
//        uri?.let {
//            if (uri.pathSegments.isNotEmpty()) {
//                getConversations(uri)
//            } else {
//                deleteSmsFromConversations(uri)
//            }
//        }

        /**BY SMS*/
        uri?.let {
            isPrint = false
            if (uri.pathSegments.isNotEmpty()) {
                newSms(uri)
            } else {
                deleteSms(uri)
            }
        }
    }

    /**BY Conversations*/
    private fun getConversations(uri: Uri?) {
        conversations.clear()
        var threadId: Int
        var msgCount: Int
        val cursor = context.contentResolver.query(
            Telephony.Sms.Conversations.CONTENT_URI,
            null,
            null,
            null,
            Telephony.Sms.Conversations.DEFAULT_SORT_ORDER
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    threadId = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.Conversations.THREAD_ID))
                    msgCount = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.Conversations.MESSAGE_COUNT))
                    conversations[threadId] = msgCount
                } while (cursor.moveToNext())
            }
            cursor.close()
        }

        logToastHelper.showLogMsg(context, "$TAG ${uri.toString()} size conversations= ", conversations.size.toString())

        for (i in conversations) {
            logToastHelper.showLogMsg(context, "$TAG ${i.key.toString()}", i.value.toString())
        }

    }

    private fun deleteSmsFromConversations(uri: Uri) {

        val conversationsCopy = conversations.toMutableMap()
        getConversations(uri)

        for (i in conversationsCopy) {
            if (conversations.containsKey(i.key)) {
                if (i.value != conversations[i.key]) {
                    logToastHelper.showLogMsg(context, "$TAG Delete SMS From Conversion ", i.key.toString())
                }
            }
        }
    }


    /**BY SMS*/
    private fun getAllSms(uri: Uri?) {
        allSms.clear()
        var id: Int
        var threadId: Int
        var address: String
        var body: String
        var date: Long
        var type: String
        var status: String
        var readState: String
        var locked: String
        var smsObject: SmsObject
        val formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        val calendar = Calendar.getInstance()
        uri?.let { it ->
            val cursor = context.contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, Telephony.Sms.DEFAULT_SORT_ORDER)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms._ID))
                        threadId = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
                        address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                        body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                        date = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))
                        calendar.timeInMillis = date
                        val formatted: String = calendar.time.toString()
                        type = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE))
                        status = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.STATUS))
                        readState = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.READ))
                        locked = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.LOCKED))
                        Log.d("dorin", "$locked $body")
                        smsObject = SmsObject(id, threadId, address, body, formatted, type, status, readState)
                        allSms[id] = smsObject
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
            logToastHelper.showLogMsg(context, "$TAG ${uri.toString()}", Telephony.Sms.CONTENT_URI.toString())
            logToastHelper.showLogMsg(context, "$TAG ${uri.toString()} size allSms= ", allSms.size.toString())

            for (i in allSms) {
                logToastHelper.showLogMsg(context, "$TAG ${i.key}", i.value.toString())
            }

        }
    }


    private fun newSms(uri: Uri) {
        getAllSms(uri)
        val cursor = context.contentResolver.query(uri, null, null, null, Telephony.Sms.DEFAULT_SORT_ORDER)
        val path: Int? = uri.pathSegments[0].toIntOrNull()
        if (path != null && cursor != null) {
            cursor.moveToFirst()

            /**way 1*/
            Log.d("dorin", cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms._ID)))
            /**way 2*/
            if (cursor.getColumnCount() > 0) {
                val id: String = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms._ID))
                val threadId: String = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
                val body: String = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                val address: String = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                if (cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)) == Telephony.Sms.Inbox.MESSAGE_TYPE_INBOX) {
                    logToastHelper.showLogMsg(context, "$TAG Inbox ID:$id  ThreadId:$threadId ", "SMS From $address : $body")
                } else if (cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)) == Telephony.Sms.Inbox.MESSAGE_TYPE_SENT) {
                    logToastHelper.showLogMsg(context, "$TAG Outbox", "SMS Sent To $address : $body")
                }
            }
            cursor.close()
        }
    }

    private fun deleteSms(uri: Uri) {
        val allSmsCopy = allSms.toMutableMap()
        getAllSms(uri)
        for (i in allSmsCopy) {
            if (!allSms.containsKey(i.key)) {
                logToastHelper.showLogMsg(context, "$TAG SMS Delete ", "SMS ID:" + i.key + " SMS BODY:" + i.value._body)
            }
        }
    }


}
