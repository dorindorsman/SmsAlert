package com.example.smsalert

import android.Manifest
import android.content.ContentResolver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.view.View
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {

    //private lateinit var smsContentObserver: SMSContentObserver
    private val CALL_REQUEST_CODE = 369

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //smsContentObserver = SMSContentObserver(this, Handler(Looper.getMainLooper()))
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
            contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, SmsContentObserver(this, Handler(Looper.getMainLooper())))
            contentResolver.registerContentObserver(Telephony.Mms.CONTENT_URI, true, SmsContentObserver(this, Handler(Looper.getMainLooper())))
            contentResolver.registerContentObserver(Telephony.MmsSms.CONTENT_URI, true, SmsContentObserver(this, Handler(Looper.getMainLooper())))
        }


    }
}