package com.mediagallery.hashone.gallery.model

import java.io.Serializable

data class ImageItem(
    var id: Long = -1L,
    var bucketId: Long = -1L,
    var bucketName:String="",
    var name: String = "",
    var path: String = "",
    val duration: Long,
    val type: String
): Serializable
