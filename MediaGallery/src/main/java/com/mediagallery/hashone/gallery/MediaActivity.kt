package com.mediagallery.hashone.gallery

/*import com.puzzle.maker.instagram.post.R
import com.puzzle.maker.instagram.post.base.MyApplication
import com.puzzle.maker.instagram.post.croppy.Croppy
import com.puzzle.maker.instagram.post.croppy.main.CropRequest
import com.puzzle.maker.instagram.post.croppy.util.file.FileCreator
import com.puzzle.maker.instagram.post.croppy.util.file.FileOperationRequest*/
//import kotlinx.android.synthetic.main.activity_cover_download.*
import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mediagallery.hashone.R
import com.mediagallery.hashone.gallery.config.GalleryConfig
import com.mediagallery.hashone.gallery.fragment.FoldersFragment
import com.mediagallery.hashone.gallery.model.ImageItem
import com.mediagallery.hashone.gallery.model.MediaType
import com.mediagallery.hashone.gallery.utils.KeyUtils
import com.mediagallery.hashone.gallery.utils.URIPathHelper
import com.mediagallery.hashone.gallery.utils.Utils
import kotlinx.android.synthetic.main.activity_media.*


class MediaActivity : AppCompatActivity() {

    private lateinit var activity: Activity
    val selectedImagesList = ArrayList<ImageItem>()

    var requestCode: Int = 0
    var isCropMode: Boolean = false
    var isMultipleMode: Boolean = false
    var maxSize: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this@MediaActivity
        setContentView(R.layout.activity_media)

