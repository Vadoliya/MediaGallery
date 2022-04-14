package com.mediagallery.hashone.model

import kotlin.collections.ArrayList

data class GalleryAlbums(var id: Long = 0L, var name: String = "", var coverUri: String = "", var albumPhotos: ArrayList<GalleryData> = ArrayList())