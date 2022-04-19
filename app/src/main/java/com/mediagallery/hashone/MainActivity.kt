package com.mediagallery.hashone

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.mediagallery.hashone.gallery.config.builder.OpenGallery
import com.mediagallery.hashone.gallery.model.MediaType
import com.mediagallery.hashone.gallery.utils.KeyUtils
//import com.mediagallery.hashone.presentation.MediaType
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

          /*  val intent = Intent(this@MainActivity, MediaActivity::class.java)
//            intent.putExtra("IMAGES_LIMIT", 10)
            intent.putExtra("maxSize", 10)
            intent.putExtra("isMultipleMode", true)
//            if (requestCode == RC_CROP_IMAGE) {
//                intent.putExtra("ENABLE_CROP", true)
//            }
//                        receiveData.launch(intent)
            startActivityForResult(intent, 10)*/

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("onActivityResult","requestCode:$requestCode resultCode:$resultCode $data")
        /*
        if (resultCode == Activity.RESULT_OK) {
            val selectedMedia =
                data?.getSerializableExtra(KeyUtils.PATHS)
            if (intent.hasExtra("paths")) {
                val pathsList = intent.extras!!.getStringArrayList("paths")
                Log.e("onActivityResult", "paths pathsList:$pathsList ")

            } else if (intent.hasExtra("path")) {
                Log.e(
                    "onActivityResult",
                    "path :${intent.extras!!.getString("path")!!} "
                )
            }

            Toast.makeText(
                this,
                "Select $selectedMedia  $mediaType files \n${selectedMedia}",
                Toast.LENGTH_LONG
            ).show()
        }
        */
    }
    private val receiveData =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
          /*      if (intent.hasExtra("paths")) {
                    val pathsList = intent.extras!!.getStringArrayList("paths")
                    Log.e("onActivityResult", "paths pathsList:$pathsList ")
                    val selectedMedia = it.data?.getSerializableExtra(KeyUtils.PATHS)
                    Toast.makeText(
                        this,
                        "Select ${selectedMedia}  $mediaType files \n${selectedMedia}",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (intent.hasExtra("path")) {
                    val selectedMedia = it.data?.getSerializableExtra(KeyUtils.PATHS)

                    Log.e(
                        "onActivityResult",
                        "path :${intent.extras!!.getString("path")!!} "
                    )

                }else*/
//                    if(intent.hasExtra(KeyUtils.SELECTED_MEDIA)){
                    val selectedMedia = it.data?.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as ArrayList<*>
                    Log.e("receiveData", "selectedMedia:$selectedMedia")
                    Toast.makeText(
                        this,
                        "Select ${selectedMedia.size}  $mediaType files \n${selectedMedia}",
                        Toast.LENGTH_LONG
                    ).show()
//                }

            }
        }
}