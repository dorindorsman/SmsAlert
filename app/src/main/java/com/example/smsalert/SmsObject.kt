package com.example.smsalert

data class SmsObject(
    val _id : Int,
    val _threadId : Int,
    val _address : String,
    val _body : String,
    val _date : String,
    val _type : String, // "1" for inbox "2" for outbox
    val _status : String,
    val _readState : String,  //"0" for have not read sms and "1" for have read sms
)


