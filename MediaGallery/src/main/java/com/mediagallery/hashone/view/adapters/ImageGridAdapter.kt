package com.mediagallery.hashone.view.adapters

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.media.MediaPlayer
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.AdapterView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.mediagallery.hashone.R
import com.mediagallery.hashone.fastScroll.ScrollFastRecyclerLib
import com.mediagallery.hashone.fastScroll.Utils
import com.mediagallery.hashone.model.GalleryData
import com.mediagallery.hashone.util.DateUtil
import com.mediagallery.hashone.view.PhotosFragment
import kotlinx.android.synthetic.main.fragment_media.*
import kotlinx.android.synthetic.main.grid_item.view.*
import java.io.File


open class ImageGridAdapter() : RecyclerView.Adapter<ImageGridAdapter.MyViewHolder>(),
    ScrollFastRecyclerLib.SectionedAdapter {

    var currentFragment: Fragment? = null

    lateinit var ctx: Context
    private var mimageList: ArrayList<GalleryData> = ArrayList()
    private var fullimagelist: ArrayList<GalleryData> = ArrayList()
    var THRESHOLD = 1

    private lateinit var onItemClickListener: AdapterView.OnItemClickListener

    constructor(
        imageList: ArrayList<GalleryData> = ArrayList(),
        filter: Long = 0L,
        threshold: Int = 1,
        fragment: Fragment? = null
    ) : this() {
        fullimagelist = imageList
        THRESHOLD = threshold
        currentFragment = fragment
        if (filter == 0L) mimageList = imageList
        else imageList.filter { it.albumId == filter }.forEach { mimageList.add(it) }
    }

    fun setOnItemClickListener(onItemClickListener: AdapterView.OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        ctx = parent.context
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.grid_item, parent, false)
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (position == RecyclerView.NO_POSITION) {
            return
        }

        val cR: ContentResolver = ctx.contentResolver
        val mime = MimeTypeMap.getSingleton()
        val type =
            mime.getExtensionFromMimeType(cR.getType(mimageList[holder.adapterPosition].contentUri!!))

        holder.durationFrame.isVisible = type == "mp4"

        if (THRESHOLD > 1) {
            if (THRESHOLD != 0) {
                if (getSelectedCount() >= THRESHOLD) mimageList.filterNot { it.isSelected }
                    .forEach {
                        it.isEnabled = false
                    }
                else mimageList.forEach { it.isEnabled = true }
            }

            if (mimageList[holder.adapterPosition].isEnabled) {
                holder.frame.alpha = 1.0f
                holder.image.isEnabled = true
                holder.checkbox.visibility = View.VISIBLE
            } else {
                holder.frame.alpha = 0.3f
                holder.image.isEnabled = false
                holder.checkbox.visibility = View.INVISIBLE
            }
        } else {
            holder.checkbox.visibility = View.GONE
        }

        if (mimageList[holder.adapterPosition].isSelected) holder.checkbox.setImageResource(R.drawable.tick)
        else holder.checkbox.setImageResource(R.drawable.round)

        holder.image.setOnClickListener {
            if (THRESHOLD > 1) {
                when {
                    getSelectedCount() <= THRESHOLD -> {
                        if (mimageList[holder.adapterPosition].isSelected) {
                            mimageList[holder.adapterPosition].isSelected = false
                            holder.checkbox.setImageResource(R.drawable.round)
                            if (getSelectedCount() == (THRESHOLD - 1) && !mimageList[holder.adapterPosition].isSelected) {
                                mimageList.forEach { it.isEnabled = true }
                                for ((index, item) in mimageList.withIndex()) {
                                    if (item.isEnabled && !item.isSelected) notifyItemChanged(index)
                                }
                            }
                        } else {
                            mimageList[holder.adapterPosition].isSelected = true
                            holder.checkbox.setImageResource(R.drawable.tick)
                            if (getSelectedCount() == THRESHOLD && mimageList[holder.adapterPosition].isSelected) {
                                mimageList.filterNot { it.isSelected }
                                    .forEach { it.isEnabled = false }
                                for ((index, item) in mimageList.withIndex()) {
                                    if (!item.isEnabled) notifyItemChanged(index)
                                }
                            }
                        }
                    }
                    getSelectedCount() > THRESHOLD -> {
                        for (image in mimageList) {
                            mimageList.filter { it.isSelected && !it.isEnabled }
                                .forEach { it.isSelected = false }
                        }
                    }
                    else -> {
                    }
                }
            } else {
                mimageList[holder.adapterPosition].isSelected = true
                holder.checkbox.setImageResource(R.drawable.tick)
                if (getSelectedCount() == THRESHOLD && mimageList[holder.adapterPosition].isSelected) {
                    mimageList.filterNot { it.isSelected }.forEach { it.isEnabled = false }
                    for ((index, item) in mimageList.withIndex()) {
                        if (!item.isEnabled) notifyItemChanged(index)
                    }
                }

                if (THRESHOLD == 1)
                    onItemClickListener.onItemClick(null, it, position, getItemId(position))
            }

            if (THRESHOLD > 1) {
                if (currentFragment != null) {
                    if (currentFragment is PhotosFragment) {
                        val count = getSelectedCount()
                        (currentFragment as PhotosFragment).albumselectionCount.text =
                            "($count/$THRESHOLD)"
                        (currentFragment as PhotosFragment).done.visibility = if (count > 0) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }
            }
        }

        try {
            if (Utils.isVideoFile(mimageList[holder.adapterPosition].photoUri)) {
                val thumb = ThumbnailUtils.createVideoThumbnail(
                    mimageList[holder.adapterPosition].photoUri,
                    MediaStore.Images.Thumbnails.MINI_KIND
                )
                holder.image.setImageBitmap(thumb)

            } else {
                holder.image.setImageURI(Uri.fromFile(File(mimageList[holder.adapterPosition].photoUri)))
            }

            /* Glide.with(ctx).load(mimageList[holder.adapterPosition].photoUri)
                 .thumbnail(0.15F)
                 .apply(
                     RequestOptions()
                         .diskCacheStrategy(DiskCacheStrategy.DATA)
                         .centerCrop()
                         .dontAnimate()
                         .format(DecodeFormat.PREFER_RGB_565)
                         .override(180)
                 )
                 .into(holder.image)*/
            if (type == "mp4") {
                val millis = getDuration(ctx, mimageList[holder.adapterPosition].photoUri)
                holder.durationLabel.text = String.format(
                    "%02d:%02d",
                    java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(millis),
                    java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(millis) -
                            java.util.concurrent.TimeUnit.MINUTES.toSeconds(
                                java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(
                                    millis
                                )
                            )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDuration(context: Context?, path: String?): Long {
        Log.e("getDuration", "path:" + path)
        var mMediaPlayer: MediaPlayer? = null
        var duration: Long = 0
        try {
            mMediaPlayer = MediaPlayer()
            mMediaPlayer.setDataSource(context!!, Uri.parse(path))
            mMediaPlayer.prepare()
            duration = mMediaPlayer.duration.toLong()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset()
                mMediaPlayer.release()
                mMediaPlayer = null
            }
        }
        return duration
    }

    private fun getSelectedCount(): Int = fullimagelist.count { it.isSelected }

    override fun getItemCount(): Int = mimageList.size

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image = view.image
        var checkbox = view.checkbox
        var frame = view.frame
        var durationFrame = view.durationFrame
        var durationLabel = view.durationLabel
    }

    override fun getSectionName(position: Int): String =
        DateUtil().getMonthAndYearString(mimageList[position].dateAdded.toLong() * 1000)

}