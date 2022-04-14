package com.mediagallery.hashone

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.mediagallery.hashone.presentation.MediaType
import com.mediagallery.hashone.presentation.builder.OpenGallery
import com.mediagallery.hashone.util.KeyUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var mediaType = "IMAGE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        image.setOnClickListener {
            receiveData.launch(
                OpenGallery(this).setMaxCount(1).setMediaType(MediaType.IMAGE).enableCrop().build()
            )
            mediaType = "IMAGE"
        }

        video.setOnClickListener {
            receiveData.launch(
                OpenGallery(this).setMaxCount(1).setMediaType(MediaType.VIDEO).build()
            )
            mediaType = "VIDEO"
        }

        image_video.setOnClickListener {
            receiveData.launch(
                OpenGallery(this).setMaxCount(10).setMediaType(MediaType.IMAGE_VIDEO).build()
            )
            mediaType = "IMAGE & VIDEO"
        }


    }

    private val receiveData =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val selectedMedia =
                    it.data?.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as ArrayList<*>

                Log.e("receiveData", "selectedMedia:$selectedMedia")
                Toast.makeText(
                    this,
                    "Select ${selectedMedia.size}  $mediaType files \n${selectedMedia}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
}