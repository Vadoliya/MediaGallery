package com.mediagallery.hashone.gallery.fragment

import android.app.Activity
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mediagallery.hashone.R
import com.mediagallery.hashone.gallery.CoroutineAsyncTask
import com.mediagallery.hashone.gallery.MediaActivity
import com.mediagallery.hashone.gallery.adapters.ImageAdapter
import com.mediagallery.hashone.gallery.callback.OnSelectionChangeListener
import com.mediagallery.hashone.gallery.config.GalleryConfig
import com.mediagallery.hashone.gallery.model.ImageItem
import com.mediagallery.hashone.gallery.model.MediaType
import kotlinx.android.synthetic.main.activity_media.*
import kotlinx.android.synthetic.main.fragment_images.*
import java.io.File

class ImagesFragment : Fragment() {

    lateinit var mActivity: Activity

    var bucketId: Long = -1L
    var folderName: String = ""
    var folderPath: String = ""

    val imagesList = ArrayList<ImageItem>()

    private var isHandled: Int = 0
    private val handlerLoadingWait = Handler()
    private val runnableLoadingWait =
        Runnable {
            textViewProgressMessage.text =
                requireActivity().getString(R.string.photos_taking_long_time)
            isHandled = 1
            handlerLoadingWait.postDelayed(runnableLoadingWait1, 7 * 1000L)
        }
    private val runnableLoadingWait1 =
        Runnable {
            isHandled = 2
            textViewProgressMessage.text =
                requireActivity().getString(R.string.photos_taking_more_time)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_images, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity = requireActivity()

        layoutContentLoading.visibility = View.VISIBLE
        handlerLoadingWait.postDelayed(runnableLoadingWait, 3 * 1000L)

        initViews()
    }

    private fun initViews() {
        try {
            bucketId = requireArguments().getLong("bucketId", -1L)
            folderName = requireArguments().getString("folderName", "")
            folderPath = requireArguments().getString("folderPath", "")

            (mActivity as MediaActivity).textViewTitle.text = folderName

            GetFoldersTask().execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inner class GetFoldersTask : CoroutineAsyncTask<Void, Void, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
            imagesList.clear()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            try {
//                getImages()
                fetchAlbumSync(GalleryConfig.getConfig().mediaType)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            try {
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

        fun getImages() {
            val projection = arrayOf(
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.MIME_TYPE
            )
            val cursor: Cursor? =
                mActivity.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    MediaStore.Images.Media.BUCKET_ID + " = ? ",
                    arrayOf("$bucketId"),
                    "${MediaStore.Images.Media.DATE_ADDED} DESC"
                )
            try {
                if (cursor == null)
                    return
                cursor.moveToFirst()
                do {
                    val filePath =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                    val mimeType =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE))
                    if (mimeType.equals("image/jpeg", ignoreCase = true)
                        || mimeType.equals("image/png", ignoreCase = true)
                        || mimeType.equals("image/jpg", ignoreCase = true)
                    ) {
                        imagesList.add(
                            ImageItem(
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)),
                                bucketId,
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)),
                                filePath,
                                0,
                                "image"
                            )
                        )
                    }
                } while (cursor.moveToNext())
                cursor.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }


        private fun fetchAlbumSync(mediaType: MediaType) {
//            albumItemMapping.clear()
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
                val nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val dateCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                val mimeType = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                val sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val durationCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                val widthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)

                do {
                    val path = cursor.getString(pathCol)
                    val bucketName = cursor.getString(bucketNameCol)
                    val name = cursor.getString(nameCol)
                    val dateTime = cursor.getLong(dateCol)
                    val type = cursor.getString(mimeType)
                    val size = cursor.getLong(sizeCol)
                    val duration = cursor.getLong(durationCol)
                    val width = cursor.getInt(widthCol)
                    val height = cursor.getInt(heightCol)

                    if (path.isNullOrEmpty() || type.isNullOrEmpty())
                        continue

                    val file = File(path)
                    if (!file.exists() || !file.isFile)
                        continue

                    if (folderName == bucketName) {
                        if (mediaType == MediaType.IMAGE_VIDEO) {
                            imagesList.add(
                                ImageItem(
                                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)),
                                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)),
                                    name,
                                    path,
                                    duration,
                                    type
                                )
                            )
                        } else {
                            if (type.contains(mediaType.value)) {
                                imagesList.add(
                                    ImageItem(
                                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)),
                                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)),
                                        name,
                                        path,
                                        duration,
                                        type
                                    )
                                )
                            }
                        }
                    }


                } while (cursor.moveToNext())
            }
            cursor?.close()
        }

    }

    private fun setAdapter() {
        try {
            val imageAdapter = ImageAdapter(
                mActivity,
                imagesList,
                isMultipleMode = (mActivity as MediaActivity).isMultipleMode,
                maxSize = (mActivity as MediaActivity).maxSize
            )
            recyclerViewImages.layoutManager =
                GridLayoutManager(mActivity, 3, RecyclerView.VERTICAL, false)
            recyclerViewImages.setHasFixedSize(true)
            recyclerViewImages.adapter = imageAdapter
            imageAdapter.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->

                }
            imageAdapter.onSelectionChangeListener = object : OnSelectionChangeListener {
                override fun onSelectedImagesChanged(selectedImages: ArrayList<ImageItem>) {
                    try {
                        if ((mActivity as MediaActivity).textViewDone != null) {
                            (mActivity as MediaActivity).textViewDone!!.isVisible =
                                selectedImages.size > 0
                        }

                        (mActivity as MediaActivity).textViewTotalCount.text =
                            (if ((mActivity as MediaActivity).maxSize > 1) {
                                " (" + (mActivity as MediaActivity).selectedImagesList.size + "/" + (mActivity as MediaActivity).maxSize + ")"
                            } else "")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onSingleModeImageSelected(imageItem: ImageItem) {
                    try {
                        (mActivity as MediaActivity).finishPickImages(arrayListOf(imageItem))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        (mActivity as MediaActivity).textViewTitle.text =
            requireActivity().getString(R.string.label_photos)
        if (isHandled == 0)
            handlerLoadingWait.removeCallbacks(runnableLoadingWait)
        else if (isHandled == 1)
            handlerLoadingWait.removeCallbacks(runnableLoadingWait1)
        super.onDestroyView()
    }
}