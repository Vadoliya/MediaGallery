package com.mediagallery.hashone

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.mediagallery.hashone.gallery.config.builder.OpenGallery
import com.mediagallery.hashone.gallery.model.MediaType
import com.mediagallery.hashone.gallery.utils.KeyUtils
import com.mediagallery.hashone.gallery.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    var mediaType = "IMAGE"

    override fun onCreate(savedInstanceState: Bundle?) {

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR//  set status text dark

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intArray = ArrayList<Int>()
        for (i in 1 until 51) {
            intArray.add(i)
        }
        spCount.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, intArray)

        val fontList = arrayOf("Default", "Nunito Bold", "Roboto Medium", "Tinos Bold")
        spFont.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, fontList)


        image.setOnClickListener {
            receiveData.launch(
                OpenGallery(this)
                    .setMaxCount(spCount.selectedItemPosition + 1)
                    .setMediaType(MediaType.IMAGE)
                    .setFontResource(
                        when (spFont.selectedItemPosition) {
                            0 -> -1
                            1 -> R.font.nunito_bold
                            2 -> R.font.roboto_medium
                            3 -> R.font.tinos_bold
                            else -> -1
                        }
                    )
                    .setBannerAdsId(if (cbBannerAds.isChecked) "ca-app-pub-3940256099942544/6300978111" else "")
                    .setDoneText(edtDoneText.text.toString())
                    .setLoadingText(edtLoadingText.text.toString())
                    .build()
            )
            mediaType = "IMAGE"
        }

        video.setOnClickListener {
            receiveData.launch(
                OpenGallery(this)
                    .setMaxCount(spCount.selectedItemPosition + 1)
                    .setMediaType(MediaType.VIDEO)
                    .setFontResource(
                        when (spFont.selectedItemPosition) {
                            0 -> -1
                            1 -> R.font.nunito_bold
                            2 -> R.font.roboto_medium
                            3 -> R.font.tinos_bold
                            else -> -1
                        }
                    )
                    .setBannerAdsId(if (cbBannerAds.isChecked) "ca-app-pub-3940256099942544/6300978111" else "")
                    .setDoneText(edtDoneText.text.toString())
                    .setLoadingText(edtLoadingText.text.toString())
                    .build()
            )
            mediaType = "VIDEO"
        }

        image_video.setOnClickListener {
            receiveData.launch(
                OpenGallery(this).setMaxCount(spCount.selectedItemPosition + 1)
                    .setMediaType(MediaType.IMAGE_VIDEO)
                    .setFontResource(
                        when (spFont.selectedItemPosition) {
                            0 -> -1
                            1 -> R.font.nunito_bold
                            2 -> R.font.roboto_medium
                            3 -> R.font.tinos_bold
                            else -> -1
                        }
                    )
                    .setBannerAdsId(if (cbBannerAds.isChecked) "ca-app-pub-3940256099942544/6300978111" else "")
                    .setDoneText(edtDoneText.text.toString())
                    .setLoadingText(edtLoadingText.text.toString())
                    .build()
            )
            mediaType = "IMAGE & VIDEO"
        }

    }

    private val receiveData =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val selectedMedia =
                    it.data?.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as ArrayList<*>
                Toast.makeText(
                    this,
                    "Select ${selectedMedia.size}  $mediaType files \n${selectedMedia}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
}