package com.mediagallery.hashone.gallery.adapters

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.mediagallery.hashone.R
import com.mediagallery.hashone.gallery.Constants
import com.mediagallery.hashone.gallery.MediaActivity
import com.mediagallery.hashone.gallery.callback.OnSelectionChangeListener
import com.mediagallery.hashone.gallery.model.ImageItem
import kotlinx.android.synthetic.main.adapter_item_media_image.view.*

class ImageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    var context: Context
    var imagesList = ArrayList<ImageItem>()
    var isMultipleMode: Boolean = false
    var maxSize: Int = 1

    var onItemClickListener: AdapterView.OnItemClickListener? = null
    var onSelectionChangeListener: OnSelectionChangeListener? = null

    constructor(
        context: Context,
        imagesList: ArrayList<ImageItem>,
        isMultipleMode: Boolean = false,
        maxSize: Int = 1
    ) {
        this.context = context
        this.imagesList = imagesList
        this.isMultipleMode = isMultipleMode
        this.maxSize = maxSize
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(context).inflate(R.layout.adapter_item_media_image, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            when {
                payloads.any { it is ImageSelectedOrUpdated } -> {
                    val itemViewHolder = holder as ItemViewHolder
                    if (isMultipleMode) {
                        val imageItem = imagesList[position]
                        val selectedIndex = (context as MediaActivity).selectedIndex(imageItem)
                        if (selectedIndex != -1) {
                            itemViewHolder.itemView.textViewImageCount.text =
                                (selectedIndex + 1).toString()
                            itemViewHolder.itemView.textViewImageCount.visibility = View.VISIBLE
                            setupItemForeground(itemViewHolder.itemView.imageViewImageItem, true)
                        } else {
                            itemViewHolder.itemView.textViewImageCount.visibility = View.GONE
                            setupItemForeground(itemViewHolder.itemView.imageViewImageItem, false)
                        }
                    } else {
                        itemViewHolder.itemView.textViewImageCount.visibility = View.GONE
                        setupItemForeground(itemViewHolder.itemView.imageViewImageItem, false)
                    }
                }
                payloads.any { it is ImageUnselected } -> {
                    val itemViewHolder = holder as ItemViewHolder
                    if (isMultipleMode) itemViewHolder.itemView.textViewImageCount.visibility =
                        View.GONE
                    setupItemForeground(itemViewHolder.itemView.imageViewImageItem, false)
                }
                else -> {
                    onBindViewHolder(holder, position)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        try {
            val itemViewHolder = holder as ItemViewHolder
            val imageItem = imagesList[position]

            val selectedIndex = (context as MediaActivity).selectedIndex(imageItem)
            val isSelected = isMultipleMode && selectedIndex != -1

            Glide.with(context)
                .load(imageItem.path)
                .thumbnail(0.25F)
                .apply(RequestOptions().centerCrop())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(itemViewHolder.itemView.imageViewImageItem)

            setupItemForeground(itemViewHolder.itemView.imageViewImageItem, isSelected)
            itemViewHolder.itemView.textViewImageCount.visibility =
                if (isSelected && isMultipleMode) View.VISIBLE else View.GONE
            if (itemViewHolder.itemView.textViewImageCount.visibility == View.VISIBLE) {
                itemViewHolder.itemView.textViewImageCount.text = (selectedIndex + 1).toString()
            }

            itemViewHolder.itemView.setOnClickListener {
                selectOrRemoveImage(imageItem, position)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        val itemViewHolder = holder as ItemViewHolder
        val imageItem = imagesList[holder.adapterPosition]

        if (imageItem.type.contains("video")) {
            itemViewHolder.itemView.layoutDuration.visibility = View.VISIBLE
            itemViewHolder.itemView.durationLabel.text = String.format(
                "%02d:%02d",
                java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(imageItem.duration),
                java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(imageItem.duration) -
                        java.util.concurrent.TimeUnit.MINUTES.toSeconds(
                            java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(
                                imageItem.duration
                            )
                        )
            )
        }else{
            itemViewHolder.itemView.layoutDuration.visibility = View.GONE
        }
    }

    private fun setupItemForeground(view: View, isSelected: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.foreground = if (isSelected) ColorDrawable(
                ContextCompat.getColor(
                    context,
                    R.color.imagepicker_black_alpha_30
                )
            ) else null
        }
    }

    override fun getItemCount(): Int {
        return imagesList.size
    }

    private fun selectOrRemoveImage(image: ImageItem, position: Int) {
        if (isMultipleMode) {
            val selectedIndex = (context as MediaActivity).selectedIndex(image)
            if (selectedIndex != -1) {
                (context as MediaActivity).removeItem(selectedIndex)
                notifyItemChanged(position, ImageUnselected())
//                val indexes = (context as MediaActivity).selectedIndexes(imagesList)
//                for (index in indexes) {
//                    notifyItemChanged(index, ImageSelectedOrUpdated())
//                }
                notifyDataSetChanged()

                val newIntent = Intent()
                newIntent.action = Constants.ACTION_UPDATE_FOLDER_COUNT
                newIntent.putExtra("bucketId", image.bucketId)
                newIntent.putExtra("add", false)
                context.sendBroadcast(newIntent)
            } else {
                if ((context as MediaActivity).selectedImagesList.size >= maxSize) {
                    return
                } else {
                    (context as MediaActivity).addItem(image)
                    notifyItemChanged(position, ImageSelectedOrUpdated())

                    val newIntent = Intent()
                    newIntent.action = Constants.ACTION_UPDATE_FOLDER_COUNT
                    newIntent.putExtra("bucketId", image.bucketId)
                    newIntent.putExtra("add", true)
                    context.sendBroadcast(newIntent)
                }
            }
            if (onSelectionChangeListener != null)
                onSelectionChangeListener!!.onSelectedImagesChanged((context as MediaActivity).selectedImagesList)
        } else {
            if (onSelectionChangeListener != null)
                onSelectionChangeListener!!.onSingleModeImageSelected(image)
        }
    }

    private inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class ImageSelectedOrUpdated

    class ImageUnselected
}