package com.mediagallery.hashone.gallery.model

import java.io.Serializable

data class FolderItem(
    var id: Long = -1L,
    var name: String = "",
    var path: String = "",
    var previewImage: String = "",
    var count: Int = 0,
    var selectedCount: Int = 0,
    var albumPhotos: ArrayList<ImageItem> = ArrayList()
): Serializable {

    fun increaseCount() {
        count++
    }
}
