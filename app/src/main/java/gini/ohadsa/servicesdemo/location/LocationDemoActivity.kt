package gini.ohadsa.servicesdemo.location

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import gini.ohadsa.servicesdemo.BuildConfig
import gini.ohadsa.servicesdemo.R
import gini.ohadsa.servicesdemo.databinding.ActivityLocationDemoBinding
import gini.ohadsa.servicesdemo.utils.SharedPreferenceUtil
import gini.ohadsa.servicesdemo.utils.permissions.Permission
import gini.ohadsa.servicesdemo.utils.toText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val TAG = "MainActivity"
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

@AndroidEntryPoint
class LocationDemoActivity : AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private var foregroundOnlyLocationServiceBound = false
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null
    private lateinit var foregroundOnlyBroadcastReceiver: ForegroundOnlyBroadcastReceiver

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    private val viewModel: LocationDemoViewModel by viewModels()
    private var _binding: ActivityLocationDemoBinding? = null
    private val binding
        get() = _binding!!

    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLocationDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()

        lifecycleScope.launch {
            viewModel.buttonPressedFlow.collectLatest { needToRun ->
                if (needToRun) {
                    if (foregroundPermissionApproved()) foregroundOnlyLocationService?.subscribeToLocationUpdates()
                        ?: Log.d(TAG, "Service Not Bound")
                    else requestForegroundPermissions()

                } else foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
            }
        }
        lifecycleScope.launch {
            viewModel.permissionResultFlow.collectLatest { result ->
                when (result) {
                    true -> {
                        foregroundOnlyLocationService?.subscribeToLocationUpdates()
                    }
                    false -> {
                        viewModel.serviceStateChangedFlow.value = false
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val serviceIntent = Intent(this, ForegroundOnlyLocationService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            foregroundOnlyBroadcastReceiver,
            IntentFilter(
                ForegroundOnlyLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST
            )
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            foregroundOnlyBroadcastReceiver
        )
        super.onPause()
    }

    override fun onStop() {

        if (foregroundOnlyLocationServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestForegroundPermissions() {
        lifecycleScope.launch {
            viewModel.permissionRequesterFlow.emit(
                PermissionData(
                    Permission.Location, getString(R.string.permission_rationale)
                )
            )
        }
    }

    private fun logResultsToScreen(output: String) {
        viewModel.logResultFlowFromActivity.value = "$output\n"
    }

    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(
                ForegroundOnlyLocationService.EXTRA_LOCATION
            )

            if (location != null) {
                logResultsToScreen("Foreground location: ${location.toText()}")
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
            viewModel.serviceStateChangedFlow.value = sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
            )
        }
    }
}