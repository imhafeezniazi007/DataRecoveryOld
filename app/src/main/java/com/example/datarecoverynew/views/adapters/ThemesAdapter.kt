package com.example.datarecoverynew.views.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.datarecoverynew.R

class ThemesAdapter(val context: Context) : RecyclerView.Adapter<ThemesAdapter.PostViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.theme_layout_new, parent, false)
        return PostViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        if (position == 0) {

          holder.themeIV.setImageResource(R.drawable.theme_1)
        }else if (position == 1) {
            holder.themeIV.setImageResource(R.drawable.theme_2)

        }else if (position == 2) {
            holder.themeIV.setImageResource(R.drawable.theme_3)
        }else if (position == 3) {
            holder.themeIV.setImageResource(R.drawable.theme_4)
        }else if (position == 4) {
            holder.themeIV.setImageResource(R.drawable.theme_5)
        }else if (position == 5) {
            holder.themeIV.setImageResource(R.drawable.theme_6)
        }
        else if (position == 6) {
            holder.themeIV.setImageResource(R.drawable.theme_7)
        }
    }

    override fun getItemCount(): Int = 7

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val themeIV: ImageView = itemView.findViewById(R.id.themeIV)



    }

}