package gini.ohadsa.servicesdemo.bound

import android.app.*
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.AndroidEntryPoint
import gini.ohadsa.servicesdemo.R
import gini.ohadsa.servicesdemo.utils.notifications.NotificationHandler
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val BOUND_TAG = "MyBoundService"

private const val CHANNEL_ID = "gini.ohadsa.servicesdemo.timer.CHANNEL_ID"
private const val CHANNEL_NAME = "gini.ohadsa.servicesdemo.timer.CHANNEL_NAME"
private const val NOTIFICATION_ID = 10_000

@AndroidEntryPoint
class MyBoundService : Service() {

    private var exacter = Executors.newSingleThreadScheduledExecutor()
    private var timer = 0

    private var configurationChange = false
    private var serviceRunningInForeground = false

    @Inject
    lateinit var notification: NotificationHandler

    private val localBinder = LocalBinder()


    private fun initNotificationArgs() {
        notification.initNotificationParams(
            channelID = CHANNEL_ID,
            channelName = CHANNEL_NAME,
            notificationID = NOTIFICATION_ID,
            priority = NotificationCompat.PRIORITY_HIGH,
            contentText = "2 SINIM IM CINOR GADOL",
            icon = (android.R.drawable.star_on),
            title = getString(R.string.app_name),
            directTo = BoundDemoActivity::class.java,
        )
    }


    override fun onBind(intent: Intent?): IBinder {
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent?) {
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {

        if (!configurationChange) {
            initNotificationArgs()
            val notification = notification.getNotification()
            startForeground(NOTIFICATION_ID, notification)
            serviceRunningInForeground = true
        }
        return true

    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTimer()
        return START_NOT_STICKY
    }


    inner class LocalBinder : Binder() {
        internal val service: MyBoundService
            get() = this@MyBoundService
    }

    fun subscribeToTimer() {
        startService(Intent(applicationContext, MyBoundService::class.java))
    }

    fun unsubscribeToTimer() {
        stopTimer()
        stopSelf()
    }

    private fun stopTimer() {
        exacter.shutdown()
    }

    private fun startTimer() {
        exacter = Executors.newSingleThreadScheduledExecutor()
        exacter.scheduleAtFixedRate({
            timer++
            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(
                    Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST).apply {
                        putExtra(TIMER_KEY, "$timer")
                    }
                )
            if (serviceRunningInForeground) notification.updateNotification("$timer")

            Log.d(BOUND_TAG, "timer - $timer")
        }, 0, 1, TimeUnit.SECONDS)
    }


    companion object {
        private const val PACKAGE_NAME = "gini.ohadsa.servicesdemo"
        const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST: String =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"
        const val START = "START_SERVICE"
        const val STOP = "STOP_SERVICE"
        const val BROADCAST_TIMER = "$PACKAGE_NAME.timer"
        const val TIMER_KEY = "TIMER"
    }

}
