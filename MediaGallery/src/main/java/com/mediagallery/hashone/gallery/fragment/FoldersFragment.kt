package com.mediagallery.hashone.gallery.fragment

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mediagallery.hashone.R
import com.mediagallery.hashone.gallery.Constants
import com.mediagallery.hashone.gallery.CoroutineAsyncTask
import com.mediagallery.hashone.gallery.MediaActivity
import com.mediagallery.hashone.gallery.adapters.FolderAdapter
import com.mediagallery.hashone.gallery.adnetworks.AdmobUtils
import com.mediagallery.hashone.gallery.config.GalleryConfig
import com.mediagallery.hashone.gallery.model.FolderItem
import com.mediagallery.hashone.gallery.model.ImageItem
import com.mediagallery.hashone.gallery.model.MediaType
import com.mediagallery.hashone.gallery.utils.Utils
import kotlinx.android.synthetic.main.activity_media.*
import kotlinx.android.synthetic.main.fragment_folders.*
import java.io.File


class FoldersFragment : Fragment() {

    private lateinit var mActivity: Activity

    val foldersList = ArrayList<FolderItem>()
    val imagesList = ArrayList<String>()
    val albumPhotosList = ArrayList<ImageItem>()
    val allPhotosList = ArrayList<ImageItem>()

    var folderAdapter: FolderAdapter? = null

    private var maxSize: Int = 1

    private var isMultipleMode: Boolean = false

    private var isHandled: Int = 0
    private val handlerLoadingWait = Handler(Looper.getMainLooper())
    private val runnableLoadingWait =
        Runnable {
            textViewProgressMessage.text =
                mActivity.getString(R.string.photos_taking_long_time)
            isHandled = 1
            handlerLoadingWait.postDelayed(runnableLoadingWait1, 7 * 1000L)
        }
    private val runnableLoadingWait1 =
        Runnable {
            isHandled = 2
            textViewProgressMessage.text =
                mActivity.getString(R.string.photos_taking_more_time)
        }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                if (intent != null) {
                    if (intent.action != null && intent.action == Constants.ACTION_UPDATE_FOLDER_COUNT) {
                        if (intent.extras != null) {
                            val bucketId = intent.extras!!.getLong("bucketId", -1L)
                            val add = intent.extras!!.getBoolean("add", false)
                            val folderName = intent.extras!!.getString("folderName", "")

                            if (folderAdapter != null) {
                                for (i in 0 until foldersList.size) {
                                    if (foldersList[i].name == folderName) {
                                        foldersList[i].selectedCount =
                                            if (add) foldersList[i].selectedCount + 1 else foldersList[i].selectedCount - 1
                                        folderAdapter!!.notifyItemChanged(i)
                                        break
                                    }
                                }
                            }
                            loadBannerAds()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_folders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity = requireActivity()

        allowAccessButton.outlineProvider = ViewOutlineProvider.BACKGROUND

        initViews()

        allowAccessButton.setOnClickListener {
            if (isReadWritePermitted()) initGalleryViews() else checkReadWritePermission()
        }

        if (activity != null) Utils.closeKeyboard(requireActivity(), allowAccessButton)

        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.ACTION_UPDATE_FOLDER_COUNT)
        mActivity.registerReceiver(broadcastReceiver, intentFilter)

    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (it.value) initGalleryViews()
                else {
                    (mActivity as MediaActivity).fabGooglePhotos.visibility = View.GONE
                    allowAccessFrame.visibility = View.VISIBLE
                }
            }
        }

    @TargetApi(android.os.Build.VERSION_CODES.JELLY_BEAN)
    fun checkReadWritePermission(): Boolean {

        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
        return true
    }

    private fun isReadWritePermitted(): Boolean =
        (context?.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && context?.checkCallingOrSelfPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)


    private fun initViews() {

        if (isReadWritePermitted()) initGalleryViews() else {
            (mActivity as MediaActivity).fabGooglePhotos.visibility = View.GONE
            allowAccessFrame.visibility = View.VISIBLE
        }

        if (GalleryConfig.getConfig().loadingText.isNotEmpty()) {
            textViewProgressMessage.text = GalleryConfig.getConfig().loadingText
        }
    }


