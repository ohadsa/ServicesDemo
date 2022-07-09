package gini.ohadsa.servicesdemo.location

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gini.ohadsa.servicesdemo.R
import gini.ohadsa.servicesdemo.utils.SharedPreferenceUtil
import gini.ohadsa.servicesdemo.utils.permissions.Permission
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.lang.StringBuilder
import javax.inject.Inject


@HiltViewModel
class LocationDemoViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    val permissionRequesterFlow =
        MutableSharedFlow<PermissionData>(replay = 1, extraBufferCapacity = 1)
    val permissionResultFlow =
        MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 1)

    val serviceStateChangedFlow =
        MutableStateFlow(
            sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED,
                false
            )
        )

    var buttonPressedFlow = MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 1)
    val buttonTextFlow = serviceStateChangedFlow.map { isServiceRun ->
        return@map when (isServiceRun) {
            true -> R.string.stop_location_updates_button_text
            false -> R.string.start_location_updates_button_text
        }
    }

    private var logResults = StringBuilder("")
    val logResultFlowFromActivity = MutableStateFlow(logResults.toString())
    val logResultToFragmentFlow = logResultFlowFromActivity.map {
        logResults.append(it)
        return@map logResults.toString()
    }

    fun serviceButtonPressed() {

        buttonPressedFlow.tryEmit(
            !sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
            )
        )
    }


}

data class PermissionData(
    val request: Permission,
    val rationale: String
)
