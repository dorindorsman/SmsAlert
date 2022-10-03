package com.example.smsalert

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.Telephony
import kotlin.system.measureTimeMillis

class SMSContentObserver(context: Context, handler: Handler?) : ContentObserver(handler) {
    private val context: Context = context
    private val logToastHelper: LogToastHelper = LogToastHelper()
    private val smsDeleteDetector: SmsDeleteDetector = SmsDeleteDetector(context)
    private val tag: String = javaClass.kotlin.simpleName.toString()

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        /**BY Conversations*/
        byConversations()

        /**BY SMS*/
        bySms()
    }

    private fun byConversations() {
        var deletedConversations: Set<Int>
        val executionTime = measureTimeMillis {
            deletedConversations = smsDeleteDetector.getDeletedConversationsId()
        }

        logToastHelper.showLogMsg(context, tag, "Execution time: $executionTime , SIZE: ${deletedConversations.size}")

        deletedConversations.forEach {
            logToastHelper.showLogMsg(context, "$tag Delete SMS From Conversion ", "<${it}>")
        }
    }

    private fun bySms() {
        newSms()
        deleteSms()
    }

    private fun newSms() {
        val newSms: Map<Int, SmsObject> = smsDeleteDetector.newSms()
        newSms.forEach {
            if ((it.value._type).toInt() == Telephony.Sms.Inbox.MESSAGE_TYPE_INBOX) {
                logToastHelper.showLogMsg(
                    context,
                    "$tag Inbox ID:${it.value._id}  ThreadId:${it.value._threadId} ",
                    "SMS From ${it.value._address} : ${it.value._body}"
                )
            } else if (((it.value._type).toInt()) == Telephony.Sms.Inbox.MESSAGE_TYPE_SENT) {
                logToastHelper.showLogMsg(
                    context,
                    "$tag Outbox ID:${it.value._id}  ThreadId:${it.value._threadId}",
                    "SMS Sent To ${it.value._address} : ${it.value._body}"
                )
            }
        }
    }

    private fun deleteSms() {
        val deletedSms: Map<Int, SmsObject> = smsDeleteDetector.deleteSms()
        deletedSms.forEach {
            logToastHelper.showLogMsg(context, "$tag SMS Delete ", "SMS ID:" + it.key + " SMS BODY:" + it.value._body)
        }
    }

}
