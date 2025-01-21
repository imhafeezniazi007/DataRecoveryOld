package com.example.datarecoverynew.views.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.databinding.AudioLayoutBinding
import com.example.datarecoverynew.databinding.LargeNativeadRecyclerViewBinding
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.recoverydata.interfaces.DataListener
import com.example.recoverydata.models.Duplicate
import com.example.recoverydata.models.DuplicateFile

class DuplicateParentAdapter(
    private val context: Context,
    private val activity: Activity,
    private val isAdEnabled: Boolean,
    private val duplicateParentList: ArrayList<Duplicate>,
    val dataListener: DataListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_CONTENT = 0
        private const val VIEW_TYPE_AD = 1
        private const val AD_INTERVAL = 4
    }

    override fun getItemViewType(position: Int): Int {
        return if (isAdEnabled && (position + 1) % AD_INTERVAL == 0) VIEW_TYPE_AD else VIEW_TYPE_CONTENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CONTENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.parent_duplicate_layout, parent, false)
                ContentViewHolder(view)
            }
            VIEW_TYPE_AD -> {
                val binding = LargeNativeadRecyclerViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AdViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ContentViewHolder -> {
                // Calculate actual position based on whether ads are enabled
                val actualPosition = if (isAdEnabled) {
                    position - (position / AD_INTERVAL)
                } else {
                    position
                }
                holder.bind(actualPosition)
            }
            is AdViewHolder -> {
                holder.bindAd()
            }
        }
    }

    override fun getItemCount(): Int {
        val contentCount = duplicateParentList.size
        return if (isAdEnabled) {
            val adCount = contentCount / AD_INTERVAL
            contentCount + adCount
        } else {
            contentCount
        }
    }

    inner class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupTv: TextView = itemView.findViewById(R.id.groupTv)
        private val childRv: RecyclerView = itemView.findViewById(R.id.childRv)

        fun bind(actualPosition: Int) {
            groupTv.text = context.getString(R.string.group) + " ${actualPosition + 1}"
            val duplicateChildAdapter = ChildAdapter(
                context,
                duplicateParentList[actualPosition].getDuplicateFiles()
            ) { it: DuplicateFile ->
                dataListener.onRecieve(it)
            }
            childRv.adapter = duplicateChildAdapter
        }
    }

    inner class AdViewHolder(val binding: LargeNativeadRecyclerViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindAd() = with(binding as LargeNativeadRecyclerViewBinding) {
            val adId = AdDatabaseUtil.getAdmobNativeAdId(binding.reffragcontainer.rootView.context)
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
    }
}