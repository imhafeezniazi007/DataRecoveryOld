package com.example.datarecoverynew.views.adapters

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.databinding.ImageLayoutBinding
import com.example.datarecoverynew.databinding.LargeNativeadRecyclerViewBinding
import com.example.datarecoverynew.databinding.SmallNativeAdScreenTranbgBinding
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.datarecoverynew.utils.AppPreferences
import com.example.datarecoverynew.views.adapters.DocumentAdapter.VIEW_TYPE
import com.example.recoverydata.interfaces.DataListener
import com.example.recoverydata.models.FilesModel

class ImagesAdapter(
    private val context: Context,
    val activity: Activity,
    val isAdEnabled: Boolean,
    private var childImagesList: List<FilesModel>,
    private var onItemClicked: DataListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var check = true
    var isDataLoaded = false
    var isWatchAd = false

    fun updateList(childImagesList: List<FilesModel>) {
        this.childImagesList = childImagesList
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        return when (viewType) {
            VIEW_TYPE.CARD_VIEW.id -> {
                val view = DataBindingUtil.inflate<ImageLayoutBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.image_layout,
                    parent,
                    false
                )
                AdViewHolder(view)
            }

            VIEW_TYPE.AD_TYPE.id -> {
                val view = DataBindingUtil.inflate<LargeNativeadRecyclerViewBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.large_nativead_recycler_view,
                    parent,
                    false
                )
                PostViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    var TAG = "de_tag"
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = childImagesList[position]
        Log.d(TAG, "onBindViewHolder: ")
        when (holder) {
            is PostViewHolder -> if (isAdEnabled) holder.bindAdLargeNative()
            is AdViewHolder -> holder.bind(model)
        }
    }

    override fun getItemCount(): Int =
        if (!AppPreferences.getInstance(context).isAppPurchased && isDataLoaded) {
            if (childImagesList.size > 50 && !isWatchAd) {
                50
            } else {
                childImagesList.size
            }
        } else {
            childImagesList.size
        }

    override fun getItemViewType(position: Int): Int {
        Log.d("TAG", "getItemViewType: $position")
        val item = childImagesList[position]
        return if (item.dataType == VIEW_TYPE.CARD_VIEW) VIEW_TYPE.CARD_VIEW.id else VIEW_TYPE.AD_TYPE.id
    }

    fun setCheckFalse() {
//       check = false
        notifyDataSetChanged()
    }

    fun setIsDataLoaded() {
        isDataLoaded = true
    }

    inner class AdViewHolder(val binding: ImageLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FilesModel) = with(binding as ImageLayoutBinding) {
            try {
                Glide.with(context)
                    .load("file://" + item.file.path)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH)
                    .centerCrop()
                    .error(R.drawable.ic_images)
                    .into(binding.imageIV)
            } catch (e: Exception) {
                Toast.makeText(context, "Exception: " + e.message, Toast.LENGTH_SHORT).show()
            }
            if (check) {
                binding.checkbox.visibility = View.VISIBLE
                binding.checkbox.isChecked = false
            } else {
                binding.checkbox.visibility = View.GONE
            }
            binding.root.setOnClickListener {
                onItemClicked.onClick(item)
            }
            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                item.isChecked = isChecked
                onItemClicked.onRecieve(item)
            }
            binding.root.setOnLongClickListener {
                check = true
                notifyDataSetChanged()
                true
            }
        }

    }

    inner class PostViewHolder(val binding: LargeNativeadRecyclerViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindAdLargeNative() = with(binding as LargeNativeadRecyclerViewBinding) {
            if (isAdEnabled) {
                val adId =
                    AdDatabaseUtil.getAdmobNativeAdId(binding.reffragcontainer.rootView.context)
                if (adId.isNotEmpty()) {
                    AppController.nativeAdRef.loadNativeAd(
                        activity,
                        binding.reffragcontainer,
                        false,
                        adId
                    ) {}
                }
            } else {
                binding.reffragcontainer.visibility =
                    View.GONE // Hide ad container if ads are disabled
            }
        }
    }


    fun setIsWatchAd() {
        isWatchAd = true
        notifyDataSetChanged()
    }
}