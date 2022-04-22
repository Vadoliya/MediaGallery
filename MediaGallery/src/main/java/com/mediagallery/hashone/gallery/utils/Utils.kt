package com.mediagallery.hashone.gallery.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
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
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun fadeInAndShowView(view: View, duration: Long) {
        if (view.visibility == View.GONE || view.visibility == View.INVISIBLE) {
            val fadeOut = AlphaAnimation(0f, 1f)
            fadeOut.interpolator = AccelerateInterpolator()
            fadeOut.duration = duration

            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationEnd(animation: Animation) {
                    view.visibility = View.VISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {}

                override fun onAnimationStart(animation: Animation) {
                    view.visibility = View.VISIBLE

                }
            })
            view.startAnimation(fadeOut)
        }
    }

    fun setMargins(context: Context, view: View, left: Int, top: Int, right: Int, bottom: Int) {
        if (view.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = view.layoutParams as ViewGroup.MarginLayoutParams
            val scale: Float = context.resources.displayMetrics.density
            // convert the DP into pixel
            val l = (left * scale + 0.5f).toInt()
            val r = (right * scale + 0.5f).toInt()
            val t = (top * scale + 0.5f).toInt()
            val b = (bottom * scale + 0.5f).toInt()
            p.setMargins(l, t, r, b)
            view.requestLayout()
        }
    }

}