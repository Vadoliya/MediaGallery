package com.mediagallery.hashone.gallery.callback

import com.mediagallery.hashone.gallery.model.ImageItem

interface OnSelectionChangeListener {
    fun onSelectedImagesChanged(selectedImages: ArrayList<ImageItem>)
    fun onSingleModeImageSelected(imageItem: ImageItem)
}