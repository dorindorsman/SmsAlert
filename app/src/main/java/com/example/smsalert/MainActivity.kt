package com.example.smsalert

import android.Manifest
import android.content.ContentResolver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.Telephony
import android.view.View
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {

    private var smsContentObserver: SmsContentObserver? = null
    private val CALL_REQUEST_CODE = 369
    private lateinit var conversationThread: HandlerThread
    private lateinit var conversationsHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.RECEIVE_MMS,
                Manifest.permission.READ_PHONE_NUMBERS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS
            ), CALL_REQUEST_CODE
        )

        findViewById<View>(R.id.button).setOnClickListener {
            val contentResolver: ContentResolver = contentResolver
            startThread()
            smsContentObserver = SmsContentObserver(this, Handler(Looper.getMainLooper()), conversationsHandler)
            //smsContentObserver = SmsContentObserver(this, Handler(Looper.getMainLooper()))
            smsContentObserver?.let {
                //it.start()
                contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, it)
                contentResolver.registerContentObserver(Telephony.Mms.CONTENT_URI, true, it)
                contentResolver.registerContentObserver(Telephony.MmsSms.CONTENT_URI, true, it)
            }
        }
    }

    fun startThread() {
        conversationThread = HandlerThread("conversationsThread")
        conversationThread.start()
        conversationsHandler = Handler(conversationThread.looper)
    }

    fun stopThread() {
        conversationThread.quit()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopThread()
        // smsContentObserver?.stop()

    }


}