package ru.alzhanov.drinkkollect

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.material.color.DynamicColors

class DrinkKollectApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        Fresco.initialize(this)
    }
}