    private fun initGalleryViews() {
        allowAccessFrame.visibility = View.GONE
        (mActivity as MediaActivity).fabGooglePhotos.visibility = View.VISIBLE
        try {
            isMultipleMode = requireArguments().getBoolean("isMultipleMode", false)
            maxSize = requireArguments().getInt("maxSize", 1)

            layoutContentLoading.visibility = View.VISIBLE
//            handlerLoadingWait.postDelayed(runnableLoadingWait, 3 * 1000L)

            GetFoldersTask().execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inner class GetFoldersTask : CoroutineAsyncTask<Void, Void, Void>() {

        override fun onPreExecute() {
            super.onPreExecute()
            foldersList.clear()
            imagesList.clear()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            try {
                fetchAlbumSync(GalleryConfig.getConfig().mediaType)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            try {
                var totalImage = 0
                foldersList.forEach { totalImage += it.count }
                if (imagesList.size > 0) {
                    foldersList.add(
                        0,
                        FolderItem(
                            0,
                            when (GalleryConfig.getConfig().mediaType) {
                                MediaType.IMAGE -> getString(R.string.all_photos)
                                MediaType.VIDEO -> getString(R.string.all_video)
                                MediaType.IMAGE_VIDEO -> getString(R.string.all_media)
                            },
                            previewImage = foldersList[foldersList.size - 1].previewImage,
                            count = totalImage,
                            albumPhotos = allPhotosList
                        )
                    )
                }

                if (isHandled == 0)
                    handlerLoadingWait.removeCallbacks(runnableLoadingWait)
                else if (isHandled == 1)
                    handlerLoadingWait.removeCallbacks(runnableLoadingWait1)
                layoutContentLoading.visibility = View.GONE
                setAdapter()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun fetchAlbumSync(mediaType: MediaType) {
            val contentUri = MediaStore.Files.getContentUri("external")
            val selection =
                "(${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR " +
                        "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?) AND " +
                        "${MediaStore.MediaColumns.SIZE} > 0"
            val selectionArgs =
                arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
                )
            val projections =
                arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.MediaColumns.DATA,
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.DATE_MODIFIED,
                    MediaStore.MediaColumns.MIME_TYPE,
                    MediaStore.MediaColumns.WIDTH,
                    MediaStore.MediaColumns.HEIGHT,
                    MediaStore.MediaColumns.SIZE,
                    MediaStore.Video.Media.DURATION
                )

            val sortBy = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
            val cursor = context!!.contentResolver.query(
                contentUri,
                projections,
                selection,
                selectionArgs,
                sortBy
            )
            if (true == cursor?.moveToFirst()) {
                val pathCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                val bucketNameCol =
                    cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val mimeType = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                val nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val durationCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                /*
                  val dateCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                  val sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)

                  val widthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                  val heightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)*/

                do {
                    val path = cursor.getString(pathCol)
                    val bucketName = if (cursor.getString(bucketNameCol)!=null) cursor.getString(bucketNameCol) else ""
                    val type = cursor.getString(mimeType)
                    val name = cursor.getString(nameCol)
                    val duration = cursor.getLong(durationCol)
                    /*
                     val dateTime = cursor.getLong(dateCol)
                     val size = cursor.getLong(sizeCol)

                     val width = cursor.getInt(widthCol)
                     val height = cursor.getInt(heightCol)*/

                    if (path.isNullOrEmpty() || type.isNullOrEmpty())
                        continue

                    val file = File(path)
                    if (!file.exists() || !file.isFile)
                        continue

                    val folderItem = FolderItem(
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)),
                        bucketName,
                        path.substring(0, path.lastIndexOf("$bucketName/")),
                        path,
                        1
                    )

                    val imageItem = ImageItem(
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)),
                        bucketName,
                        name,
                        path,
                        duration,
                        type
                    )
                    // Put the current media in the corresponding album
                    val folder = file.parentFile?.absolutePath ?: ""

//                    folderPath = "$folderPath$folderName/"
//                    if (type.contains("image")) {
                    if (mediaType == MediaType.IMAGE_VIDEO) {
                        if (!imagesList.contains(folder)) {
                            imagesList.add(folder)
                            albumPhotosList.clear()

                            folderItem.path = folder
                            folderItem.name = bucketName
                            folderItem.previewImage = path
                            folderItem.albumPhotos.add(imageItem)
//                            folderItem.albumPhotos = albumPhotosList
                            foldersList.add(folderItem)
                        } else {
                            for (i in foldersList.indices) {
                                if (foldersList[i].path == folder) {
                                    foldersList[i].increaseCount()
                                    foldersList[i].albumPhotos.add(imageItem)
                                }
                            }
                        }

                        allPhotosList.add(imageItem)
                    } else {
                        if (type.contains(mediaType.value)) {
                            if (!imagesList.contains(folder)) {
                                imagesList.add(folder)
                                albumPhotosList.clear()
                                folderItem.path = folder
                                folderItem.name = bucketName
                                folderItem.previewImage = path
                                folderItem.albumPhotos.add(imageItem)
                                foldersList.add(folderItem)
                            } else {
                                for (i in foldersList.indices) {
                                    if (foldersList[i].path == folder) {
                                        foldersList[i].increaseCount()
                                        foldersList[i].albumPhotos.add(imageItem)

                                    }
                                }
                            }
                            allPhotosList.add(imageItem)
                        }
                    }
                } while (cursor.moveToNext())
            }
            cursor?.close()
        }

    }

    private fun setAdapter() {
        try {
            recyclerViewFolders.layoutManager =
                LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
            recyclerViewFolders.setHasFixedSize(true)

            folderAdapter = FolderAdapter(
                mActivity, foldersList
            ) { _, _, position, _ ->

                val newBundle = Bundle()
                newBundle.putLong("bucketId", foldersList[position].id)
                newBundle.putString("folderName", foldersList[position].name)
                newBundle.putString("folderPath", foldersList[position].path)
                newBundle.putSerializable("imageList", foldersList[position].albumPhotos)
                (mActivity as MediaActivity).loadFragment(ImagesFragment(), newBundle, true)
            }
            recyclerViewFolders.adapter = folderAdapter
            loadBannerAds()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        if (isHandled == 0)
            handlerLoadingWait.removeCallbacks(runnableLoadingWait)
        else if (isHandled == 1)
            handlerLoadingWait.removeCallbacks(runnableLoadingWait1)

        mActivity.unregisterReceiver(broadcastReceiver)
        super.onDestroyView()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            loadBannerAds()
        }
    }

    private fun loadBannerAds() {
        val admobUtils = AdmobUtils(requireActivity())
        admobUtils.loadBannerAd(bannerad_layout, GalleryConfig.getConfig().admobId)
    }

}