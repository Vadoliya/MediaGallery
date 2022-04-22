package com.mediagallery.hashone.gallery.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.mediagallery.hashone.gallery.config.GalleryConfig


class MyTextView : AppCompatTextView {
    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        if (GalleryConfig.getConfig().fontResource != -1) {
            val resource = GalleryConfig.getConfig().fontResource
            typeface = ResourcesCompat.getFont(context, resource)
        }
    }
}