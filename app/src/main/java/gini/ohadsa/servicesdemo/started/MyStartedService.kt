package gini.ohadsa.servicesdemo.started

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import gini.ohadsa.servicesdemo.R
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

const val STARTED_TAG = "MyService"


class MyStartedService : Service() {
    private val CHANNEL_ID = "gini.ohadsa.servicesdemo.timer.CHANNEL_ID"
    private val CHANNEL_NAME = "gini.ohadsa.servicesdemo.timer.CHANNEL_NAME"
    private val NOTIFICATION_ID = 10_000
    private val exacter = Executors.newSingleThreadScheduledExecutor()
    private var timer = 0
    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }
    private val contentIntent by lazy {
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    private val notificationBuilder: NotificationCompat.Builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setSound(null)
            .setContentIntent(contentIntent)
            .setSmallIcon(android.R.drawable.btn_star_big_on)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentText("2 sinim im cinor gadol ")
            .setAutoCancel(true)
    }


    companion object {
        const val STARTED_START = "START_SERVICE"
        const val STARTED_STOP = "STOP_SERVICE"
        const val STARTED_BROADCAST_TIMER = "gini.ohadsa.servicesdemo.timer"
        const val STARTED_TIMER_KEY = "TIMER"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            if (it == STARTED_START) {
                startTimer()
            } else {
                stopTimer()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_NOT_STICKY

    }

    private fun stopTimer() {
        exacter.shutdown()
    }

    private fun startTimer() {
        exacter.scheduleAtFixedRate({
            timer++
            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(
                    Intent(STARTED_BROADCAST_TIMER).apply {
                        putExtra(STARTED_TIMER_KEY, "$timer")
                    }
                )
            updateNotification("$timer")

            Log.d(STARTED_TAG, "timer - $timer")
        }, 0, 1, TimeUnit.SECONDS)
    }


    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }



    private fun getNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(createChannel())
        }

        return notificationBuilder.build()
    }


    private fun updateNotification(notificationText: String? = null) {
        notificationText?.let { notificationBuilder.setContentText(it) }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() =
        NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )

}