package gini.ohadsa.servicesdemo

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppManager : Application() {

    override fun onCreate() {
        super.onCreate()
        SHARED_PREFERENCES = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    companion object {

        lateinit var SHARED_PREFERENCES: SharedPreferences
        const val DB_LAST_UPDATE = "LAST_UPDATE"
    }


}


private const val SHARED_PREFERENCES_NAME = "TMDB"

