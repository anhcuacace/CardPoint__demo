package tunanh.test_app

import android.app.Application
import timber.log.Timber


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
//    val isFinish= MutableStateFlow(false)


}