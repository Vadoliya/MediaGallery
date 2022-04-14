package com.mediagallery.hashone.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GalleryData(
    var id: Long = 0L,
    var name: String = "",
    var albumName: String = "",
    var photoUri: String = "",
    var albumId: Long = 0L,
    var isSelected: Boolean = false,
    var isEnabled: Boolean = true,
    var dateAdded: String = "",
    var duration: String = "",
    var contentUri: Uri? = null
) : Parcelable