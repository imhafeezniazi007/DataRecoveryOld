package com.example.recoverydata.utils

import java.util.Date

class MyObject : Comparable<MyObject> {
    var dateTime: Date? = null
    override fun compareTo(o: MyObject): Int {
        return dateTime!!.compareTo(o.dateTime)
    }
}