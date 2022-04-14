package com.mediagallery.hashone.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.mediagallery.hashone.presentation.GalleryConfig
import com.mediagallery.hashone.util.AnimationLessViewpager
import com.mediagallery.hashone.util.KeyUtils
import com.google.android.material.tabs.TabLayout
import com.mediagallery.hashone.R
import kotlinx.android.synthetic.main.activity_pickers.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PickersActivity : AppCompatActivity() {

    private val PERMISSIONS_CAMERA = 124
    var IMAGES_THRESHOLD = 0
    var VIDEOS_THRESHOLD = 0
    var TAB_POSITION = 0
    var REQUEST_RESULT_CODE = 101
    var PICKER_VIEW_TYPE = -1
    var ENABLE_CROP = false
    var IS_DROPDOWN_SHOWN: Boolean = false
    private var lassiConfig = GalleryConfig()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickers)

        val i = intent
//        IMAGES_THRESHOLD = i.getIntExtra("IMAGES_LIMIT", 0)
        IMAGES_THRESHOLD = GalleryConfig.getConfig().maxCount
        VIDEOS_THRESHOLD = i.getIntExtra("VIDEOS_LIMIT", 0)
        REQUEST_RESULT_CODE = i.getIntExtra("REQUEST_RESULT_CODE", 0)
        PICKER_VIEW_TYPE = i.getIntExtra("PICKER_VIEW_TYPE", -1)
        ENABLE_CROP = i.getBooleanExtra("ENABLE_CROP", false)


        tabs.setupWithViewPager(viewpager as AnimationLessViewpager)
        setUpViewPager(viewpager as AnimationLessViewpager)

        camera.setOnClickListener {
            if (isCameraPermitted()) dispatchTakePictureIntent() else checkCameraPermission()
        }
    }

    private fun showPermissionDisableAlert() {
        val alertDialog = AlertDialog.Builder(this@PickersActivity, R.style.dialogTheme)
        alertDialog.setMessage(R.string.storage_permission_rational)
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton(R.string.ok) { _, _ ->
            val intent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", packageName, null)
            }
            permissionSettingResult.launch(intent)
        }
        alertDialog.setNegativeButton(R.string.cancel) { _, _ ->
            onBackPressed()
        }
        val permissionDialog = alertDialog.create()
        permissionDialog.setCancelable(false)
        permissionDialog.show()
    }

    private val permissionSettingResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requestPermission()
        }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map.entries.all { it.value }) {
//                tabs.setupWithViewPager(viewpager)
//                setUpViewPager(viewpager)
            } else {
                showPermissionDisableAlert()
            }
        }


    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        } else {
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun isCameraPermitted(): Boolean {
        val permission = android.Manifest.permission.CAMERA
        val cameraPermission = checkCallingOrSelfPermission(permission)
        return (cameraPermission == PackageManager.PERMISSION_GRANTED)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkCameraPermission(): Boolean {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSIONS_CAMERA)
        return true
    }

    private val REQUEST_TAKE_PHOTO = 1
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
            }
            if (photoFile != null) {
                val photoURI =
                    FileProvider.getUriForFile(this, "com.picker.gallery.fileprovider", photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "PNG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".png", storageDir)
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            galleryAddPic()
        } else if (requestCode == 102) {
            try {
                if (data != null) {
                    if (data.extras != null) {
//                        MyApplication.instance.freeMemory()
                        val filePath = data.extras!!.getString("path")
                        /* val intent = Intent()
                         intent.putExtra("path", filePath)
                         activity!!.setResult(Activity.RESULT_OK, intent)
                         activity!!.finish()*/
                        val list = ArrayList<String>()
                        list.add(filePath!!)
                        setResultOk(list)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun setResultOk(selectedMedia: ArrayList<String>) {
        val intent = Intent().apply {
            putExtra(KeyUtils.SELECTED_MEDIA, selectedMedia)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private var mCurrentPhotoPath: String = ""

    private fun galleryAddPic() {
        val f = File(mCurrentPhotoPath)
        val contentUri = Uri.fromFile(f)
        val path =
            "${Environment.getExternalStorageDirectory()}${File.separator}Zoho Social${File.separator}media${File.separator}Zoho Social Images"
        val folder = File(path)
        if (!folder.exists()) folder.mkdirs()
        val file = File(
            path,
            "${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}_picture.png"
        )
        val out = FileOutputStream(file)
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, contentUri)
        val ei = ExifInterface(mCurrentPhotoPath)
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        val rotatedBitmap: Bitmap? = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            ExifInterface.ORIENTATION_NORMAL -> bitmap
            else -> null
        }
        rotatedBitmap?.compress(Bitmap.CompressFormat.PNG, 70, out)
        out.close()
        ContentUris.parseId(
            Uri.parse(
                MediaStore.Images.Media.insertImage(
                    contentResolver,
                    file.absolutePath,
                    file.name,
                    file.name
                )
            )
        )
        try {
            viewpager.currentItem = 0
            ((viewpager.adapter as ViewPagerAdapter).mFragmentList[0] as? PhotosFragment)?.initViews()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }


    private fun setUpViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(this@PickersActivity.supportFragmentManager)
        val photosFragment = PhotosFragment()
        adapter.addFragment(photosFragment, "PHOTOS")
//        adapter.addFragment(VideoFragment(), "VIDEO")
        viewPager.adapter = adapter
        (viewPager.adapter as ViewPagerAdapter).notifyDataSetChanged()
        viewPager.currentItem = 0


        tabs!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                TAB_POSITION = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        Log.e("viewPager", "TAB_POSITION:$TAB_POSITION")

    }

    internal inner class ViewPagerAdapter(manager: FragmentManager) :
        FragmentStatePagerAdapter(manager) {
        val mFragmentList: ArrayList<Fragment> = ArrayList()
        val mFragmentTitleList: ArrayList<String> = ArrayList()

        override fun getItem(position: Int): Fragment = mFragmentList[position]

        override fun getCount(): Int = mFragmentList.size

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getItemPosition(`object`: Any): Int = POSITION_NONE

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }
//        override fun getPageTitle(position: Int): CharSequence? = null
    }

    override fun onBackPressed() {
        if (!IS_DROPDOWN_SHOWN) {
            try {
                if (viewpager.adapter != null) {
                    if ((viewpager.adapter as ViewPagerAdapter).mFragmentList.size > 0) {
                        if (TAB_POSITION == 0)
                            ((viewpager.adapter as ViewPagerAdapter).mFragmentList[0] as? PhotosFragment)?.toggleDropdown()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            super.onBackPressed()
        }
    }
}