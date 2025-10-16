package id.co.app.pocpenalty

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
    }
    companion object {
        @JvmStatic
        fun getContext(): Context {
            return this.getContext()
        }
    }
}