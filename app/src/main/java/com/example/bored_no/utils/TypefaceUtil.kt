package com.example.bored_no.utils

import android.content.Context
import android.graphics.Typeface
import java.lang.reflect.Field

object TypefaceUtil {
    fun overrideFont(
        context: Context,
        defaultFontNameToOverride: String,
        customFontFileNameInAssets: String
    ) {
        try {
            val customFontTypeface = Typeface.createFromAsset(context.assets, customFontFileNameInAssets)
            val defaultFontTypefaceField: Field = Typeface::class.java.getDeclaredField(defaultFontNameToOverride)
            defaultFontTypefaceField.isAccessible = true
            defaultFontTypefaceField.set(null, customFontTypeface)
        } catch (e: Exception) {  }
    }
}