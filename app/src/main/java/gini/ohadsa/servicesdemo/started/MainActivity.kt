package gini.ohadsa.servicesdemo.started

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.AndroidEntryPoint
import gini.ohadsa.servicesdemo.bound.MyBoundService
import gini.ohadsa.servicesdemo.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding
        get() = _binding!!

    private val lbcManager get() = LocalBroadcastManager.getInstance(this)
    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.extras?.apply {
                binding.textView.text = getString(MyStartedService.STARTED_TIMER_KEY)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lbcManager.registerReceiver(
            timerReceiver,
            IntentFilter(MyStartedService.STARTED_BROADCAST_TIMER)
        )
    }

    override fun onPause() {
        super.onPause()
        lbcManager.unregisterReceiver(timerReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.start.setOnClickListener {
            actionService(MyStartedService.STARTED_START)
        }
        binding.stop.setOnClickListener {
            actionService(MyStartedService.STARTED_STOP)
        }
    }

    private fun actionService(state: String) {
        val intent = Intent(this, MyBoundService::class.java)
        Log.d(STARTED_TAG, "1234567")
        intent.action = state
        Log.d(STARTED_TAG, "1234567")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}