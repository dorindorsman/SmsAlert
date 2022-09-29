package com.example.smsalert

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.provider.Telephony
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {

    private var mHandler: MyHandler? = null
    private var smsContentObserver: SMSContentObserver? = null
    private val SMS_REQUEST_CODE = 111
    private val MMS_REQUEST_CODE = 105
    private val READ_PHONE_REQUEST_CODE = 111
    private val CALL_REQUEST_CODE = 369

    init {
        mHandler = MyHandler(this)
        smsContentObserver = SMSContentObserver(this, mHandler)
    }

    class MyHandler(activity: MainActivity?) : Handler() {
        private val softReference: WeakReference<MainActivity> = WeakReference(activity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.READ_PHONE_STATE) , CALL_REQUEST_CODE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS), SMS_REQUEST_CODE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_MMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_MMS), MMS_REQUEST_CODE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_NUMBERS), READ_PHONE_REQUEST_CODE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.READ_CALL_LOG), READ_PHONE_REQUEST_CODE)
        }


        val contentResolver : ContentResolver = contentResolver
        smsContentObserver?.let { contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true,it)}

    }
}