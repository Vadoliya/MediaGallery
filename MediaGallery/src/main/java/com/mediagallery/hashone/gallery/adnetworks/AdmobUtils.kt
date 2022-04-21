package com.mediagallery.hashone.gallery.adnetworks

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.android.gms.ads.*
import com.mediagallery.hashone.gallery.MediaActivity
import com.mediagallery.hashone.gallery.utils.Utils
import kotlinx.android.synthetic.main.activity_media.*


class AdmobUtils(private var context: Context) {

    //Banner ad logic start
    private var adRequest: AdRequest? = null
    private var isBannerLoaded = false

    fun loadBannerAd(viewGroup: ConstraintLayout?, adId: String) {
        try {

            val adView = AdView(context)
            adView.adSize = getAdSize()
//            adView!!.adUnitId = context.resources.getString(R.string.admob_banner_preview)
            adView.adUnitId = adId

            adView.loadAd(getAdRequest())

            adView.adListener = object : AdListener() {
                override fun onAdClicked() {
                    super.onAdClicked()
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                }

                override fun onAdFailedToLoad(p0: LoadAdError?) {
                    super.onAdFailedToLoad(p0)
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    isBannerLoaded = true
                    viewGroup!!.isVisible = true
                    try {
                        if (viewGroup != null) {
                            if (viewGroup.childCount > 0) {
                                viewGroup.removeAllViews()
                            }
                            if (adView != null) {
                                viewGroup.addView(adView)
                                Utils.fadeInAndShowView(viewGroup!!, 400)
                            }

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    Utils.setMargins(context,(context as MediaActivity).fabGooglePhotos,0,0,24,74)
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                }
            }
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBannerView(viewGroup: ConstraintLayout, adView: AdView) {
        try {
            viewGroup.addView(adView)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onDestroyBannerAd(viewGroup: ConstraintLayout) {
        try {
            for (i in 0 until viewGroup.childCount) {
                if (viewGroup.getChildAt(i) is AdView) {
                    val adView = viewGroup.getChildAt(i) as AdView
                    adView.destroy()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAdSize(): AdSize {
//        val display = context.getWindowManager().getDefaultDisplay()
//        val outMetrics = DisplayMetrics()
        val outMetrics = context.resources.displayMetrics

        val widthPixels = outMetrics.widthPixels
        val density = outMetrics.density

        val adWidth = (widthPixels / density).toInt()

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

    fun getAdRequest(): AdRequest {
        if (adRequest == null) {
            adRequest = AdRequest.Builder()
//                .addTestDevice("A78B392C077C99F05868A8F704987217")
                .build()
        }
        return adRequest!!
    }
    //banner ad logic end
}