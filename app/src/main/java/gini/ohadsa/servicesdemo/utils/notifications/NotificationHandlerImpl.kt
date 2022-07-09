package gini.ohadsa.servicesdemo.utils.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import androidx.appcompat.resources.Compatibility.Api18Impl.setAutoCancel
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import gini.ohadsa.servicesdemo.R
import javax.inject.Inject

class NotificationHandlerImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val notificationManager: NotificationManager
) : NotificationHandler {

    private lateinit var channelID: String
    private lateinit var channelName: String
    private var notificationID: Int = -1
    private var priority = -1
    private var contentText: String? = null
    private lateinit var directTo: Class<*>
    private var style: NotificationCompat.BigTextStyle? = null
    private var icon: Int? = null
    private var title: String? = null

    private fun notificationBuilder(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channelID)
            .setContentTitle(context.getString(R.string.app_name))// fun
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(false)
            .setSmallIcon(android.R.drawable.btn_star_big_on)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(priority)
            .setContentText(contentText ?: "")
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(style ?: NotificationCompat.BigTextStyle())
            .setSmallIcon(icon ?: android.R.drawable.ic_menu_view)
            .setContentTitle(title ?: "")


    private val contentIntent by lazy {
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, directTo), // get in constractor
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun initNotificationParams(
        channelID: String,
        channelName: String,
        notificationID: Int,
        priority: Int,
        contentText: String?,
        style: NotificationCompat.BigTextStyle?,
        icon: Int?,
        title: String?,
        directTo: Class<*>,

        ) {
        this.channelID = channelID
        this.channelName = channelName
        this.notificationID = notificationID
        this.priority = priority
        this.contentText = contentText
        this.directTo = directTo
        this.style = style
        this.icon = icon
        this.title = title
    }

    override fun getNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(createChannel())
        }

        return notificationBuilder().build()
    }

    override fun updateNotification(notificationText: String?) {
        contentText = notificationText
        notificationManager.notify(
            notificationID,
            notificationBuilder().build()
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() =
        NotificationChannel(
            channelID,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )


}