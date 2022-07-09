package com.kinandcarta.permissionmanager.permissions

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import gini.ohadsa.servicesdemo.R
import gini.ohadsa.servicesdemo.utils.permissions.Permission
import java.lang.ref.WeakReference
import javax.inject.Inject


class PermissionRequestHandlerImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) :
    PermissionHandler {
    private var fragmentWeekRef: WeakReference<Fragment>? = null
    private val requiredPermissions = mutableListOf<Permission>()
    private var rationale: String? = null
    private var callback: (Boolean) -> Unit = {}
    private var detailedCallback: (Map<Permission, Boolean>) -> Unit = {}
    private var permissionCheck: ActivityResultLauncher<Array<String>>? = null


    override fun from(fragment: Fragment): PermissionRequestHandlerImpl {
        fragmentWeekRef = WeakReference(fragment)
        permissionCheck = fragmentWeekRef?.get()
            ?.registerForActivityResult(RequestMultiplePermissions()) { grantResults ->
                sendResultAndCleanUp(grantResults)
            }

        return this
    }


    companion object {
        private const val REQUIRE_SETTINGS_TAG = "REQUEIRE_SETTINGS_TAG"
    }

    override fun rationale(description: String): PermissionRequestHandlerImpl {
        rationale = description
        return this
    }

    override fun request(vararg permission: Permission): PermissionRequestHandlerImpl {
        requiredPermissions.addAll(permission)
        return this
    }

    override fun checkPermission(callback: (Boolean) -> Unit) {
        this.callback = callback
        handlePermissionRequest()
    }

    override fun checkDetailedPermission(callback: (Map<Permission, Boolean>) -> Unit) {
        this.detailedCallback = callback
        handlePermissionRequest()
    }

    private fun handlePermissionRequest() {
        fragmentWeekRef?.get()?.let { fragment ->
            when {
                areAllPermissionsGranted(fragment) -> sendPositiveResult()
                shouldShowPermissionRationale(fragment) -> displayRationale(fragment)
                else -> {
                    val numOfCalls =
                        sharedPreferences.getInt(REQUIRE_SETTINGS_TAG, 1) ?: 0

                    if (numOfCalls >= 2) {
                        moveToSettingsDialog(fragment)
                    } else {
                        requestPermissions()
                    }
                    sharedPreferences.edit()
                        .putInt(REQUIRE_SETTINGS_TAG, numOfCalls + 1).apply()
                }
            }
        }
    }


    private fun moveToSettingsDialog(fragment: Fragment) {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle(fragment.getString(R.string.dialog_permission_title))
            .setMessage("please change permission in settings")
            .setCancelable(false)
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", fragment.requireContext().packageName, null)
                intent.data = uri
                fragment.requireContext().startActivity(intent)
            }.show()

    }

    private fun displayRationale(fragment: Fragment) {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle(fragment.getString(R.string.dialog_permission_title))
            .setMessage(rationale ?: fragment.getString(R.string.dialog_permission_default_message))
            .setCancelable(false)
            .setPositiveButton(fragment.getString(R.string.dialog_permission_button_positive)) { _, _ ->
                requestPermissions()
            }
            .show()
    }

    private fun sendPositiveResult() {
        sendResultAndCleanUp(getPermissionList().associate { it to true })
    }

    private fun sendResultAndCleanUp(grantResults: Map<String, Boolean>) {
        callback(grantResults.all { it.value })
        detailedCallback(grantResults.mapKeys { Permission.from(it.key) })
        cleanUp()
    }

    private fun cleanUp() {
        requiredPermissions.clear()
        rationale = null
        callback = {}
        detailedCallback = {}
    }

    private fun requestPermissions() {
        permissionCheck?.launch(getPermissionList())
    }

    private fun areAllPermissionsGranted(fragment: Fragment) =
        requiredPermissions.all { it.isGranted(fragment) }

    private fun shouldShowPermissionRationale(fragment: Fragment) =
        requiredPermissions.any { it.requiresRationale(fragment) }

    private fun getPermissionList() =
        requiredPermissions.flatMap { it.permissions.toList() }.toTypedArray()

    private fun Permission.isGranted(fragment: Fragment) =
        permissions.all { hasPermission(fragment, it) }

    private fun Permission.requiresRationale(fragment: Fragment) =
        permissions.any { fragment.shouldShowRequestPermissionRationale(it) }

    private fun hasPermission(fragment: Fragment, permission: String) =
        ContextCompat.checkSelfPermission(
            fragment.requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
}