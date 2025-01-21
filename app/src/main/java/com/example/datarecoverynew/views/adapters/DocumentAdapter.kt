package com.example.datarecoverynew.views.adapters

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.databinding.AudioLayoutBinding
import com.example.datarecoverynew.databinding.ImageLayoutBinding
import com.example.datarecoverynew.databinding.LargeNativeadRecyclerViewBinding
import com.example.datarecoverynew.databinding.ViewMoreLayoutBinding
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.datarecoverynew.utils.AppPreferences
import com.example.datarecoverynew.views.adapters.ImagesAdapter.AdViewHolder
import com.example.datarecoverynew.views.adapters.ImagesAdapter.PostViewHolder
import com.example.recoverydata.interfaces.DataListener
import com.example.recoverydata.models.FilesModel

class DocumentAdapter(
    private val context: Context,
    val activity: Activity,
    val isAdsEnabled: Boolean,
    private var childImagesList: List<FilesModel>,
    private var onItemClicked: DataListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun updateList(childImagesList: List<FilesModel>) {
        this.childImagesList = childImagesList
        notifyDataSetChanged()
    }


    private var check = false
    private var isDataLoaded = false
    var isWatchAd = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE.CARD_VIEW.id -> {
                val view = DataBindingUtil.inflate<AudioLayoutBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.audio_layout,
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

    inner class AdViewHolder(val binding: AudioLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FilesModel) = with(binding as AudioLayoutBinding) {
            binding.nameTV.text = item.file.name
            val fileSize = item.file.length().toDouble()
            val sizeInKb = fileSize / 1024
            val sizeInMb = sizeInKb / 1024
            binding.sizeTV.text =
                String.format("%.2f", sizeInMb) + context.getString(R.string.mb)

            if (item.file.name.contains(".opus") || item.file.name.contains(".mp3") ||
                item.file.name.contains(".aac") || item.file.name.contains(".m4a")
            ) {
                binding.audioIV.setImageResource(R.drawable.ic_scan_audio)
            } else {
                binding.audioIV.setImageResource(R.drawable.ic_files)
            }



            binding.audioIV.setOnClickListener {
                onItemClicked.onRecieve(item)
            }
            binding.nameTV.setOnClickListener {
                onItemClicked.onRecieve(item)
            }
            binding.sizeTV.setOnClickListener {
                onItemClicked.onRecieve(item)

            }
            binding.checkbox.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    binding.checkbox.isChecked = true
                    item.isChecked = true
                    onItemClicked.onClick(item)
                } else {
                    binding.checkbox.isChecked = false
                    item.isChecked = false
                    onItemClicked.onClick(item)
                }

            }
        }


    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = childImagesList[position]

        when (holder) {
            is PostViewHolder -> if (isAdsEnabled) holder.bindAdLargeNative()
            is AdViewHolder -> holder.bind(model)
        }
    }

    override fun getItemCount(): Int {
        return if (!AppPreferences.getInstance(context).isAppPurchased && isDataLoaded) {
            if (isAdsEnabled && childImagesList.size > 50 && !isWatchAd) {
                50
            } else {
                childImagesList.size
            }
        } else {
            childImagesList.size
        }
    }


    fun setCheckFalse() {
        notifyDataSetChanged()
    }

    inner class PostViewHolder(val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bindAdLargeNative() = with(binding as LargeNativeadRecyclerViewBinding) {
            val adId =
                AdDatabaseUtil.getAdmobNativeAdId(binding.reffragcontainer.rootView.context)
            if (adId.isNotEmpty()) {
                AppController.nativeAdRef.loadNativeAd(
                    activity,
                    binding.reffragcontainer,
                    false,
                    adId
                ) {

                }
            }
        }

        fun bindViewMore() = with(binding as ViewMoreLayoutBinding) {
            binding.root.setOnClickListener {
                onItemClicked.onWatchAD(true)
            }

        }
    }


    override fun getItemViewType(position: Int): Int {
        Log.d("TAG", "getItemViewType: $position")
        val item = childImagesList[position]
        return if (item.dataType == VIEW_TYPE.CARD_VIEW) VIEW_TYPE.CARD_VIEW.id else VIEW_TYPE.AD_TYPE.id
    }


    enum class VIEW_TYPE(val id: Int) {
        CARD_VIEW(12),
        AD_TYPE(13),
    }

    fun setIsWatchAd() {
        isWatchAd = true
        notifyDataSetChanged()
    }

    fun setIsDataLoaded() {
        isDataLoaded = true
    }
}