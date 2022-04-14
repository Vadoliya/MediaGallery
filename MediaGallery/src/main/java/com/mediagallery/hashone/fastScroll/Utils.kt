package com.mediagallery.hashone.fastScroll

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.net.URLConnection

object Utils {

    fun toPixels(res: Resources, dp: Float): Int {
        return (dp * res.displayMetrics.density).toInt()
    }

    fun toScreenPixels(res: Resources, sp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, res.displayMetrics).toInt()
    }

    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun isRtl(res: Resources): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && res.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }

    fun closeKeyboard(activity: Activity, view: View) {
        val inputManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(
            view.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    fun pxToDp(px: Int): Int {
        return (px / Resources.getSystem().displayMetrics.density).toInt()
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun dpToPx(dp: Float): Float {
        return (dp * Resources.getSystem().displayMetrics.density)
    }

    fun getScreenWidth(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        return displayMetrics.heightPixels - 50
    }

    fun isVideoFile(path: String?): Boolean {
        var mimeType: String? = null
        if (path != null)
            mimeType = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("video")
    }

   /* fun getAppPackageName(): String {
        return BuildConfig.APPLICATION_ID
    }*/
}