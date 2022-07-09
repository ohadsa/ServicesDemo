package com.kinandcarta.permissionmanager.permissions

import androidx.fragment.app.Fragment
import gini.ohadsa.servicesdemo.utils.permissions.Permission

interface PermissionHandler {

    fun rationale(description: String): PermissionRequestHandlerImpl
    fun request(vararg permission: Permission): PermissionRequestHandlerImpl
    fun checkPermission(callback: (Boolean) -> Unit)
    fun checkDetailedPermission(callback: (Map<Permission, Boolean>) -> Unit)
    fun from(fragment: Fragment): PermissionRequestHandlerImpl
}