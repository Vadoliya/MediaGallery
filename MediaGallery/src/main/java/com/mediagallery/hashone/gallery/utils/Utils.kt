package com.mediagallery.hashone.gallery.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemClock
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.google.android.material.snackbar.Snackbar
import com.mediagallery.hashone.R
import com.mediagallery.hashone.gallery.Constants


object Utils {

    fun showSnackBar(view: View, content: String) {
        try {
            val snackBar = Snackbar.make(view, content, Snackbar.LENGTH_SHORT)
            val snackView = snackBar.view
            snackView.setBackgroundResource(R.drawable.drawable_snackbar)

            ViewCompat.setOnApplyWindowInsetsListener(snackView) { v, insets ->
                v.updatePadding(bottom = 0)
                // Return the insets so that they keep going down the view hierarchy
                insets
            }
            val txtView = snackView.findViewById<TextView>(R.id.snackbar_text)
            txtView.setPadding(32, 16, 32, 16)
            snackBar.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun closeKeyboard(context: Context, view: View) {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(
            view.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    fun checkClickTime(): Boolean {
        return if (((SystemClock.elapsedRealtime() - Constants.mLastClickTime) >= 600)) {
            Constants.mLastClickTime = SystemClock.elapsedRealtime()
            true
        } else {
            false
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