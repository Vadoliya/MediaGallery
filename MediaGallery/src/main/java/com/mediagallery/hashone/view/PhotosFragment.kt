package com.mediagallery.hashone.view

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.mediagallery.hashone.fastScroll.ScrollFastRecyclerLib
import com.mediagallery.hashone.fastScroll.Utils
import com.mediagallery.hashone.model.GalleryAlbums
import com.mediagallery.hashone.model.GalleryData
import com.mediagallery.hashone.presentation.GalleryConfig
import com.mediagallery.hashone.presentation.MediaType
import com.mediagallery.hashone.util.CoroutineAsyncTask
import com.mediagallery.hashone.util.KeyUtils
import com.mediagallery.hashone.view.adapters.AlbumAdapter
import com.mediagallery.hashone.view.adapters.ImageGridAdapter
import com.google.android.material.snackbar.Snackbar
import com.mediagallery.hashone.R
import kotlinx.android.synthetic.main.fragment_media.*
import java.text.SimpleDateFormat
import java.util.*

class PhotosFragment : Fragment() {

    var photoList: ArrayList<GalleryData> = ArrayList()
    var albumList: ArrayList<GalleryAlbums> = ArrayList()
    lateinit var glm: GridLayoutManager
    var photoids: ArrayList<Int> = ArrayList()
    private val PERMISSIONS_READ_WRITE = 123

    var showMessageSnackBar: Snackbar? = null

    lateinit var ctx: Context
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ctx = inflater.context
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            allowAccessButton.outlineProvider = ViewOutlineProvider.BACKGROUND
        }

        initViews()

        allowAccessButton.setOnClickListener {
            if (isReadWritePermitted()) initGalleryViews() else checkReadWritePermission()
        }

        if (activity != null) Utils.closeKeyboard(requireActivity(), allowAccessButton)

        toolbarMedia.setNavigationOnClickListener { activity?.onBackPressed() }
    }

    fun initViews() {
        photoids.clear()
        if (isReadWritePermitted()) initGalleryViews() else allowAccessFrame.visibility =
            View.VISIBLE
    }

    fun initGalleryViews() {
        allowAccessFrame.visibility = View.GONE
        glm = GridLayoutManager(ctx, 3)
        (imageGrid as ScrollFastRecyclerLib).itemAnimator = null
        val bundle = this.arguments
        if (bundle != null) photoids =
            if (bundle.containsKey("photoids")) bundle.getIntegerArrayList("photoids") as ArrayList<Int> else ArrayList()

        AccessImagesTask().execute()

        if ((ctx as PickersActivity).IMAGES_THRESHOLD > 1) {
            albumselectionCount.visibility = View.VISIBLE
            albumselectionCount.text = "(0/${(activity as PickersActivity).IMAGES_THRESHOLD})"
        } else {
            albumselectionCount.visibility = View.GONE
        }
    }

    fun initRecyclerViews() {
        albumsrecyclerview.layoutManager = LinearLayoutManager(ctx)
        albumsrecyclerview.setHasFixedSize(true)
        albumsrecyclerview.adapter = AlbumAdapter(requireActivity(), ArrayList(), this)
        (albumsrecyclerview.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        albumsrecyclerview.itemAnimator = null

        (imageGrid as ScrollFastRecyclerLib).adapter =
            ImageGridAdapter(
                imageList = photoList,
                threshold = (ctx as PickersActivity).IMAGES_THRESHOLD, fragment = this
            )

        ((imageGrid as ScrollFastRecyclerLib).adapter as ImageGridAdapter).setOnItemClickListener { p0, p1, itemPosition, p3 ->
            if ((ctx as PickersActivity).IMAGES_THRESHOLD == 1) {
                startCrop(photoList[itemPosition].photoUri)
            }
        }
        albumsrecyclerview.visibility = View.GONE
    }

    var albumAdapter: AlbumAdapter? = null

    fun toggleDropdown() {
        if (albumsrecyclerview.visibility == View.GONE) {
            (activity as PickersActivity).IS_DROPDOWN_SHOWN = true
            if (albumAdapter != null) {
                albumAdapter!!.notifyDataSetChanged()
            } else {
                albumAdapter = AlbumAdapter(requireActivity(), albumList, this)
                albumsrecyclerview.adapter = albumAdapter
            }
            try {
                done.isEnabled = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if ((ctx as PickersActivity).IMAGES_THRESHOLD > 1) {
            } else {
                done.visibility = View.GONE
            }
            albumsrecyclerview.visibility = View.VISIBLE

            val folderName = when (GalleryConfig.getConfig().mediaType) {
                MediaType.IMAGE -> R.string.label_photos
                MediaType.VIDEO -> R.string.label_video
                MediaType.IMAGE_VIDEO -> R.string.label_media
                else -> R.string.label_photos
            }

            albumselection.text = getString(folderName)

            if (showMessageSnackBar != null && showMessageSnackBar!!.isShown) {
                showMessageSnackBar!!.dismiss()
            }
        } else {
            albumsrecyclerview.visibility = View.GONE
            done.isEnabled = true
            if ((ctx as PickersActivity).IMAGES_THRESHOLD > 1) {
            } else {
                done.visibility = View.GONE
            }
            (activity as PickersActivity).IS_DROPDOWN_SHOWN = false

            if (showMessageSnackBar != null && !showMessageSnackBar!!.isShown) {
                showMessageSnackBar!!.dismiss()
            }
        }
    }

    fun updateTitle(galleryAlbums: GalleryAlbums) {
        albumselection.text = galleryAlbums.name
    }

    @TargetApi(android.os.Build.VERSION_CODES.JELLY_BEAN)
    fun checkReadWritePermission(): Boolean {
        requestPermissions(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ), PERMISSIONS_READ_WRITE
        )
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_READ_WRITE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) initGalleryViews()
            else allowAccessFrame.visibility = View.VISIBLE
        }
    }

    private fun isReadWritePermitted(): Boolean =
        (context?.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && context?.checkCallingOrSelfPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)

    fun startCrop(filePath: String) {
//        MyApplication.instance.freeMemory()
/*        if (GalleryConfig.getConfig().isCrop && (GalleryConfig.getConfig().mediaType == MediaType.IMAGE)) {
            startCroppy(filePath)
        } else {*/
            val filePathList = ArrayList<String>()
            filePathList.add(filePath)
            val intent = Intent()
            intent.putExtra("path", filePath)
            intent.putExtra(KeyUtils.SELECTED_MEDIA, filePathList)
            requireActivity().setResult(Activity.RESULT_OK, intent)
            requireActivity().finish()
//        }
    }

    fun setMultipleResult(filePath: ArrayList<String>) {
        val intent = Intent()
        intent.putStringArrayListExtra("paths", filePath)
        intent.putStringArrayListExtra(KeyUtils.SELECTED_MEDIA, filePath)
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finish()
    }

