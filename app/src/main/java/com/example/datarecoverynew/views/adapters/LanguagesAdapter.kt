package com.example.datarecoverynew.views.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources.Theme
import android.content.res.TypedArray
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.databinding.LanguageLayoutBinding
import com.example.datarecoverynew.databinding.LargeNativeadRecyclerViewBinding
import com.example.datarecoverynew.models.LanguageModel
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.recoverydata.interfaces.DataListener


class LanguagesAdapter(val context: Context,val  activity: Activity,
                       var selectedLan:Int, private var list:ArrayList<LanguageModel>, val dataListener: DataListener) : RecyclerView.Adapter<LanguagesAdapter.PostViewHolder>() {
    var selectedPosition = 1
    var lastSelectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {

        val binding = when (viewType) {
            VIEW_TYPE.CARD_VIEW.id -> {
                DataBindingUtil.inflate<LanguageLayoutBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.language_layout,
                    parent,
                    false
                )
            }

            VIEW_TYPE.AD_TYPE.id -> {
                DataBindingUtil.inflate<LargeNativeadRecyclerViewBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.large_nativead_recycler_view,
                    parent,
                    false
                )
            }

            else -> null
        }

        return PostViewHolder(binding!!)
    }


    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {

       /* holder.languageTv.text = list.get(position).name
        holder.logoIV.setImageResource(list[position].icon)

        holder.itemView.setOnClickListener {

            dataListener.onRecieve(position)

           *//* lastSelectedPosition = selectedPosition
            selectedPosition = position
            selectedLan = position
            dataListener.onRecieve(position)
            notifyDataSetChanged()*//*
        }*/


        val model = list.get(position)
        when (model.dataType) {
            VIEW_TYPE.CARD_VIEW -> holder.bind(model)
            VIEW_TYPE.AD_TYPE -> holder.bindAdLargeNative()

        }

    }

    override fun getItemCount(): Int = list.size


    inner class PostViewHolder(val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: LanguageModel) = with(binding as LanguageLayoutBinding) {
            binding.languageTV.text = model.name
            binding.logoIV.setImageResource(model.icon)

            binding.root.setOnClickListener {
                dataListener.onRecieve(position)
            }
        }


        fun bindAdLargeNative() = with(binding as LargeNativeadRecyclerViewBinding) {
            val adId = AdDatabaseUtil.getAdmobNativeAdId( binding.reffragcontainer.rootView.context)
            if(adId.isNotEmpty()) {
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

    override fun getItemViewType(position: Int): Int {
        return list.get(position).dataType.id
    }

    enum class VIEW_TYPE(val id: Int) {
        CARD_VIEW(12),
        AD_TYPE(13)
    }
}