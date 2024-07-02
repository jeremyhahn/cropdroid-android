package com.jeremyhahn.cropdroid.ui.organization

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.model.Organization

class OrganizationRecyclerAdapter(val organizations: ArrayList<Organization>, val context: Context, val organizationListener: OrganizationListener)
    : RecyclerView.Adapter<OrganizationRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.cardview_organization, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val organizations = organizationListener.getOrganizations()
        holder.bindItems(organizations[position])
        holder.itemView.setOnLongClickListener { v ->
            organizationListener.showContextMenu(position)
           true
       }
        holder.itemView.setOnClickListener { v ->
            organizationListener.onOrganizationClick(position)
        }
    }

    override fun getItemCount(): Int {
        return organizationListener.size()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(item: Organization) {
            val name = itemView.findViewById(R.id.name) as TextView
            name.text = item.name
        }
    }

//    fun setOrganizations(list : List<Organization>) {
//        organizations.clear()
//        organizations.addAll(list)
//        notifyDataSetChanged()
//    }

    fun clear() {
        organizationListener.clear()
        notifyDataSetChanged()
    }
}