package com.mediagallery.hashone.gallery.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.google.android.material.snackbar.Snackbar
import com.mediagallery.hashone.R
import com.mediagallery.hashone.gallery.Constants
/*import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mediagallery.hashone.R
import com.puzzle.maker.instagram.post.BuildConfig
import com.puzzle.maker.instagram.post.R
import com.puzzle.maker.instagram.post.base.MyApplication
import com.puzzle.maker.instagram.post.main.WebViewActivity*/


object Utils {

    //TODO: 0 : Navigation is displaying with 3 buttons
    //TODO: 1 : Navigation is displaying with 2 button(Android P navigation mode)
    //TODO: 2 : Full screen gesture(Gesture on android Q)
    fun isEdgeToEdgeEnabled(context: Context): Int {
        val resources = context.resources
        val resourceId =
            resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
        return if (resourceId > 0) {
            resources.getInteger(resourceId)
        } else 0
    }

    fun getNavigationBarHeight(context: Context): Int {
        val resources: Resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    fun hasNavigationBar(activity: Activity): Boolean {
        val rectangle = Rect()
        val displayMetrics = DisplayMetrics()
        activity.window.decorView.getWindowVisibleDisplayFrame(rectangle)
        activity.windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        return displayMetrics.heightPixels != rectangle.top + rectangle.height()
    }

    fun showSnackBar(view: View, content: String) {
        try {
            val snackbar = Snackbar.make(view, content, Snackbar.LENGTH_SHORT)
            val snackview = snackbar.view
            snackview.setBackgroundResource(R.drawable.drawable_snackbar)

            ViewCompat.setOnApplyWindowInsetsListener(snackview) { v, insets ->
                v.updatePadding(bottom = 0)
                // Return the insets so that they keep going down the view hierarchy
                insets
            }
            val txtView = snackview.findViewById<TextView>(R.id.snackbar_text)
            txtView.setPadding(32, 16, 32, 16)
            snackbar.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return isPackageInstalled(context, packageName)
//        try {
//            context.packageManager.getPackageInfo(packageName, 0)
//        } catch (e: PackageManager.NameNotFoundException) {
//            e.printStackTrace()
//            return false
//        }
//        return true
    }

    fun isPackageInstalled(context: Context, packageName: String?): Boolean {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName!!) ?: return false
        val list =
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return list.isNotEmpty()
    }

    fun pxToSp(px: Float): Float {
        return (px / Resources.getSystem().displayMetrics.scaledDensity)
    }

    fun spToPx(sp: Float): Float {
        return (sp * Resources.getSystem().displayMetrics.scaledDensity)
    }

    fun pxToDp(px: Float): Float {
        return (px / Resources.getSystem().displayMetrics.density)
    }

    fun dpToPx(dp: Float): Float {
        return (dp * Resources.getSystem().displayMetrics.density)
    }

    fun openKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    fun closeKeyboard(context: Context, view: View) {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(
            view.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }


    fun getScreenDensity(context: Context): Float {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.density
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

    fun checkClickTime(): Boolean {
        return if (((SystemClock.elapsedRealtime() - Constants.mLastClickTime) >= 600)) {
            Constants.mLastClickTime = SystemClock.elapsedRealtime()
            true
        } else {
            false
        }
    }

    fun checkClickTime1(): Boolean {
        return if (((SystemClock.elapsedRealtime() - Constants.mLastClickTime) >= 350)) {
            Constants.mLastClickTime = SystemClock.elapsedRealtime()
            true
        } else {
            false
        }
    }

    fun checkClickTime2(): Boolean {
        return if (((SystemClock.elapsedRealtime() - Constants.mLastClickTime) >= 250)) {
            Constants.mLastClickTime = SystemClock.elapsedRealtime()
            true
        } else {
            false
        }
    }


    public fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }

    private fun capitalize(s: String): String {
        if (TextUtils.isEmpty(s)) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first) + s.substring(1)
        }
    }

    fun openBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            var broseUrl = if (!url.startsWith("http")) {
                "https://$url"
            } else if (url.startsWith("http:/", ignoreCase = true)) {
                url.replace("http:/", "https:/")
            } else url
            intent.data = Uri.parse(broseUrl)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openPlayStore(context: Context, packageName: String) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$packageName")
                )
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun openApplication(context: Context, packageName: String?, link: String?) {
        if (link != null && link.isNotEmpty()) {
            openBrowser(context, link)
        } else {
            if (packageName != null)
                openPlayStore(context, packageName)
        }
    }


    fun isGooglePhotosAppInstalled(context: Context, packageName: String): Boolean {
        try {
            context.packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return false
        }
        return true
    }

}