package com.example.bored_no

import android.app.Application
import com.example.bored_no.utils.TypefaceUtil

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        TypefaceUtil.overrideFont(
            applicationContext,
            "SANS_SERIF",
            "fonts/Nunito-Regular.ttf"
        )
    }
}