/*

    private fun startCroppy(path: String) {
        try {
//            MyApplication.instance.freeMemory()

            val file = File(path)
            val uri = Uri.fromFile(File(path))
            */
/* FileProvider.getUriForFile(
                 requireActivity(),
                 "${Utils.getAppPackageName()}.provider",
                 file
             )*//*


            val fileOperationRequest = FileOperationRequest.createRandom(file.extension)

            val destinationPath =
                FileCreator
                    .createFile(fileOperationRequest, requireContext())

            val excludeAspectRatiosCropRequest = CropRequest.Manual(
                sourceUri = uri,
                sourcePath = file,
                destinationUri = destinationPath,
                requestCode = 102
            )

            Croppy.start(requireActivity(), excludeAspectRatiosCropRequest)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
*/

    private inner class AccessImagesTask : CoroutineAsyncTask<Void, Void, Void>() {

        val simpleDateFormat = SimpleDateFormat("mm:ss:SSS", Locale.ENGLISH)

        override fun onPreExecute() {
            super.onPreExecute()

            progressBarGalleryPhotos.visibility = View.VISIBLE

            albumList.clear()
            photoList.clear()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            try {
                when (GalleryConfig.getConfig().mediaType) {
                    MediaType.IMAGE -> getAllPhotosByBucketId(-1L)
                    MediaType.VIDEO -> getAllVideoByBucketId(-1L)
                    MediaType.IMAGE_VIDEO -> {
                        getAllPhotosByBucketId(-1L)
                        getAllVideoByBucketId(-1L)
                    }
                    else -> getAllPhotosByBucketId(-1L)

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        @SuppressLint("Range")
        fun getAllVideoByBucketId(bucketId: Long) {
            try {
                var projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    arrayOf(
                        MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.RELATIVE_PATH,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.MIME_TYPE,
                        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                        MediaStore.Video.Media.BUCKET_ID,
                        MediaStore.Video.Media.DATE_ADDED,
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.DATA
                    )
                } else {
                    arrayOf(
                        MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.DATA,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.MIME_TYPE,
                        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                        MediaStore.Video.Media.BUCKET_ID,
                        MediaStore.Video.Media.DATE_ADDED,
                        MediaStore.Video.Media.DISPLAY_NAME
                    )
                }

                val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"

                activity!!.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    sortOrder
                )?.use { cursor ->

                    while (cursor.moveToNext()) {
                        val mimeType =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE))
                        if (mimeType.equals("video/mp4", ignoreCase = true)
                        ) {
                            val id =
                                cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                            val name =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
                            val dateAdded =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED))

                            val uri = ContentUris.withAppendedId(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                id
                            )

                            // Discard invalid images that might exist on the device

                            val galleryData = GalleryData()
                            galleryData.name = name
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val albumName =
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                                val albumId =
                                    cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID))
                                val path =
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH))
                                galleryData.albumName = albumName ?: ""
                                galleryData.albumId = albumId
                                galleryData.photoUri =
                                    "${Environment.getExternalStorageDirectory().absolutePath}/$path$name"
                                try {
                                    val data =
                                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                                    if (data != null) {
                                        galleryData.photoUri = data
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else {
                                val data =
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                                galleryData.photoUri = data
                                val albumName =
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                                val albumId =
                                    cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID))
                                galleryData.albumName = albumName ?: ""
                                galleryData.albumId = albumId
                            }
                            galleryData.id = id
                            galleryData.dateAdded = dateAdded
                            galleryData.contentUri = uri
                            galleryData.isSelected = photoids.contains(id.toInt())

                            photoList.add(galleryData)

                            addAlbumIfNotExist(galleryData)
                        }
                    }

                    cursor.close()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @SuppressLint("Range")
        fun getAllPhotosByBucketId(bucketId: Long) {
            try {
                var projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    arrayOf(
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.RELATIVE_PATH,
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.MIME_TYPE,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.Media.BUCKET_ID,
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATA
                    )
                } else {
                    arrayOf(
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.MIME_TYPE,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.Media.BUCKET_ID,
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Media.DISPLAY_NAME
                    )
                }

                val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

                activity!!.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    sortOrder
                )?.use { cursor ->


                    while (cursor.moveToNext()) {
                        val mimeType =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE))
                        if (mimeType.equals("image/jpeg", ignoreCase = true)
                            || mimeType.equals("image/png", ignoreCase = true)
                            || mimeType.equals("image/jpg", ignoreCase = true)
                        ) {
                            val id =
                                cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                            val name =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                            val dateAdded =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED))

                            val uri = ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                id
                            )

                            // Discard invalid images that might exist on the device

                            val galleryData = GalleryData()
                            galleryData.name = name
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val albumName =
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
                                val albumId =
                                    cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID))
                                val path =
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH))
                                galleryData.albumName = albumName ?: ""
                                galleryData.albumId = albumId
                                galleryData.photoUri =
                                    "${Environment.getExternalStorageDirectory().absolutePath}/$path$name"
                                try {
                                    val data =
                                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                                    if (data != null) {
                                        galleryData.photoUri = data
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else {
                                val data =
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                                galleryData.photoUri = data
                                val albumName =
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
                                val albumId =
                                    cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID))
                                galleryData.albumName = albumName ?: ""
                                galleryData.albumId = albumId
                            }
                            galleryData.id = id
                            galleryData.dateAdded = dateAdded
                            galleryData.contentUri = uri
                            galleryData.isSelected = photoids.contains(id.toInt())

                            photoList.add(galleryData)

                            addAlbumIfNotExist(galleryData)
                        }
                    }

                    cursor.close()
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val albumIdsList = ArrayList<Long>()

        fun addAlbumIfNotExist(galleryData: GalleryData) {
            try {
                var isAdded = false
                if (albumIdsList.contains(galleryData.albumId)) {
                    albumList[albumIdsList.indexOf(galleryData.albumId)].albumPhotos.add(galleryData)
                    isAdded = true
                }

//                for (i in (albumList.size - 1) downTo 0) {
//                    if (albumList[i].id == galleryData.albumId) {
//                        albumList[i].albumPhotos.add(galleryData)
//                        isAdded = true
//                        break
//                    }
//                }
                if (!isAdded) {
                    albumIdsList.add(galleryData.albumId)
                    val galleryAlbum = GalleryAlbums()
                    galleryAlbum.id = galleryData.albumId
                    galleryAlbum.name = galleryData.albumName
                    galleryAlbum.coverUri = galleryData.photoUri
                    galleryAlbum.albumPhotos.add(galleryData)
                    albumList.add(galleryAlbum)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            try {
                if (photoList.size > 0) {
                    val folderName = when (GalleryConfig.getConfig().mediaType) {
                        MediaType.IMAGE -> R.string.all_photos
                        MediaType.VIDEO -> R.string.all_video
                        MediaType.IMAGE_VIDEO -> R.string.all_media
                        else -> R.string.all_photos
                    }

                    albumList.add(
                        0,
                        GalleryAlbums(
                            0,
                            activity!!.getString(folderName),
                            albumPhotos = photoList
                        )
                    )
                }
                progressBarGalleryPhotos.visibility = View.GONE

                (imageGrid as ScrollFastRecyclerLib).layoutManager = glm
                initRecyclerViews()
                done.setOnClickListener {
                    val newList: ArrayList<GalleryData> = ArrayList<GalleryData>()
                    photoList.filterTo(newList) { it.isSelected && it.isEnabled }

                    val pathList = ArrayList<String>()
                    for (i in 0 until newList.size) {
                        pathList.add(newList[i].photoUri)
                    }
                    setMultipleResult(pathList)
                }
                toggleDropdown()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
//                MyApplication.instance.freeMemory()
            }
        }
    }
}