package gini.ohadsa.servicesdemo.utils.notifications

import android.app.Notification
import androidx.core.app.NotificationCompat

interface NotificationHandler {

    fun initNotificationParams(
        channelID: String,
        channelName: String,
        notificationID: Int,
        priority: Int,
        contentText: String? = null,
        style :  NotificationCompat.BigTextStyle? = null,
        icon: Int? = null,
        title: String? = null,
        directTo: Class<*>,
    )

    fun getNotification(): Notification
    fun updateNotification(notificationText: String? = null)

}