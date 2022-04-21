package com.mediagallery.hashone.gallery.utils

import android.content.Context
import android.graphics.Typeface
import android.os.Environment
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.mediagallery.hashone.gallery.config.GalleryConfig
import java.io.*


class MyTextView : AppCompatTextView {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
//        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
//        InputStream is = classloader.getResourceAsStream("test.csv");
//        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "Tinos-Bold.ttf");
//        typeface = Typeface.createFromAsset(context.assets, GalleryConfig.getConfig().fontFamilyName)
//        typeface = Typeface.createFromAsset(context.assets, GalleryConfig.getConfig().fontFamilyName)
        //        Typeface tf = Typeface.createFromFile(new File(getClass().getClassLoader().getResource("font/roboto_medium.ttf").getFile()));
//        typeface = tf
//        typeface = GalleryConfig.getConfig().typeface
        if (GalleryConfig.getConfig().fontResource != -1) {
            val resource = GalleryConfig.getConfig().fontResource
            typeface = FileStreamTypeface(resource)
        }
    }

    private fun FileStreamTypeface(resource: Int): Typeface? {
        var tf: Typeface? = null
        val `is`: InputStream = resources.openRawResource(resource)
        val path = Environment.getExternalStorageDirectory().absolutePath + "/gmg_underground_tmp"
        val f = File(path)
        if (!f.exists()) {
            if (!f.mkdirs()) return null
        }
        val outPath = "$path/tmp.raw"
        try {
            val buffer = ByteArray(`is`.available())
            val bos = BufferedOutputStream(FileOutputStream(outPath))
            var l = 0
            while (`is`.read(buffer).also { l = it } > 0) {
                bos.write(buffer, 0, l)
            }
            bos.close()
            tf = Typeface.createFromFile(outPath)
            val f2 = File(outPath)
            f2.delete()
        } catch (e: IOException) {
            return null
        }
        return tf
    }
}