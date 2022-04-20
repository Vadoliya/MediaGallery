package com.mediagallery.hashone.gallery.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.mediagallery.hashone.R
import com.mediagallery.hashone.gallery.model.FolderItem
import kotlinx.android.synthetic.main.adapter_item_folder.view.*


class FolderAdapter(
    var context: Context,
    private var foldersList: ArrayList<FolderItem>,
    private var onItemClickListener: AdapterView.OnItemClickListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return  ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_item_folder, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        try {
            val itemViewHolder = holder as ItemViewHolder
            val folderItem = foldersList[position]

            val requestBuilder: RequestBuilder<Drawable> = Glide.with(holder.itemView.context)
                .asDrawable().sizeMultiplier(0.25f)
            Glide.with(context)
                .load(folderItem.previewImage)
                .thumbnail(requestBuilder)
                .apply(RequestOptions().centerCrop())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(itemViewHolder.itemView.imageViewFolderItem)

            itemViewHolder.itemView.textViewFolderName.text = folderItem.name
            itemViewHolder.itemView.textViewFilesCount.text = "${folderItem.count}"
            itemViewHolder.itemView.textViewSelectedCount.isVisible = folderItem.selectedCount > 0
            itemViewHolder.itemView.textViewSelectedCount.text = "${folderItem.selectedCount}"

            itemViewHolder.itemView.setOnClickListener {
                if (onItemClickListener != null) {
                    onItemClickListener!!.onItemClick(null, it, position, getItemId(position))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return foldersList.size
    }

    private inner class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
}