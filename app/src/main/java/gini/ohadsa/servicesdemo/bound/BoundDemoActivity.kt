package gini.ohadsa.servicesdemo.bound

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.AndroidEntryPoint
import gini.ohadsa.servicesdemo.bound.MyBoundService
import gini.ohadsa.servicesdemo.databinding.ActivityBoundDemoBinding


@AndroidEntryPoint
class BoundDemoActivity : AppCompatActivity() {

    private var _binding: ActivityBoundDemoBinding? = null
    private val binding
        get() = _binding!!

    private var foregroundOnlyTimerServiceBound = false

    private var foregroundOnlyTimerService: MyBoundService? = null

    private var foregroundOnlyBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.extras?.apply {
                binding.textView.text = getString(MyBoundService.TIMER_KEY)
            }
        }
    }


    private var foregroundOnlyServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MyBoundService.LocalBinder
            foregroundOnlyTimerService = binder.service
            foregroundOnlyTimerServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            foregroundOnlyTimerService = null
            foregroundOnlyTimerServiceBound = false
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityBoundDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.start.setOnClickListener {
            Log.d("TAG_MAIN" , "started... ${ foregroundOnlyTimerService != null}")
            foregroundOnlyTimerService?.subscribeToTimer()
        }
        binding.stop.setOnClickListener {
            Log.d("TAG_MAIN" , "stopped...")

            foregroundOnlyTimerService?.unsubscribeToTimer()
        }
    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, MyBoundService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)

    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            foregroundOnlyBroadcastReceiver,
            IntentFilter(
                MyBoundService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST
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
        if (foregroundOnlyTimerServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyTimerServiceBound = false
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}

