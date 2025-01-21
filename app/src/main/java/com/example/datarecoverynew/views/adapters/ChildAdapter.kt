package com.example.datarecoverynew.views.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.datarecoverynew.R
import com.example.recoverydata.extensions.getFileSize
import com.example.recoverydata.models.DuplicateFile

class ChildAdapter(val context: Context, private val duplicateChildList: List<DuplicateFile>, private var onItemClicked: ((duplicateFile: DuplicateFile) -> Unit)) : RecyclerView.Adapter<ChildAdapter.PostViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.duplicate_child_layout, parent, false)
        return PostViewHolder(view)
    }


    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val singleItem = duplicateChildList[position]
        holder.name.text = singleItem.getFile().name
        holder.size.text = singleItem.getFile().length().getFileSize()

        holder.checkbox.isChecked = singleItem.isSelect

        Glide.with(context).load(singleItem.getFile().path)
            .placeholder(R.drawable.ic_images)
            .error(R.drawable.ic_images)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
            .into(holder.thumbnail)


        holder.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d("TAG", "onBindViewHolder: setOnCheckedChangeListener ${isChecked}")
            onItemClicked(singleItem)
        }
    }

    override fun getItemCount(): Int = duplicateChildList.size

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val name: TextView = itemView.findViewById(R.id.fileName)
        val size: TextView = itemView.findViewById(R.id.fileSize)
        val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)

    }

}