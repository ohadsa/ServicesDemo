package gini.ohadsa.servicesdemo.utils.permissions

import android.Manifest.permission.*

sealed class Permission(vararg val permissions: String) {
    object Camera : Permission(CAMERA)
    object MandatoryForFeatureOne : Permission(WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION)
    object Location : Permission(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
    object Storage : Permission(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)

    companion object {
        fun from(permission: String) = when (permission) {
            ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION -> Location
            WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE -> Storage
            CAMERA -> Camera
            else -> throw IllegalArgumentException("Unknown permission: $permission")
        }
    }
}