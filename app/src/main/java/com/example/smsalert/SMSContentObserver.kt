package com.example.smsalert

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.Telephony
import android.util.Log


class SMSContentObserver(context: Context, handler: Handler?) : ContentObserver(handler) {
    private val context: Context = context
    private val logToastHelper: LogToastHelper = LogToastHelper()
    private val TAG: String = javaClass.kotlin.simpleName.toString()
    private var conversations = emptyMap<Int, Int>().toMutableMap()
    private var allSms = emptyMap<Int, SmsObject>().toMutableMap()


    /**
     * <thread id, num of msgs> count of msgs
     * <id, smsobject> - all the msgs
     *
     *
     * */


    private fun getSms(uri: Uri?) {
        conversations.clear()
        var id: Int
        var threadId: Int
        var address: String
        var body: String
        var date: String
        var smsObject: SmsObject
        uri?.let { it ->
            val cursor = context.contentResolver.query(it, null, null, null, null)
//            val path: Int? = uri.pathSegments[0].toIntOrNull()
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms._ID))
                        threadId = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
                        address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                        body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                        date = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))
                        smsObject = SmsObject(id, threadId, address, body, date)
                        allSms[id] = smsObject
                        if (conversations.containsKey(threadId)) {
                            conversations[threadId] = conversations.getValue(threadId).inc()
                        } else {
                            conversations[threadId] = 1
                        }
                    }while(cursor.moveToNext())
                }
                cursor.close()
            }
        }
        Log.d("size ", conversations.size.toString())
    }


    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        getSms(uri)


//        uri?.let {
//            if (uri.pathSegments.isNotEmpty()) {
//                newSms(uri)
//            } else {
//                deleteSms(uri)
//            }
//        }
    }


    private fun newSms(uri: Uri) {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        val path: Int? = uri.pathSegments[0].toIntOrNull()
        if (path != null && cursor != null) {
            cursor.moveToFirst()
            if (cursor.getColumnCount() > 0) {
                val id: String = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms._ID))
                val threadId: String = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
                val body: String = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                val address: String = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                if (cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)) == Telephony.Sms.Inbox.MESSAGE_TYPE_INBOX) {
                    logToastHelper.showLogMsg(context, "SMS From $address : $body", TAG + " Inbox " + id + " " + threadId)
                } else if (cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)) == Telephony.Sms.Inbox.MESSAGE_TYPE_SENT) {
                    logToastHelper.showLogMsg(context, "SMS Sent To $address : $body", TAG + " Outbox")
                }
            }
            cursor.close()
        }
    }

    private fun deleteSms(uri: Uri) {
        val allSmsCopy = allSms
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            if (cursor.getColumnCount() > 0) {
                cursor.moveToFirst()
                while (cursor.moveToNext()) {
                    if (allSmsCopy.containsKey(cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms._ID)))) {
                        allSmsCopy.remove(cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms._ID)))
                    }
                }
                Log.d("dorin dorsman", allSmsCopy.size.toString())
                Log.d("dorin dorsman", allSms.size.toString())
                cursor.close()
            }
        }
    }
}
