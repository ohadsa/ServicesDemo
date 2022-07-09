package gini.ohadsa.servicesdemo.location

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.components.SingletonComponent
import gini.ohadsa.servicesdemo.R
import gini.ohadsa.servicesdemo.utils.SharedPreferenceUtil
import gini.ohadsa.servicesdemo.utils.notifications.NotificationHandler
import gini.ohadsa.servicesdemo.utils.toText
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundOnlyLocationService : Service() {

    private var configurationChange = false
    private var serviceRunningInForeground = false
    private val localBinder = LocalBinder()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null


    lateinit var notificationHandler: NotificationHandler

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MyEntryPoint{
        fun getNotification() : NotificationHandler
    }

    override fun onCreate() {

        Log.d(TAG, "onCreate()")
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        notificationHandler = EntryPoints.get(applicationContext , MyEntryPoint::class.java).getNotification()
        locationRequest = LocationRequest.create().apply {
            interval = 300
            fastestInterval = 150
            maxWaitTime = 600
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                super.onLocationResult(locationResult)
                currentLocation = locationResult.lastLocation
                val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                intent.putExtra(EXTRA_LOCATION, currentLocation)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                if (serviceRunningInForeground) {
                    notificationHandler.updateNotification(currentLocation.toText())
                }
            }
        }

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")
        val cancelLocationTrackingFromNotification =
            intent.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)

        if (cancelLocationTrackingFromNotification) {
            unsubscribeToLocationUpdates()
            stopSelf()
        }
        // Tells the system not to recreate the service after it's been killed.
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind()")

        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Log.d(TAG, "onRebind()")

        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind()")
        if (!configurationChange && SharedPreferenceUtil.getLocationTrackingPref(this)) {
            Log.d(TAG, "Start foreground service")
            startForeground(NOTIFICATION_ID, generateNotification(currentLocation))
            serviceRunningInForeground = true
        }
        return true
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    inner class LocalBinder : Binder() {
        internal val service: ForegroundOnlyLocationService
            get() = this@ForegroundOnlyLocationService
    }

    fun subscribeToLocationUpdates() {
        Log.d(TAG, "subscribeToLocationUpdates()")
        SharedPreferenceUtil.saveLocationTrackingPref(this, true)

        startService(Intent(applicationContext, ForegroundOnlyLocationService::class.java))
        try {
            // TODO: Step 1.5, Subscribe to location changes.
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    fun unsubscribeToLocationUpdates() {
        Log.d(TAG, "unsubscribeToLocationUpdates()")

        try {
            // TODO: Step 1.6, Unsubscribe to location changes.
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Location Callback removed.")
                    stopSelf()
                } else {
                    Log.d(TAG, "Failed to remove Location Callback.")
                }
            }
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)

        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, true)
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    private fun generateNotification(location: Location?): Notification {
        val mainNotificationText = location?.toText() ?: getString(R.string.no_location_text)

        notificationHandler.initNotificationParams(
            channelID = NOTIFICATION_CHANNEL_ID,
            channelName = NOTIFICATION_CHANNEL_ID,
            notificationID = NOTIFICATION_ID,
            priority = NotificationCompat.PRIORITY_HIGH,
            contentText = mainNotificationText,
            style = null,
            icon = null,
            title = null,
            directTo = LocationDemoActivity::class.java
        )

        return notificationHandler.getNotification()
    }

    companion object {
        private const val TAG = "ForegroundOnlyLocationService"
        private const val PACKAGE_NAME = "com.example.android.whileinuselocation"
        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"
        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"
        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"
        private const val NOTIFICATION_ID = 12345678
        private const val NOTIFICATION_CHANNEL_ID = "while_in_use_channel_01"
    }
}