        initViews()
    }

    private fun initViews() {
        try {
            setSupportActionBar(toolBarMedia)
            supportActionBar!!.title = ""
            supportActionBar!!.subtitle = ""

            /*       isCropMode = intent!!.extras!!.getBoolean("isCropMode", false)
                   isMultipleMode = intent!!.extras!!.getBoolean("isMultipleMode", false)
                   maxSize = intent!!.extras!!.getInt("maxSize", 1)
                   requestCode = intent!!.extras!!.getInt("requestCode", 0)*/


            isCropMode = GalleryConfig.getConfig().isCrop
            isMultipleMode = GalleryConfig.getConfig().maxCount > 1
            maxSize = GalleryConfig.getConfig().maxCount
//            requestCode = intent!!.extras!!.getInt("requestCode", 0)


            val bundle = Bundle()
            bundle.putBoolean("isMultipleMode", isMultipleMode)
            bundle.putInt("maxSize", maxSize)
            loadFragment(FoldersFragment(), bundle, false)

            textViewTotalCount.text = (if (maxSize > 1) {
                " (" + selectedImagesList.size + "/" + maxSize + ")"
            } else "")
            textViewDone.setOnClickListener {
                if (Utils.checkClickTime()) {
                    finishPickImages(selectedImagesList)
                }
            }
            fabGooglePhotos.setOnClickListener {
                if (Utils.checkClickTime()) {
                    if (Utils.isGooglePhotosAppInstalled(
                            applicationContext,
                            "com.google.android.apps.photos"
                        )
                    ) {
                        val ai: ApplicationInfo =
                            packageManager.getApplicationInfo("com.google.android.apps.photos", 0)

                        if (ai.enabled) {
                            val intent = Intent()
                            intent.action = Intent.ACTION_PICK
                            intent.type =
                                (if (GalleryConfig.getConfig().mediaType == MediaType.IMAGE_VIDEO) "*" else GalleryConfig.getConfig().mediaType.value) + "/*"
                            intent.setPackage("com.google.android.apps.photos")
                            startActivityForResult(intent, 2020)
                        } else {
                            showEnableGooglePhotosSnackbar()
                        }
                    } else {
                        Utils.showSnackBar(
                            layoutMediaParent,
                            getString(R.string.google_photos_not_installed)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var snackBar: Snackbar? = null
    fun showEnableGooglePhotosSnackbar() {
        try {
            layoutMediaParent.post {
                if (snackBar == null) {
                    snackBar =
                        Snackbar.make(
                            layoutMediaParent,
                            getString(R.string.google_photos_disable),
                            Snackbar.LENGTH_LONG
                        )
                    snackBar!!.setActionTextColor(Color.YELLOW)
                    snackBar!!.setAction(getString(R.string.label_enable)) { }
                    snackBar!!.addCallback(object :
                        BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onShown(transientBottomBar: Snackbar?) {
                            super.onShown(transientBottomBar)
                            transientBottomBar!!.view.findViewById<View>(R.id.snackbar_action)
                                .setOnClickListener { view ->
                                    view!!.isEnabled = false
                                    snackBar!!.dismiss()
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts(
                                            "package",
                                            "com.google.android.apps.photos",
                                            null
                                        )
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)

                                    Handler().postDelayed({
                                        view.isEnabled = true
                                    }, 2500L)
                                }
                        }
                    })

                    val snackview = snackBar!!.view
                    val txtView = snackview.findViewById<TextView>(R.id.snackbar_text)
                    txtView.setPadding(32, 16, 32, 16)

                    ViewCompat.setOnApplyWindowInsetsListener(snackview) { v, insets ->
                        v.updatePadding(bottom = 0)
                        // Return the insets so that they keep going down the view hierarchy
                        insets
                    }
                }
                if (!snackBar!!.isShown) {
                    snackBar!!.show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

/*
    private inner class CopyFileTask(val context: Context, val uri: Uri) :
        CoroutineAsyncTask<Void, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Void?): String? {
            try {
                return createCopyAndReturnRealPath(context, uri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                if (result != null) {
                    val file = File(result)
                    val fileOperationRequest = FileOperationRequest.createRandom(file.extension)
                    val destinationPath =
                        FileCreator
                            .createFile(
                                fileOperationRequest,
                                MyApplication.instance.applicationContext
                            )
                    val excludeAspectRatiosCropRequest = CropRequest.Manual(
                        sourceUri = uri,
                        sourcePath = file,
                        destinationUri = destinationPath,
                        requestCode = requestCode
                    )
                    Croppy.start(activity, excludeAspectRatiosCropRequest)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun createCopyAndReturnRealPath(@NonNull context: Context, @NonNull uri: Uri?): String? {
            val contentResolver = context.contentResolver ?: return null

            // Create file path inside app's data dir
            val filePath = (context.applicationInfo.dataDir.toString() + File.separator
                    + System.currentTimeMillis())
            val file = File(filePath)
            try {
                val inputStream = contentResolver.openInputStream(uri!!) ?: return null
                val outputStream: OutputStream = FileOutputStream(file)
                val buf = ByteArray(1024)
                var len: Int
                while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
                outputStream.close()
                inputStream.close()
            } catch (ignore: IOException) {
                return null
            }
            return file.absolutePath
        }
    }
*/

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_done, menu)
//        this.menu = menu
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun loadFragment(fragment: Fragment, bundle: Bundle? = null, toAdd: Boolean = false) {
        try {
            if (bundle != null) {
                fragment.arguments = bundle
            }
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            if (toAdd) {
                fragmentTransaction.add(R.id.frameContainer, fragment)
                fragmentTransaction.addToBackStack("New Content")
            } else {
                fragmentTransaction.replace(R.id.frameContainer, fragment)
            }
            fragmentTransaction.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun selectedIndex(imageItem: ImageItem): Int {
        for (i in selectedImagesList.indices) {
            if (selectedImagesList[i].id == imageItem.id) {
                return i
            }
        }
        return -1
    }

    fun selectedIndexes(subImages: ArrayList<ImageItem>): ArrayList<Int> {
        val indexes = arrayListOf<Int>()
        for (image in subImages) {
            for (i in selectedImagesList.indices) {
                if (selectedImagesList[i].path == image.path) {
                    indexes.add(i)
                    break
                }
            }
        }
        return indexes
    }

    fun addItem(imageItem: ImageItem) {
        try {
            selectedImagesList.add(imageItem)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeItem(position: Int) {
        try {
            selectedImagesList.removeAt(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCode && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                /* val filePath = data.extras!!.getString("path")
                 val intent = Intent()
                 intent.putExtra("path", filePath)
                 activity.setResult(Activity.RESULT_OK, intent)
                 activity.finish()*/

                when (requestCode) {
                    2020 -> {
                        if (intent != null) {
//                            CopyFileTaskGoogle(applicationContext, data.data!!).execute()
//                            Toast.makeText(this, "CopyFileTaskGoogle", Toast.LENGTH_LONG).show()
                            val uriPathHelper = URIPathHelper()
                            val filePath = uriPathHelper.getPath(this, data.data!!)

//                            val filePath = data.data!!
                            val images = ArrayList<String>()
                            images.add(filePath!!)
                            val intent = Intent()
                            intent.putExtra(KeyUtils.SELECTED_MEDIA, images)
                            activity.setResult(Activity.RESULT_OK, intent)
                            activity.finish()
                        }
                    }
                    else -> {
                        val filePath = data.extras!!.getString("path")
                        val images = ArrayList<String>()
                        images.add(filePath!!)
                        val intent = Intent()
                        intent.putExtra(KeyUtils.SELECTED_MEDIA, images)
                        activity.setResult(Activity.RESULT_OK, intent)
                        activity.finish()
                    }
                }
/*                when (requestCode) {
                   2020 -> {
                        if (intent != null) {
//                            CopyFileTaskGoogle(applicationContext, data.data!!).execute()
                            Toast.makeText(this, "CopyFileTaskGoogle", Toast.LENGTH_LONG).show()
                        }
                    }
                    else -> {
                        val filePath = data.extras!!.getString("path")
                        val intent = Intent()
                        intent.putExtra("path", filePath)
                        activity.setResult(Activity.RESULT_OK, intent)
                        activity.finish()
                    }
                }*/
            }
        }
    }


    fun finishPickImages(images: ArrayList<ImageItem>) {
        /*if (isCropMode) {
            val uri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                images[0].id
            )
//            CopyFileTask(applicationContext, uri).execute()
            Toast.makeText(this, "finishPickImages CopyFileTask", Toast.LENGTH_LONG).show()

        } else {
            val newIntent = Intent()
            newIntent.putExtra("paths", images)
            setResult(RESULT_OK, newIntent)
            finish()
        }*/
        val imageList = ArrayList<String>()
        images.forEach { imageList.add(it.path) }
        Log.e("finishPickImages", "images$images imageList:$imageList")
        val newIntent = Intent()
        newIntent.putExtra(KeyUtils.SELECTED_MEDIA, imageList)
        setResult(RESULT_OK, newIntent)
        finish()
    }
}