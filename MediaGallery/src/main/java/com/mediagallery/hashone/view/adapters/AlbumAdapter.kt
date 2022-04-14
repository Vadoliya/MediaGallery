package com.mediagallery.hashone.view.adapters

//import org.jetbrains.anko.doAsync
//import org.jetbrains.anko.uiThread
import android.app.Activity
import android.content.Context
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.mediagallery.hashone.R
import com.mediagallery.hashone.fastScroll.Utils
import com.mediagallery.hashone.model.GalleryAlbums
import com.mediagallery.hashone.model.GalleryData
import com.mediagallery.hashone.view.PhotosFragment
import com.mediagallery.hashone.view.PickersActivity
import kotlinx.android.synthetic.main.album_item.view.*
import kotlinx.android.synthetic.main.fragment_media.*
import java.io.File

class AlbumAdapter() : RecyclerView.Adapter<AlbumAdapter.MyViewHolder>() {
    var malbumList: ArrayList<GalleryAlbums> = ArrayList()
    lateinit var currentFragment: Fragment
    var activity: Activity? = null

    private lateinit var onItemClickListener: AdapterView.OnItemClickListener

    constructor(
        activity: Activity?,
        albumList: ArrayList<GalleryAlbums> = ArrayList(),
        currentFragment: Fragment
    ) : this() {
        malbumList = albumList
        this.activity = activity
        this.currentFragment = currentFragment
    }

    fun setOnItemClickListener(onItemClickListener: AdapterView.OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    lateinit var ctx: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        ctx = parent.context
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.album_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        if ((ctx as PickersActivity).IMAGES_THRESHOLD > 1) {
            val count = malbumList[holder.adapterPosition].albumPhotos.count { it.isSelected }
            if (count > 0 && malbumList[holder.adapterPosition].id != 0L) {
                holder.selectedcount.visibility = View.VISIBLE
                holder.selectedcount.text = count.toString()
            } else holder.selectedcount.visibility = View.GONE
        } else {
            holder.selectedcount.visibility = View.GONE
        }

        holder.albumtitle.text = malbumList[holder.adapterPosition].name
        holder.photoscount.text = malbumList[holder.adapterPosition].albumPhotos.size.toString()
//        doAsync {
//            uiThread {
        if (holder.adapterPosition != -1) {
            if (position == 0 && (currentFragment as PhotosFragment).photoList.size > 0) {
                if (activity != null && !activity!!.isDestroyed) {

                    if (Utils.isVideoFile((currentFragment as PhotosFragment).photoList[holder.adapterPosition].photoUri)) {
                        val thumb = ThumbnailUtils.createVideoThumbnail(
                            (currentFragment as PhotosFragment).photoList[holder.adapterPosition].photoUri,
                            MediaStore.Images.Thumbnails.MINI_KIND
                        )
                        holder.albumthumbnail.setImageBitmap(thumb)
                    } else {
                        holder.albumthumbnail.setImageURI(Uri.fromFile(File((currentFragment as PhotosFragment).photoList[holder.adapterPosition].photoUri)))
                    }


                    /*   Glide.with(activity!!)
                           .load((currentFragment as PhotosFragment).photoList[holder.adapterPosition].photoUri)
                           .apply(
                               RequestOptions()
                                   .centerCrop()
                                   .placeholder(R.drawable.ic_link_cont_default_img_1_5x)
                                   .skipMemoryCache(true)
                           )
                           .into(holder.albumthumbnail)*/
                }
            } else {
                if (activity != null && !activity!!.isDestroyed) {

                    if (Utils.isVideoFile(malbumList[holder.adapterPosition].coverUri)) {
                        val thumb = ThumbnailUtils.createVideoThumbnail(
                            malbumList[holder.adapterPosition].coverUri,
                            MediaStore.Images.Thumbnails.MINI_KIND
                        )
                        holder.albumthumbnail.setImageBitmap(thumb)
                    } else {
                        holder.albumthumbnail.setImageURI(Uri.fromFile(File(malbumList[holder.adapterPosition].coverUri)))
                    }
                    /*   Glide.with(activity!!)
                           .load(malbumList[holder.adapterPosition].coverUri)
                           .apply(
                               RequestOptions().centerCrop()
                                   .placeholder(R.drawable.ic_link_cont_default_img_1_5x)
                                   .skipMemoryCache(true)
                           )
                           .into(holder.albumthumbnail)*/
                }
            }
        }
//            }
//        }

        holder.albumFrame.setOnClickListener {
            when (currentFragment) {
                is PhotosFragment -> {
                    updateGallery(holder.adapterPosition)
                    (currentFragment as PhotosFragment).toggleDropdown()
                }
            }
        }
    }

    override fun getItemCount(): Int = malbumList.size

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var albumthumbnail: ImageView = view.albumthumbnail
        var albumtitle: TextView = view.albumtitle
        var photoscount: TextView = view.photoscount
        var selectedcount: TextView = view.selectedcount
        var albumFrame: FrameLayout = view.albumFrame
    }

    private fun updateGallery(position: Int) {
        try {
            (currentFragment as PhotosFragment).updateTitle(malbumList[position])
            (currentFragment as PhotosFragment).imageGrid.adapter = ImageGridAdapter(
                (currentFragment as PhotosFragment).photoList,
                malbumList[position].id,
                threshold = (ctx as PickersActivity).IMAGES_THRESHOLD,
                fragment = currentFragment
            )
            ((currentFragment as PhotosFragment).imageGrid.adapter as ImageGridAdapter).setOnItemClickListener { _, _, itemPosition, _ ->
                val newList: ArrayList<GalleryData> = ArrayList()
                if (position == 0) {
                    newList.addAll((currentFragment as PhotosFragment).photoList)
                } else {
                    for (j in (currentFragment as PhotosFragment).photoList) {
                        if (malbumList[position].name == j.albumName) {
                            newList.add(j)
                        }
                    }
                }

                if (itemPosition >= 0 && itemPosition < newList.size) {
                    if ((ctx as PickersActivity).IMAGES_THRESHOLD == 1)
                        (currentFragment as PhotosFragment).startCrop(newList[itemPosition].photoUri)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}