package com.tzsafe.tazantagwritter.app

import android.app.Application
import android.content.Context
import com.tzsafe.tazantagwritter.entity.MyObjectBox
import io.objectbox.BoxStore

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ObjectBox.init(this)
    }




}

object ObjectBox {
    lateinit var boxStore: BoxStore
        private set

    fun init(context: Context) {
        boxStore = MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .build()
    }
}