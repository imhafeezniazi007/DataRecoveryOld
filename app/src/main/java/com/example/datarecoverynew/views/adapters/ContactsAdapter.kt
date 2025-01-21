package com.example.datarecoverynew.views.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.datarecoverynew.R
import com.example.recoverydata.interfaces.DataListener
import com.example.recoverydata.models.ContactModel

class ContactsAdapter(private val context: Context, private val duplicateContactParentList: ArrayList<ArrayList<ContactModel>?>,
                      private val selectedList: ArrayList<ContactModel>, val dataListener: DataListener
) : RecyclerView.Adapter<ContactsAdapter.PostViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.parent_duplicate_layout, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {

        holder.groupTv.text = context.getString(R.string.group)+" ${position + 1}"
        val duplicateChildAdapter = ContactsChildAdapter(context,duplicateContactParentList[position]!!,selectedList){ it: ContactModel ->
            Log.d("TAG", "onRecieve call back : ${it.name}")
            dataListener.onRecieve(it)
        }

        holder.childRv.adapter = duplicateChildAdapter

    }

    override fun getItemCount(): Int = duplicateContactParentList.size

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val groupTv: TextView = itemView.findViewById(R.id.groupTv)
        val childRv: RecyclerView = itemView.findViewById(R.id.childRv)

    }
}